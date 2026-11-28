package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Carries an edited text field from the port window to the block entity.
 *
 * <p>Sent when a field loses focus rather than on every keystroke: the value only has to reach the
 * server once the player is done typing, and a packet per character would be pure traffic.</p>
 *
 * <p>The server re-checks everything. A text box's own length limit is a convenience for the
 * player, not a guarantee — a modified client can send any string.</p>
 */
public record C2SPortTextPacket(BlockPos pos, GTPortBlockEntity.Field field, String value)
        implements CustomPacketPayload {

    public static final Type<C2SPortTextPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "port_text"));

    /**
     * Written by hand: the field is an enum and the text is length-capped, and neither maps cleanly
     * onto a composite codec without a cast, which would hide a real type error.
     */
    public static final StreamCodec<FriendlyByteBuf, C2SPortTextPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public C2SPortTextPacket decode(FriendlyByteBuf buffer) {
                    BlockPos pos = buffer.readBlockPos();
                    GTPortBlockEntity.Field field =
                            buffer.readEnum(GTPortBlockEntity.Field.class);
                    String value = buffer.readUtf(GTPortBlockEntity.TEXT_LIMIT);
                    return new C2SPortTextPacket(pos, field, value);
                }

                @Override
                public void encode(FriendlyByteBuf buffer, C2SPortTextPacket packet) {
                    buffer.writeBlockPos(packet.pos());
                    buffer.writeEnum(packet.field());
                    buffer.writeUtf(packet.value(), GTPortBlockEntity.TEXT_LIMIT);
                }
            };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SPortTextPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            // Range-checked like any other block interaction: a packet cannot edit a port the
            // player could not have opened.
            if (!player.level().isLoaded(pkt.pos())) {
                return;
            }
            if (player.distanceToSqr(
                    pkt.pos().getX() + 0.5D, pkt.pos().getY() + 0.5D, pkt.pos().getZ() + 0.5D) > 64.0D) {
                return;
            }
            if (player.level().getBlockEntity(pkt.pos()) instanceof GTPortBlockEntity port) {
                port.setText(pkt.field(), pkt.value());
            }
        });
    }
}
