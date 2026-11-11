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
    private static final int CRUCIBLE_STEP = 24;

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

    /** 坩埚：每页 8 个，3 / 3 / 2 分三行，带轮播。 */
    public static List<RecipePageLayout.AspectSlot> crucible(AspectList aspects, int pageX, int pageY) {
        if (aspects == null || aspects.isEmpty()) {
            return List.of();
        }
        List<Aspect> order = aspects.sortedByTag();
        List<Aspect> window = AspectCarousel.window("crucible:" + AspectCarousel.keyOf(order), order, CRUCIBLE_PER_PAGE);
        int rows = window.size() <= 3 ? 1 : window.size() <= 6 ? 2 : 3;
        List<RecipePageLayout.AspectSlot> slots = new ArrayList<>(window.size());
        int index = 0;
        for (int row = 0; row < rows; row++) {
            int countInRow = Math.min(row < 2 ? 3 : 2, window.size() - index);
            int rowY = pageY + 128 + (row * 2 - (rows - 1)) * ASPECT_ROW_GAP / 2 - ASPECT_SIZE / 2;
            for (int column = 0; column < countInRow; column++) {
                Aspect aspect = window.get(index);
                slots.add(new RecipePageLayout.AspectSlot(pageX + 16 + column * CRUCIBLE_STEP, rowY,
                        aspect, aspects.amount(aspect)));
                index++;
            }
        }
        return slots;
    }

    private static int aspectStep(int count, int width) {
        return count <= 1 ? 0 : Math.clamp((width - ASPECT_SIZE) / (count - 1), 13, 24);
    }
}
