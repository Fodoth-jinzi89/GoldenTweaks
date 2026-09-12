package net.fodoth.skina.goldentweaks.mixin.fix.tmrv;

import dev.emi.emi.api.widget.WidgetHolder;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.widgets.ScrollGridWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.ScrollBarWidget;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.common.util.ImmutableRect2i;
import net.fodoth.skina.goldentweaks.util.tmrv.GtTmrvScrollBar;
import net.fodoth.skina.goldentweaks.util.tmrv.GtTmrvScrollGrid;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 暴露 TMRV 滚动网格的矩形，并把网格回填给同一个 EMI widget group 里的
 * {@code ScrollBarWidget}，这样 {@code TmrvRecipeScreenScrollMixin} 只要遍历当前页实际的
 * widget 就能判断鼠标是否在物品列表/滑条上，不会误命中其它页残留的网格。
 */
@Mixin(value = ScrollGridWidget.class, remap = false)
public abstract class TmrvScrollGridWidgetMixin implements GtTmrvScrollGrid {

    @Final
    @Shadow
    @Nullable
    private ScrollBarWidget scrollBar;

    @Shadow
    private ImmutableRect2i rect;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void goldentweaks$attachScrollGrid(WidgetHolder widgets, List<IRecipeSlotDrawable> slots,
                                               int columns, int visibleRows, CallbackInfo cir) {
        if (this.scrollBar != null) {
            ((GtTmrvScrollBar) this.scrollBar).goldentweaks$setScrollGrid(this);
        }
    }

    @Override
    public boolean goldentweaks$contains(double x, double y) {
        return this.rect != null && this.rect.contains(x, y);
    }
}
