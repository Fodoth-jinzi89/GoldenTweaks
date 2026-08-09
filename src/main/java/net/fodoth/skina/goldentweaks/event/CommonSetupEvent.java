package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

import java.lang.reflect.Method;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public class CommonSetupEvent {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

            // Tritium
            try {
                Class<?> clazz = Class.forName(
                        "org.craftamethyst.tritium.util.random.TritiumRandomManager"
                );

                Method reload = clazz.getMethod("reloadConfig");
                reload.invoke(null);

                GoldenTweaks.LOGGER.info("Reloaded Tritium random config");
            } catch (Throwable ignored) {
            }

            // QuestShop FTB Quests integration bootstrap fix
            try {
                Class<?> clazz = Class.forName(
                        "com.holysweet.questshop.integrations.IntegrationBootstrap"
                );

                Method bootstrap = clazz.getMethod("bootstrap");
                bootstrap.invoke(null);

                GoldenTweaks.LOGGER.info(
                        "Delayed QuestShop integration bootstrap completed"
                );
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable t) {
                GoldenTweaks.LOGGER.warn(
                        "Failed to bootstrap QuestShop integrations",
                        t
                );
            }
        });
    }
}