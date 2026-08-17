package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonObject;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import thaumcraft.api.ThaumcraftApi;

public class GTCrucibleRecipe extends AbstractGTThaumcraftRecipe<GTCrucibleRecipe> {

    private static final String TYPE = "thaumcraft:crucible";
    private static final String RECIPE_NAME = "crucible";
    private static final String RECIPE_PATH = "recipe/thaumcraft/crucible";

    private final Object catalyst;

    private GTCrucibleRecipe(String research, ItemStack output, Object catalyst) {
        super(research, output);
        this.catalyst = catalyst;
    }

    public static GTCrucibleRecipe create(String research, ItemLike output, Object catalyst) {
        return create(research, new ItemStack(output), catalyst);
    }

    public static GTCrucibleRecipe create(String research, ItemStack output, Object catalyst) {
        return new GTCrucibleRecipe(research, output, catalyst);
    }

    @Override
    protected GTCrucibleRecipe self() {
        return this;
    }

    public Object register() {
        if (output.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft crucible recipe '{}': empty output.", research);
            return null;
        }

        if (catalyst == null) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft crucible recipe '{}': missing catalyst.", research);
            return null;
        }

        try {
            Object registered = ThaumcraftApi.addCrucibleRecipe(research, output, catalyst, aspects);
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft crucible recipe '{}'.", research);
            return registered;
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft crucible recipe '{}': {}", research, e.getMessage());
            return null;
        }
    }

    public static GTCrucibleRecipe fromJson(JsonObject json) {
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
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft crucible recipe '{}': result is empty.", research);
            return null;
        }

        JsonObject catalystObject = ThaumcraftRecipeUtil.getRequiredObject(json, "catalyst");
        if (catalystObject == null) {
            return null;
        }

        ResourceLocation source = ResourceLocation.fromNamespaceAndPath("goldentweaks", research);
        Object catalyst = ThaumcraftRecipeUtil.parseIngredient(catalystObject, source, RECIPE_NAME, "catalyst");
        if (catalyst == null) {
            return null;
        }

        GTCrucibleRecipe recipe = create(research, output, catalyst);
        return ThaumcraftRecipeUtil.readAspects(json, research, RECIPE_NAME, recipe) ? recipe : null;
    }

    public static void load(ResourceManager resourceManager) {
        ThaumcraftRecipeUtil.load(resourceManager, RECIPE_PATH, RECIPE_NAME, (json, location) -> {
            GTCrucibleRecipe recipe = fromJson(json);
            Object registered = recipe == null ? null : recipe.register();
            GTResearchRecipePages.put(location, registered);
            return registered != null;
        });
    }
}
