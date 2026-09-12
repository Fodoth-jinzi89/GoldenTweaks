package net.fodoth.skina.goldentweaks.util.tmrv;

/**
 * 由 TMRV 的 {@code dev.nolij.toomanyrecipeviewers.impl.widget.ScrollBarWidget}
 * mixin 实现的鸭子接口：让滚轮可以把行数增量交给它自己的滚动逻辑
 * （内部会 clamp、同步拖动条位置并回调 {@code ScrollGridWidget#updateGrid}）。
 * <p>
 * 滑条自己不知道所属网格，所以由 {@code ScrollGridWidget} 在构造完成后回填，
 * 这样滚轮处理只需要遍历当前页真正的 widget，不会命中其它页残留的网格。
 */
public interface GtTmrvScrollBar {

    /**
     * @param delta 行增量，正数向列表末尾滚动
     * @return 是否真的发生了滚动
     */
    boolean goldentweaks$scrollRows(int delta);

    void goldentweaks$setScrollGrid(GtTmrvScrollGrid grid);

    GtTmrvScrollGrid goldentweaks$getScrollGrid();
}
