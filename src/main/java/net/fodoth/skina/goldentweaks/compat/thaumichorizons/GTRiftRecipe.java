package net.fodoth.skina.goldentweaks.compat.thaumichorizons;

import com.google.gson.JsonObject;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.ThaumcraftRecipeUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON-driven void crafting recipes for the Thaumic Horizons rift (planar vortex).
 * <p>
 * JSON files go in {@code data/<namespace>/recipe/thaumichorizons/rift_crafting/}.
 * <p>
 * Offerings can be a single item or an item tag; the result may carry a count. Recipes are
 * consulted after Thaumic Horizons' own hard-coded rift recipes, so anything the rift already
 * handles keeps its original behaviour.
 *
 * <pre>{@code
 * {
 *   "type": "goldentweaks:rift_crafting",
 *   "input": { "type": "item", "id": "minecraft:wither_skeleton_skull" },
 *   "result": { "id": "minecraft:nether_star", "count": 1 }
 * }
 * }</pre>
 *
 * <pre>{@code
 * {
 *   "type": "goldentweaks:rift_crafting",
 *   "input": { "type": "tag", "id": "minecraft:skulls" },
 *   "result": { "id": "minecraft:wither_skeleton_skull" }
 * }
 * }</pre>
 */
public final class GTRiftRecipe {

    private static final String TYPE = "goldentweaks:rift_crafting";
    private static final String ENTRY_NAME = "rift crafting";
    private static final String ENTRY_PATH = "recipe/thaumichorizons/rift_crafting";

    private static final List<GTRiftRecipe> RECIPES = new ArrayList<>();

    private final ResourceLocation id;
    /** Either an {@link ItemStack} or a {@link TagKey TagKey&lt;Item&gt;}. */
    private final Object input;
    private final ItemStack result;

    private GTRiftRecipe(ResourceLocation id, Object input, ItemStack result) {
        this.id = id;
        this.input = input;
        this.result = result;
    }

    // ---- Lookup ----------------------------------------------------------

    public static boolean isEmpty() {
        return RECIPES.isEmpty();
    }

    /** Recipe id as loaded, used by the recipe viewers. */
    public ResourceLocation id() {
        return id;
    }

    /** Either the expected {@link ItemStack} or a {@link TagKey TagKey&lt;Item&gt;}. */
    public Object input() {
        return input;
    }

    public ItemStack result() {
        return result.copy();
    }

    static List<GTRiftRecipe> entries() {
        return List.copyOf(RECIPES);
    }

    /**
     * @return a copy of the result for the given offering, or an empty stack when no recipe matches.
     */
    public static ItemStack find(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (GTRiftRecipe recipe : RECIPES) {
            if (recipe.matches(stack)) {
                return recipe.result.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean matches(ItemStack stack) {
        if (input instanceof ItemStack expected) {
            return ItemStack.isSameItemSameComponents(expected, stack);
        }

        if (input instanceof TagKey<?> tag) {
            @SuppressWarnings("unchecked")
            TagKey<Item> itemTag = (TagKey<Item>) tag;
            return stack.is(itemTag);
        }

        return false;
    }

    // ---- JSON parsing ----------------------------------------------------

    static GTRiftRecipe fromJson(JsonObject json, ResourceLocation location) {
        if (json == null || !ThaumcraftRecipeUtil.isType(json, TYPE)) {
            return null;
        }

        JsonObject inputObject = ThaumcraftRecipeUtil.getRequiredObject(json, "input");
        if (inputObject == null) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumic Horizons rift crafting recipe '{}': missing 'input'.", location);
            return null;
        }

        Object input = ThaumcraftRecipeUtil.parseIngredient(inputObject, location, ENTRY_NAME, "input");
        if (input == null) {
            return null;
        }

        JsonObject resultObject = ThaumcraftRecipeUtil.getRequiredObject(json, "result");
        if (resultObject == null) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumic Horizons rift crafting recipe '{}': missing 'result'.", location);
            return null;
        }

        ItemStack result = ThaumcraftRecipeUtil.parseStack(resultObject, "result");
        if (result.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumic Horizons rift crafting recipe '{}': invalid 'result'.", location);
            return null;
        }

        return new GTRiftRecipe(location, input, result);
    }

    // ---- Resource loading -------------------------------------------------

    public static void load(ResourceManager resourceManager) {
        RECIPES.clear();
        ThaumcraftRecipeUtil.load(resourceManager, ENTRY_PATH, ENTRY_NAME, (json, location) -> {
            GTRiftRecipe recipe = fromJson(json, location);
            if (recipe == null) {
                return false;
            }
            RECIPES.add(recipe);
            return true;
        });
    }
}
