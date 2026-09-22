package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumichorizons.GTRiftRecipe;
import net.minecraft.server.packs.resources.ResourceManager;
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

    /**
     * Registers the datapack-defined content the client has to know about to mirror the server.
     *
     * <p>The server loads all of it from its datapack resource manager while starting up, but the
     * client-side resource manager only exposes {@code assets/}. On a dedicated server the client
     * would therefore keep an empty aspect registry, an empty {@code ThaumcraftApi} object-tag
     * registry and an empty research index: custom aspects, item aspects and the thaumonomicon's
     * custom categories and research would all stay invisible. The client reads the same files
     * through a server-data view of its own packs (see {@code GTThaumcraftClientData}).</p>
     *
     * <p>The order mirrors {@link #onServerAboutToStart} / {@link #onServerStarted}: aspects first
     * because everything below resolves aspects by tag, then the recipes because research pages
     * resolve their recipe by id, then the research that refers to both, then the object tags.</p>
     */
    public static void loadClientData(ResourceManager resourceManager) {
        GTAspectEntry.load(resourceManager);
        // 要素安瓿靠静态字段记住自己的要素，客户端不绑定就会显示为空。
        GTAspectPhials.bind();

        GTThaumcraftCategory.load(resourceManager);
        GTResearchRecipePages.clear();
        GTInfusionRecipe.load(resourceManager);
        GTCrucibleRecipe.load(resourceManager);
        GTArcaneRecipe.load(resourceManager);
        GTThaumcraftResearch.load(resourceManager);
        if (ModList.get().isLoaded("thaumichorizons")) {
            GTRiftRecipe.load(resourceManager);
        }

        GTItemAspectEntry.load(resourceManager);
        GTEntityAspectEntry.load(resourceManager);
        // 刚注册的要素必须立刻生效，不能命中启动阶段（JEI/EMI 预索引）用回退数据算出的缓存。
        ObjectTagsCache.clear();
    }
}
