package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.gui.AspectGuiRenderer;

import java.util.List;

/**
 * 「注魔」JEI/EMI 页面：
 * 1) 结果物品（输出槽）向右下各移 1 像素；
 * 2) 「不稳定度」文字上移 10 像素；
 * 3) 要素按每行 7 个排布：不超过 7 个单行显示在原来那一行位置；8~14 个两行
 *    （第一行在上方 18 像素，第二行在原来位置）；超过 14 个则轮播，每次显示
 *    两行，定时向下滚动切换。两行共用同一图标间距（取两行中较小的）。
 * JEI 与 EMI 共用 Thaumcraft 的 JEI 配方分类渲染，改这里两边同时生效。
 */
@Mixin(targets = "thaumcraft.integration.jei.InfusionRecipeCategory", remap = false)
public abstract class InfusionRecipeCategoryMixin {

    private static final int ASPECT_SIZE = 18;
    private static final int ASPECTS_PER_ROW = 7;
    /** 行间距：图标高 18 + 第一行再上移 1。 */
    private static final int ROW_GAP = 19;
    private static final long CAROUSEL_INTERVAL_MS = 2000;

    @Unique
    private static long gt$carouselStart = Util.getMillis();
    @Unique
    private static int gt$carouselRow;

    @ModifyArg(
            method = "setRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;addOutputSlot(II)Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;"
            ),
            index = 0
    )
    private int gt$outputRight(int x) {
        return x + 1;
    }

    @ModifyArg(
            method = "setRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;addOutputSlot(II)Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;"
            ),
            index = 1
    )
    private int gt$outputDown(int y) {
        return y + 1;
    }

    @ModifyArg(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            ),
            index = 3
    )
    private int gt$instabilityUp(int y) {
        return y - 10;
    }

    // ---- 要素按行排布 + 轮播 --------------------------------------------------

    @Redirect(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/integration/jei/JeiCategoryDrawing;drawAspects(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/AspectList;III)V"
            )
    )
    private static void gt$drawAspectsCarousel(GuiGraphics gui, AspectList aspects, int x, int y, int width) {
        List<Aspect> list = aspects.sortedByTag();
        if (list.isEmpty()) {
            return;
        }
        int rows = (list.size() + ASPECTS_PER_ROW - 1) / ASPECTS_PER_ROW;
        int startRow = gt$carouselWindowStart(rows);
        int shownRows = Math.min(rows, 2);
        int step = gt$windowStep(list, startRow, shownRows, width);
        for (int i = 0; i < shownRows; i++) {
            int row = startRow + i;
            int start = row * ASPECTS_PER_ROW;
            int end = Math.min(start + ASPECTS_PER_ROW, list.size());
            int rowY = y - (shownRows - 1 - i) * ROW_GAP;
            gt$drawAspectRow(gui, aspects, list.subList(start, end), x, rowY, width, step);
        }
    }

    @Redirect(
            method = "getTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/integration/jei/JeiCategoryDrawing;addAspectTooltip(Lmezz/jei/api/gui/builder/ITooltipBuilder;Lthaumcraft/api/aspects/AspectList;IIIDD)V"
            )
    )
    private static void gt$addAspectTooltipCarousel(ITooltipBuilder builder, AspectList aspects, int x, int y, int width,
                                                    double mouseX, double mouseY) {
        List<Aspect> list = aspects.sortedByTag();
        if (list.isEmpty()) {
            return;
        }
        int rows = (list.size() + ASPECTS_PER_ROW - 1) / ASPECTS_PER_ROW;
        int startRow = gt$carouselWindowStart(rows);
        int shownRows = Math.min(rows, 2);
        int step = gt$windowStep(list, startRow, shownRows, width);
        for (int i = 0; i < shownRows; i++) {
            int row = startRow + i;
            int start = row * ASPECTS_PER_ROW;
            int end = Math.min(start + ASPECTS_PER_ROW, list.size());
            int rowY = y - (shownRows - 1 - i) * ROW_GAP;
            gt$addAspectRowTooltip(builder, aspects, list.subList(start, end), x, rowY, width, step, mouseX, mouseY);
        }
    }

    /**
     * 当前显示窗口的起始行。不超过 2 行时固定显示全部；超过 2 行时每
     * {@link #CAROUSEL_INTERVAL_MS} 毫秒向下滚动一行（窗口始终为 2 行）。
     */
    @Unique
    private static int gt$carouselWindowStart(int rows) {
        if (rows < 3) {
            return 0;
        }
        long now = Util.getMillis();
        if (now - gt$carouselStart >= CAROUSEL_INTERVAL_MS) {
            gt$carouselStart = now;
            gt$carouselRow = (gt$carouselRow + 1) % (rows - 1);
        }
        return gt$carouselRow;
    }

    /** 当前显示窗口中各行共用的图标间距：取各显示行中较小的那个（只有 1 个的行不参与，避免间距被算成 0 导致重叠）。 */
    @Unique
    private static int gt$windowStep(List<Aspect> list, int startRow, int shownRows, int width) {
        int step = Integer.MAX_VALUE;
        for (int i = 0; i < shownRows; i++) {
            int row = startRow + i;
            int start = row * ASPECTS_PER_ROW;
            int end = Math.min(start + ASPECTS_PER_ROW, list.size());
            int count = end - start;
            if (count <= 1) {
                continue;
            }
            step = Math.min(step, gt$aspectStep(count, width));
        }
        return step == Integer.MAX_VALUE ? 0 : step;
    }

    @Unique
    private static void gt$drawAspectRow(GuiGraphics gui, AspectList aspects, List<Aspect> row, int x, int y, int width, int step) {
        if (row.isEmpty()) {
            return;
        }
        int totalWidth = ASPECT_SIZE + step * (row.size() - 1);
        int startX = x + Math.max(0, (width - totalWidth) / 2);
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < row.size(); i++) {
            Aspect aspect = row.get(i);
            int iconX = startX + i * step;
            AspectGuiRenderer.draw(gui, aspect, iconX, y, ASPECT_SIZE, 1.0f);
            AspectGuiRenderer.drawCount(gui, font, aspects.amount(aspect), iconX, y, -1);
        }
    }

    @Unique
    private static void gt$addAspectRowTooltip(ITooltipBuilder builder, AspectList aspects, List<Aspect> row,
                                               int x, int y, int width, int step, double mouseX, double mouseY) {
        if (mouseY < y || mouseY >= y + ASPECT_SIZE) {
            return;
        }
        int totalWidth = ASPECT_SIZE + step * (row.size() - 1);
        int startX = x + Math.max(0, (width - totalWidth) / 2);
        for (int i = 0; i < row.size(); i++) {
            int iconX = startX + i * step;
            if (mouseX < iconX || mouseX >= iconX + ASPECT_SIZE) {
                continue;
            }
            Aspect aspect = row.get(i);
            builder.add(Component.translatable("jei.thaumcraft.aspect_cost", aspect.displayName(), aspects.amount(aspect)));
            return;
        }
    }

    /** 与 {@code JeiCategoryDrawing.aspectStep} 相同：相邻图标间距，13..24 之间。 */
    @Unique
    private static int gt$aspectStep(int count, int width) {
        if (count <= 1) {
            return 0;
        }
        return Math.max(13, Math.min(24, (width - ASPECT_SIZE) / (count - 1)));
    }
}
