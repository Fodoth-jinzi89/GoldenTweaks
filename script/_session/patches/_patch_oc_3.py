import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    public static final int DATA_CHANNEL = 10;
    public static final int DATA_COUNT = 11;''',
'''    public static final int DATA_CHANNEL = 10;
    /** How far the machine may overclock; {@code -1} for no ceiling. */
    public static final int DATA_OVERCLOCK = 11;
    public static final int DATA_COUNT = 12;''', 1)

s = s.replace('''        this.data.set(DATA_CHANNEL, channel == null ? NO_CHANNEL : channel);
    }''',
'''        this.data.set(DATA_CHANNEL, channel == null ? NO_CHANNEL : channel);
    }

    /** The overclock ceiling as the side pane shows it. */
    public int getOverclockLimit() {
        return this.data.get(DATA_OVERCLOCK);
    }''', 1)

s = s.replace('''        this.data.set(DATA_FLAGS, this.tile.getStateFlags());
        syncStatus();''',
'''        this.data.set(DATA_FLAGS, this.tile.getStateFlags());
        this.data.set(DATA_OVERCLOCK, this.tile.getOverclockLimit());
        syncStatus();''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
