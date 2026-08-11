package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import thaumcraft.api.ThaumcraftApi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GTArcaneRecipe extends AbstractGTThaumcraftRecipe<GTArcaneRecipe> {

    private static final String TYPE = "thaumcraft:arcane_crafting";
    private static final String RECIPE_NAME = "arcane";
    private static final String RECIPE_PATH = "recipe/thaumcraft/arcane_crafting";

    private GTArcaneRecipe(String research, ItemStack output) {
        super(research, output);
    }

    public static GTArcaneRecipe create(String research, ItemLike output) {
        return create(research, new ItemStack(output));
    }

    public static GTArcaneRecipe create(String research, ItemStack output) {
        return new GTArcaneRecipe(research, output);
    }

    @Override
    protected GTArcaneRecipe self() {
        return this;
    }

    public void registerShaped(boolean mirrored, Object... recipe) {
        if (output.isEmpty() || recipe.length == 0) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft arcane shaped recipe '{}': invalid recipe.", research);
            return;
        }

        try {
            Object[] args = new Object[recipe.length + 1];
            args[0] = mirrored;
            System.arraycopy(recipe, 0, args, 1, recipe.length);

            ThaumcraftApi.addArcaneCraftingRecipe(research, output, aspects, args);
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft arcane shaped recipe '{}'.", research);
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft arcane shaped recipe '{}': {}", research, e.getMessage());
        }
    }

    public void registerShapeless(Object... recipe) {
        if (output.isEmpty() || recipe.length == 0) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft arcane shapeless recipe '{}': invalid recipe.", research);
            return;
        }

        try {
            ThaumcraftApi.addShapelessArcaneCraftingRecipe(research, output, aspects, recipe);
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft arcane shapeless recipe '{}'.", research);
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft arcane shapeless recipe '{}': {}", research, e.getMessage());
        }
    }

    public static GTArcaneRecipe fromJson(JsonObject json) {
        if (json == null || !ThaumcraftRecipeUtil.isType(json, TYPE)) {
            return null;
        }

        String research = ThaumcraftRecipeUtil.getRequiredString(json, "research");
        if (research == null || research.isBlank()) {
            return null;
        }

        JsonObject resultObject = ThaumcraftRecipeUtil.getRequiredObject(json, "result");
        if (resultObject == null) {
            return null;
        }

        ItemStack output = ThaumcraftRecipeUtil.parseStack(resultObject, "result");
        if (output.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft arcane recipe '{}': result is empty.", research);
            return null;
        }

        GTArcaneRecipe recipe = create(research, output);
        return ThaumcraftRecipeUtil.readAspects(json, research, RECIPE_NAME, recipe) ? recipe : null;
    }

    public static void load(ResourceManager resourceManager) {
        ThaumcraftRecipeUtil.load(resourceManager, RECIPE_PATH, RECIPE_NAME, GTArcaneRecipe::loadRecipe);
    }

    private static boolean loadRecipe(JsonObject json, ResourceLocation location) {
        GTArcaneRecipe recipe = fromJson(json);
        if (recipe == null) {
            return false;
        }

        String mode = ThaumcraftRecipeUtil.getRequiredString(json, "mode");
        if (mode == null) {
            return false;
        }

        return switch (mode) {
            case "shaped" -> recipe.loadShaped(json, location);
            case "shapeless" -> recipe.loadShapeless(json, location);
            default -> {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': unsupported mode '{}'.", location, mode);
                yield false;
            }
        };
    }

    private boolean loadShaped(JsonObject json, ResourceLocation location) {
        JsonArray patternArray = ThaumcraftRecipeUtil.getRequiredArray(json, "pattern");
        if (patternArray == null || patternArray.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': pattern is empty.", location);
            return false;
        }

        String[] pattern = parsePattern(patternArray, location);
        if (pattern == null) {
            return false;
        }

        JsonObject keyObject = ThaumcraftRecipeUtil.getRequiredObject(json, "key");
        if (keyObject == null) {
            return false;
        }

        Object[] recipe = buildShapedRecipe(pattern, keyObject, location);
        if (recipe == null) {
            return false;
        }

        JsonElement mirroredElement = json.get("mirrored");
        boolean mirrored = true;
        if (mirroredElement != null) {
            if (!mirroredElement.isJsonPrimitive() || !mirroredElement.getAsJsonPrimitive().isBoolean()) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': 'mirrored' must be boolean.", location);
                return false;
            }
            mirrored = mirroredElement.getAsBoolean();
        }

        registerShaped(mirrored, recipe);
        return true;
    }

    private boolean loadShapeless(JsonObject json, ResourceLocation location) {
        JsonArray ingredients = ThaumcraftRecipeUtil.getRequiredArray(json, "ingredients");
        if (ingredients == null || ingredients.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': ingredients are empty.", location);
            return false;
        }

        if (ingredients.size() > 9) {
            GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': too many ingredients (max 9).", location);
            return false;
        }

        Object[] recipe = new Object[ingredients.size()];
        for (int i = 0; i < ingredients.size(); i++) {
            Object ingredient = ThaumcraftRecipeUtil.parseIngredient(ingredients.get(i), location, RECIPE_NAME, "ingredient");
            if (ingredient == null) {
                return false;
            }
            recipe[i] = ingredient;
        }

        registerShapeless(recipe);
        return true;
    }

    private static String[] parsePattern(JsonArray patternArray, ResourceLocation location) {
        String[] pattern = new String[patternArray.size()];
        int width = -1;

        for (int i = 0; i < patternArray.size(); i++) {
            JsonElement element = patternArray.get(i);
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': pattern[{}] is invalid.", location, i);
                return null;
            }

            pattern[i] = element.getAsString();
            if (pattern[i].isEmpty()) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': pattern[{}] is empty.", location, i);
                return null;
            }

            if (width == -1) {
                width = pattern[i].length();
            } else if (pattern[i].length() != width) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': pattern rows have different widths.", location);
                return null;
            }

            if (width > 3 || pattern.length > 3) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': pattern exceeds 3x3.", location);
                return null;
            }
        }

        return pattern;
    }

    private static Object[] buildShapedRecipe(String[] pattern, JsonObject keyObject, ResourceLocation location) {
        Set<Character> usedKeys = new HashSet<>();
        for (String row : pattern) {
            for (char character : row.toCharArray()) {
                if (character != ' ') {
                    usedKeys.add(character);
                }
            }
        }

        Object[] recipe = new Object[pattern.length + keyObject.size() * 2];
        int index = 0;
        for (String row : pattern) {
            recipe[index++] = row;
        }

        for (Map.Entry<String, JsonElement> entry : keyObject.entrySet()) {
            if (entry.getKey().length() != 1) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': invalid key '{}'.", location, entry.getKey());
                return null;
            }

            char key = entry.getKey().charAt(0);
            if (!usedKeys.contains(key)) {
                GoldenTweaks.LOGGER.warn("Skipped Thaumcraft arcane recipe '{}': key '{}' is not used by pattern.", location, key);
                return null;
            }

            Object ingredient = ThaumcraftRecipeUtil.parseIngredient(entry.getValue(), location, RECIPE_NAME, "ingredient");
            if (ingredient == null) {
                return null;
            }

            recipe[index++] = key;
            recipe[index++] = ingredient;
        }

        return recipe;
    }
}
