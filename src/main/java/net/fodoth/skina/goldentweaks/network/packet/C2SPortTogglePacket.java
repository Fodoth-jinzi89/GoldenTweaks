package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Flips one of a port's two switches.
 *
 * <p>The block entity decides the outcome, not the client: this only carries the request, and the
 * server applies it to the port at that position. Range and existence are checked the same way as
 * any other block interaction, so a packet cannot reach a port the player could not have opened.</p>
 */
public record C2SPortTogglePacket(BlockPos pos, Target target) implements CustomPacketPayload {

    /** Which switch to flip. */
    public enum Target {
        /** Whether the port takes part in the machine. */
        ENABLED,
        /** Whether each entry occupies one cell or spans as many as it needs. */
        STACK_MODE,
        /** Whether the port also reaches through its own face. */
        ACTIVE
    }

    public static final Type<C2SPortTogglePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "port_toggle"));

    public static final StreamCodec<FriendlyByteBuf, C2SPortTogglePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, packet) -> {
                        buffer.writeBlockPos(packet.pos());
                        buffer.writeEnum(packet.target());
                    },
                    buffer -> new C2SPortTogglePacket(buffer.readBlockPos(),
                            buffer.readEnum(Target.class)));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SPortTogglePacket pkt, IPayloadContext ctx) {
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
            if (player.level().getBlockEntity(pkt.pos()) instanceof GTPortBlockEntity port) {
                switch (pkt.target()) {
                    case ENABLED -> port.setEnabled(!port.isEnabled());
                    case STACK_MODE -> port.toggleSingleCell();
                    case ACTIVE -> port.toggleActive();
                }
            }
        });
    }
}
