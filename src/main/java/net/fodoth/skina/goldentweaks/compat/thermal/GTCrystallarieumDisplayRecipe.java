package net.fodoth.skina.goldentweaks.compat.thermal;

import de.dafuqs.spectrum.recipe.crystallarieum.CrystallarieumAdditive;
import de.dafuqs.spectrum.recipe.crystallarieum.CrystallarieumRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 一条「光谱结晶 → 热力结晶器」的转换配方，只用于 JEI 展示：
 * 晶种 + 培养液（+ 可选添加剂）→ 长成后的结晶簇方块。
 */
public record GTCrystallarieumDisplayRecipe(
        Ingredient seed,
        FluidStack fluid,
        List<Ingredient> additives,
        ItemStack result
) {

    /** 培养液在 JEI 上按一整桶展示（实际只要求存在，不消耗）。 */
    public static final int FLUID_AMOUNT = 1000;

    /** 页面最多画几条添加剂，多出来的忽略。 */
    public static final int MAX_ADDITIVES = 3;

    public static GTCrystallarieumDisplayRecipe of(CrystallarieumRecipe recipe) {
        List<Ingredient> additives = new ArrayList<>();
        for (CrystallarieumAdditive additive : recipe.getAdditives()) {
            additives.add(additive.ingredient());
        }
        return new GTCrystallarieumDisplayRecipe(
                recipe.getIngredientStack(),
                fluidOf(recipe),
                List.copyOf(additives),
                resultOf(recipe));
    }

    private static FluidStack fluidOf(CrystallarieumRecipe recipe) {
        FluidStack[] stacks = recipe.getFluid().getStacks();
        return stacks.length == 0 ? FluidStack.EMPTY : stacks[0].copyWithAmount(FLUID_AMOUNT);
    }

    /** 产物就是最终生长阶段对应的那个方块（原版 {@code getResultItem} 也是这么取的）。 */
    private static ItemStack resultOf(CrystallarieumRecipe recipe) {
        List<BlockState> stages = recipe.getGrowthStages();
        return stages.isEmpty() ? ItemStack.EMPTY : new ItemStack(stages.getLast().getBlock());
    }
}
