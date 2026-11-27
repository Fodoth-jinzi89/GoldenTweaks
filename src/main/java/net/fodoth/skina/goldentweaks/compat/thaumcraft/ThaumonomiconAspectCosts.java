package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.gui.thaumonomicon.RecipePageLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 要素耗费页的旧版排布（取代 0.3.0 的「一行铺满/简单换行」）。
 *
 * <ul>
 *   <li><b>奥术 / 注魔</b>：每行 7 个、最多 2 行（一页 14 个）；每页按
 *       {@link AspectCarousel} 轮播，行内按可用宽度居中，步距 13~24。</li>
 *   <li><b>坩埚</b>：每页 8 个，按 3 / 3 / 2 分三行（≤3 个一行、≤6 个两行），
 *       每页按 {@link AspectCarousel} 轮播。</li>
 * </ul>
 */
public final class ThaumonomiconAspectCosts {

    private static final int ASPECT_SIZE = 18;
    private static final int ASPECT_ROW_GAP = 19;
    private static final int INFUSION_PER_ROW = 7;
    private static final int INFUSION_ROWS = 2;
    private static final int INFUSION_PER_PAGE = INFUSION_PER_ROW * INFUSION_ROWS;
    private static final int CRUCIBLE_PER_PAGE = 8;
    /**
     * Geometry of the upstream aspect grid, copied value for value from
     * {@code RecipePageLayout.crucibleAspectCost}: three per row, a 20px cell, the block inset 28px
     * right and 128px down from the page origin. These are not derived from anything because the
     * caller only supplies the page origin; if the layout shifts again, re-check them there.
     */
    private static final int CRUCIBLE_COLUMNS = 3;
    private static final int CRUCIBLE_STEP = 20;
    private static final int CRUCIBLE_X_INSET = 28;
    private static final int CRUCIBLE_Y_INSET = 128;

    private ThaumonomiconAspectCosts() {
    }

    /** 奥术 / 注魔：2 行 × 7，整体居中，带轮播。 */
    public static List<RecipePageLayout.AspectSlot> centered(AspectList aspects, int x, int y, int width) {
        if (aspects == null || aspects.isEmpty()) {
            return List.of();
        }
        List<Aspect> order = aspects.sortedByAmountDescending();
        List<Aspect> window = AspectCarousel.window("cost:" + AspectCarousel.keyOf(order), order, INFUSION_PER_PAGE);
        int rows = (window.size() + INFUSION_PER_ROW - 1) / INFUSION_PER_ROW;
        List<RecipePageLayout.AspectSlot> slots = new ArrayList<>(window.size());
        for (int i = 0; i < window.size(); i++) {
            int row = i / INFUSION_PER_ROW;
            int rowStart = row * INFUSION_PER_ROW;
            int countInRow = Math.min(INFUSION_PER_ROW, window.size() - rowStart);
            int step = aspectStep(countInRow, width);
            int totalWidth = ASPECT_SIZE + step * (countInRow - 1);
            int rowX = x + Math.max(0, (width - totalWidth) / 2);
            int rowY = y - (rows - 1 - row) * ASPECT_ROW_GAP;
            Aspect aspect = window.get(i);
            slots.add(new RecipePageLayout.AspectSlot(rowX + (i - rowStart) * step, rowY,
                    aspect, aspects.amount(aspect)));
        }
        return slots;
    }

    /**
     * 坩埚：每页最多 8 个，按 3 个一行换行（最后一页凑不满时行数随之减少），带轮播。
     *
     * <p>坐标不用自己的公式，而是逐项照搬上游 {@code RecipePageLayout.crucibleAspectCost}：
     * {@code rowCount = (size-1)/3}、{@code originX = x+28}、
     * {@code originY = y+128-10*rowCount}、格子 20 像素，末行按 {@code (3-size%3)*10} 右移。
     * 上游那套数字改过，我们用旧公式算出来的位置整体错位，看上去就像布局退回原版。</p>
     *
     * <p>唯一的区别是分页：上游把所有要素一路排下去，要素超过一页就会画出页面；这里每页只取
     * {@link #CRUCIBLE_PER_PAGE} 个，其余按 {@link AspectCarousel} 轮播。</p>
     */
    public static List<RecipePageLayout.AspectSlot> crucible(AspectList aspects, int pageX, int pageY) {
        if (aspects == null || aspects.isEmpty()) {
            return List.of();
        }
        List<Aspect> order = aspects.sortedByTag();
        List<Aspect> window = AspectCarousel.window("crucible:" + AspectCarousel.keyOf(order), order, CRUCIBLE_PER_PAGE);
        int count = window.size();
        List<RecipePageLayout.AspectSlot> slots = new ArrayList<>(count);

        int lastRow = (count - 1) / CRUCIBLE_COLUMNS;
        int nudge = (CRUCIBLE_COLUMNS - count % CRUCIBLE_COLUMNS) * CRUCIBLE_STEP / 2;
        int originX = pageX + CRUCIBLE_X_INSET;
        int originY = pageY + CRUCIBLE_Y_INSET - CRUCIBLE_STEP / 2 * lastRow;

        for (int index = 0; index < count; index++) {
            int row = index / CRUCIBLE_COLUMNS;
            int column = index % CRUCIBLE_COLUMNS;
            // 末行的偏移只在不该与上一行对齐时才加：多行时加在最后一行，单行短配方也加。
            boolean shift = row >= lastRow && (lastRow > 0 || count < CRUCIBLE_COLUMNS);
            int x = originX + column * CRUCIBLE_STEP + (shift ? nudge : 0);
            int y = originY + row * CRUCIBLE_STEP;
            Aspect aspect = window.get(index);
            slots.add(new RecipePageLayout.AspectSlot(x, y, aspect, aspects.amount(aspect)));
        }
        return slots;
    }

    private static int aspectStep(int count, int width) {
        return count <= 1 ? 0 : Math.clamp((width - ASPECT_SIZE) / (count - 1), 13, 24);
    }
}
