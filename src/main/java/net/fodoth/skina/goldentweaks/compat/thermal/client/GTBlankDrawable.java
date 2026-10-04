package net.fodoth.skina.goldentweaks.compat.thermal.client;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 什么都不画的 {@link IDrawableStatic}，仅保留原始尺寸。
 * <p>
 * Thermal 的 JEI 分类把机器 GUI 贴图当作配方背景绘制，贴图里同时硬画了淡灰色面板与物品格子；
 * 本类用于把这些绘制替换成空实现，让 JEI 默认（可被资源包覆盖）的格子渲染接手。
 */
public final class GTBlankDrawable implements IDrawableStatic {

    private final int width;
    private final int height;

    public GTBlankDrawable(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int width, int height, int textureWidth, int textureHeight) {
    }
}
