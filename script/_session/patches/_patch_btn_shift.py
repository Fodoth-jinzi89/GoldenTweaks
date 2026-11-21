import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * The stacking-mode switch, on the first of the plates at the panel's top right.
     *
     * <p>Two icons on the same 21 by 17 footprint. They are drawn flush with the plate's left inner
     * edge, not centered in it: the sheet's left margin is the alignment the pair was drawn for, so
     * offsetting by a centering term would push them off it. Yellow marks single-cell mode, red
     * marks multi-cell; the icon itself is the state, so which colour shows is the whole answer.</p>
     */
    private static final int MODE_X = 149;
    private static final int MODE_Y = 27;''',
'''    /**
     * The stacking-mode switch, on the first of the plates at the panel's top right.
     *
     * <p>Both the plate and its icons share one 21 by 17 footprint. The plate is drawn by the sheet
     * at this spot; the icons are drawn over it, nudged right so they sit inside the plate's inner
     * margin rather than hard against its border. Yellow marks single-cell mode, red marks
     * multi-cell, so which colour shows is the whole answer.</p>
     */
    private static final int MODE_X = 154;
    private static final int MODE_Y = 178;''', 1)

s = s.replace('''    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 67;
    private static final int MODE_ICON_V = 195;''',
'''    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 67;
    private static final int MODE_ICON_V = 195;
    /** The plate sprite, shared with the run switch: the sheet provides only one. */
    private static final int MODE_PLATE_U = 0;
    private static final int MODE_PLATE_PRESSED_U = 22;
    private static final int MODE_PLATE_V = 178;''', 1)

# The icons come from their own row; the plate is blitted from the button-plate row.
s = s.replace('''    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        boolean held = mouseX >= x && mouseX < x + MODE_WIDTH
                && mouseY >= y && mouseY < y + MODE_HEIGHT;

        // Base plate, stretched to this button's footprint. The run switch's plate is the only one
        // the sheet provides, so both switches share it rather than looking like different controls.
        graphics.blit(IO_TEXTURE, x, y,
                held ? RUN_SPRITE_PRESSED_U : RUN_SPRITE_U, RUN_SPRITE_V,
                MODE_WIDTH, MODE_HEIGHT);

        boolean single = this.menu.isSingleCellMode();
        graphics.blit(IO_TEXTURE, x, y,
                single ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI, MODE_ICON_V,
                MODE_WIDTH, MODE_HEIGHT);
    }''',
'''    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        boolean held = mouseX >= x && mouseX < x + MODE_WIDTH
                && mouseY >= y && mouseY < y + MODE_HEIGHT;

        // Base plate, stretched to this button's footprint. The run switch's plate is the only one
        // the sheet provides, so both switches share it rather than looking like different controls.
        graphics.blit(IO_TEXTURE, x, y,
                held ? MODE_PLATE_PRESSED_U : MODE_PLATE_U, MODE_PLATE_V,
                MODE_WIDTH, MODE_HEIGHT);

        boolean single = this.menu.isSingleCellMode();
        graphics.blit(IO_TEXTURE, x, y,
                single ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI, MODE_ICON_V,
                MODE_WIDTH, MODE_HEIGHT);
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
