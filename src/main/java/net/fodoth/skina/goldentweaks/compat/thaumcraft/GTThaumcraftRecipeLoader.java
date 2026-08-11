package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class GTThaumcraftRecipeLoader {

    private GTThaumcraftRecipeLoader() {
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        var resourceManager = event.getServer().getResourceManager();
        GTInfusionRecipe.load(resourceManager);
        GTCrucibleRecipe.load(resourceManager);
        GTArcaneRecipe.load(resourceManager);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        GTItemAspectEntry.load(event.getServer().getResourceManager());
    }
}
