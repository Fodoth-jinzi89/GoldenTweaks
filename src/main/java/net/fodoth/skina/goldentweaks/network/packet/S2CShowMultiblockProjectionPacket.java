package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.client.GTMultiblockProjectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Tells the client that a projector build left part of the structure unfinished.
 *
 * <p>Sent only when the build could not place every casing cell, which is exactly when the player
 * needs to see where the gaps are. A complete build sends nothing, so the projection never flashes
 * up for a shape that is already done.</p>
 *
 * <p>The payload carries the host position so the client can show the projection for that specific
 * machine rather than whichever one it last looked at.</p>
 */
public record S2CShowMultiblockProjectionPacket(BlockPos hostPos) implements CustomPacketPayload {

    public static final Type<S2CShowMultiblockProjectionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    GoldenTweaks.MODID, "show_multiblock_projection"));

    public static final StreamCodec<FriendlyByteBuf, S2CShowMultiblockProjectionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    S2CShowMultiblockProjectionPacket::hostPos,
                    S2CShowMultiblockProjectionPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CShowMultiblockProjectionPacket packet, IPayloadContext context) {
        // The receiving side resolves the client-only handler lazily, so this class itself never
        // references a client type: loading it on a dedicated server is safe.
        context.enqueueWork(() -> GTMultiblockProjectionHandler.show(packet.hostPos()));
    }
}
