package net.fodoth.skina.goldentweaks.content.multiblock.recipe.client;

/**
 * Geometry of the chemical reaction page, shared by the EMI and JEI renderers.
 *
 * <p>Both viewers draw the same page, so the numbers live in one place: a slot that moved in one
 * renderer but not the other would be a silent inconsistency. The layout was transcribed from
 * {@code multiblock/gt_multiblock_recipe.png}: an input grid, a scrollbar to its right, an arrow,
 * an output grid with its own scrollbar, and a block of numbers underneath.</p>
 *
 * <p>Slots sit on an 18 pixel pitch and are 16 pixels wide, so a 3 by 3 block is 54 pixels
 * across — {@code 3 * SLOT}. The grid starts at {@code y = 6} and the arrow is vertically centred
 * on it.</p>
 */
public final class GTChemicalReactionLayout {

    public static final int WIDTH = 180;
    /**
     * Tall enough that two pages fill EMI's recipe column without leaving a gap at the bottom.
     *
     * <p>The grids stay at the top and the figures below them; at 115 the margins come out even —
     * 6 above the grids, 10 between them and the text, 3 under the last of the four lines.</p>
     */
    public static final int HEIGHT = 115;

    public static final int COLUMNS = 3;
    public static final int ROWS = 3;
    /** Slot pitch; the visible slot frame itself is 16 pixels, the extra 2 is the gap. */
    public static final int SLOT = 18;
    public static final int GRID_WIDTH = COLUMNS * SLOT;
    public static final int GRID_HEIGHT = ROWS * SLOT;
    public static final int GRID_Y = 6;

    public static final int INPUT_X = 6;
    public static final int OUTPUT_X = 104;

    /** Scrollbar column, immediately right of each grid. */
    public static final int SCROLLBAR_WIDTH = 10;
    public static final int INPUT_SCROLLBAR_X = INPUT_X + GRID_WIDTH + 2;

    public static final int ARROW_HEIGHT = 17;
    public static final int ARROW_X = INPUT_SCROLLBAR_X + SCROLLBAR_WIDTH + 4;
    public static final int ARROW_Y = GRID_Y + (GRID_HEIGHT - ARROW_HEIGHT) / 2;

    /** Number block under the grids. */
    public static final int TEXT_X = 6;
    public static final int TEXT_Y = 70;
    public static final int LINE_HEIGHT = 11;

    private GTChemicalReactionLayout() {
    }

    /** Slots visible at once on either side. */
    public static int pageSize() {
        return COLUMNS * ROWS;
    }

    /** Pages needed for the given number of stacks; never zero, so an empty side still renders. */
    public static int pageCount(int stacks) {
        return Math.max(1, (stacks + pageSize() - 1) / pageSize());
    }
}
