package net.fodoth.skina.goldentweaks.util.tmrv;

/**
 * 由 TMRV 的 {@code dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.widgets.ScrollGridWidget}
 * mixin 实现的鸭子接口：暴露滚动网格的矩形（坐标相对配方 widget group，与
 * {@code ScrollBarWidget} 落在同一个 EMI widget group 里）。
 */
public interface GtTmrvScrollGrid {

    /**
     * @param x 相对配方 widget group 的 X
     * @param y 相对配方 widget group 的 Y
     * @return 该点是否落在网格（含网格右侧的滑条列）内
     */
    boolean goldentweaks$contains(double x, double y);
}
