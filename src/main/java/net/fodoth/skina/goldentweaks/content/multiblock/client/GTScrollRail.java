package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.minecraft.client.gui.GuiGraphics;

/**
 * A plain scrolling rail, drawn as filled rectangles.
 *
 * <p>Two shapes in use: the port windows draw a bordered track with a marker floating inside it,
 * and the machine's product list draws a bare track with a marker flush to its edges. Both are the
 * same arithmetic — how much of the content fits decides how much of the rail the marker takes, and
 * how far down the list is scrolled decides where it sits — so only the metrics differ.</p>
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

    /**
     * Draws the rail.
     *
     * <p>Coordinates are panel-relative, matching how the port windows and the machine's label pass
     * both address the sheet.</p>
     *
     * @param totalRows   how many rows the content needs
     * @param visibleRows how many the window shows at once
     * @param scrollRow   the first row on screen
     */
    public void draw(GuiGraphics graphics, int totalRows, int visibleRows, int scrollRow) {
        int inset = this.border == 0 ? 0 : 1;
        if (this.border != 0) {
            graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, this.border);
        }
        graphics.fill(this.x + inset, this.y + inset,
                this.x + this.width - inset, this.y + this.height - inset, this.track);

        int inner = this.height - inset * 2;
        int total = Math.max(totalRows, visibleRows);
        int size = Math.clamp(Math.round(inner * (float) visibleRows / (float) total),
                Math.min(this.markerMinHeight, inner), inner);
        int travel = inner - size;
        int max = Math.max(0, totalRows - visibleRows);
        int offset = max == 0 ? 0 : Math.round(travel * (float) scrollRow / (float) max);

        int markerX = this.x + inset + (this.border == 0 ? 0 : 1);
        int markerWidth = this.width - inset * 2 - (this.border == 0 ? 0 : 2);
        int markerY = this.y + inset + offset;
        graphics.fill(markerX, markerY, markerX + markerWidth, markerY + size, this.marker);
    }
}
