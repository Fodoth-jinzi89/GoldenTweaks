package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * 魔导手册左侧/右侧分类标签的分页状态。
 *
 * <p>Thaumcraft 0.3.0 的 {@code ThaumonomiconLayout} 仍然只按索引摆 18 个标签
 * （每侧 9 个，{@code categoryTabX/Y} 只由索引决定），分类超过 18 个时多出来的标签会画到
 * 书本外面。这里把当前页的分类切片喂给 {@code ResearchMapView.drawCategoryTabs}，并在两侧
 * 标签列下面画 {@code « / »} 翻页箭头；点击由 {@code ThaumonomiconScreenMixin} 拦截
 * {@code handleMapCategoryClick} 处理（那里的坐标已经是标签坐标系的局部坐标）。</p>
 */
public final class ThaumonomiconTabPaging {

    public static final int TABS_PER_PAGE = 18;
    public static final int TAB_SIZE = 24;
    public static final int LEFT_X = -24;
    public static final int RIGHT_X = 256;
    public static final int ARROW_Y = 216;

    private static int page;
    private static int pages = 1;

    private ThaumonomiconTabPaging() {
    }

    /** 取当前页的分类；分类不足一页时原样返回。 */
    public static <T> List<T> slice(List<T> all) {
        int count = Math.max(1, (all.size() + TABS_PER_PAGE - 1) / TABS_PER_PAGE);
        pages = count;
        page = Mth.clamp(page, 0, count - 1);
        if (count <= 1) {
            return all;
        }
        int start = page * TABS_PER_PAGE;
        return all.subList(start, Math.min(start + TABS_PER_PAGE, all.size()));
    }

    public static boolean hasPages() {
        return pages > 1;
    }

    public static int page() {
        return page;
    }

    public static void setPage(int value) {
        page = Mth.clamp(value, 0, Math.max(0, pages - 1));
    }

    public static boolean insidePrevious(double mouseX, double mouseY) {
        return mouseX >= LEFT_X && mouseX < LEFT_X + TAB_SIZE && mouseY >= ARROW_Y && mouseY < ARROW_Y + TAB_SIZE;
    }

    public static boolean insideNext(double mouseX, double mouseY) {
        return mouseX >= RIGHT_X && mouseX < RIGHT_X + TAB_SIZE && mouseY >= ARROW_Y && mouseY < ARROW_Y + TAB_SIZE;
    }

    /** 在标签坐标系里画翻页箭头（调用点已处于地图的平移/缩放坐标中）。 */
    public static void drawArrows(GuiGraphics graphics) {
        if (pages <= 1) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        graphics.drawString(font, "«", LEFT_X + 6, ARROW_Y + 7, 0xFF606060, false);
        graphics.drawString(font, "»", RIGHT_X + 7, ARROW_Y + 7, 0xFF606060, false);
        graphics.pose().popPose();
    }
}
