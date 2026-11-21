package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.connector.GTMappingStrings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class ConnectorMappingMemoryEvent {
    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        if (!ModList.get().isLoaded("connector")) return;
        event.enqueueWork(() -> {
            try {
                int replaced = GTMappingStrings.compactLoadedResolver();
                GoldenTweaks.LOGGER.info("Shared {} duplicate Connector mapping strings", replaced);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                GoldenTweaks.LOGGER.warn("Could not compact Connector mapping strings", exception);
            }
        });
    }
}
