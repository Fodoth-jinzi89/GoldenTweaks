import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    public static final int DATA_OVERCLOCK = 11;
    public static final int DATA_COUNT = 12;''',
'''    public static final int DATA_OVERCLOCK = 11;
    /** Parallel ceiling; {@code 0} means "always at maximum". */
    public static final int DATA_PARALLEL = 12;
    public static final int DATA_COUNT = 13;''', 1)

s = s.replace('''    /** The overclock ceiling as the side pane shows it. */
    public int getOverclockLimit() {
        return this.data.get(DATA_OVERCLOCK);
    }''',
'''    /** The overclock ceiling as the side pane shows it. */
    public int getOverclockLimit() {
        return this.data.get(DATA_OVERCLOCK);
    }

    /** The parallel ceiling as the side pane shows it; {@code 0} means "always at maximum". */
    public int getParallelLimit() {
        return this.data.get(DATA_PARALLEL);
    }''', 1)

s = s.replace('''        this.data.set(DATA_OVERCLOCK, this.tile.getOverclockLimit());''',
'''        this.data.set(DATA_OVERCLOCK, this.tile.getOverclockLimit());
        this.data.set(DATA_PARALLEL, this.tile.getParallelLimit());''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
