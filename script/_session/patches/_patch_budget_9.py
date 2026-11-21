import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- 1. the field the last patch failed to insert ----
if 'private boolean refusedForCost;' not in s:
    anchor = '''    /**
     * The ceiling on a recipe's base energy cost, in FE per tick.'''
    s = s.replace(anchor,
'''    /** Set when the last search skipped at least one recipe for cost alone. */
    private boolean refusedForCost;

''' + anchor, 1)

# ---- 2. steps become recipe-aware: the supply ceiling is the real cap ----
s = s.replace('''    /**
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
    }''',
'''    /**
     * Overclock levels this machine should run a recipe at.
     *
     * <p>Three limits apply and the smallest wins. The tier ceiling says how far this class of
     * machine can overclock at all; the player's setting says how far they want to; and the supply
     * ceiling says how far the hatches can actually pay for — a step quadruples the bill, so a
     * recipe two steps up would draw {@code 16 ×} its base cost, which the ports may not carry.</p>
     *
     * <p>That last limit is what ties the pane's number to the hatches: with a pair of tier I ports
     * the supply ceiling is 128 FE/t, so a 128 FE/t recipe affords no step and a 32 FE/t recipe
     * affords exactly one, because {@code 32 × 4 = 128}.</p>
     */
    public int overclockSteps(int baseCostPerTick) {
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
    }

    /** The levels the running craft uses, or the ceiling when nothing is running. */
    public int overclockSteps() {
        int base = this.activeRecipe == null ? 0 : this.activeRecipe.energyPerTick();
        return base > 0 ? overclockSteps(base) : maxOverclock();
    }''', 1)

# ---- 3. the start path uses the recipe's own cost ----
s = s.replace('''    private void applyOverclock(MatchedMultiblockRecipe recipe) {
        this.activeOverclock = overclockSteps();''',
'''    private void applyOverclock(MatchedMultiblockRecipe recipe) {
        this.activeOverclock = overclockSteps(recipe.energyPerTick());''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('machine:', 'changed' if s != orig else 'NO CHANGE')

# ---- 4. exhaustive switch over the new status ----
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''            // STARTING is reserved for later; until something sets it, it reads as the idle case.
            case STARTING, OTHER ->
                    Component.translatable("gui.goldentweaks.chemical_reactor.status.other");''',
'''            // STARTING is reserved for later; until something sets it, it reads as the idle case.
            case STARTING, OTHER ->
                    Component.translatable("gui.goldentweaks.chemical_reactor.status.other");
            case INSUFFICIENT_ENERGY_HATCH -> Component.translatable(
                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch");''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('screen:', 'changed' if s2 != orig2 else 'NO CHANGE')
