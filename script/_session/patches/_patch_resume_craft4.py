import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- tags ----
s = s.replace('''    private static final String TAG_ACTIVE_OVERCLOCK = "ActiveOverclock";''',
'''    private static final String TAG_ACTIVE_OVERCLOCK = "ActiveOverclock";
    /** The group the craft was matched against, so it keeps drawing from and delivering to it. */
    private static final String TAG_ACTIVE_CHANNEL = "ActiveChannel";
    private static final String TAG_ACTIVE_MODE = "ActiveMode";''', 1)

# ---- save ----
s = s.replace('''                tag.putInt(TAG_ACTIVE_OVERCLOCK, this.activeOverclock);''',
'''                tag.putInt(TAG_ACTIVE_OVERCLOCK, this.activeOverclock);
                // The ports the run is pinned to. Restoring the recipe without them would let a
                // resumed craft draw from a different group than the one it took its inputs from.
                if (this.activeChannel != null) {
                    tag.putInt(TAG_ACTIVE_CHANNEL, this.activeChannel);
                }
                if (this.activeMode != null) {
                    tag.putInt(TAG_ACTIVE_MODE, this.activeMode);
                }''', 1)

# ---- load ----
s = s.replace('''        this.pendingLength = tag.getInt(TAG_ACTIVE_LENGTH);
        this.activeOverclock = tag.getInt(TAG_ACTIVE_OVERCLOCK);''',
'''        this.pendingLength = tag.getInt(TAG_ACTIVE_LENGTH);
        this.activeOverclock = tag.getInt(TAG_ACTIVE_OVERCLOCK);
        this.pendingChannel = tag.contains(TAG_ACTIVE_CHANNEL) ? tag.getInt(TAG_ACTIVE_CHANNEL) : null;
        this.pendingMode = tag.contains(TAG_ACTIVE_MODE) ? tag.getInt(TAG_ACTIVE_MODE) : null;''', 1)

# ---- pending fields ----
s = s.replace('''    private int pendingProgress;
    private int pendingLength;''',
'''    private int pendingProgress;
    private int pendingLength;
    /** The group the parked craft belonged to; unlimited when the save did not name one. */
    @Nullable
    private Integer pendingChannel;
    @Nullable
    private Integer pendingMode;''', 1)

# ---- resume: restore the group ----
s = s.replace('''        this.activeRecipe = resumed;
        this.activeProducts = productsOf(resumed);''',
'''        this.activeRecipe = resumed;
        // Pinned before the first tick can run, or the resumed craft would be billed against every
        // port on the machine instead of the ones it was matched with.
        this.activeChannel = this.pendingChannel;
        this.activeMode = this.pendingMode;
        this.selectedChannel = this.pendingChannel;
        this.selectedMode = this.pendingMode;
        this.pendingChannel = null;
        this.pendingMode = null;
        this.activeProducts = productsOf(resumed);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
