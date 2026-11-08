package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.miophas.singularity_iteration.common.client.integration.emi.MioIcifEmiRecipe;
import com.miophas.singularity_iteration.common.recipe.blast_furnace.mio_icif_BlastFurnaceRecipe;
import com.miophas.singularity_iteration.common.recipe.centrifuge.mio_icif_CentrifugeRecipe;
import com.miophas.singularity_iteration.common.recipe.washer.mio_icif_WasherRecipe;
import com.miophas.singularity_iteration.core.api.recipe.IRecipeProcessingInfo;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * mio_icif 的 EMI 配方页与它自己的 JEI 分类有两处对不上：
 * <ul>
 *     <li>输入数量：EMI 侧只对粉碎机（powder）与混合机（mix）用了配方自带的
 *     {@code getIngredientCount()}，其余类型统一走 {@code EmiIngredient.of(ingredient)}（恒为 1），
 *     于是压缩机（9 板 → 1 致密板）、高炉、切石机、金属成型机这些输入数量不为 1 的配方全显示成
 *     「1 → 结果」，而 JEI 分类显示的是真实数量。</li>
 *     <li>副产物：{@link MioIcifEmiRecipe} 只画一个输出槽、且只取 {@code getResultItem()}（主产物），
 *     洗涤机 / 离心机的次级、三级产物与高炉的副产物在 EMI 里完全看不到。</li>
 * </ul>
 * 这里在配方对象构造完成后（{@code TAIL}）按 {@link IRecipeProcessingInfo#getIngredientCount()} 回填输入
 * 数量，并把洗涤机 / 离心机（{@code getAllResults()}，已带各自数量）与高炉副产物追加进输出列表；
 * 因为原布局放不下，再在页面尾部补画这些额外输出槽位、并把展示高度相应加高。
 */
@Mixin(value = MioIcifEmiRecipe.class, remap = false)
public abstract class MioIcifEmiRecipeMixin {

    /** 原布局里主产物所在列，额外产物顺着这一列往下排。 */
    @Unique
    private static final int gt$OUTPUT_X = 109;
    @Unique
    private static final int gt$PRIMARY_Y = 28;
    @Unique
    private static final int gt$SLOT_STEP = 22;

    @Shadow
    @Final
    private List<EmiIngredient> inputs;

    @Shadow
    @Final
    private List<EmiStack> outputs;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gt$applyRecipeData(EmiRecipeCategory category, RecipeHolder<? extends Recipe<?>> holder,
                                    CallbackInfo ci) {
        Recipe<?> recipe = holder.value();
        if (recipe instanceof IRecipeProcessingInfo info) {
            int count = info.getIngredientCount();
            if (count > 1) {
                for (int i = 0; i < this.inputs.size(); i++) {
                    this.inputs.set(i, this.inputs.get(i).setAmount(count));
                }
            }
        }
        gt$appendExtraOutputs(recipe);
    }

    @Unique
    private void gt$appendExtraOutputs(Recipe<?> recipe) {
        if (recipe instanceof mio_icif_WasherRecipe washer) {
            gt$useAllResults(washer.getAllResults());
        } else if (recipe instanceof mio_icif_CentrifugeRecipe centrifuge) {
            gt$useAllResults(centrifuge.getAllResults());
        } else if (recipe instanceof mio_icif_BlastFurnaceRecipe blastFurnace) {
            ItemStack secondary = blastFurnace.getSecondaryResult();
            if (!secondary.isEmpty()) {
                this.outputs.add(EmiStack.of(secondary));
            }
        }
    }

    /** 用配方自带的完整产物列表（已按各自数量设置 count）替换原来的单个主产物。 */
    @Unique
    private void gt$useAllResults(List<ItemStack> results) {
        if (results.isEmpty()) {
            return;
        }
        this.outputs.clear();
        for (ItemStack stack : results) {
            this.outputs.add(EmiStack.of(stack));
        }
    }

    @Inject(method = "addWidgets", at = @At("TAIL"))
    private void gt$addExtraOutputSlots(WidgetHolder widgets, CallbackInfo ci) {
        for (int i = 1; i < this.outputs.size(); i++) {
            widgets.addSlot(this.outputs.get(i), gt$OUTPUT_X, gt$PRIMARY_Y + gt$SLOT_STEP * i);
        }
    }

    @Inject(method = "getDisplayHeight", at = @At("HEAD"), cancellable = true)
    private void gt$growForExtraOutputs(CallbackInfoReturnable<Integer> cir) {
        int rows = this.outputs.size() - 1;
        if (rows > 1) {
            cir.setReturnValue(gt$PRIMARY_Y + 20 + gt$SLOT_STEP * rows);
        }
    }
}
