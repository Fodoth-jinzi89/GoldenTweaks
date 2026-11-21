import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------- tooltips ----------
s = s.replace('''        // Only the icon itself explains itself; the rest of the pane is just a container, so it
        // shows no tooltip at all.
        if (sidebarSlotContains(mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.overclock"),
                    mouseX, mouseY);
            return;
        }

        // The ceiling's own field, which sits on the artwork's black band.
        if (this.overclockField != null && this.overclockField.isVisible()
                && this.overclockField.isMouseOver(mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.overclock_limit"),
                    mouseX, mouseY);
            return;
        }''',
'''        // Only the icon itself explains itself; the rest of a pane is just a container, so it shows
        // no tooltip at all.
        Pane iconPane = paneIconAt(mouseX, mouseY);
        if (iconPane != null) {
            graphics.renderTooltip(this.font, Component.translatable(iconPane == this.overclockPane
                            ? "gui.goldentweaks.chemical_reactor.sidebar.overclock"
                            : "gui.goldentweaks.chemical_reactor.sidebar.parallel"),
                    mouseX, mouseY);
            return;
        }

        // Each box names what it edits, so the two panes' identical layouts stay tellable apart.
        Pane fieldPane = paneFieldAt(mouseX, mouseY);
        if (fieldPane != null) {
            graphics.renderTooltip(this.font, Component.translatable(
                            fieldPane == this.overclockPane
                                    ? "gui.goldentweaks.chemical_reactor.sidebar.overclock_limit"
                                    : "gui.goldentweaks.chemical_reactor.sidebar.parallel_limit"),
                    mouseX, mouseY);
            return;
        }''', 1)

# ---------- click ----------
s = s.replace('''        if (this.overclockField != null && this.overclockField.isVisible()
                && !this.overclockField.isMouseOver(mouseX, mouseY)) {
            commitOverclock();
        }''',
'''        // A click outside every pane box commits them all, the same way the name box behaves.
        if (paneFieldAt(mouseX, mouseY) == null) {
            commitOverclock();
        }''', 1)

s = s.replace('''        if (button == 0 && sidebarSlotContains(mouseX, mouseY)) {
            this.sidebarOpen = !this.sidebarOpen;
            return true;
        }''',
'''        Pane clicked = paneIconAt(mouseX, mouseY);
        if (button == 0 && clicked != null) {
            clicked.open = !clicked.open;
            return true;
        }''', 1)

# ---------- key ----------
s = s.replace('''        if ((keyCode == 257 || keyCode == 335)
                && this.overclockField != null && this.overclockField.isFocused()) {
            commitOverclock();
            this.setFocused(null);
            return true;
        }''',
'''        if (keyCode == 257 || keyCode == 335) {
            for (Pane pane : this.panes) {
                if (pane.field != null && pane.field.isFocused()) {
                    commitOverclock();
                    this.setFocused(null);
                    return true;
                }
            }
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
