package net.fodoth.skina.goldentweaks.mixin.fix.scexaspect;

import dev.scex.aspectjei.SourceCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 「要素来源（物品含量）」页（`scex_aspect_jei:sources`）的可见行数补喂。
 *
 * <p>这一页的布局是：{@code getWidth() = 160}、{@code getHeight() = 52 + visibleRows * 18}，
 * 物品格由 {@code createRecipeExtras} 里的
 * {@code IRecipeExtrasBuilder#addScrollGridWidget(slots, 7, visibleRows)} 交给 TMRV 的
 * {@code ScrollGridWidget}（滚动条就在它右侧）。</p>
 *
 * <p>rc7 起，可见行数改由它自带的
 * {@code dev/scex/aspectjei/mixin/SourceLayoutResizeMixin} 在每次布局时通过
 * {@code SourceCategory#fitAvailableHeight(可用高度)} 重算 —— 那个 mixin 是
 * {@code @Pseudo @Mixin(targets = "mezz.jei.gui.recipes.RecipeGuiLogic")}，
 * 只挂在<b>真 JEI</b> 的配方界面逻辑上。</p>
 *
 * <p>我们这套是 EMI + TMRV：真 JEI 的 GUI 类不存在，那个 mixin 直接不应用，
 * {@code fitAvailableHeight} 永远没人调用，页面就一直停在构造器给的 4 行
 * （52 + 4×18 = 124 高），在 EMI 的配方区里既填不满、也不会随窗口变化。</p>
 *
 * <p>这里把「喂可用高度」这一步补上：在 EMI 取页面高度 / 排布之前，先按 EMI 配方页的标准高度
 * 算一次可见行数。取 168 是为了得到 6 行（52 + 6×18 = 160，正好是 EMI 的标准页高），
 * 超出的来源继续由 TMRV 的滚动网格翻页（GT 另修过它的滚轮与滑条绘制）。</p>
 */
@Mixin(targets = "dev.scex.aspectjei.SourceCategory", remap = false)
public abstract class SourceCategoryFitMixin {

    /** EMI 配方页的可用高度：对应 6 行（52 + 6×18 = 160）。 */
    @Unique
    private static final int GT_AVAILABLE_HEIGHT = 168;

    @Inject(method = "getHeight", at = @At("HEAD"))
    private void gt$fitBeforeHeight(CallbackInfoReturnable<Integer> cir) {
        gt$fitRows();
    }

    @Inject(method = "setRecipe", at = @At("HEAD"))
    private void gt$fitBeforeRecipe(CallbackInfo ci) {
        gt$fitRows();
    }

    @Inject(method = "createRecipeExtras", at = @At("HEAD"))
    private void gt$fitBeforeExtras(CallbackInfo ci) {
        gt$fitRows();
    }

    @Unique
    private void gt$fitRows() {
        try {
            ((SourceCategory) (Object) this).fitAvailableHeight(GT_AVAILABLE_HEIGHT);
        } catch (Throwable ignored) {
            // 拿不到目标方法时保持构造器给的默认行数，不影响页面本身
        }
    }
}
