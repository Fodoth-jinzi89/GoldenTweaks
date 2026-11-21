import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- tag key ----
s = s.replace('''    private static final String TAG_RUNNING = "Running";''',
'''    private static final String TAG_RUNNING = "Running";
    /** How far this machine may overclock; see {@link #NO_OVERCLOCK_LIMIT}. */
    private static final String TAG_OVERCLOCK_LIMIT = "OverclockLimit";''', 1)

# ---- the field, next to the other switch state ----
s = s.replace('''    private boolean running;

    /**
     * True when this entity was just read back from disk''',
'''    private boolean running;

    /** Value of {@link #overclockLimit} meaning "no ceiling". */
    public static final int NO_OVERCLOCK_LIMIT = -1;

    /**
     * How far this machine may overclock, as the ratio's denominator.
     *
     * <p>{@link #NO_OVERCLOCK_LIMIT} means unlimited, which is also the default: a machine nobody
     * configured behaves exactly as it did before the setting existed.</p>
     */
    private int overclockLimit = NO_OVERCLOCK_LIMIT;

    /**
     * True when this entity was just read back from disk''', 1)

# ---- accessors ----
s = s.replace('''    /** Whether the machine is switched on, as the GUI switch sees it.''',
'''    /** How far this machine may overclock; {@link #NO_OVERCLOCK_LIMIT} for no ceiling. */
    public int getOverclockLimit() {
        return this.overclockLimit;
    }

    /**
     * Stores the overclock ceiling.
     *
     * <p>Clamped rather than validated: anything below {@code -1} has no meaning, so it collapses to
     * the unlimited value instead of being rejected — a typo in the box should leave the machine
     * usable, not silently inert.</p>
     */
    public void setOverclockLimit(int limit) {
        int sane = limit < NO_OVERCLOCK_LIMIT ? NO_OVERCLOCK_LIMIT : limit;
        if (this.overclockLimit == sane) {
            return;
        }
        this.overclockLimit = sane;
        setChanged();
        syncToClient();
    }

    /** Whether the machine is switched on, as the GUI switch sees it.''', 1)

# ---- persistence ----
s = s.replace('''        this.running = tag.getBoolean(TAG_RUNNING);
        // The shape has to be re-resolved after a load, and that scan must not switch the machine on.''',
'''        this.running = tag.getBoolean(TAG_RUNNING);
        this.overclockLimit = tag.contains(TAG_OVERCLOCK_LIMIT)
                ? tag.getInt(TAG_OVERCLOCK_LIMIT)
                : NO_OVERCLOCK_LIMIT;
        // The shape has to be re-resolved after a load, and that scan must not switch the machine on.''', 1)

s = s.replace('''        tag.putBoolean(TAG_RUNNING, this.running);
    }

    /**
     * AMI's update tag carries only the progress''',
'''        tag.putBoolean(TAG_RUNNING, this.running);
        tag.putInt(TAG_OVERCLOCK_LIMIT, this.overclockLimit);
    }

    /**
     * AMI's update tag carries only the progress''', 1)

# ---- client sync ----
s = s.replace('''        tag.putBoolean(TAG_RUNNING, this.running);
        return tag;''',
'''        tag.putBoolean(TAG_RUNNING, this.running);
        tag.putInt(TAG_OVERCLOCK_LIMIT, this.overclockLimit);
        return tag;''', 1)

s = s.replace('''        this.running = tag.getBoolean(TAG_RUNNING);
    }

    private void writeDisplayState''',
'''        this.running = tag.getBoolean(TAG_RUNNING);
        this.overclockLimit = tag.contains(TAG_OVERCLOCK_LIMIT)
                ? tag.getInt(TAG_OVERCLOCK_LIMIT)
                : NO_OVERCLOCK_LIMIT;
    }

    private void writeDisplayState''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('occurrences:', s.count('overclockLimit'))
