import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- constants: the switch lives in the top-right plate row, not the button strip ----
s = s.replace('''    /**
     * The stacking-mode switch: one cell per entry, or as many as the entry needs.
     *
     * <p>Two icons on the same 21 by 17 footprint, with the transparent margin the sheet reserves so
     * the pair lines up with the plates above. Yellow marks single-cell mode, red marks multi-cell;
     * the pair sits at the head of the button row and the labels below are shifted to clear it.</p>
     */
    private static final int MODE_X = 44;
    private static final int MODE_Y = 195;
    private static final int MODE_WIDTH = 21;
    private static final int MODE_HEIGHT = 17;
    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 67;
    private static final int MODE_ICON_V = 195;''',
'''    /**
     * The stacking-mode switch, on the first of the plates at the panel's top right.
     *
     * <p>Two icons on the same 21 by 17 footprint, drawn with the transparent margin the sheet
     * reserves so the button sits centered inside the plate instead of touching its edge. Yellow
     * marks single-cell mode, red marks multi-cell; the icon itself is the state, so which colour
     * is showing is the whole answer.</p>
     */
    private static final int MODE_X = 150;
    private static final int MODE_Y = 27;
    private static final int MODE_WIDTH = 21;
    private static final int MODE_HEIGHT = 17;
    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 67;
    private static final int MODE_ICON_V = 195;''', 1)

# ---- the button needs a backing plate: these plates are drawn by the sheet already ----
s = s.replace('''    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        boolean single = this.menu.isSingleCellMode();
        graphics.blit(IO_TEXTURE, x, y,
                single ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI, MODE_ICON_V,
                MODE_WIDTH, MODE_HEIGHT);
    }''',
'''    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        boolean single = this.menu.isSingleCellMode();
        // The plate underneath is part of the sheet, so only the icon is drawn here.
        graphics.blit(IO_TEXTURE, x, y,
                single ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI, MODE_ICON_V,
                MODE_WIDTH, MODE_HEIGHT);
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
