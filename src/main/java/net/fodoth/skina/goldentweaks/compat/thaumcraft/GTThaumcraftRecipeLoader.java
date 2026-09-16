package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumichorizons.GTRiftRecipe;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class GTThaumcraftRecipeLoader {

    private GTThaumcraftRecipeLoader() {
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        var resourceManager = event.getServer().getResourceManager();
        // Aspects must be registered before recipes reference them.
        GTAspectEntry.load(resourceManager);
        // Bind the freshly registered aspects onto the vanilla phial items.
        GTAspectPhials.bind();
        // Categories must be registered before research entries reference them.
        GTThaumcraftCategory.load(resourceManager);
        GTResearchRecipePages.clear();
        GTInfusionRecipe.load(resourceManager);
        GTCrucibleRecipe.load(resourceManager);
        GTArcaneRecipe.load(resourceManager);
        // Recipe pages resolve the recipes registered immediately above.
        GTThaumcraftResearch.load(resourceManager);
        // Thaumic Horizons rift crafting (planar vortex).
        if (ModList.get().isLoaded("thaumichorizons")) {
            GTRiftRecipe.load(resourceManager);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        GTItemAspectEntry.load(event.getServer().getResourceManager());
        GTEntityAspectEntry.load(event.getServer().getResourceManager());
    }
}
