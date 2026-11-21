import io

# ---------- 1. machine: reachable == the ceiling, not a supply logarithm ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
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
    }''',
'''    /**
     * The most steps this machine may apply, before a recipe has a say.
     *
     * <p>Answered by the tier and the player's setting alone: {@code -1} means "as far as the tier
     * goes", anything else is the player's own ceiling. What a given recipe can actually afford is
     * a separate question, answered per recipe by {@link #overclockSteps(int)} — a step quadruples
     * the bill, so a costly recipe may only reach a fraction of this number.</p>
     */
    public int reachableOverclock() {
        int tierCeiling = maxOverclock();
        if (tierCeiling <= 0) {
            return 0;
        }
        int wanted = this.overclockLimit < 0 ? tierCeiling : this.overclockLimit;
        return Math.min(tierCeiling, Math.max(0, wanted));
    }

    /** The steps in force for whatever is running, or the ceiling while idle. */
    public int effectiveOverclock() {
        return this.activeRecipe == null
                ? reachableOverclock() : this.activeOverclock;
    }''', 1)

# overclockSteps(base) can now lean on reachableOverclock for the ceiling half
s = s.replace('''    public int overclockSteps(int baseCostPerTick) {
        int tierCeiling = maxOverclock();
        if (tierCeiling <= 0) {
            return 0;
        }
        int wanted = this.overclockLimit < 0 ? tierCeiling : this.overclockLimit;
        int bySetting = Math.min(tierCeiling, Math.max(0, wanted));

        // Walk down from the setting until the bill fits. Costs saturate, so this terminates.
        int ceiling = maxRecipeCost();
        int steps = bySetting;
        while (steps > 0 && overclockedCost(baseCostPerTick, steps) > ceiling) {
            steps--;
        }
        return steps;
    }''',
'''    public int overclockSteps(int baseCostPerTick) {
        // Walk down from the ceiling until the bill fits the hatches. Costs saturate, so this
        // terminates; the answer is 0 when even the base cost is out of reach.
        int ceiling = maxRecipeCost();
        int steps = reachableOverclock();
        while (steps > 0 && overclockedCost(baseCostPerTick, steps) > ceiling) {
            steps--;
        }
        return steps;
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('machine:', 'changed' if s != orig else 'NO CHANGE')

# ---------- 2. jade: cover the new status ----------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''            case STARTING, OTHER -> "gui.goldentweaks.chemical_reactor.status.other";''',
'''            case STARTING, OTHER -> "gui.goldentweaks.chemical_reactor.status.other";
            case INSUFFICIENT_ENERGY_HATCH ->
                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch";''', 1)
s2 = s2.replace('''            case OFF, NO_ENERGY, BROKEN_STRUCTURE, OTHER -> ChatFormatting.RED;''',
'''            case OFF, NO_ENERGY, BROKEN_STRUCTURE, OTHER, INSUFFICIENT_ENERGY_HATCH ->
                    ChatFormatting.RED;''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('jade:', 'changed' if s2 != orig2 else 'NO CHANGE')
