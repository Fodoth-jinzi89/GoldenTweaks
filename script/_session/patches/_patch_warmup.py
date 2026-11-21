import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------------- tags ----------------
s = s.replace('''    private static final String TAG_ACTIVE_MODE = "ActiveMode";''',
'''    private static final String TAG_ACTIVE_MODE = "ActiveMode";
    /** Ticks left in the warm-up that follows a load, so nothing starts before the world is ready. */
    private static final String TAG_WARMUP = "Warmup";''', 1)

# ---------------- fields ----------------
s = s.replace('''    @Nullable
    private Integer pendingChannel;
    @Nullable
    private Integer pendingMode;''',
'''    @Nullable
    private Integer pendingChannel;
    @Nullable
    private Integer pendingMode;

    /**
     * Ticks left before a reloaded machine may do anything.
     *
     * <p>A chunk comes back before its neighbours do: the ports, their block entities and the recipe
     * manager all settle over the first few ticks. Starting work in that window is what made a
     * reloaded machine swallow a set of ingredients and then find the saved craft could no longer be
     * resumed — the base tick runs before this class gets a look in, so it had already started
     * something. The wait is the fix: the machine reports itself as starting, and neither starts a
     * new craft nor resumes the saved one until everything around it has loaded.</p>
     */
    private int warmupTicks;''', 1)

# ---------------- save ----------------
s = s.replace('''                if (this.activeMode != null) {
                    tag.putInt(TAG_ACTIVE_MODE, this.activeMode);
                }''',
'''                if (this.activeMode != null) {
                    tag.putInt(TAG_ACTIVE_MODE, this.activeMode);
                }
            }
            tag.putInt(TAG_WARMUP, WARMUP_TICKS);''', 1)

# ---------------- load ----------------
s = s.replace('''        this.pendingChannel = tag.contains(TAG_ACTIVE_CHANNEL) ? tag.getInt(TAG_ACTIVE_CHANNEL) : null;
        this.pendingMode = tag.contains(TAG_ACTIVE_MODE) ? tag.getInt(TAG_ACTIVE_MODE) : null;''',
'''        this.pendingChannel = tag.contains(TAG_ACTIVE_CHANNEL) ? tag.getInt(TAG_ACTIVE_CHANNEL) : null;
        this.pendingMode = tag.contains(TAG_ACTIVE_MODE) ? tag.getInt(TAG_ACTIVE_MODE) : null;
        this.warmupTicks = tag.getInt(TAG_WARMUP);''', 1)

# ---------------- the gate ----------------
s = s.replace('''    private void resumePendingRecipe(Level level) {
        if (this.pendingRecipe == null) {
            return;
        }''',
'''    private void resumePendingRecipe(Level level) {
        // The warm-up gates both the resume and any fresh start: a machine that is still settling
        // must not spend ingredients on a craft it may be about to resume instead.
        if (this.warmupTicks > 0) {
            this.warmupTicks--;
            return;
        }
        if (this.pendingRecipe == null) {
            return;
        }''', 1)

# ---------------- status ----------------
s = s.replace('''        if (this.activeRecipe != null) {
            return GTReactorStatus.PROCESSING;
        }
        if (!this.running) {
            return this.stopReason;
        }''',
'''        if (this.activeRecipe != null) {
            return GTReactorStatus.PROCESSING;
        }
        if (!this.running) {
            return this.stopReason;
        }
        // Reported ahead of idle and of any refusal: during the warm-up the machine has simply not
        // decided anything yet, and saying "insufficient hatch" then would be a guess.
        if (this.warmupTicks > 0) {
            return GTReactorStatus.STARTING;
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
