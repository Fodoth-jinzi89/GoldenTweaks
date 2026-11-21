package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.GTMixinTreeText;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class MixinTreeMemoryEvent {
    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            try {
                int replaced = GTMixinTreeText.compactLoaded();
                GoldenTweaks.LOGGER.info("Shared {} Mixin template text references; all bytecode retained", replaced);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                GoldenTweaks.LOGGER.warn("Could not share Mixin template text", exception);
            }
        });
    }
}
