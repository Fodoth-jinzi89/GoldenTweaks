package net.fodoth.skina.goldentweaks.mixin.fix.tmrv;

import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.WidgetGroup;
import net.fodoth.skina.goldentweaks.util.tmrv.GtTmrvScrollBar;
import net.fodoth.skina.goldentweaks.util.tmrv.GtTmrvScrollGrid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * EMI 的配方界面在 {@code mouseScrolled} 里只做「翻页 / 换标签」，由 JEI 插件经 TMRV
 * 转换过来的滚动网格（例如 scex-thaumcraft-aspects-jei 的「要素来源」物品列表）拿不到滚轮，
 * 于是鼠标悬停在物品列表或滑条上滚轮会直接换 tab。
 * <p>
 * 这里在 EMI 处理之前，遍历当前页每个配方 widget group 里的滑条 widget，按
 * 「group 局部坐标」判断鼠标是否落在它所属网格（含网格右侧滑条列）内；命中时滚动网格并
 * 取消 EMI 自己的处理。
 * <p>
 * 上一版直接拿 {@code JemiScrollGridWidget} 的 {@code x/y} 与屏幕坐标比较，但 TMRV 会用
 * mixin 强制 {@code isModLoaded("jei") == false}，EMI 根本不会加载 JEMI，那个 widget
 * 不会参与渲染；而且它的坐标也是配方局部坐标，所以从未生效。
 */
@Mixin(value = RecipeScreen.class, remap = false)
public abstract class TmrvRecipeScreenScrollMixin {

    @Shadow
    private List<WidgetGroup> currentPage;

    /** 平滑滚动会一次事件拆成多份，累计满一行再滚。 */
    @Unique
    private static double gt$scrollAccumulator;

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$scrollTmrvGrid(double mouseX, double mouseY, double horizontalAmount,
                                             double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (verticalAmount == 0.0 || this.currentPage == null) {
            return;
        }

        for (WidgetGroup group : this.currentPage) {
            double localX = mouseX - group.x();
            double localY = mouseY - group.y();

            for (Widget widget : group.widgets) {
                if (!(widget instanceof GtTmrvScrollBar bar)) {
                    continue;
                }

                GtTmrvScrollGrid grid = bar.goldentweaks$getScrollGrid();
                if (grid == null || !grid.goldentweaks$contains(localX, localY)) {
                    continue;
                }

                gt$scrollAccumulator += verticalAmount;
                int rows = (int) gt$scrollAccumulator;
                gt$scrollAccumulator -= rows;
                if (rows != 0) {
                    bar.goldentweaks$scrollRows(-rows);
                }

                // 命中网格就消费掉滚轮，即使不足一行，否则 EMI 会当成翻页 / 换标签
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
