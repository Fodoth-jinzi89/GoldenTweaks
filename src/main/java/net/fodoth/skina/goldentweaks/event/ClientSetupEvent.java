package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksConfigScreen;
import net.fodoth.skina.goldentweaks.util.GTState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public class ClientSetupEvent {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        GTState.setReady();

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
