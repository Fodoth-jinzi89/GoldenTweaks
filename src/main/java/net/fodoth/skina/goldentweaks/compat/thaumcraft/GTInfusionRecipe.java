package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import thaumcraft.api.ThaumcraftApi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class GTInfusionRecipe extends AbstractGTThaumcraftRecipe<GTInfusionRecipe> {

    public enum Kind {
        CRAFTING, TRANSFORM, ENCHANTMENT
    }

    private static final String TYPE_CRAFTING = "thaumcraft:infusion_matrix";
    private static final String TYPE_TRANSFORM = "thaumcraft:infusion_transform";
    private static final String TYPE_ENCHANTMENT = "thaumcraft:infusion_enchantment";
    private static final String RECIPE_NAME = "infusion";
    private static final String RECIPE_PATH = "recipe/thaumcraft/infusion_matrix";

    private final Kind kind;
    private final int instability;
    private final List<ItemStack> components = new ArrayList<>();
    private ItemStack catalyst;
    private UnaryOperator<ItemStack> transform;
    private boolean centralItemOnly;
    private ResourceKey<Enchantment> enchantment;
    private int recipeXP = 1;

    private GTInfusionRecipe(Kind kind, String research, ItemStack output, int instability) {
        super(research, output);
        this.kind = kind;
        this.instability = instability;
    }

    public static GTInfusionRecipe create(String research, ItemLike output, int instability) {
        return create(research, new ItemStack(output), instability);
    }

    public static GTInfusionRecipe create(String research, ItemStack output, int instability) {
        return new GTInfusionRecipe(Kind.CRAFTING, research, output, instability);
    }

    public static GTInfusionRecipe createTransform(String research, UnaryOperator<ItemStack> transform, int instability) {
        GTInfusionRecipe recipe = new GTInfusionRecipe(Kind.TRANSFORM, research, ItemStack.EMPTY, instability);
        recipe.transform = transform;
        return recipe;
    }

    public static GTInfusionRecipe createEnchantment(String research, ResourceKey<Enchantment> enchantment, int instability) {
        return createEnchantment(research, enchantment, instability, 1);
    }

    public static GTInfusionRecipe createEnchantment(String research, ResourceKey<Enchantment> enchantment, int instability, int recipeXP) {
        GTInfusionRecipe recipe = new GTInfusionRecipe(Kind.ENCHANTMENT, research, ItemStack.EMPTY, instability);
        recipe.enchantment = enchantment;
        recipe.recipeXP = Math.max(1, recipeXP);
        return recipe;
    }

    @Override
    protected GTInfusionRecipe self() {
        return this;
    }

    /**
     * Transform recipes only: when true, the central item only has to be the same item
     * as the catalyst (NBT ignored); when false, the full stack must match.
     */
    public GTInfusionRecipe centralItemOnly(boolean centralItemOnly) {
        this.centralItemOnly = centralItemOnly;
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
        try {
            switch (kind) {
                case CRAFTING -> {
                    if (output.isEmpty() || !hasCatalystAndComponents()) {
                        GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft infusion recipe '{}': incomplete recipe.", research);
                        return false;
                    }
                    ThaumcraftApi.addInfusionCraftingRecipe(
                            research,
                            output,
                            instability,
                            aspects,
                            catalyst,
                            components.toArray(ItemStack[]::new)
                    );
                }
                case TRANSFORM -> {
                    if (transform == null || !hasCatalystAndComponents()) {
                        GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft infusion transform recipe '{}': incomplete recipe.", research);
                        return false;
                    }
                    ThaumcraftApi.addInfusionTransformRecipe(
                            research,
                            transform,
                            instability,
                            aspects,
                            catalyst,
                            centralItemOnly,
                            components.toArray(ItemStack[]::new)
                    );
                }
                case ENCHANTMENT -> {
                    if (enchantment == null || components.isEmpty()) {
                        GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft infusion enchantment recipe '{}': incomplete recipe.", research);
                        return false;
                    }
                    ThaumcraftApi.addInfusionEnchantmentRecipe(
                            research,
                            enchantment,
                            instability,
                            recipeXP,
                            aspects,
                            components.toArray(ItemStack[]::new)
                    );
                }
            }
            GoldenTweaks.LOGGER.debug("Registered Thaumcraft {} recipe '{}'.", kind.name().toLowerCase(), research);
            return true;
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to register Thaumcraft infusion recipe '{}': {}", research, e.getMessage());
            return false;
        }
    }

    private boolean hasCatalystAndComponents() {
        return catalyst != null && !catalyst.isEmpty() && !components.isEmpty();
    }

    public static GTInfusionRecipe fromJson(JsonObject json) {
        if (json == null) {
            return null;
        }

        JsonElement typeElement = json.get("type");
        if (typeElement == null || !typeElement.isJsonPrimitive() || !typeElement.getAsJsonPrimitive().isString()) {
            return null;
        }

        String type = typeElement.getAsString();
        return switch (type) {
            case TYPE_CRAFTING -> fromCraftingJson(json);
            case TYPE_TRANSFORM -> fromTransformJson(json);
            case TYPE_ENCHANTMENT -> fromEnchantmentJson(json);
            default -> null;
        };
    }

    private static GTInfusionRecipe fromCraftingJson(JsonObject json) {
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

        if (!readCatalyst(json, research, recipe)) {
            return null;
        }

        if (!readIngredients(json, research, recipe)) {
            return null;
        }

        return recipe;
    }

    private static GTInfusionRecipe fromTransformJson(JsonObject json) {
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
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion transform recipe '{}': result is empty.", research);
            return null;
        }

        // JSON transform recipes are restricted to a fixed output; code-driven recipes
        // may pass an arbitrary UnaryOperator via createTransform instead.
        GTInfusionRecipe recipe = createTransform(research, input -> output.copy(), instability);

        JsonElement centralElement = json.get("centralItemOnly");
        if (centralElement != null && centralElement.isJsonPrimitive() && centralElement.getAsJsonPrimitive().isBoolean()) {
            recipe.centralItemOnly(centralElement.getAsBoolean());
        }

        if (!ThaumcraftRecipeUtil.readAspects(json, research, RECIPE_NAME, recipe)) {
            return null;
        }

        if (!readCatalyst(json, research, recipe)) {
            return null;
        }

        if (!readIngredients(json, research, recipe)) {
            return null;
        }

        return recipe;
    }

    private static GTInfusionRecipe fromEnchantmentJson(JsonObject json) {
        String research = ThaumcraftRecipeUtil.getRequiredString(json, "research");
        if (research == null || research.isBlank()) {
            return null;
        }

        String enchantmentId = ThaumcraftRecipeUtil.getRequiredString(json, "enchantment");
        if (enchantmentId == null || enchantmentId.isBlank()) {
            return null;
        }

        ResourceKey<Enchantment> enchantment;
        try {
            enchantment = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(enchantmentId));
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion enchantment recipe '{}': invalid enchantment '{}'.", research, enchantmentId);
            return null;
        }

        JsonElement instabilityElement = json.get("instability");
        int instability = (instabilityElement != null && instabilityElement.isJsonPrimitive()
                && instabilityElement.getAsJsonPrimitive().isNumber())
                ? instabilityElement.getAsInt()
                : 1;
        if (instability < 1) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion enchantment recipe '{}': instability must be positive.", research);
            return null;
        }

        JsonElement xpElement = json.get("recipeXP");
        int recipeXP = (xpElement != null && xpElement.isJsonPrimitive()
                && xpElement.getAsJsonPrimitive().isNumber())
                ? xpElement.getAsInt()
                : 1;
        if (recipeXP < 1) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion enchantment recipe '{}': recipeXP must be positive.", research);
            return null;
        }

        GTInfusionRecipe recipe = createEnchantment(research, enchantment, instability, recipeXP);
        if (!ThaumcraftRecipeUtil.readAspects(json, research, RECIPE_NAME, recipe)) {
            return null;
        }

        if (!readIngredients(json, research, recipe)) {
            return null;
        }

        return recipe;
    }

    private static boolean readIngredients(JsonObject json, String research, GTInfusionRecipe recipe) {
        JsonArray ingredients = ThaumcraftRecipeUtil.getRequiredArray(json, "ingredients");
        if (ingredients == null || ingredients.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion recipe '{}': ingredients are empty.", research);
            return false;
        }

        for (int i = 0; i < ingredients.size(); i++) {
            JsonElement element = ingredients.get(i);
            if (!element.isJsonObject()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion recipe '{}': ingredients[{}] must be an object.", research, i);
                return false;
            }

            ItemStack ingredient = ThaumcraftRecipeUtil.parseStack(element.getAsJsonObject(), "ingredients[" + i + "]");
            if (ingredient.isEmpty()) {
                GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion recipe '{}': ingredients[{}] is empty.", research, i);
                return false;
            }
            recipe.component(ingredient);
        }

        return true;
    }

    private static boolean readCatalyst(JsonObject json, String research, GTInfusionRecipe recipe) {
        JsonObject catalystObject = ThaumcraftRecipeUtil.getRequiredObject(json, "catalyst");
        if (catalystObject == null) {
            return false;
        }
        ItemStack catalyst = ThaumcraftRecipeUtil.parseStack(catalystObject, "catalyst");
        if (catalyst.isEmpty()) {
            GoldenTweaks.LOGGER.warn("Invalid Thaumcraft infusion recipe '{}': catalyst is empty.", research);
            return false;
        }
        recipe.catalyst(catalyst);
        return true;
    }

    public static void load(ResourceManager resourceManager) {
        ThaumcraftRecipeUtil.load(resourceManager, RECIPE_PATH, RECIPE_NAME, (json, location) -> {
            GTInfusionRecipe recipe = fromJson(json);
            return recipe != null && recipe.register();
        });
    }
}
