package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

public final class GTInfusionRecipeLoader {

    private GTInfusionRecipeLoader() {
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        GTInfusionRecipe.load(event.getServer().getResourceManager());
    }
}