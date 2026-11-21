import io

# ---------------- the accessor ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    private void completeRecipe() {''',
'''    /**
     * The power the running craft actually draws each tick, after its overclock.
     *
     * <p>The figure the machine is really billed, not the recipe's authored cost: a craft at two
     * overclock steps pays sixteen times its base rate, and a tooltip quoting the base would be off
     * by that factor on every overclocked run. Zero while nothing is running.</p>
     */
    public int activePowerDraw() {
        if (this.activeRecipe == null) {
            return 0;
        }
        return overclockedCost(this.activeRecipe.energyPerTick(), this.activeOverclock);
    }

    private void completeRecipe() {''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- provider ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeProvider.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''    /** Cost of the cheapest recipe the last search turned down, in FE per tick. */''',
'''    /** What the running craft draws each tick after its overclock, in FE per tick. */
    static final String POWER = "Power";
    /** Cost of the cheapest recipe the last search turned down, in FE per tick. */''', 1)
s2 = s2.replace('''        root.putInt(RATE, machine.getInputEnergyRate());''',
'''        root.putInt(RATE, machine.getInputEnergyRate());
        // Next to the hatch rate on purpose: this is what the craft actually costs, and the two are
        // what a player compares when deciding whether the hatches can carry it.
        root.putInt(POWER, machine.activePowerDraw());''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('provider:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---------------- component ----------------
p3 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s3 = io.open(p3, encoding='utf-8').read()
orig3 = s3
s3 = s3.replace('''        if (root.getInt(GTReactorJadeProvider.RATE) > 0) {''',
'''        // Only while a craft runs: an idle machine draws the hatch rate at most, and stating a power
        // figure for a reaction that is not happening would be a guess.
        int power = root.getInt(GTReactorJadeProvider.POWER);
        if (power > 0) {
            tooltip.add(Component.translatable("gui.goldentweaks.jade.power",
                    GTEnergyFormat.format(power)).withStyle(ChatFormatting.GREEN));
        }

        if (root.getInt(GTReactorJadeProvider.RATE) > 0) {''', 1)
io.open(p3, 'w', encoding='utf-8', newline='\n').write(s3)
print('component:', 'changed' if s3 != orig3 else 'NO CHANGE')
