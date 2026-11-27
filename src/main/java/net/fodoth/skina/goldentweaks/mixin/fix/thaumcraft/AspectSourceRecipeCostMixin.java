package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.nolij.toomanyrecipeviewers.impl.recipe.TMRVRecipe;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 「要素来源（物品含量）」页在 EMI 下悬停时，不要为整条来源配方生成「每批材料」工具提示。
 *
 * <p>EMI 的 {@code SlotWidget#getTooltip} 在 {@code showCostPerBatch} 开启且
 * {@code EmiRecipe#supportsRecipeTree()} 为真时，会现场 new 一个
 * {@code RecipeCostTooltipComponent}：它会构建整棵 {@code MaterialTree}、调用
 * {@code calculateCost()}，再把每种材料按物品图标绘制进工具提示。</p>
 *
 * <p>来源页把「含有该要素的全部物品」作为数千个输入槽，输出槽只有一个要素，因此 TMRV 提取出的
 * {@code supportsRecipeTree} 恒为真。结果就是鼠标每移到页面上（哪怕只是要素图标）都会遍历并
 * 绘制数千个物品，且每帧重新构建一次材料树；而「每批材料」对来源页本身也没有意义
 * （这些物品是并列来源，不是配方成本）。</p>
 *
 * <p>不能重定向 {@code SlotWidget#getTooltip} 里的调用：生产环境里那处调用点会被其它 mixin
 * 改写（{@code Scanned 0 target(s)}），重定向失败会拖垮整次 EMI 重载。这里直接改
 * {@code TMRVRecipe#supportsRecipeTree()} 的返回值。</p>
 *
 * <p><b>迁移说明：</b>这段逻辑原先针对独立模组 {@code scex_aspect_jei}，其分类 id 为
 * {@code scex_aspect_jei:sources}。该模组已并入神秘时代本体，页面改由
 * {@code thaumcraft.integration.jei.AspectSourceRecipeCategory} 提供，UID 变成
 * {@code thaumcraft:aspect_sources}。判定因此改为匹配新 id，防止卡顿的作用保持不变。</p>
 */
@Mixin(value = TMRVRecipe.class, remap = false)
public abstract class AspectSourceRecipeCostMixin {

    /** 内置后的来源页分类 id：{@code ThaumcraftJeiPlugin} 的 {@code ASPECT_SOURCES} 常量。 */
    @Unique
    private static final ResourceLocation GT_ASPECT_SOURCES =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "aspect_sources");

    /** 旧独立模组的 id，仅作兜底：万一整合包里仍装着旧 jar，也不该重新出现卡顿。 */
    @Unique
    private static final ResourceLocation GT_LEGACY_ASPECT_SOURCES =
            ResourceLocation.fromNamespaceAndPath("scex_aspect_jei", "sources");

    @Unique
    private static final String TMRV = "toomanyrecipeviewers";

    @Unique
    private static final String SOURCE_PATH_PREFIX = "source/";

    @Unique
    private static boolean gt$logged;

    @Shadow
    public ResourceLocation id;

    @Inject(method = "supportsRecipeTree", at = @At("HEAD"), cancellable = true)
    private void gt$skipAspectSourceCost(CallbackInfoReturnable<Boolean> cir) {
        if (!gt$isAspectSource()) {
            return;
        }
        if (!gt$logged) {
            gt$logged = true;
            GoldenTweaks.LOGGER.info(
                    "[GT] aspect-source recipe: disabled EMI cost-per-batch tooltip"
                            + " (prevents building/rendering thousands of source items on hover)");
        }
        cir.setReturnValue(false);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private boolean gt$isAspectSource() {
        EmiRecipeCategory category = ((TMRVRecipe<Object>) (Object) this).getCategory();
        if (category != null) {
            ResourceLocation categoryId = category.getId();
            if (GT_ASPECT_SOURCES.equals(categoryId) || GT_LEGACY_ASPECT_SOURCES.equals(categoryId)) {
                return true;
            }
        }
        return gt$matchesWrappedId(this.id);
    }

    /**
     * 兜底：TMRV 的 {@code RecipeManager.Category.Recipe#getID()} 会把原 id 包成
     * {@code toomanyrecipeviewers:/<namespace>/<path>}（注意 getID 的拼接模板是 {@code "/\u0001"}，
     * 路径前面会多一个斜杠），所以这里不做前缀匹配，直接找标记片段。
     */
    @Unique
    private static boolean gt$matchesWrappedId(ResourceLocation recipeId) {
        if (recipeId == null) {
            return false;
        }
        if ("thaumcraft".equals(recipeId.getNamespace())
                && recipeId.getPath().startsWith(SOURCE_PATH_PREFIX)) {
            return true;
        }
        if ("scex_aspect_jei".equals(recipeId.getNamespace())
                && recipeId.getPath().startsWith(SOURCE_PATH_PREFIX)) {
            return true;
        }
        String wrappedPath = recipeId.getPath();
        return TMRV.equals(recipeId.getNamespace())
                && (wrappedPath.contains("thaumcraft/source/")
                        || wrappedPath.contains("scex_aspect_jei/source/"));
    }
}
