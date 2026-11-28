package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.minecraft.client.gui.GuiGraphics;

/**
 * A scrolling rail, drawn as filled rectangles.
 *
 * <p>Two shapes in use: the port windows draw a bordered track with a marker floating inside it,
 * and the machine's product list draws a bare track with a marker flush to its edges. Both are the
 * same arithmetic — how much of the content fits decides how much of the rail the marker takes, and
 * how far down the list is scrolled decides where it sits — so only the metrics differ.</p>
 *
 * <p>The rail stores panel-relative coordinates and is told where the panel is when it draws, the
 * same contract {@link GTButton} follows. Handing it absolute pixels instead makes the two callers
 * disagree: the port draws from a pass whose pose is not yet translated, while the machine's list
 * draws from one that is, and a rail written for either convention lands off-screen in the other.</p>
 */
public final class GTScrollRail {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int border;
    private final int track;
    private final int marker;
    private final int markerMinHeight;

    private GTScrollRail(int x, int y, int width, int height,
                         int border, int track, int marker, int markerMinHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.border = border;
        this.track = track;
        this.marker = marker;
        this.markerMinHeight = markerMinHeight;
    }

    /**
     * A bordered rail, like the port windows use: an outline, an inset track, and a marker drawn
     * two pixels in from either edge.
     */
    public static GTScrollRail bordered(int x, int y, int width, int height,
                                        int borderColour, int trackColour,
                                        int markerColour, int markerMinHeight) {
        return new GTScrollRail(x, y, width, height,
                borderColour, trackColour, markerColour, markerMinHeight);
    }

    /** A bare rail with no outline, like the machine's product list uses. */
    public static GTScrollRail plain(int x, int y, int width, int height,
                                     int trackColour, int markerColour, int markerMinHeight) {
        return new GTScrollRail(x, y, width, height, 0, trackColour, markerColour, markerMinHeight);
    }

    /** Width of the rail, for a caller that hit-tests the whole track. */
    public int width() {
        return this.width;
    }

    /** Height of the rail, for a caller that hit-tests the whole track. */
    public int height() {
        return this.height;
    }

    /**
     * Whether the cursor is inside the rail, in the same coordinates the drawing uses.
     *
     * <p>A click on the track jumps the list to that position, so the click handler and the drawing
     * have to agree on where the rail is — the same reason the button owns its own hit test.</p>
     */
    public boolean hits(double mouseX, double mouseY, int panelLeft, int panelTop) {
        int left = panelLeft + this.x;
        int top = panelTop + this.y;
        return mouseX >= left && mouseX < left + this.width
                && mouseY >= top && mouseY < top + this.height;
    }

    /**
     * Where along the rail a point lands, as a fraction of the track.
     *
     * <p>Used by the click handler: a press at the top goes to the first row and a press at the
     * bottom to the last. Clamped, so a click on the border still means something.</p>
     */
    public double fractionAt(double mouseY, int panelTop) {
        int inset = this.border == 0 ? 0 : 1;
        int inner = this.height - inset * 2;
        if (inner <= 0) {
            return 0.0D;
        }
        double progress = (mouseY - (panelTop + this.y + inset)) / (double) inner;
        return Math.clamp(progress, 0.0D, 1.0D);
    }

    /**
     * Draws the rail.
     *
     * @param totalRows   how many rows the content needs
     * @param visibleRows how many the window shows at once
     * @param scrollRow   the first row on screen
     * @param panelLeft   window origin, since the rail stores panel coordinates
     * @param panelTop    window origin
     */
    public void draw(GuiGraphics graphics, int totalRows, int visibleRows, int scrollRow,
                     int panelLeft, int panelTop) {
        int left = panelLeft + this.x;
        int top = panelTop + this.y;
        int inset = this.border == 0 ? 0 : 1;

        if (this.border != 0) {
            graphics.fill(left, top, left + this.width, top + this.height, this.border);
        }
        graphics.fill(left + inset, top + inset,
                left + this.width - inset, top + this.height - inset, this.track);

        int inner = this.height - inset * 2;
        int total = Math.max(totalRows, visibleRows);
        int size = Math.clamp(Math.round(inner * (float) visibleRows / (float) total),
                Math.min(this.markerMinHeight, inner), inner);
        int travel = inner - size;
        int max = Math.max(0, totalRows - visibleRows);
        int offset = max == 0 ? 0 : Math.round(travel * (float) scrollRow / (float) max);

        int markerX = left + inset + (this.border == 0 ? 0 : 1);
        int markerWidth = this.width - inset * 2 - (this.border == 0 ? 0 : 2);
        int markerY = top + inset + offset;
        graphics.fill(markerX, markerY, markerX + markerWidth, markerY + size, this.marker);
    }
}
