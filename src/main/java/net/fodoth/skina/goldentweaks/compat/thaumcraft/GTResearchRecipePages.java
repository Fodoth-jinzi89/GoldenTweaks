package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.InfusionEnchantmentRecipe;
import thaumcraft.api.research.ResearchPage;

import java.util.HashMap;
import java.util.Map;

final class GTResearchRecipePages {

    private static final Map<ResourceLocation, Object> RECIPES = new HashMap<>();

    private GTResearchRecipePages() {
    }

    static void clear() {
        RECIPES.clear();
    }

    static void put(ResourceLocation id, Object recipe) {
        if (id != null && recipe != null) {
            RECIPES.put(recipeId(id), recipe);
        }
    }

    private static ResourceLocation recipeId(ResourceLocation resource) {
        String path = resource.getPath();
        for (String directory : new String[]{"arcane_crafting/", "crucible/", "infusion_matrix/"}) {
            int index = path.indexOf(directory);
            if (index >= 0) {
                String recipePath = path.substring(index + directory.length());
                if (recipePath.endsWith(".json")) {
                    recipePath = recipePath.substring(0, recipePath.length() - 5);
                }
                return ResourceLocation.fromNamespaceAndPath(resource.getNamespace(), recipePath);
            }
        }
        return resource;
    }

    static ResearchPage page(ResourceLocation id, String type) {
        Object recipe = RECIPES.get(id);
        if ("goldentweaks:arcane_crafting".equals(type) && recipe instanceof IArcaneRecipe arcane) {
            return new ResearchPage(arcane);
        }
        if ("goldentweaks:crucible".equals(type) && recipe instanceof CrucibleRecipe crucible) {
            return new ResearchPage(crucible);
        }
        if ("goldentweaks:infusion".equals(type) && recipe instanceof InfusionRecipe infusion) {
            return new ResearchPage(infusion);
        }
        if ("goldentweaks:infusion_enchantment".equals(type) && recipe instanceof InfusionEnchantmentRecipe enchantment) {
            return new ResearchPage(enchantment);
        }
        return null;
    }
}
