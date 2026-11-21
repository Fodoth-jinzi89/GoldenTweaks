package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.GTScanDataCompactor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class ScanDataMemoryEvent {
    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            try {
                int replaced = GTScanDataCompactor.compact(ModList.get().getAllScanData());
                GoldenTweaks.LOGGER.info("Compacted {} scan metadata sets; all records retained", replaced);
                int shared = GTScanDataCompactor.compactTypeText(ModList.get().getAllScanData());
                GoldenTweaks.LOGGER.info("Shared {} scan type text references; all type objects retained", shared);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                GoldenTweaks.LOGGER.warn("Could not compact scan metadata sets", exception);
            }
        });
    }
}
