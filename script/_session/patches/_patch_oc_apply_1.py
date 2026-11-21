import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------- pinned overclock for the running craft ----------
s = s.replace('''    /** The mode pinned for the running craft; {@code null} when none. */
    @Nullable
    private Integer activeMode;''',
'''    /** The mode pinned for the running craft; {@code null} when none. */
    @Nullable
    private Integer activeMode;

    /**
     * Overclock levels this craft runs at, pinned when it started.
     *
     * <p>Pinned rather than read live: the limit comes from the energy ports, and a player adding a
     * port mid-craft must not change how much power the run in flight is billed.</p>
     */
    private int activeOverclock;''', 1)

# ---------- the overclock maths ----------
s = s.replace('''    /** How far this machine may overclock; {@link #NO_OVERCLOCK_LIMIT} for no ceiling. */
    public int getOverclockLimit() {
        return this.overclockLimit;
    }''',
'''    /** How far this machine may overclock; {@link #NO_OVERCLOCK_LIMIT} for no ceiling. */
    public int getOverclockLimit() {
        return this.overclockLimit;
    }

    /**
     * The highest energy port tier present, as a one-based level.
     *
     * <p>This is what caps overclocking: a machine is only as fast as the best energy hatch it can
     * draw from. Zero when there is no energy input port at all.</p>
     */
    public int maxOverclock() {
        if (this.structure == null || this.level == null) {
            return 0;
        }
        int best = 0;
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.ENERGY) {
                continue;
            }
            if (this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
                best = Math.max(best, entity.tier().ordinal() + 1);
            }
        }
        return best;
    }

    /**
     * Overclock levels this machine should run at.
     *
     * <p>{@link #NO_OVERCLOCK_LIMIT} means "as far as the ports allow"; any other value is the
     * player's own ceiling, and either way it never exceeds what the energy ports can carry.</p>
     */
    public int overclockSteps() {
        int ceilingForPorts = maxOverclock();
        if (ceilingForPorts <= 0) {
            return 0;
        }
        int wanted = this.overclockLimit < 0 ? ceilingForPorts : this.overclockLimit;
        return Math.min(ceilingForPorts, Math.max(0, wanted));
    }

    /**
     * Energy one tick costs at {@code steps} overclocks.
     *
     * <p>Each step quadruples the rate while halving the run time, so one craft costs twice as much
     * in total. Saturated rather than wrapped: a high tier with a costly recipe can reach the int
     * limit, and a negative cost would credit the machine instead of billing it.</p>
     */
    private static int overclockedCost(int baseCost, int steps) {
        long cost = baseCost;
        for (int step = 0; step < steps; step++) {
            cost *= 4L;
            if (cost >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) cost;
    }

    /**
     * Run time in ticks at {@code steps} overclocks, never below one.
     *
     * <p>The floor is the whole point of the "more overclock only costs more" rule: once a craft is
     * down to a single tick there is nothing left to halve, so further steps keep raising the bill
     * and leave the speed alone.</p>
     */
    private static int overclockedLength(int baseLength, int steps) {
        int length = baseLength;
        for (int step = 0; step < steps; step++) {
            length = Math.max(1, length >> 1);
        }
        return Math.max(1, length);
    }

    /** Applies the pinned overclock to a freshly started craft. */
    private void applyOverclock(MatchedMultiblockRecipe recipe) {
        this.activeOverclock = overclockSteps();
        this.maxProgress = overclockedLength(recipe.processingTime(), this.activeOverclock);
        this.progress = 0;
        this.setChanged();
        syncToClient();
    }''', 1)

# ---------- bill the overclocked rate ----------
s = s.replace('''        int cost = this.activeRecipe.energyPerTick();
        if (cost > 0 && !consumeEnergy(cost)) {
            tripBreaker();
            return;
        }''',
'''        // Billed at the rate pinned when the craft started, which is what the overclock bought.
        int cost = overclockedCost(this.activeRecipe.energyPerTick(), this.activeOverclock);
        if (cost > 0 && !consumeEnergy(cost)) {
            tripBreaker();
            return;
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
