package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import thaumcraft.api.ThaumcraftApi;

import java.util.ArrayList;
import java.util.List;

public class GTInfusionRecipe extends AbstractGTThaumcraftRecipe<GTInfusionRecipe> {

    private static final String TYPE = "thaumcraft:infusion_matrix";
    private static final String RECIPE_NAME = "infusion";
    private static final String RECIPE_PATH = "recipe/thaumcraft/infusion_matrix";

    private final int instability;
    private final List<ItemStack> components = new ArrayList<>();
    private ItemStack catalyst;

    private GTInfusionRecipe(String research, ItemStack output, int instability) {
        super(research, output);
        this.instability = instability;
    }

    public static GTInfusionRecipe create(String research, ItemLike output, int instability) {
        return create(research, new ItemStack(output), instability);
    }

    public static GTInfusionRecipe create(String research, ItemStack output, int instability) {
        return new GTInfusionRecipe(research, output, instability);
    }

    @Override
    protected GTInfusionRecipe self() {
        return this;
    }

    public GTInfusionRecipe catalyst(ItemLike item) {
        if (item != null) {
            catalyst = new ItemStack(item);
        }
        return this;
    }

    public GTInfusionRecipe catalyst(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            catalyst = stack.copy();
        }
        return this;
    }

    public GTInfusionRecipe component(ItemLike item) {
        if (item != null) {
            components.add(new ItemStack(item));
        }
        return this;
    }

    public GTInfusionRecipe component(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            components.add(stack.copy());
        }
        return this;
    }

    public GTInfusionRecipe components(ItemLike... items) {
        if (items != null) {
            for (ItemLike item : items) {
                component(item);
            }
        }
        return this;
    }

    public GTInfusionRecipe components(ItemStack... stacks) {
        if (stacks != null) {
            for (ItemStack stack : stacks) {
                component(stack);
            }
        }
        return this;
    }

    public boolean register() {
        if (output.isEmpty() || catalyst == null || catalyst.isEmpty() || components.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft infusion recipe '{}': incomplete recipe.", research);
            return false;
        }

        try {
            ThaumcraftApi.addInfusionCraftingRecipe(
                    research,
                    output,
                    instability,
                    aspects,
                    catalyst,
                    components.toArray(ItemStack[]::new)
            );
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft infusion recipe '{}'.", research);
            return true;
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft infusion recipe '{}': {}", research, e.getMessage());
            return false;
        }
    }

    public static GTInfusionRecipe fromJson(JsonObject json) {
        if (json == null || !ThaumcraftRecipeUtil.isType(json, TYPE)) {
            return null;
        }

        String research = ThaumcraftRecipeUtil.getRequiredString(json, "research");
        if (research == null || research.isBlank()) {
            return null;
        }

        int instability = ThaumcraftRecipeUtil.getRequiredInt(json, "instability");
        if (instability < 0) {
            return null;
        }

        JsonObject resultObject = ThaumcraftRecipeUtil.getRequiredObject(json, "result");
        if (resultObject == null) {
            return null;
        }

        ItemStack output = ThaumcraftRecipeUtil.parseStack(resultObject, "result");
        if (output.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion recipe '{}': result is empty.", research);
            return null;
        }

        GTInfusionRecipe recipe = create(research, output, instability);
        if (!ThaumcraftRecipeUtil.readAspects(json, research, RECIPE_NAME, recipe)) {
            return null;
        }

        JsonObject catalystObject = ThaumcraftRecipeUtil.getRequiredObject(json, "catalyst");
        if (catalystObject == null) {
            return null;
        }

        ItemStack catalyst = ThaumcraftRecipeUtil.parseStack(catalystObject, "catalyst");
        if (catalyst.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion recipe '{}': catalyst is empty.", research);
            return null;
        }
        recipe.catalyst(catalyst);

        JsonArray ingredients = ThaumcraftRecipeUtil.getRequiredArray(json, "ingredients");
        if (ingredients == null || ingredients.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion recipe '{}': ingredients are empty.", research);
            return null;
        }

        for (int i = 0; i < ingredients.size(); i++) {
            JsonElement element = ingredients.get(i);
            if (!element.isJsonObject()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion recipe '{}': ingredients[{}] must be an object.", research, i);
                return null;
            }

            ItemStack ingredient = ThaumcraftRecipeUtil.parseStack(element.getAsJsonObject(), "ingredients[" + i + "]");
            if (ingredient.isEmpty()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion recipe '{}': ingredients[{}] is empty.", research, i);
                return null;
            }
            recipe.component(ingredient);
        }

        return recipe;
    }

    public static void load(ResourceManager resourceManager) {
        ThaumcraftRecipeUtil.load(resourceManager, RECIPE_PATH, RECIPE_NAME, (json, location) -> {
            GTInfusionRecipe recipe = fromJson(json);
            return recipe != null && recipe.register();
        });
    }
}
