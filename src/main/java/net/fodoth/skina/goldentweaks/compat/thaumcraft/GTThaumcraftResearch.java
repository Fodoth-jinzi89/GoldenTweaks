package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON-driven Thaumcraft research registration.
 * <p>
 * JSON files go in {@code data/<namespace>/thaumcraft/research/}.
 * <pre>{@code
 * {
 *   "type": "goldentweaks:research",
 *   "key": "GT_INFUSION_INTERCEPTER",
 *   "category": "ARTIFICE",
 *   "column": -4,
 *   "row": 6,
 *   "complexity": 3,
 *   "icon": { "id": "goldentweaks:infusion_intercepter" },
 *   "aspects": { "praecantatio": 8, "instrumentum": 4, "machina": 4 },
 *   "parents": ["INFUSION"],
 *   "concealed": true,
 *   "pages": [
 *     { "type": "goldentweaks:text", "text": "tc.research_page.GT_INFUSION_INTERCEPTER.1" },
 *     { "type": "goldentweaks:infusion", "recipe_id": "goldentweaks:infusion_intercepter" }
 *   ]
 * }
 * }</pre>
 * <p>For compatibility, a string page entry is treated as a text translation key.
 */
public final class GTThaumcraftResearch {

    private static final String TYPE = "goldentweaks:research";
    private static final String ENTRY_NAME = "research";
    private static final String ENTRY_PATH = "thaumcraft/research";

    private final String key;
    private final String category;
    private final int column;
    private final int row;
    private final int complexity;
    private final ItemStack icon;
    private final AspectList tags = new AspectList();
    private final List<String> parents = new ArrayList<>();
    private final List<ResearchPage> pages = new ArrayList<>();
    private boolean concealed;

    private GTThaumcraftResearch(
            String key,
            String category,
            int column,
            int row,
            int complexity,
            ItemStack icon
    ) {
        this.key = key;
        this.category = category;
        this.column = column;
        this.row = row;
        this.complexity = complexity;
        this.icon = icon;
    }

    boolean register() {
        if (key == null || key.isBlank() || category == null || category.isBlank()) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft research: missing key or category.");
            return false;
        }
        if (icon.isEmpty() || tags.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft research '{}': empty icon or aspects.", key);
            return false;
        }

        try {
            ResearchItem research = new ResearchItem(key, category, tags, column, row, complexity, icon);
            if (!parents.isEmpty()) {
                research.setParents(parents.toArray(String[]::new));
            }
            if (concealed) {
                research.setConcealed();
            }
            if (!pages.isEmpty()) {
                research.setPages(pages.toArray(ResearchPage[]::new));
            }
            research.registerResearchItem();
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft research '{}'.", key);
            return true;
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft research '{}': {}", key, e.getMessage());
            return false;
        }
    }

    // ---- JSON parsing ----------------------------------------------------

    static GTThaumcraftResearch fromJson(JsonObject json) {
        if (json == null || !ThaumcraftRecipeUtil.isType(json, TYPE)) {
            return null;
        }

        String key = ThaumcraftRecipeUtil.getRequiredString(json, "key");
        if (key == null || key.isBlank()) {
            return null;
        }

        String category = ThaumcraftRecipeUtil.getRequiredString(json, "category");
        if (category == null || category.isBlank()) {
            return null;
        }

        if (!json.has("column") || !json.has("row") || !json.has("complexity")) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': missing column/row/complexity.", key);
            return null;
        }
        int column = ThaumcraftRecipeUtil.getRequiredInt(json, "column");
        int row = ThaumcraftRecipeUtil.getRequiredInt(json, "row");
        int complexity = ThaumcraftRecipeUtil.getRequiredInt(json, "complexity");
        if (column == -1 || row == -1 || complexity < 0) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': invalid column/row/complexity.", key);
            return null;
        }

        JsonObject iconObject = ThaumcraftRecipeUtil.getRequiredObject(json, "icon");
        if (iconObject == null) {
            return null;
        }
        ItemStack icon = ThaumcraftRecipeUtil.parseStack(iconObject, "icon");
        if (icon.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': icon is empty.", key);
            return null;
        }

        GTThaumcraftResearch research = new GTThaumcraftResearch(key, category, column, row, complexity, icon);

        if (!readAspects(json, key, research.tags)) {
            return null;
        }

        if (!readStringList(json, "parents", key, research.parents)) {
            return null;
        }

        if (!readPages(json, key, research.pages)) {
            return null;
        }

        JsonElement concealedElement = json.get("concealed");
        if (concealedElement != null) {
            if (!concealedElement.isJsonPrimitive() || !concealedElement.getAsJsonPrimitive().isBoolean()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': 'concealed' must be a boolean.", key);
                return null;
            }
            research.concealed = concealedElement.getAsBoolean();
        }

        return research;
    }

    private static boolean readAspects(JsonObject json, String key, AspectList tags) {
        JsonObject aspectObject = ThaumcraftRecipeUtil.getRequiredObject(json, "aspects");
        if (aspectObject == null || aspectObject.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': aspects must not be empty.", key);
            return false;
        }

        for (Map.Entry<String, JsonElement> entry : aspectObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': aspect '{}' amount must be a number.", key, entry.getKey());
                return false;
            }

            int amount = value.getAsInt();
            if (amount <= 0) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': aspect '{}' amount must be positive.", key, entry.getKey());
                return false;
            }

            Aspect aspect = Aspect.get(entry.getKey());
            if (aspect == null) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': unknown aspect '{}'.", key, entry.getKey());
                return false;
            }

            tags.add(aspect, amount);
        }

        return true;
    }

    private static boolean readStringList(JsonObject json, String field, String key, List<String> out) {
        JsonElement element = json.get(field);
        if (element == null) {
            return true;
        }
        if (!element.isJsonArray()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': '{}' must be an array.", key, field);
            return false;
        }

        JsonArray array = element.getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            JsonElement child = array.get(i);
            if (!child.isJsonPrimitive() || !child.getAsJsonPrimitive().isString()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': '{}'[{}] must be a string.", key, field, i);
                return false;
            }
            out.add(child.getAsString());
        }

        return true;
    }

    private static boolean readPages(JsonObject json, String key, List<ResearchPage> out) {
        JsonElement element = json.get("pages");
        if (element == null) {
            return true;
        }
        if (!element.isJsonArray()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': 'pages' must be an array.", key);
            return false;
        }

        JsonArray array = element.getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            JsonElement child = array.get(i);
            if (child.isJsonPrimitive() && child.getAsJsonPrimitive().isString()) {
                out.add(new ResearchPage(child.getAsString()));
                continue;
            }
            if (!child.isJsonObject()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': pages[{}] must be a string or object.", key, i);
                return false;
            }

            JsonObject pageJson = child.getAsJsonObject();
            String type = ThaumcraftRecipeUtil.getRequiredString(pageJson, "type");
            if (type == null) {
                return false;
            }
            if ("goldentweaks:text".equals(type)) {
                String text = ThaumcraftRecipeUtil.getRequiredString(pageJson, "text");
                if (text == null || text.isBlank()) {
                    return false;
                }
                out.add(new ResearchPage(text));
                continue;
            }

            if (!type.equals("goldentweaks:arcane_crafting")
                    && !type.equals("goldentweaks:crucible")
                    && !type.equals("goldentweaks:infusion")
                    && !type.equals("goldentweaks:infusion_enchantment")) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': unsupported pages[{}] type '{}'.", key, i, type);
                return false;
            }

            JsonElement recipeElement = pageJson.has("recipe_id") ? pageJson.get("recipe_id") : pageJson.get("recipe");
            if (recipeElement == null || !recipeElement.isJsonPrimitive() || !recipeElement.getAsJsonPrimitive().isString()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': pages[{}] is missing string field 'recipe_id'.", key, i);
                return false;
            }
            String recipeId = recipeElement.getAsString();
            try {
                ResourceLocation id = ResourceLocation.parse(recipeId);
                ResearchPage page = GTResearchRecipePages.page(id, type);
                if (page == null) {
                    GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': pages[{}] cannot resolve {} recipe '{}'.", key, i, type, id);
                    return false;
                }
                out.add(page);
            } catch (Exception e) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft research '{}': pages[{}] has invalid recipe '{}'.", key, i, recipeId);
                return false;
            }
        }
        return true;
    }

    // ---- Resource loading -------------------------------------------------

    public static void load(ResourceManager resourceManager) {
        ThaumcraftRecipeUtil.load(resourceManager, ENTRY_PATH, ENTRY_NAME, (json, location) -> {
            GTThaumcraftResearch research = fromJson(json);
            return research != null && research.register();
        });
    }
}
