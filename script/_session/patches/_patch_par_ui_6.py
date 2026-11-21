import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- drop the old single-pane icon helper ----
s = s.replace('''    /** The control circuit's icon, in the pane's slot. */
    private void renderSidebarSlot(GuiGraphics graphics, int x, int y) {
        graphics.renderItem(overclockIcon(), x, y);
    }

''', '')

# ---- EMI bounds: both panes ----
s = s.replace('''    public int sidebarRight() {
        return this.leftPos + SIDEBAR_X + this.sidebarWidth;
    }

    public int sidebarBottom() {
        return this.topPos + Math.max(this.imageHeight, SIDEBAR_Y + this.sidebarHeight);
    }''',
'''    public int sidebarRight() {
        int widest = SIDEBAR_MIN_WIDTH;
        for (Pane pane : this.panes) {
            widest = Math.max(widest, pane.width);
        }
        return this.leftPos + SIDEBAR_X + widest;
    }

    public int sidebarBottom() {
        int lowest = this.imageHeight;
        for (int index = 0; index < this.panes.size(); index++) {
            lowest = Math.max(lowest, paneTop(index) + this.panes.get(index).height);
        }
        return this.topPos + lowest;
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
