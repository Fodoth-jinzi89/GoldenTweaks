import io, json

# ---------- machine: expose the reachable ceiling ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** The levels the running craft uses, or the ceiling when nothing is running. */''',
'''    /**
     * The highest overclock the hatches can support, as a recipe-independent figure.
     *
     * <p>This is what the side pane reports: how many steps the machine could reach at all, given
     * its energy hatches. A lone top-tier hatch only affords half of what a pair does, so the count
     * drops by one when a second is removed.</p>
     *
     * <p>Steps are counted against the supply ceiling rather than the tier alone, because the two
     * coincide by construction: each step quadruples the bill, and the dual-hatch ceiling is twice a
     * single hatch's rate — so the reachable count is exactly the log of that doubling.</p>
     */
    public int reachableOverclock() {
        int tierCeiling = maxOverclock();
        if (tierCeiling <= 0) {
            return 0;
        }
        int wanted = this.overclockLimit < 0 ? tierCeiling : this.overclockLimit;
        int bySetting = Math.min(tierCeiling, Math.max(0, wanted));

        // Base the walk on the cheapest possible step cost, one FE per tick: the ceiling is a power
        // of four and the ports' supply is too, so this reduces to "how many doublings fit".
        int supply = maxRecipeCost();
        if (supply <= 0) {
            return 0;
        }
        int steps = 0;
        long bill = 1L;
        while (steps < bySetting) {
            bill *= 4L;
            if (bill > supply) {
                break;
            }
            steps++;
        }
        return steps;
    }

    /** The levels the running craft uses, or the ceiling when nothing is running. */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('machine:', 'changed' if s != orig else 'NO CHANGE')

# ---------- menu ----------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''    /**
     * The overclock levels actually in force.
     *
     * <p>Not the same as {@link #getOverclockLimit}: the setting may say automatic, and either way
     * the energy ports cap it. The side pane shows this one, because it is what the machine runs
     * at and what the cost is billed against.</p>
     */
    public int getOverclockSteps() {
        return this.tile == null ? 0 : this.tile.overclockSteps();
    }''',
'''    /**
     * The overclock levels actually in force.
     *
     * <p>Not the same as {@link #getOverclockLimit}: the setting may say automatic, and either way
     * the energy ports cap it. The side pane shows this one, because it is what the machine runs
     * at and what the cost is billed against.</p>
     */
    public int getOverclockSteps() {
        return this.tile == null ? 0 : this.tile.overclockSteps();
    }

    /** The highest overclock this machine's energy hatches can support at all. */
    public int getReachableOverclock() {
        return this.tile == null ? 0 : this.tile.reachableOverclock();
    }''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('menu:', 'changed' if s2 != orig2 else 'NO CHANGE')
