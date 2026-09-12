package net.fodoth.skina.goldentweaks.mixin.fix.tmrv;

import dev.nolij.toomanyrecipeviewers.impl.widget.ScrollBarWidget;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import net.fodoth.skina.goldentweaks.util.tmrv.GtTmrvScrollBar;
import net.fodoth.skina.goldentweaks.util.tmrv.GtTmrvScrollGrid;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TMRV 把 JEI 的滚动网格画成「上/下按钮 + 中间轨道 + 拖动块」，四段全都用 20x20 的
 * <b>按钮</b>贴图（{@code Textures#getButtonForState}）九宫格绘制进 10 像素宽的滑条列：
 * 两边切片之和（6+6）比目标还大，贴图中段被整段丢掉、上下两段被挤在一起重叠，
 * 看起来就是「一个完整的贴图从中间切断、上下拼错位」；深色资源包（Mandala）里
 * {@code button_disabled} 甚至只有中空描边，轨道会直接变成透明。
 * <p>
 * 这里改成用 JEI 专为滑条准备的 {@code scrollbar_background} / {@code scrollbar_marker}
 * 贴图绘制（资源包覆盖的正是这两张），滚轮与点击逻辑保持 TMRV 原样。
 */
@Mixin(value = ScrollBarWidget.class, remap = false)
public abstract class TmrvScrollBarWidgetMixin implements GtTmrvScrollBar {

    @Shadow
    @Final
    private int maxScroll;

    @Shadow
    private int scroll;

    @Shadow
    private ImmutableRect2i rect;

    @Shadow
    private ImmutableRect2i dragRect;

    @Shadow
    private ImmutableRect2i scrollRect;

    @Shadow
    private ImmutableRect2i upRect;

    @Shadow
    private ImmutableRect2i downRect;

    @Unique
    private GtTmrvScrollGrid gt$scrollGrid;

    @Unique
    private int gt$visibleRows;

    @Invoker("scroll")
    protected abstract boolean goldentweaks$invokeScroll(int newScroll);

    @Invoker("canScroll")
    protected abstract boolean goldentweaks$canScroll(int newScroll);

    @Override
    public boolean goldentweaks$scrollRows(int delta) {
        if (delta == 0 || this.maxScroll <= 0) {
            return false;
        }

        return this.goldentweaks$invokeScroll(Mth.clamp(this.scroll + delta, 0, this.maxScroll));
    }

    @Override
    public void goldentweaks$setScrollGrid(GtTmrvScrollGrid grid) {
        this.gt$scrollGrid = grid;
    }

    @Override
    public GtTmrvScrollGrid goldentweaks$getScrollGrid() {
        return this.gt$scrollGrid;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void goldentweaks$rememberVisibleRows(ImmutableRect2i rect, int rows, int visibleRows,
                                                  Runnable onScroll, CallbackInfo cir) {
        this.gt$visibleRows = visibleRows;
    }

    /**
     * 拖动块长度/位置改用原版（JEI {@code AbstractScrollWidget}）算法：长度按可见比例算、
     * 最小 14 像素，位置 =（轨道高 - 拖动块高）× scroll / maxScroll。原算法是
     * {@code 轨道高 / (maxScroll + 1)} 且不设下限，列表一长拖动块就缩成一条线。
     */
    @Inject(method = "updateDragRect", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$updateDragRectVanilla(CallbackInfo cir) {
        int trackHeight = this.scrollRect == null ? 0 : this.scrollRect.height();
        if (this.maxScroll <= 0 || trackHeight <= 0) {
            this.dragRect = ImmutableRect2i.EMPTY;
            cir.cancel();
            return;
        }

        int minHeight = Math.min(14, trackHeight);
        int visible = Math.max(1, this.gt$visibleRows);
        int markerHeight = Mth.clamp(
                Math.round((float) trackHeight * (float) visible / (float) (visible + this.maxScroll)),
                minHeight, trackHeight);
        int markerY = Math.round((float) (trackHeight - markerHeight) * (float) this.scroll / (float) this.maxScroll);

        this.dragRect = new ImmutableRect2i(this.scrollRect.getX(), this.scrollRect.getY() + markerY,
                this.scrollRect.getWidth(), markerHeight);
        cir.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$drawJeiScrollbar(GuiGraphics draw, int mouseX, int mouseY, float delta,
                                               CallbackInfo cir) {
        try {
            Textures textures = Internal.getTextures();
            if (this.rect == null || this.rect.isEmpty()) {
                return;
            }

            textures.getScrollbarBackground()
                    .draw(draw, this.rect.getX(), this.rect.getY(), this.rect.getWidth(), this.rect.getHeight());

            // 上下按钮是 TMRV 原本就有的（点击区域和 hover 状态都在），沿用原来的按钮贴图
            textures.getButtonForState(false, this.goldentweaks$canScroll(this.scroll - 1),
                            this.upRect.contains((double) mouseX, (double) mouseY))
                    .draw(draw, this.upRect.getX(), this.upRect.getY(),
                            this.upRect.getWidth(), this.upRect.getHeight());
            textures.getButtonForState(false, this.goldentweaks$canScroll(this.scroll + 1),
                            this.downRect.contains((double) mouseX, (double) mouseY))
                    .draw(draw, this.downRect.getX(), this.downRect.getY(),
                            this.downRect.getWidth(), this.downRect.getHeight());

            if (this.dragRect != null && !this.dragRect.isEmpty()) {
                textures.getScrollbarMarker()
                        .draw(draw, this.dragRect.getX(), this.dragRect.getY(),
                                this.dragRect.getWidth(), this.dragRect.getHeight());
            }

            cir.cancel();
        } catch (Throwable ignored) {
            // 拿不到滑条贴图时退回 TMRV 原本的按钮绘制
        }
    }
}
