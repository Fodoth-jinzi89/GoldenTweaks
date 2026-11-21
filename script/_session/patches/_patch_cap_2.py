import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''        if (pane.pending != null) {
            if (stored != pane.pending) {
                return;
            }
            pane.pending = null;
        }
        String shown = String.valueOf(stored);''',
'''        if (pane.pending != null) {
            if (stored != pane.pending) {
                return;
            }
            pane.pending = null;
        }
        // A stored value out of range is shown as the range, not as itself: removing an energy hatch
        // lowers the ceiling, and the box must not keep offering a number the machine would refuse.
        if (pane == this.overclockPane && stored > 0) {
            int ceiling = this.menu.getOverclockCeiling();
            if (stored > ceiling) {
                stored = ceiling;
            }
        }
        String shown = String.valueOf(stored);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
