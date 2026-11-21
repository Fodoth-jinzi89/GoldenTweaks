import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()

# ---- the gate inside tickRecipe ----
s = s.replace('''        // The base class ticks before this one does, so the warm-up has to be honoured here and not
        // only where the saved craft is resumed: this is the path that would otherwise start a fresh
        // recipe, take its ingredients, and leave the resumed craft unstartable.
        if (this.warmupTicks > 0) {
            return;
        }

''', '')

# ---- the gate inside resumePendingRecipe ----
s = s.replace('''        // The warm-up gates both the resume and any fresh start: a machine that is still settling
        // must not spend ingredients on a craft it may be about to resume instead.
        if (this.warmupTicks > 0) {
            this.warmupTicks--;
            return;
        }
''', '')

# ---- the status branch ----
s = s.replace('''        // Reported ahead of idle and of any refusal: during the warm-up the machine has simply not
        // decided anything yet, and saying "insufficient hatch" then would be a guess.
        if (this.warmupTicks > 0) {
            return GTReactorStatus.STARTING;
        }
''', '')

# ---- the load assignment ----
s = s.replace('''        // Unconditional, and not read from the tag: this is what a load *is*, not something a save
        // records. An older save has no field for it and must still get the wait.
        this.warmupTicks = WARMUP_TICKS;
''', '')

# ---- the field ----
s = s.replace('''
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
    private int warmupTicks;
''', '')

# ---- the constant ----
s = s.replace('''    /**
     * How long a reloaded machine waits before it touches its ports.
     *
     * <p>Five seconds. A chunk is restored before its neighbours, and the ports that give the
     * machine its inputs are separate block entities in maybe-separate chunks — so early in a load
     * the machine can look complete while its ports are still empty, which reads either as "no
     * ingredients" or, worse, matches a recipe and takes the ingredients before the real contents
     * arrive. Nothing about that is visible in the same session, which is why the wait has to be
     * time rather than a condition.</p>
     */
    private static final int WARMUP_TICKS = 100;

''', '')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('warmupTicks left:', s.count('warmupTicks'))
print('WARMUP left:', s.count('WARMUP_TICKS'))
print('STARTING uses:', s.count('GTReactorStatus.STARTING'))
