import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * Whether a recipe can be powered at the overclock this machine would apply to it.
     *
     * <p>The comparison is against what a tick actually costs once overclocked, not the base figure:
     * overclocking quadruples the bill, so a recipe that fits at 0 steps may not fit at 1. That is
     * also what makes the steps worth having — a 32 FE/t recipe under a pair of tier I ports leaves
     * room for exactly one step, because 32 × 4 lands on the 128 ceiling.</p>
     *
     * <p>Checked before the craft starts rather than discovered mid-run: a recipe that cannot be
     * powered should never begin, because starting it consumes the inputs and then trips the breaker
     * on the first tick.</p>
     */
    public boolean withinEnergyBudget(int baseCostPerTick) {
        if (baseCostPerTick <= 0) {
            return true;
        }
        return overclockedCost(baseCostPerTick, overclockSteps()) <= maxRecipeCost();
    }''',
'''    /**
     * Whether a recipe's base cost fits what the energy ports can carry.
     *
     * <p>Compared against the base figure, not the overclocked one. Overclocking is the player's
     * choice to trade power for speed, so it must not decide which recipes exist for the machine —
     * judging by the boosted cost would leave a deliberately overclocked machine unable to start
     * anything at the top of its budget. The boosted bill is what the store has to sustain, and a
     * shortfall there trips the breaker as it would for any other power failure.</p>
     *
     * <p>Checked before the craft starts rather than discovered mid-run: a recipe that cannot be
     * powered should never begin, because starting it consumes the inputs and then trips the breaker
     * on the first tick.</p>
     */
    public boolean withinEnergyBudget(int baseCostPerTick) {
        return baseCostPerTick <= 0 || baseCostPerTick <= maxRecipeCost();
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
