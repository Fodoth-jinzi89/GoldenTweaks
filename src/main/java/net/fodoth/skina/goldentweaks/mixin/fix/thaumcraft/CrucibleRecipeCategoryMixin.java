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
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.gui.AspectGuiRenderer;

import java.util.List;

/**
 * 「坩埚炼金」JEI/EMI 页面要素排布：
 * 1) 原来那一行上移 30 像素，以其中心作为布局的中间位置；
 * 2) 每页最多 3 行：第 1/2 行最多 3 个要素，第 3 行最多 2 个要素（每页 8 个）；
 * 3) 超过一页容量则轮播，每 {@link #CAROUSEL_INTERVAL_MS} 毫秒切换一页；
 * 4) 无论几行，整块水平、垂直居中于中间位置。
 * JEI 与 EMI 共用 Thaumcraft 的 JEI 配方分类渲染，改这里两边同时生效。
 */
@Mixin(targets = "thaumcraft.integration.jei.CrucibleRecipeCategory", remap = false)
public abstract class CrucibleRecipeCategoryMixin {

    private static final int ASPECT_SIZE = 18;
    /** 行间距：图标高 18 + 1。 */
    private static final int ROW_GAP = 19;
    /** 原行上移 30 像素后，中心位置再上移 30 像素。 */
    private static final int ROW_SHIFT_UP = 60;
    private static final int ROW_CAP_1 = 3;
    private static final int ROW_CAP_2 = 3;
    private static final int ROW_CAP_3 = 2;
    private static final int PER_PAGE = ROW_CAP_1 + ROW_CAP_2 + ROW_CAP_3;
    private static final long CAROUSEL_INTERVAL_MS = 2000;

    @Unique
    private static long gt$carouselStart = Util.getMillis();
    @Unique
    private static int gt$carouselWindow;

    @Redirect(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/integration/jei/JeiCategoryDrawing;drawAspects(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/AspectList;III)V"
            )
    )
    private static void gt$drawAspectsCentered(GuiGraphics gui, AspectList aspects, int x, int y, int width) {
        List<Aspect> list = aspects.sortedByTag();
        if (list.isEmpty()) {
            return;
        }
        List<Aspect> page = gt$currentPage(list);
        int rows = gt$rowCount(page.size());
        int step = gt$pageStep(page, rows, width);
        int rowStart = 0;
        for (int i = 0; i < rows; i++) {
            int cap = gt$rowCapacity(i);
            int rowEnd = Math.min(rowStart + cap, page.size());
            int rowY = gt$rowY(i, rows, y);
            gt$drawAspectRow(gui, aspects, page.subList(rowStart, rowEnd), x, rowY, width, step);
            rowStart = rowEnd;
        }
    }

    @Redirect(
            method = "getTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/integration/jei/JeiCategoryDrawing;addAspectTooltip(Lmezz/jei/api/gui/builder/ITooltipBuilder;Lthaumcraft/api/aspects/AspectList;IIIDD)V"
            )
    )
    private static void gt$addAspectTooltipCentered(ITooltipBuilder builder, AspectList aspects, int x, int y, int width,
                                                    double mouseX, double mouseY) {
        List<Aspect> list = aspects.sortedByTag();
        if (list.isEmpty()) {
            return;
        }
        List<Aspect> page = gt$currentPage(list);
        int rows = gt$rowCount(page.size());
        int step = gt$pageStep(page, rows, width);
        int rowStart = 0;
        for (int i = 0; i < rows; i++) {
            int cap = gt$rowCapacity(i);
            int rowEnd = Math.min(rowStart + cap, page.size());
            int rowY = gt$rowY(i, rows, y);
            gt$addAspectRowTooltip(builder, aspects, page.subList(rowStart, rowEnd), x, rowY, width, step, mouseX, mouseY);
            rowStart = rowEnd;
        }
    }

    /** 轮播当前页：总数超过一页容量时定时切换；否则固定第 0 页。 */
    @Unique
    private static List<Aspect> gt$currentPage(List<Aspect> list) {
        int pages = (list.size() + PER_PAGE - 1) / PER_PAGE;
        int page = 0;
        if (pages > 1) {
            long now = Util.getMillis();
            if (now - gt$carouselStart >= CAROUSEL_INTERVAL_MS) {
                gt$carouselStart = now;
                gt$carouselWindow = (gt$carouselWindow + 1) % pages;
            }
            page = gt$carouselWindow;
        }
        int start = page * PER_PAGE;
        return list.subList(start, Math.min(start + PER_PAGE, list.size()));
    }

    /** 行数：1~3 个 1 行；4~6 个 2 行；7~8 个 3 行。 */
    @Unique
    private static int gt$rowCount(int count) {
        if (count <= ROW_CAP_1) {
            return 1;
        }
        return count <= ROW_CAP_1 + ROW_CAP_2 ? 2 : 3;
    }

    /** 每行容量：第 1/2 行 3 个，第 3 行 2 个。 */
    @Unique
    private static int gt$rowCapacity(int row) {
        return switch (row) {
            case 0 -> ROW_CAP_1;
            case 1 -> ROW_CAP_2;
            default -> ROW_CAP_3;
        };
    }

    /** 第 {@code row} 行的行顶 y：整块以原行中心（上移 30 后的行中心）为中间垂直居中。 */
    @Unique
    private static int gt$rowY(int row, int rows, int baseY) {
        int middle = baseY - ROW_SHIFT_UP + ASPECT_SIZE / 2;
        return middle + (row * 2 - (rows - 1)) * ROW_GAP / 2 - ASPECT_SIZE / 2;
    }

    /** 当前页各行共用的图标间距：取各实际显示行中较小的那个（只有 1 个的行不参与）。 */
    @Unique
    private static int gt$pageStep(List<Aspect> page, int rows, int width) {
        int step = Integer.MAX_VALUE;
        int rowStart = 0;
        for (int i = 0; i < rows; i++) {
            int cap = gt$rowCapacity(i);
            int rowEnd = Math.min(rowStart + cap, page.size());
            int count = rowEnd - rowStart;
            if (count > 1) {
                step = Math.min(step, gt$aspectStep(count, width));
            }
            rowStart = rowEnd;
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
