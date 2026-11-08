package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.ThaumcraftApi;
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

    /**
     * 摘下上一次注册的配方对象。{@code ThaumcraftApi} 的配方表是静态 {@code CopyOnWriteArrayList}，
     * 只增不删；集成服务端每读一次档都会重新执行整套加载，不先摘掉旧对象的话，同一配方会在
     * JEI/EMI 里随读档次数成倍增加。
     */
    static void unregisterAll() {
        if (RECIPES.isEmpty()) {
            return;
        }
        ThaumcraftApi.getCraftingRecipes().removeAll(RECIPES.values());
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
