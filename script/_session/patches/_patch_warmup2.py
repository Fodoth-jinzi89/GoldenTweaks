import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------------- the constant ----------------
s = s.replace('''    /** Value of {@link #overclockLimit} meaning "no ceiling". */''',
'''    /**
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

    /** Value of {@link #overclockLimit} meaning "no ceiling". */''', 1)

# ---------------- gate the start of work ----------------
s = s.replace('''    @Override
    protected void tickRecipe(Level level) {
        if (this.structure == null) {
            return;
        }

        if (this.activeRecipe == null) {''',
'''    @Override
    protected void tickRecipe(Level level) {
        if (this.structure == null) {
            return;
        }

        // The base class ticks before this one does, so the warm-up has to be honoured here and not
        // only where the saved craft is resumed: this is the path that would otherwise start a fresh
        // recipe, take its ingredients, and leave the resumed craft unstartable.
        if (this.warmupTicks > 0) {
            return;
        }

        if (this.activeRecipe == null) {''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('warmup refs:', s.count('warmupTicks'))
