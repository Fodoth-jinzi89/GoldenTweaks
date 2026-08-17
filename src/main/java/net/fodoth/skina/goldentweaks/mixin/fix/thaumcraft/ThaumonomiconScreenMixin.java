package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.gui.AspectGuiRenderer;
import thaumcraft.client.gui.ThaumonomiconData;
import thaumcraft.client.gui.ThaumonomiconScreen;

import java.util.List;

/**
 * 研究标签页翻页（移植自 TC4Tweaks 的 BrowserPaging）。
 *
 * <p>移植版 {@code drawCategoryTabs} 固定每列画 9 个标签（左右两列共 18 个），
 * 超过 18 个分类时会重叠。这里按每页 18 个标签分页：{@code drawCategoryTabs}
 * 只绘制当前页的分类子列表，并在标签列下方绘制 {@code « / »} 翻页箭头；
 * 点击处理挂到 {@code handleMapCategoryClick}（与标签点击同一坐标体系）。
 * 标签数不超过 18 时完全不生效。</p>
 */
@Mixin(value = ThaumonomiconScreen.class, remap = false)
public abstract class ThaumonomiconScreenMixin {

    @Unique
    private static final int TABS_PER_PAGE = 18;
    @Unique
    private static final int ASPECT_SIZE = 18;
    @Unique
    private static final int ASPECT_ROW_GAP = 19;
    @Unique
    private static final int INFUSION_ASPECTS_PER_ROW = 7;
    @Unique
    private static final int CRUCIBLE_ASPECTS_PER_PAGE = 8;
    @Unique
    private static final long ASPECT_CAROUSEL_INTERVAL_MS = 2000;

    /** 左/右翻页箭头的位置（与标签列同一坐标体系，标签列底部 y≈216）。 */
    @Unique
    private static final int PREV_X = -20;
    @Unique
    private static final int NEXT_X = 262;
    @Unique
    private static final int BUTTON_Y = 220;

    @Unique
    private static int gt$tabPage;
    @Unique
    private static long gt$infusionCarouselStart = Util.getMillis();
    @Unique
    private static int gt$infusionCarouselRow;
    @Unique
    private static long gt$crucibleCarouselStart = Util.getMillis();
    @Unique
    private static int gt$crucibleCarouselPage;

    @Redirect(
            method = "drawInfusionRecipePage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V",
                    ordinal = 0
            )
    )
    private void gt$moveInfusionHaloLeft(GuiGraphics graphics, ResourceLocation texture, int x, int y,
                                          float u, float v, int width, int height, int textureWidth, int textureHeight) {
        graphics.pose().pushPose();
        graphics.pose().translate(-2.0F, 0.0F, 0.0F);
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        graphics.pose().popPose();
    }

    @Redirect(
            method = "drawInfusionRecipePage",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/client/gui/ThaumonomiconScreen;drawAspectCost(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/AspectList;III)V"
            )
    )
    private void gt$drawInfusionAspectsLikeEmi(ThaumonomiconScreen screen, GuiGraphics graphics,
                                                AspectList aspects, int x, int y, int width) {
        List<Aspect> list = aspects.sortedByTag();
        if (list.isEmpty()) {
            return;
        }
        int rows = (list.size() + INFUSION_ASPECTS_PER_ROW - 1) / INFUSION_ASPECTS_PER_ROW;
        int startRow = gt$infusionCarouselStart(rows);
        int shownRows = Math.min(rows, 2);
        int step = gt$infusionWindowStep(list, startRow, shownRows, width);
        for (int i = 0; i < shownRows; i++) {
            int start = (startRow + i) * INFUSION_ASPECTS_PER_ROW;
            int end = Math.min(start + INFUSION_ASPECTS_PER_ROW, list.size());
            int rowY = y - (shownRows - 1 - i) * ASPECT_ROW_GAP;
            gt$drawAspectRow(graphics, aspects, list.subList(start, end), x, rowY, width, step);
        }
    }

    @Redirect(
            method = "drawCrucibleRecipePage",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/client/gui/ThaumonomiconScreen;drawCrucibleAspectCost(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/AspectList;II)V"
            )
    )
    private void gt$drawCrucibleAspectsLikeEmi(ThaumonomiconScreen screen, GuiGraphics graphics,
                                                AspectList aspects, int x, int y) {
        List<Aspect> list = aspects.sortedByTag();
        if (list.isEmpty()) {
            return;
        }
        int pages = (list.size() + CRUCIBLE_ASPECTS_PER_PAGE - 1) / CRUCIBLE_ASPECTS_PER_PAGE;
        int page = gt$crucibleCarouselPage(pages);
        List<Aspect> visible = list.subList(page * CRUCIBLE_ASPECTS_PER_PAGE,
                Math.min((page + 1) * CRUCIBLE_ASPECTS_PER_PAGE, list.size()));
        int rows = visible.size() <= 3 ? 1 : visible.size() <= 6 ? 2 : 3;
        int rowStart = 0;
        for (int row = 0; row < rows; row++) {
            int rowEnd = Math.min(rowStart + (row < 2 ? 3 : 2), visible.size());
            int rowY = y + 128 + (row * 2 - (rows - 1)) * ASPECT_ROW_GAP / 2 - ASPECT_SIZE / 2;
            gt$drawAspectRow(graphics, aspects, visible.subList(rowStart, rowEnd), x + 16, rowY, 80, 24);
            rowStart = rowEnd;
        }
    }

    @Redirect(
            method = "drawCategoryTabs",
            at = @At(value = "INVOKE", target = "Lthaumcraft/client/gui/ThaumonomiconData;categories()Ljava/util/List;")
    )
    private static List<ThaumonomiconData.Category> gt$tabsForCurrentPage() {
        List<ThaumonomiconData.Category> all = ThaumonomiconData.categories();
        int pages = gt$pageCount(all.size());
        if (pages <= 1) {
            return all;
        }

        gt$tabPage = Mth.clamp(gt$tabPage, 0, pages - 1);
        int start = gt$tabPage * TABS_PER_PAGE;
        return all.subList(start, Math.min(start + TABS_PER_PAGE, all.size()));
    }

    @Inject(method = "drawCategoryTabs", at = @At("RETURN"))
    private void gt$drawTabPaging(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (gt$pageCount() <= 1) {
            return;
        }

        guiGraphics.drawString(Minecraft.getInstance().font, "«", PREV_X, BUTTON_Y, 0xFF404040);
        guiGraphics.drawString(Minecraft.getInstance().font, "»", NEXT_X, BUTTON_Y, 0xFF404040);
    }

    @Redirect(
            method = "drawLargeAspectTag",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I")
    )
    private int gt$drawAspectAmountAboveIcon(GuiGraphics graphics, Font font, String amount,
                                              int x, int y, int color, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        int width = graphics.drawString(font, amount, x, y, color, true);
        graphics.pose().popPose();
        return width;
    }

    @Unique
    private static int gt$infusionCarouselStart(int rows) {
        if (rows < 3) {
            return 0;
        }
        long now = Util.getMillis();
        if (now - gt$infusionCarouselStart >= ASPECT_CAROUSEL_INTERVAL_MS) {
            gt$infusionCarouselStart = now;
            gt$infusionCarouselRow = (gt$infusionCarouselRow + 1) % (rows - 1);
        }
        return gt$infusionCarouselRow;
    }

    @Unique
    private static int gt$crucibleCarouselPage(int pages) {
        if (pages < 2) {
            return 0;
        }
        long now = Util.getMillis();
        if (now - gt$crucibleCarouselStart >= ASPECT_CAROUSEL_INTERVAL_MS) {
            gt$crucibleCarouselStart = now;
            gt$crucibleCarouselPage = (gt$crucibleCarouselPage + 1) % pages;
        }
        return gt$crucibleCarouselPage;
    }

    @Unique
    private static int gt$infusionWindowStep(List<Aspect> list, int startRow, int shownRows, int width) {
        int step = Integer.MAX_VALUE;
        for (int i = 0; i < shownRows; i++) {
            int start = (startRow + i) * INFUSION_ASPECTS_PER_ROW;
            int count = Math.min(start + INFUSION_ASPECTS_PER_ROW, list.size()) - start;
            if (count > 1) {
                step = Math.min(step, gt$aspectStep(count, width));
            }
        }
        return step == Integer.MAX_VALUE ? 0 : step;
    }

    @Unique
    private static void gt$drawAspectRow(GuiGraphics graphics, AspectList aspects, List<Aspect> row,
                                          int x, int y, int width, int step) {
        if (row.isEmpty()) {
            return;
        }
        int totalWidth = ASPECT_SIZE + step * (row.size() - 1);
        int startX = x + Math.max(0, (width - totalWidth) / 2);
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < row.size(); i++) {
            Aspect aspect = row.get(i);
            int iconX = startX + i * step;
            AspectGuiRenderer.draw(graphics, aspect, iconX, y, ASPECT_SIZE, 1.0F);
            AspectGuiRenderer.drawCount(graphics, font, aspects.amount(aspect), iconX, y, -1);
        }
    }

    @Unique
    private static int gt$aspectStep(int count, int width) {
        return count <= 1 ? 0 : Math.max(13, Math.min(24, (width - ASPECT_SIZE) / (count - 1)));
    }

    @Inject(method = "handleMapCategoryClick", at = @At("HEAD"), cancellable = true)
    private void gt$handleTabPaging(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (gt$pageCount() <= 1) {
            return;
        }

        if (mouseY < BUTTON_Y - 4 || mouseY > BUTTON_Y + 12) {
            return;
        }

        if (mouseX >= PREV_X - 4 && mouseX <= PREV_X + 10) {
            gt$tabPage = Math.max(0, gt$tabPage - 1);
            cir.setReturnValue(true);
        } else if (mouseX >= NEXT_X - 4 && mouseX <= NEXT_X + 10) {
            gt$tabPage = Math.min(gt$pageCount() - 1, gt$tabPage + 1);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static int gt$pageCount() {
        return gt$pageCount(ThaumonomiconData.categories().size());
    }

    @Unique
    private static int gt$pageCount(int total) {
        return (total + TABS_PER_PAGE - 1) / TABS_PER_PAGE;
    }
}
