package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMultiblock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTVacuumCoolerMultiblock;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;

/**
 * Binds the kettle's screen to its menu type.
 *
 * <p>Both the menu and the block live in the {@code avaritia_more_items} compat package and extend
 * AMI classes, so nothing is touched unless that mod is present.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public class GTChemicalReactorClientEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        if (!ModList.get().isLoaded("avaritia_more_items")) {
            return;
        }
        event.register(GTChemicalReactorMultiblock.MENU.get(), GTChemicalReactorScreen::new);
        // The advanced kettle shares the window class, so it shares the EMI bounds provider too:
        // that one is registered against the screen class, and a subclass matches it.
        event.register(GTAdvancedChemicalReactorMultiblock.MENU.get(),
                GTAdvancedChemicalReactorScreen::new);
        // The vacuum cooler has a menu type of its own, and a menu without a registered screen is
        // closed by the client on the tick it opens — which is why right-clicking the controller
        // appeared to do nothing.
        event.register(GTVacuumCoolerMultiblock.MENU.get(), GTVacuumCoolerScreen::new);
    }
}
