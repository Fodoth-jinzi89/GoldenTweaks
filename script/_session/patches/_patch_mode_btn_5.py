import io, json

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * The stacking-mode switch, on the first of the plates at the panel's top right.
     *
     * <p>Two icons on the same 21 by 17 footprint, drawn with the transparent margin the sheet
     * reserves so the button sits centered inside the plate instead of touching its edge. Yellow
     * marks single-cell mode, red marks multi-cell; the icon itself is the state, so which colour
     * is showing is the whole answer.</p>
     */
    private static final int MODE_X = 150;
    private static final int MODE_Y = 27;''',
'''    /**
     * The stacking-mode switch, on the first of the plates at the panel's top right.
     *
     * <p>Two icons on the same 21 by 17 footprint. They are drawn flush with the plate's left inner
     * edge, not centered in it: the sheet's left margin is the alignment the pair was drawn for, so
     * offsetting by a centering term would push them off it. Yellow marks single-cell mode, red
     * marks multi-cell; the icon itself is the state, so which colour shows is the whole answer.</p>
     */
    private static final int MODE_X = 149;
    private static final int MODE_Y = 27;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen:', 'changed' if s != orig else 'NO CHANGE')

# tooltip for the switch
s = io.open(p, encoding='utf-8').read()
orig2 = s
s = s.replace('''        boolean enabled = this.menu.port() != null && this.menu.port().isEnabled();
        graphics.renderTooltip(this.font, Component.translatable(enabled
                ? "gui.goldentweaks.port.running.on"
                : "gui.goldentweaks.port.running.off"), mouseX, mouseY);
    }''',
'''        boolean enabled = this.menu.port() != null && this.menu.port().isEnabled();
        graphics.renderTooltip(this.font, Component.translatable(enabled
                ? "gui.goldentweaks.port.running.on"
                : "gui.goldentweaks.port.running.off"), mouseX, mouseY);
    }

    /** Explains the stacking switch: the icon alone does not say which way round it is. */
    private void renderModeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        if (mouseX < x || mouseX >= x + MODE_WIDTH || mouseY < y || mouseY >= y + MODE_HEIGHT) {
            return;
        }
        graphics.renderTooltip(this.font, Component.translatable(this.menu.isSingleCellMode()
                ? "gui.goldentweaks.port.stack.single"
                : "gui.goldentweaks.port.stack.multi"), mouseX, mouseY);
    }''', 1)

s = s.replace('''        renderRunTooltip(graphics, mouseX, mouseY);''',
'''        renderRunTooltip(graphics, mouseX, mouseY);
        renderModeTooltip(graphics, mouseX, mouseY);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('tooltip:', 'changed' if s != orig2 else 'NO CHANGE')

zh = {
    'gui.goldentweaks.port.stack.single': '单格模式：每个条目占 1 格',
    'gui.goldentweaks.port.stack.multi': '多格模式：条目可占多格',
}
en = {
    'gui.goldentweaks.port.stack.single': 'Single cell: one cell per entry',
    'gui.goldentweaks.port.stack.multi': 'Multi cell: entries may span cells',
}
for path, extra in [
    (r"src/main/resources/assets/goldentweaks/lang/zh_cn.json", zh),
    (r"src/main/resources/assets/goldentweaks/lang/en_us.json", en),
]:
    data = json.load(io.open(path, encoding="utf-8"))
    data.update(extra)
    with io.open(path, "w", encoding="utf-8", newline="\n") as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write("\n")
    print("lang updated", path)
