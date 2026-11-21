import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''    /** The levels the running craft uses, or zero when nothing is running. */
    public int activeOverclock() {
        return this.activeRecipe == null ? 0 : this.activeOverclock;
    }

''', '')
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
