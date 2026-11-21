import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    private static final String TAG_OVERCLOCK_LIMIT = "OverclockLimit";''',
'''    private static final String TAG_OVERCLOCK_LIMIT = "OverclockLimit";
    /** Parallel ceiling; see {@link #ALWAYS_MAX_PARALLEL}. */
    private static final String TAG_PARALLEL_LIMIT = "ParallelLimit";''', 1)

s = s.replace('''    private int overclockLimit = NO_OVERCLOCK_LIMIT;
''',
'''    private int overclockLimit = NO_OVERCLOCK_LIMIT;

    /** Value of {@link #parallelLimit} meaning "always run at the maximum". */
    public static final int ALWAYS_MAX_PARALLEL = 0;

    /**
     * The ceiling on how many batches may run at once.
     *
     * <p>{@link #ALWAYS_MAX_PARALLEL} is the default and means no ceiling of its own: the machine
     * runs as many as its speed allows, which is what every machine did before this setting
     * existed.</p>
     */
    private int parallelLimit = ALWAYS_MAX_PARALLEL;

    /** The parallel ceiling; {@link #ALWAYS_MAX_PARALLEL} for no ceiling of its own. */
    public int getParallelLimit() {
        return this.parallelLimit;
    }

    /**
     * Stores the parallel ceiling.
     *
     * <p>Clamped rather than validated: a negative count has no meaning, so it collapses to the
     * "always maximum" value instead of being rejected.</p>
     */
    public void setParallelLimit(int limit) {
        int sane = Math.max(ALWAYS_MAX_PARALLEL, limit);
        if (this.parallelLimit == sane) {
            return;
        }
        this.parallelLimit = sane;
        setChanged();
        syncToClient();
    }
''', 1)

# persistence
s = s.replace('''        this.overclockLimit = tag.contains(TAG_OVERCLOCK_LIMIT)
                ? tag.getInt(TAG_OVERCLOCK_LIMIT)
                : NO_OVERCLOCK_LIMIT;
        // The shape has to be re-resolved after a load''',
'''        this.overclockLimit = tag.contains(TAG_OVERCLOCK_LIMIT)
                ? tag.getInt(TAG_OVERCLOCK_LIMIT)
                : NO_OVERCLOCK_LIMIT;
        this.parallelLimit = tag.contains(TAG_PARALLEL_LIMIT)
                ? tag.getInt(TAG_PARALLEL_LIMIT)
                : ALWAYS_MAX_PARALLEL;
        // The shape has to be re-resolved after a load''', 1)

s = s.replace('''        tag.putInt(TAG_OVERCLOCK_LIMIT, this.overclockLimit);
    }

    /**
     * AMI's update tag carries only the progress''',
'''        tag.putInt(TAG_OVERCLOCK_LIMIT, this.overclockLimit);
        tag.putInt(TAG_PARALLEL_LIMIT, this.parallelLimit);
    }

    /**
     * AMI's update tag carries only the progress''', 1)

s = s.replace('''        tag.putInt(TAG_OVERCLOCK_LIMIT, this.overclockLimit);
        return tag;''',
'''        tag.putInt(TAG_OVERCLOCK_LIMIT, this.overclockLimit);
        tag.putInt(TAG_PARALLEL_LIMIT, this.parallelLimit);
        return tag;''', 1)

s = s.replace('''        this.overclockLimit = tag.contains(TAG_OVERCLOCK_LIMIT)
                ? tag.getInt(TAG_OVERCLOCK_LIMIT)
                : NO_OVERCLOCK_LIMIT;
    }

    private void writeDisplayState''',
'''        this.overclockLimit = tag.contains(TAG_OVERCLOCK_LIMIT)
                ? tag.getInt(TAG_OVERCLOCK_LIMIT)
                : NO_OVERCLOCK_LIMIT;
        this.parallelLimit = tag.contains(TAG_PARALLEL_LIMIT)
                ? tag.getInt(TAG_PARALLEL_LIMIT)
                : ALWAYS_MAX_PARALLEL;
    }

    private void writeDisplayState''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('parallelLimit uses:', s.count('parallelLimit'))
