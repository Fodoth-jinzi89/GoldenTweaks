package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.minecraft.core.BlockPos;

/**
 * Client entry point for the projection packet.
 *
 * <p>Kept apart from the payload so the packet class stays free of client-only types and can be
 * loaded on a dedicated server.</p>
 */
public final class GTMultiblockProjectionHandler {

    private GTMultiblockProjectionHandler() {
    }

    public static void show(BlockPos hostPos) {
        GTMultiblockProjection.show(hostPos);
    }
}
