import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace("""    public static final int DATA_LENGTH = 9;
    public static final int DATA_COUNT = 10;""",
"""    public static final int DATA_LENGTH = 9;
    /** The group the running craft belongs to, or {@link #NO_CHANNEL} when there is none. */
    public static final int DATA_CHANNEL = 10;
    public static final int DATA_COUNT = 11;

    /** Sentinel for "no channel": a real channel is any int, so it needs an out-of-band value. */
    public static final int NO_CHANNEL = Integer.MIN_VALUE;""", 1)

s = s.replace("""    public int getRecipeLength() {
        return this.data.get(DATA_LENGTH);
    }""",
"""    public int getRecipeLength() {
        return this.data.get(DATA_LENGTH);
    }

    /** The group the running craft belongs to, or {@code null} when the machine is idle. */
    @Nullable
    public Integer getActiveChannel() {
        int value = this.data.get(DATA_CHANNEL);
        return value == NO_CHANNEL ? null : value;
    }""", 1)

s = s.replace("""        this.data.set(DATA_LENGTH, this.tile.getRecipeLength());""",
"""        this.data.set(DATA_LENGTH, this.tile.getRecipeLength());
        Integer channel = this.tile.activeChannel();
        this.data.set(DATA_CHANNEL, channel == null ? NO_CHANNEL : channel);""", 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
