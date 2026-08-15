package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonObject;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import thaumcraft.api.research.ResearchCategories;

/**
 * JSON-driven Thaumcraft research category registration.
 * <p>
 * JSON files go in {@code data/<namespace>/thaumcraft/categories/}.
 * <pre>{@code
 * {
 *   "type": "goldentweaks:category",
 *   "key": "GT_ARTIFICE",
 *   "icon": "goldentweaks:textures/misc/r_gt_artifice.png",
 *   "background": "thaumcraft:textures/gui/gui_researchback.png"
 * }
 * }</pre>
 * <p>{@code icon} and {@code background} are texture resource locations.
 * Categories must be registered before the research entries that reference them.
 */
public final class GTThaumcraftCategory {

    private static final String TYPE = "goldentweaks:category";
    private static final String ENTRY_NAME = "category";
    private static final String ENTRY_PATH = "thaumcraft/categories";

    private final String key;
    private final ResourceLocation icon;
    private final ResourceLocation background;

    private GTThaumcraftCategory(String key, ResourceLocation icon, ResourceLocation background) {
        this.key = key;
        this.icon = icon;
        this.background = background;
    }

    boolean register() {
        if (key == null || key.isBlank() || icon == null || background == null) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft category: missing key, icon or background.");
            return false;
        }

        try {
            ResearchCategories.registerCategory(key, icon, background);
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft category '{}'.", key);
            return true;
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft category '{}': {}", key, e.getMessage());
            return false;
        }
    }

    // ---- JSON parsing ----------------------------------------------------

    static GTThaumcraftCategory fromJson(JsonObject json) {
        if (json == null || !ThaumcraftRecipeUtil.isType(json, TYPE)) {
            return null;
        }

        String key = ThaumcraftRecipeUtil.getRequiredString(json, "key");
        if (key == null || key.isBlank()) {
            return null;
        }

        ResourceLocation icon = parseLocation(ThaumcraftRecipeUtil.getRequiredString(json, "icon"));
        if (icon == null) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft category '{}': invalid icon.", key);
            return null;
        }

        ResourceLocation background = parseLocation(ThaumcraftRecipeUtil.getRequiredString(json, "background"));
        if (background == null) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft category '{}': invalid background.", key);
            return null;
        }

        return new GTThaumcraftCategory(key, icon, background);
    }

    private static ResourceLocation parseLocation(String value) {
        try {
            return ResourceLocation.parse(value);
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Invalid resource location '{}': {}", value, e.getMessage());
            return null;
        }
    }

    // ---- Resource loading -------------------------------------------------

    public static void load(ResourceManager resourceManager) {
        ThaumcraftRecipeUtil.load(resourceManager, ENTRY_PATH, ENTRY_NAME, (json, location) -> {
            GTThaumcraftCategory category = fromJson(json);
            return category != null && category.register();
        });
    }
}
