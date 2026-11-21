import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * The stacking-mode switch.
     *
     * <p>Which icon is drawn is the answer, not a highlight: yellow means each entry occupies one
     * cell, red means an entry may span as many as it needs. There is no pressed plate because the
     * sheet has none for this pair — the colour is the state.</p>
     */
    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        boolean single = this.menu.isSingleCellMode();
        // The plate underneath is part of the sheet, so only the icon is drawn here.
        graphics.blit(IO_TEXTURE, x, y,
                single ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI, MODE_ICON_V,
                MODE_WIDTH, MODE_HEIGHT);
    }''',
'''    /**
     * The stacking-mode switch: base plate, then icon over it.
     *
     * <p>Same construction as the run switch — the sheet carries one button plate, and both
     * switches layer their own artwork over it. The icon is what states the mode: yellow means each
     * entry occupies one cell, red means an entry may span as many as it needs.</p>
     */
    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
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
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
