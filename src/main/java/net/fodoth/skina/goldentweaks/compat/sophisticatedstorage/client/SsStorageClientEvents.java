package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SsStorageMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public class SsStorageClientEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        if (!ModList.get().isLoaded("create") || !ModList.get().isLoaded("sophisticatedstorage")) {
            return;
        }
        event.register(SsStorageMenus.STORAGE.get(), SsStorageScreen::new);
        event.register(SsStorageMenus.SETTINGS.get(), SsStorageSettingsScreen::new);
    }
}
