package net.fodoth.skina.goldentweaks.compat.thermal;

import de.dafuqs.spectrum.recipe.crystallarieum.CrystallarieumRecipe;
import de.dafuqs.spectrum.registries.SpectrumRecipeTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 光谱结晶配方的运行期索引。
 *
 * <p>结晶器的输入槽 / 储罐只接受「已注册配方里出现过的物品与流体」，而光谱配方并不在 Thermal 的配方管理器里，
 * 于是种子物品与液态水晶都会被挡在门外。这里在每次配方重载时（Thermal 的配方管理器 refresh）把
 * {@code spectrum:crystallarieum_growing} 缓存下来，供 {@code validItem}/{@code validFluid} 放行，也供专业化升级
 * 匹配配方，省去每 tick 再去查一遍配方管理器。</p>
 */
public final class SpectrumCrystallarieumRecipes {

    private static volatile List<CrystallarieumRecipe> recipes = List.of();

    private SpectrumCrystallarieumRecipes() {
    }

    /**
     * 从配方管理器里取一份当前的光谱结晶配方（不改动缓存），供 JEI 这类展示端使用。
     */
    public static List<CrystallarieumRecipe> snapshot(RecipeManager recipeManager) {
        List<CrystallarieumRecipe> found = new ArrayList<>();
        for (RecipeHolder<CrystallarieumRecipe> holder : recipeManager.getAllRecipesFor(SpectrumRecipeTypes.CRYSTALLARIEUM)) {
            found.add(holder.value());
        }
        return List.copyOf(found);
    }

    public static void refresh(RecipeManager recipeManager) {
        recipes = snapshot(recipeManager);
    }

    /** 当前缓存的光谱结晶配方。 */
    public static List<CrystallarieumRecipe> all() {
        return recipes;
    }

    public static boolean isSeed(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (CrystallarieumRecipe recipe : recipes) {
            if (recipe.getIngredientStack().test(stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFluid(FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (CrystallarieumRecipe recipe : recipes) {
            if (recipe.getFluid().test(stack)) {
                return true;
            }
        }
        return false;
    }

    /** 按「种子物品 + 罐里的流体」找一条光谱结晶配方；找不到返回 null。 */
    public static CrystallarieumRecipe find(ItemStack seed, FluidStack fluid) {
        for (CrystallarieumRecipe recipe : recipes) {
            if (recipe.getIngredientStack().test(seed) && recipe.getFluid().test(fluid)) {
                return recipe;
            }
        }
        return null;
    }
}
