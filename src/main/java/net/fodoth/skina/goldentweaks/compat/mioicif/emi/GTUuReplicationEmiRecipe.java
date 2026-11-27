package net.fodoth.skina.goldentweaks.compat.mioicif.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * One replicator page: UU matter in, one item out.
 *
 * <p>The machine has no recipe list of its own — it asks the quote book what an item is worth at run
 * time — so the page is generated from the reference price table instead, which is the same table
 * the server seeds its quotes from.</p>
 */
final class GTUuReplicationEmiRecipe implements EmiRecipe {

    static final int WIDTH = 76;
    static final int HEIGHT = 18;

    /**
     * 三个元素水平排开：18 宽的流体槽、28 宽的箭头、18 宽的物品槽，间距 6。
     * 箭头宽 28 是因为 EMI 的 {@code addFillingArrow} 按 28×18 的贴图绘制，
     * 图标本身居中，留窄了会压到两侧槽位。
     */
    private static final int SLOT = 18;
    private static final int ARROW_W = 28;
    private static final int GAP = 6;
    private static final int INPUT_X = 0;
    private static final int ARROW_X = INPUT_X + SLOT + GAP;
    private static final int OUTPUT_X = ARROW_X + ARROW_W + GAP;

    private final ResourceLocation id;
    private final EmiIngredient uu;
    private final EmiStack output;

    GTUuReplicationEmiRecipe(ResourceLocation id, Fluid uuFluid, ItemStack result,
                             long millibuckets) {
        this.id = id;
        this.output = EmiStack.of(result);
        this.uu = dev.emi.emi.api.neoforge.NeoForgeEmiStack.of(
                new FluidStack(uuFluid, (int) Math.min(Integer.MAX_VALUE, millibuckets)));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTUuReplicationEmiPlugin.category();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(uu);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // 流体在左，箭头居中，物品在右，三者同一水平线。
        widgets.addSlot(uu, INPUT_X, 0);
        widgets.addFillingArrow(ARROW_X, 0, 1000);
        widgets.addSlot(output, OUTPUT_X, 0).recipeContext(this);
    }
}
