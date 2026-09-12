package net.fodoth.skina.goldentweaks.util.emi;

import dev.emi.emi.jemi.impl.extras.JemiScrollGridWidget;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class ScexAspectJeiScrollHandler {
    private static final Set<JemiScrollGridWidget> GRIDS =
            Collections.newSetFromMap(new WeakHashMap<>());

    private ScexAspectJeiScrollHandler() {
    }

    public static void register(JemiScrollGridWidget grid) {
        GRIDS.add(grid);
    }

    public static boolean scroll(double mouseX, double mouseY, double amount) {
        for (JemiScrollGridWidget grid : GRIDS) {
            if (mouseX >= grid.x && mouseX < grid.x + grid.width
                    && mouseY >= grid.y && mouseY < grid.y + grid.height) {
                int rows = Math.max(0, (grid.slots.size() + grid.gridWidth - 1) / grid.gridWidth - grid.gridHeight);
                int delta = amount < 0 ? 1 : -1;
                ScexAspectJeiScrollAccess access = (ScexAspectJeiScrollAccess) grid;
                int offset = Math.clamp(access.goldentweaks$getOffset() + delta, 0, rows);
                access.goldentweaks$setOffset(offset);
                for (int i = 0; i < grid.slots.size(); i++) {
                    IRecipeSlotDrawable slot = grid.slots.get(i);
                    slot.setPosition(grid.x + (i % grid.gridWidth) * 18 + 1,
                            grid.y + (i / grid.gridWidth - offset) * 18 + 1);
                }
                return true;
            }
        }
        return false;
    }
}
