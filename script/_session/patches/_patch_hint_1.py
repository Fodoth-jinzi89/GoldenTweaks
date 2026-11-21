import io, json

# ---------- machine: record the most demanding refused recipe ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** Set when the last search skipped at least one recipe for cost alone. */
    private boolean refusedForCost;''',
'''    /** Set when the last search skipped at least one recipe for cost alone. */
    private boolean refusedForCost;

    /**
     * The costliest rejected recipe of the last search, in FE per tick.
     *
     * <p>Kept so the status line can name a figure instead of just complaining. The largest one is
     * reported because it is the one the player most likely aimed at — with several candidates
     * rejected, the machine is short of that much power, not of the cheapest one's worth.</p>
     */
    private int refusedCost;''', 1)

s = s.replace('''                this.refusedForCost = true;
                continue;''',
'''                this.refusedForCost = true;
                this.refusedCost = Math.max(this.refusedCost, recipe.getEnergyPerTick());
                continue;''', 1)

s = s.replace('''        this.refusedForCost = false;
        for (int channel : channelGroups()) {''',
'''        this.refusedForCost = false;
        this.refusedCost = 0;
        for (int channel : channelGroups()) {''', 1)

s = s.replace('''    /** The most a rejected recipe of the last search asked for, or zero. */
''', '')
s = s.replace('''    /** Whether a recipe's base cost fits what the energy ports can carry.''',
'''    /** The costliest rejected recipe of the last search, in FE per tick; zero when none. */
    public int refusedCost() {
        return this.refusedCost;
    }

    /** Whether a recipe's base cost fits what the energy ports can carry.''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('machine:', 'changed' if s != orig else 'NO CHANGE')
