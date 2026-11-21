import io

# ---------- port screen ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''        if (!insideField) {
            commit();
            for (EditBox box : this.fields) {
                box.setFocused(false);
            }
        }''',
'''        if (!insideField) {
            commit();
            // Through the screen, not the field: Screen keeps its own pointer to the focused
            // listener, and clearing the field alone leaves it stale, after which clicking that
            // same field again is ignored because the screen believes it is still focused.
            this.setFocused(null);
        }''', 1)

s = s.replace('''        if (keyCode == 257 || keyCode == 335) {
            for (EditBox box : this.fields) {
                if (box.isFocused()) {
                    commit();
                    box.setFocused(false);
                    return true;
                }
            }
        }''',
'''        if (keyCode == 257 || keyCode == 335) {
            for (EditBox box : this.fields) {
                if (box.isFocused()) {
                    commit();
                    this.setFocused(null);
                    return true;
                }
            }
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('port screen:', 'changed' if s != orig else 'NO CHANGE')

# ---------- machine screen ----------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''            commitName();
            this.nameField.setFocused(false);
            return true;''',
'''            commitName();
            // Through the screen: it owns the focus pointer, and clearing only the field leaves it
            // pointing here, which makes the field unclickable afterwards.
            this.setFocused(null);
            return true;''', 1)

s2 = s2.replace('''            commitOverclock();
            this.overclockField.setFocused(false);
            return true;''',
'''            commitOverclock();
            this.setFocused(null);
            return true;''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('machine screen:', 'changed' if s2 != orig2 else 'NO CHANGE')
