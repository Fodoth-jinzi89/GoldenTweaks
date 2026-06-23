package net.fodoth.skina.goldentweaks.compat.kaleidoscope;

import net.neoforged.bus.api.IEventBus;

public final class KaleidoCompat {

    private KaleidoCompat() {
    }

    public static void register(IEventBus modEventBus) {
        KaleidoAdditionalBlocks.BLOCKS.register(modEventBus);
        KaleidoAdditionalBlocks.ITEMS.register(modEventBus);
        KaleidoAdditionalBlockEntities.BLOCK_ENTITIES.register(modEventBus);
    }
}
