package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.emi.GTEmiSearchEngine;
import net.fodoth.skina.goldentweaks.compat.emilink.GTEmiLinkCacheGuard;
import net.fodoth.skina.goldentweaks.compat.dangomusic.GTDangoMusicEvents;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public class ClientSetupEvent {

    /**
     * Drives the EMI tooltip index bake, a slice per tick.
     *
     * <p>It has to run on this thread: {@code getTooltipText} fires the item tooltip event, and mods
     * handling it are not all thread-safe. Doing the work here in small slices keeps it off the
     * screen's critical path without the concurrency hazard a background thread would bring.</p>
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        GTEmiSearchEngine.tickTooltipBake();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        // emilink 缓存不感知模组变化，增删模组后必须失效（否则搜索/网络标记失灵，甚至加载时 OOM）
        GTEmiLinkCacheGuard.onClientSetup();

        if (ModList.get().isLoaded("dangomusic")) {
            NeoForge.EVENT_BUS.register(GTDangoMusicEvents.class);
        }

        // Only register Cloth Config screen when Cloth Config exists
        if (ModList.get().isLoaded("cloth_config")) {

            ModLoadingContext.get().registerExtensionPoint(
                    IConfigScreenFactory.class,
                    () -> (mc, parent) ->
                            GoldenTweaksConfigScreen.create(parent)
            );


        }
    }
}
