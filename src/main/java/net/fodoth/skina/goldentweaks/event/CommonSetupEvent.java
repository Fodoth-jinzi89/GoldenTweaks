package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.lang.reflect.Method;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public class CommonSetupEvent {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                Class<?> clazz = Class.forName(
                        "org.craftamethyst.tritium.util.random.TritiumRandomManager"
                );
                Method reload = clazz.getMethod("reloadConfig");
                reload.invoke(null);
            } catch (Throwable ignored) {
            }
        });
    }
}
