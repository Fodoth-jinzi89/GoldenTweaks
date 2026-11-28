package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Renames the machine from its own window.
 *
 * <p>Separate from the port's text packet because the two act on different block entities and only
 * this one can carry a styled name: the machine's name is a whole component, and its tooltip shows
 * that styling, so flattening it to plain text here would lose it.</p>
 */
public record C2SMachineNamePacket(BlockPos pos, String name) implements CustomPacketPayload {

    /** Longest accepted name, matching the vanilla anvil's own limit. */
    public static final int NAME_LIMIT = 50;

    public static final Type<C2SMachineNamePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "machine_name"));

    public static final StreamCodec<FriendlyByteBuf, C2SMachineNamePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, packet) -> {
                        buffer.writeBlockPos(packet.pos());
                        buffer.writeUtf(packet.name(), NAME_LIMIT);
                    },
                    buffer -> new C2SMachineNamePacket(buffer.readBlockPos(),
                            buffer.readUtf(NAME_LIMIT)));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SMachineNamePacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!player.level().isLoaded(pkt.pos())) {
                return;
            }
            if (player.distanceToSqr(
                    pkt.pos().getX() + 0.5D, pkt.pos().getY() + 0.5D, pkt.pos().getZ() + 0.5D) > 64.0D) {
                return;
            }
            if (player.level().getBlockEntity(pkt.pos())
                    instanceof GTMachineBlockEntity machine) {
                // An empty name clears the override, which is what deleting the box should do.
                machine.setCustomName(pkt.name().isBlank()
                        ? null : Component.literal(pkt.name()));
            }
        });
    }
}
