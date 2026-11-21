import io, json

# ---------- 1. record the cheapest rejected recipe, not the costliest ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * The costliest rejected recipe of the last search, in FE per tick.
     *
     * <p>Kept so the status line can name a figure instead of just complaining. The largest one is
     * reported because it is the one the player most likely aimed at — with several candidates
     * rejected, the machine is short of that much power, not of the cheapest one's worth.</p>
     */
    private int refusedCost;''',
'''    /**
     * The cheapest rejected recipe of the last search, in FE per tick.
     *
     * <p>The smallest, not the largest: it is the cheapest one the player is closest to affording,
     * so naming it says how much more power is actually needed. Reporting a costly outlier would
     * suggest a bigger shortfall than the one that matters.</p>
     *
     * <p>Only meaningful when every matching recipe was rejected — if any was affordable the machine
     * runs that one instead, and this never surfaces.</p>
     */
    private int refusedCost;''', 1)

s = s.replace('''                this.refusedCost = Math.max(this.refusedCost, recipe.getEnergyPerTick());''',
'''                // Zero doubles as "nothing recorded yet", so the first rejection wins outright.
                this.refusedCost = this.refusedCost == 0
                        ? recipe.getEnergyPerTick()
                        : Math.min(this.refusedCost, recipe.getEnergyPerTick());''', 1)

s = s.replace('''    /** The costliest rejected recipe of the last search, in FE per tick; zero when none. */''',
'''    /** The cheapest rejected recipe of the last search, in FE per tick; zero when none. */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('machine:', 'changed' if s != orig else 'NO CHANGE')
