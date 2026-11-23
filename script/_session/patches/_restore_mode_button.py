"""Bring back the stacking switch on item and fluid ports.

It was removed along with the energy port's copy, but only the energy port had no use for it:
an item or fluid port has cells, and single-cell versus multi-cell is the setting that decides
whether one entry may spread across several of them.

The layout is per kind, so the two buttons stay on plates the sheet actually has:

  item / fluid   (154,27) stacking switch   (179,27) reach-through switch
  energy         (154,27) reach-through switch
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java"
s = io.open(p, encoding="utf-8").read()

# ---------------- constants: stacking switch back on the first plate ----------------
s = s.replace('''    /**
     * The reach-through switch, on the first plate of the top-right block.
     *
     * <p>At {@code (154,27)}, the slot the stacking switch used to occupy. That switch is gone — an
     * energy port has no cells for an entry to span, so the setting controlled nothing there — and
     * shifting this one left keeps the buttons on plates the sheet actually has instead of leaving a
     * hole at the front of the row.</p>
     *
     * <p>One plate from the sheet, with the state shown by which of two icons is laid over it:
     * yellow is passive (the plain behaviour, where the port waits to be reached), red is active (it
     * also reaches through its own face).</p>
     */
    private static final int ACTIVE_X = 154;''',
'''    /**
     * The stacking switch, on the first plate of the top-right block.
     *
     * <p>Only shown by item and fluid ports: an energy port has no cells for an entry to span, so
     * the setting controls nothing there and its plate is given to the reach-through switch
     * instead.</p>
     */
    private static final int MODE_X = 154;
    private static final int MODE_Y = 27;
    private static final int MODE_PLATE_U = 44;
    private static final int MODE_PLATE_V = 178;
    private static final int MODE_WIDTH = 21;
    private static final int MODE_HEIGHT = 17;
    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 65;
    private static final int MODE_ICON_V = 195;

    /**
     * The reach-through switch: second plate for item and fluid ports, first for an energy port.
     *
     * <p>Sitting it after the stacking switch is what keeps the row contiguous; an energy port has
     * no stacking switch, so it takes the first plate rather than leaving a gap.</p>
     */
    private static final int ACTIVE_X = 179;
    /** Where the reach-through switch goes when there is no stacking switch in front of it. */
    private static final int ACTIVE_X_NO_MODE = 154;''', 1)

# ---------------- draw ----------------
s = s.replace('''        drawRunButton(graphics, mouseX, mouseY);
        // The stacking switch is deliberately absent: an energy port has no cells for an entry to
        // span, so the setting had nothing to control there.
        drawActiveButton(graphics, mouseX, mouseY);''',
'''        drawRunButton(graphics, mouseX, mouseY);
        // An energy port has no cells for an entry to span, so it gets no stacking switch and its
        // reach-through switch takes the plate that would have held it.
        if (!energy) {
            drawModeButton(graphics, mouseX, mouseY);
        }
        drawActiveButton(graphics, mouseX, mouseY);''', 1)

s = s.replace('''    /** The reach-through switch: plate, then the icon for the state it is in. */
    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + ACTIVE_X;''',
'''    /** The stacking switch: base plate, then the icon for the mode it is in. */
    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        graphics.blit(IO_TEXTURE, x, y, MODE_PLATE_U, MODE_PLATE_V,
                MODE_WIDTH, MODE_HEIGHT);
        boolean single = this.menu.isSingleCellMode();
        graphics.blit(IO_TEXTURE, x, y,
                single ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI, MODE_ICON_V,
                MODE_WIDTH, MODE_HEIGHT);
    }

    /** The reach-through switch: plate, then the icon for the state it is in. */
    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();''', 1)

# ---------------- one place decides where the reach-through switch sits ----------------
s = s.replace('''    /** The reach-through switch: plate, then the icon for the state it is in. */''',
'''    /**
     * Where the reach-through switch sits for this port.
     *
     * <p>After the stacking switch when there is one, on the first plate when there is not. Kept in
     * one method so the drawing, the click area and the tooltip cannot disagree about it.</p>
     */
    private int activeX() {
        return this.menu.kind() == GTPortKind.ENERGY ? ACTIVE_X_NO_MODE : ACTIVE_X;
    }

    /** The reach-through switch: plate, then the icon for the state it is in. */''', 1)

s = s.replace('''    private void renderActiveTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + ACTIVE_X;''',
'''    private void renderActiveTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();''', 1)

s = s.replace('''                && mouseX >= this.leftPos + ACTIVE_X && mouseX < this.leftPos + ACTIVE_X + ACTIVE_WIDTH''',
'''                && mouseX >= this.leftPos + activeX() && mouseX < this.leftPos + activeX() + ACTIVE_WIDTH''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("stacking switch restored")
