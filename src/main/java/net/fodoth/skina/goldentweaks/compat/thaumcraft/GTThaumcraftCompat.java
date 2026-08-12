package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class GTThaumcraftCompat {

    private GTThaumcraftCompat() {
    }

    public static void register(IEventBus modEventBus) {
        GTThaumcraftAdditionalBlocks.BLOCKS.register(modEventBus);
        GTThaumcraftAdditionalBlocks.ITEMS.register(modEventBus);
        GTThaumcraftAdditionalItems.ITEMS.register(modEventBus);
        GTThaumcraftAdditionalBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        GTThaumcraftAdditionalTabs.register(modEventBus);

        // Defer research registration until items are available
        modEventBus.addListener(FMLCommonSetupEvent.class, e ->
                e.enqueueWork(GTThaumcraftResearch::register));
    }
}
