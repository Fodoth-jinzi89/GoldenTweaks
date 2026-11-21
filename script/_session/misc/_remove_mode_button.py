import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------------- constants: the mode button gives way to the reach-through one ----------------
s = s.replace('''    /**
     * The stacking-mode switch, on the first plate of the top-right block.
     *
     * <p>Placed at {@code (154,27)} in the panel, which is the first plate of the top-right block.
     * The same position drives the click area, so what is drawn is what is hit.</p>
     */
    private static final int MODE_X = 154;
    private static final int MODE_Y = 27;
    /** Where the sheet keeps the plate itself. */
    private static final int MODE_PLATE_U = 44;
    private static final int MODE_PLATE_V = 178;
    private static final int MODE_WIDTH = 21;
    private static final int MODE_HEIGHT = 17;
    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 65;
    private static final int MODE_ICON_V = 195;

    /**
     * The reach-through switch, on the second plate of the top-right block.
     *
     * <p>Same construction as the stacking switch: one plate from the sheet, with the state shown by
     * which of two icons is laid over it. Yellow is passive (the plain behaviour, where the port
     * waits to be reached), red is active (it also reaches through its own face).</p>
     */
    private static final int ACTIVE_X = 179;
    private static final int ACTIVE_Y = 27;
    private static final int ACTIVE_PLATE_U = 44;
    private static final int ACTIVE_PLATE_V = 178;
    private static final int ACTIVE_WIDTH = 21;
    private static final int ACTIVE_HEIGHT = 17;
    private static final int ACTIVE_ICON_U_PASSIVE = 44;
    private static final int ACTIVE_ICON_U_ACTIVE = 65;
    private static final int ACTIVE_ICON_V = 212;''',
'''    /**
     * The reach-through switch, on the first plate of the top-right block.
     *
     * <p>At {@code (154,27)}, the slot the stacking switch used to occupy. The stacking switch is
     * gone — an energy port has no cells to spread across, so the setting had no meaning there — and
     * shifting this one left keeps the two buttons on the plates the sheet actually has instead of
     * leaving a hole at the front of the row.</p>
     *
     * <p>One plate from the sheet, with the state shown by which of two icons is laid over it:
     * yellow is passive (the plain behaviour, where the port waits to be reached), red is active (it
     * also reaches through its own face).</p>
     */
    private static final int ACTIVE_X = 154;
    private static final int ACTIVE_Y = 27;
    private static final int ACTIVE_PLATE_U = 44;
    private static final int ACTIVE_PLATE_V = 178;
    private static final int ACTIVE_WIDTH = 21;
    private static final int ACTIVE_HEIGHT = 17;
    private static final int ACTIVE_ICON_U_PASSIVE = 44;
    private static final int ACTIVE_ICON_U_ACTIVE = 65;
    private static final int ACTIVE_ICON_V = 212;''', 1)

# ---------------- draw: no mode button, and the reach-through one only once ----------------
s = s.replace('''        drawRunButton(graphics, mouseX, mouseY);
        drawModeButton(graphics, mouseX, mouseY);
        drawActiveButton(graphics, mouseX, mouseY);''',
'''        drawRunButton(graphics, mouseX, mouseY);
        // The stacking switch is deliberately absent: an energy port has no cells for an entry to
        // span, so the setting had nothing to control there.
        drawActiveButton(graphics, mouseX, mouseY);''', 1)

s = s.replace('''    /**
     * The stacking-mode switch: base plate, then icon over it.
     *
     * <p>Same construction as the run switch — the sheet carries one button plate, and both
     * switches layer their own artwork over it. The icon is what states the mode: yellow means each
     * entry occupies one cell, red means an entry may span as many as it needs.</p>
     */
    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        // The plate is the sheet's own, at its own coordinates — this button has no pressed state of
        // its own, and borrowing the run switch's sprite would draw a different-looking control.
        graphics.blit(IO_TEXTURE, x, y, MODE_PLATE_U, MODE_PLATE_V,
                MODE_WIDTH, MODE_HEIGHT);

        boolean single = this.menu.isSingleCellMode();
        graphics.blit(IO_TEXTURE, x, y,
                single ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI, MODE_ICON_V,
                MODE_WIDTH, MODE_HEIGHT);
    }

    /** The reach-through switch: plate, then the icon for the state it is in. */''',
'''    /** The reach-through switch: plate, then the icon for the state it is in. */''', 1)

# ---------------- click: drop the mode branch ----------------
s = s.replace('''        // The mode switch flips how entries are laid out; like the run switch, the server owns the
        // value and reports it back.
        if (button == 0
                && mouseX >= this.leftPos + MODE_X && mouseX < this.leftPos + MODE_X + MODE_WIDTH
                && mouseY >= this.topPos + MODE_Y && mouseY < this.topPos + MODE_Y + MODE_HEIGHT) {
            GTPortBlockEntity port = this.menu.port();
            if (port != null) {
                PacketDistributor.sendToServer(new C2SPortTogglePacket(
                        port.getBlockPos(), C2SPortTogglePacket.Target.STACK_MODE));
            }
            return true;
        }

''', '')

# ---------------- tooltip: drop the mode hint ----------------
s = s.replace('''    /** Explains the stacking switch: the icon alone does not say which way round it is. */
    private void renderModeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        if (mouseX < x || mouseX >= x + MODE_WIDTH || mouseY < y || mouseY >= y + MODE_HEIGHT) {
            return;
        }
        graphics.renderTooltip(this.font, Component.translatable(this.menu.isSingleCellMode()
                ? "gui.goldentweaks.port.stack.single"
                : "gui.goldentweaks.port.stack.multi"), mouseX, mouseY);
    }

''', '')

s = s.replace('''        renderModeTooltip(graphics, mouseX, mouseY);
''', '')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen changed:', s != orig)
for token in ('MODE_X', 'MODE_WIDTH', 'drawModeButton', 'renderModeTooltip', 'STACK_MODE'):
    print(' ', token, 'left:', s.count(token))
