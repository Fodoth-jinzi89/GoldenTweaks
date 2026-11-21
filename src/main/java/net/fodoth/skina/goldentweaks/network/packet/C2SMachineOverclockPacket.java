package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Sets how far the machine may overclock.
 *
 * <p>The value is the circuit's ratio denominator as an integer, and {@code -1} means no ceiling.
 * Anything below that is meaningless, so the block entity clamps rather than rejecting: a client
 * that sends nonsense still ends up in a valid state instead of being told off.</p>
 */
public record C2SMachineOverclockPacket(BlockPos pos, int limit) implements CustomPacketPayload {

    public static final Type<C2SMachineOverclockPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    GoldenTweaks.MODID, "machine_overclock"));

    public static final StreamCodec<FriendlyByteBuf, C2SMachineOverclockPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, packet) -> {
                        buffer.writeBlockPos(packet.pos());
                        buffer.writeVarInt(packet.limit());
                    },
                    buffer -> new C2SMachineOverclockPacket(buffer.readBlockPos(),
                            buffer.readVarInt()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SMachineOverclockPacket pkt, IPayloadContext ctx) {
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
                    instanceof GTChemicalReactorMachineBlockEntity machine) {
                machine.setOverclockLimit(pkt.limit());
            }
        });
    }
}
