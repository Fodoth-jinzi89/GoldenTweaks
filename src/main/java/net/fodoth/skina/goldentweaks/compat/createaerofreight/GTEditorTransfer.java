package net.fodoth.skina.goldentweaks.compat.createaerofreight;

import cn.blockforge.generated.createareofreight.AdminOrderEditorPublishPayload;
import cn.blockforge.generated.createareofreight.admin.AdminOrderEditorMenu;
import io.netty.buffer.Unpooled;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public final class GTEditorTransfer {
    private static final int PART_BYTES = 24000;
    private static final Map<Object, Pending> INCOMING = Collections.synchronizedMap(new WeakHashMap<>());
    private static UUID readyId;
    private static byte[] readyData;

    private GTEditorTransfer() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playBidirectional(Part.TYPE, Part.CODEC, GTEditorTransfer::receive);
    }

    public static UUID send(byte[] data, Consumer<Part> sender) {
        UUID id = UUID.randomUUID();
        for (int offset = 0, index = 0; offset < data.length; index++) {
            int end = (int) Math.min((long) data.length, (long) offset + PART_BYTES);
            sender.accept(new Part(id, index, end == data.length, Arrays.copyOfRange(data, offset, end)));
            offset = end;
        }
        return id;
    }

    public static void publish(AdminOrderEditorPublishPayload payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeNbt(payload.snapshot());
            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);
            send(data, PacketDistributor::sendToServer);
        } finally {
            buffer.release();
        }
    }

    public static byte[] take(UUID id) {
        if (!id.equals(readyId) || readyData == null) {
            throw new IllegalStateException("Missing AeroFreight editor transfer " + id);
        }
        byte[] data = readyData;
        readyData = null;
        readyId = null;
        return data;
    }

    private static void receive(Part part, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && (!player.hasPermissions(2) || !(player.containerMenu instanceof AdminOrderEditorMenu))) {
                INCOMING.remove(context.connection());
                return;
            }
            try {
                Pending pending = INCOMING.get(context.connection());
                if (part.index() == 0) {
                    pending = new Pending(part.id());
                    INCOMING.put(context.connection(), pending);
                }
                if (pending == null) throw new IllegalArgumentException("Missing initial editor fragment");
                byte[] complete = pending.accept(part);
                if (complete == null) return;
                INCOMING.remove(context.connection());
                if (context.player() instanceof ServerPlayer) {
                    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(complete));
                    try {
                        var decoded = buffer.readNbt(NbtAccounter.unlimitedHeap());
                        if (!(decoded instanceof CompoundTag tag) || buffer.isReadable()) throw new IllegalArgumentException("Invalid editor publish data");
                        context.handle(new ServerboundCustomPayloadPacket(new AdminOrderEditorPublishPayload(tag)));
                    } finally {
                        buffer.release();
                    }
                } else {
                    readyId = part.id();
                    readyData = complete;
                }
            } catch (RuntimeException exception) {
                INCOMING.remove(context.connection());
                GoldenTweaks.LOGGER.error("[AeroFreight] Invalid editor transfer", exception);
            }
        });
    }

    static final class Pending {
        private final UUID id;
        private final ByteArrayOutputStream data = new ByteArrayOutputStream();
        private int nextIndex;

        Pending(UUID id) { this.id = id; }

        byte[] accept(Part part) {
            if (!id.equals(part.id()) || part.index() != nextIndex || part.data().length == 0 || part.data().length > PART_BYTES) {
                throw new IllegalArgumentException("Invalid editor fragment sequence");
            }
            data.writeBytes(part.data());
            nextIndex++;
            return part.last() ? data.toByteArray() : null;
        }
    }

    public record Part(UUID id, int index, boolean last, byte[] data) implements CustomPacketPayload {
        public static final Type<Part> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("goldentweaks", "aerofreight_editor_part"));
        public static final StreamCodec<FriendlyByteBuf, Part> CODEC = new StreamCodec<>() {
            public @NotNull Part decode(FriendlyByteBuf buffer) {
                return new Part(buffer.readUUID(), buffer.readVarInt(), buffer.readBoolean(), buffer.readByteArray(PART_BYTES));
            }
            public void encode(FriendlyByteBuf buffer, Part part) {
                buffer.writeUUID(part.id());
                buffer.writeVarInt(part.index());
                buffer.writeBoolean(part.last());
                buffer.writeByteArray(part.data());
            }
        };
        public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
