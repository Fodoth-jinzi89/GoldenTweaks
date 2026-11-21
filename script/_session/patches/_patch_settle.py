import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    @Nullable
    private Integer pendingChannel;
    @Nullable
    private Integer pendingMode;''',
'''    @Nullable
    private Integer pendingChannel;
    @Nullable
    private Integer pendingMode;

    /**
     * Ticks a freshly loaded machine waits before the base tick may start anything.
     *
     * <p>Not the same thing as the resume: this guards against a <em>new</em> craft. A chunk comes
     * back before its neighbours, so for the first moments after a load the machine can look
     * assembled with ports that have not been read yet. Matching in that window takes ingredients
     * that the real contents would not have paid for.</p>
     */
    private int settleTicks;''', 1)

s = s.replace('''        this.pendingMode = tag.contains(TAG_ACTIVE_MODE) ? tag.getInt(TAG_ACTIVE_MODE) : null;''',
'''        this.pendingMode = tag.contains(TAG_ACTIVE_MODE) ? tag.getInt(TAG_ACTIVE_MODE) : null;
        // Set unconditionally and never read back from the tag: waiting is what a load does, not
        // something a save records, and an older save has no field to record it in.
        this.settleTicks = SETTLE_TICKS;''', 1)

s = s.replace('''    /** Value of {@link #overclockLimit} meaning "no ceiling". */''',
'''    /**
     * How long a reloaded machine leaves its ports alone, in ticks.
     *
     * <p>Two seconds. Short on purpose: it only has to outlast the chunk and neighbour restore, not
     * be a visible pause. The saved craft is resumed ahead of the base tick and does not wait for
     * this; only starting something <em>new</em> does.</p>
     */
    private static final int SETTLE_TICKS = 40;

    /** Value of {@link #overclockLimit} meaning "no ceiling". */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('settleTicks refs:', s.count('settleTicks'))
