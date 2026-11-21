import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** Parallel ceiling; {@code 0} means "always at maximum". */
    public static final int DATA_PARALLEL = 12;
    public static final int DATA_COUNT = 13;''',
'''    /** Parallel ceiling; {@code 0} means "always at maximum". */
    public static final int DATA_PARALLEL = 12;
    /** Cost of the cheapest rejected recipe, split into two halves like the energy counters. */
    public static final int DATA_REFUSED_LO = 13;
    public static final int DATA_REFUSED_HI = 14;
    public static final int DATA_COUNT = 15;''', 1)

s = s.replace('''    /** The highest overclock this machine's energy hatches can support at all. */
    public int getReachableOverclock() {
        return this.tile == null ? 0 : this.tile.reachableOverclock();
    }''',
'''    /** The highest overclock this machine's energy hatches can support at all. */
    public int getReachableOverclock() {
        return this.tile == null ? 0 : this.tile.reachableOverclock();
    }

    /** Cheapst recipe the last search turned down for cost, in FE per tick; zero when none. */
    public int getRefusedCost() {
        return combine(DATA_REFUSED_LO, DATA_REFUSED_HI);
    }''', 1)

s = s.replace('''        this.data.set(DATA_PARALLEL, this.tile.getParallelLimit());''',
'''        this.data.set(DATA_PARALLEL, this.tile.getParallelLimit());
        int refused = this.tile.refusedCost();
        this.data.set(DATA_REFUSED_LO, refused & 0xFFFF);
        this.data.set(DATA_REFUSED_HI, (refused >>> 16) & 0xFFFF);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('menu:', 'changed' if s != orig else 'NO CHANGE')
