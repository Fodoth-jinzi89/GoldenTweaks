import io

# ---------- menu: report what is in force, recipe-aware ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''    public int getOverclockSteps() {
        return this.tile == null ? 0 : this.tile.overclockSteps();
    }''',
'''    public int getOverclockSteps() {
        return this.tile == null ? 0 : this.tile.effectiveOverclock();
    }''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('menu:', 'changed' if s != orig else 'NO CHANGE')

# ---------- drop the now-unused overload ----------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''    /** The levels the running craft uses, or the ceiling when nothing is running. */
    public int overclockSteps() {
        int base = this.activeRecipe == null ? 0 : this.activeRecipe.energyPerTick();
        return base > 0 ? overclockSteps(base) : maxOverclock();
    }''',
'''    /** The levels the running craft uses, or zero when nothing is running. */
    public int activeOverclock() {
        return this.activeRecipe == null ? 0 : this.activeOverclock;
    }''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('machine:', 'changed' if s2 != orig2 else 'NO CHANGE')
