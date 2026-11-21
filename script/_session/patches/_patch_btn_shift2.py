import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * The stacking-mode switch, on the first of the plates at the panel's top right.
     *
     * <p>Both the plate and its icons share one 21 by 17 footprint. The plate is drawn by the sheet
     * at this spot; the icons are drawn over it, nudged right so they sit inside the plate's inner
     * margin rather than hard against its border. Yellow marks single-cell mode, red marks
     * multi-cell, so which colour shows is the whole answer.</p>
     */
    private static final int MODE_X = 154;
    private static final int MODE_Y = 178;''',
'''    /**
     * The stacking-mode switch.
     *
     * <p>The sheet draws the button plate at {@code (44,178)} and its two icons on the row below;
     * all three share the same 21 by 17 footprint, so the plate is blitted from that spot and the
     * icons layered over it. Yellow marks single-cell mode, red marks multi-cell, so which colour
     * shows is the whole answer.</p>
     *
     * <p>Nudged right by the sheet's own margin: the plate's left edge is transparent for a few
     * pixels, and putting the pair flush against it leaves the icon looking off-centre.</p>
     */
    private static final int MODE_X = 149;
    private static final int MODE_Y = 178;
    /** Where the sheet keeps the plate itself. */
    private static final int MODE_PLATE_U = 44;
    private static final int MODE_PLATE_V = 178;''', 1)

s = s.replace('''    /** The plate sprite, shared with the run switch: the sheet provides only one. */
    private static final int MODE_PLATE_U = 0;
    private static final int MODE_PLATE_PRESSED_U = 22;
    private static final int MODE_PLATE_V = 178;
''', '')

s = s.replace('''        // Base plate, stretched to this button's footprint. The run switch's plate is the only one
        // the sheet provides, so both switches share it rather than looking like different controls.
        graphics.blit(IO_TEXTURE, x, y,
                held ? MODE_PLATE_PRESSED_U : MODE_PLATE_U, MODE_PLATE_V,
                MODE_WIDTH, MODE_HEIGHT);''',
'''        // The plate is the sheet's own, at its own coordinates — this button has no pressed state of
        // its own, and borrowing the run switch's sprite would draw a different-looking control.
        graphics.blit(IO_TEXTURE, x, y, MODE_PLATE_U, MODE_PLATE_V,
                MODE_WIDTH, MODE_HEIGHT);''', 1)

s = s.replace('''        boolean held = mouseX >= x && mouseX < x + MODE_WIDTH
                && mouseY >= y && mouseY < y + MODE_HEIGHT;

        // The plate is the sheet's own''',
'''        // The plate is the sheet's own''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
