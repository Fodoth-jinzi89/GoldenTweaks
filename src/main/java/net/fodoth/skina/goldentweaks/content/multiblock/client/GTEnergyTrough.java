package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * The horizontal energy trough: a bar lit in fixed steps rather than filled smoothly.
 *
 * <p>Laid out from the art rather than computed from the width: a wide first cell, then a run of
 * narrow ones, so the fill is a row of lit cells. Both the machine window and the port windows cut
 * the same shape from their own sheets, which is why the cell arithmetic lives here and only the
 * coordinates and the sheet differ per window.</p>
 *
 * <p>Each cell is one twentieth, so the count is the stored fraction times twenty rounded down:
 * seven percent lights one cell and ninety-nine percent lights nineteen. Drawing the run as a single
 * blit of the right width is what keeps the machine's bar and a port's bar in step instead of one
 * filling smoothly while the other jumps.</p>
 */
public final class GTEnergyTrough {

    /** Cells the bar is divided into. */
    public static final int CELLS = 20;
    /** Width of the first cell. */
    public static final int FIRST_CELL_WIDTH = 4;
    /** Advance per cell after the first. */
    public static final int CELL_STEP = 3;
    /** Height of the sprite, which is also the bar's. */
    public static final int HEIGHT = 11;

    private GTEnergyTrough() {
    }

    /** How many cells a stored/capacity pair lights. */
    public static int litCells(long stored, long capacity) {
        if (capacity <= 0L || stored <= 0L) {
            return 0;
        }
        return (int) Math.min(CELLS, stored * CELLS / capacity);
    }

    /**
     * Draws the lit part of the bar.
     *
     * <p>Nothing is drawn at zero: the sheet already carries the trough's empty frame, and a
     * zero-width blit would be a no-op anyway.</p>
     */
    public static void draw(GuiGraphics graphics, ResourceLocation sheet,
                            int x, int y, int spriteU, int spriteV,
                            long stored, long capacity) {
        int cells = litCells(stored, capacity);
        if (cells <= 0) {
            return;
        }
        int width = FIRST_CELL_WIDTH + (cells - 1) * CELL_STEP;
        graphics.blit(sheet, x, y, spriteU, spriteV, width, HEIGHT);
    }
}
