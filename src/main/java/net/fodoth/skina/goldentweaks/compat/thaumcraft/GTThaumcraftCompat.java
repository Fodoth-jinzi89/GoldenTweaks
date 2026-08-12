package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.neoforged.bus.api.IEventBus;

public final class GTThaumcraftCompat {

    private GTThaumcraftCompat() {
    }

    public static void register(IEventBus modEventBus) {
        GTThaumcraftAdditionalBlocks.BLOCKS.register(modEventBus);
        GTThaumcraftAdditionalBlocks.ITEMS.register(modEventBus);
        GTThaumcraftAdditionalBlockEntities.BLOCK_ENTITIES.register(modEventBus);
    }
}
