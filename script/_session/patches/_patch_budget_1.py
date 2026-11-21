import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * Overclock levels this machine should run at.''',
'''    /**
     * The ceiling on a recipe's base energy cost, in FE per tick.
     *
     * <p>"Dual hatch boost": two energy input ports of the highest tier installed can be drawn from
     * together at twice one of them, so a pair of tier I ports supports a 128 FE/t recipe while a
     * lone tier I port only supports 32.</p>
     *
     * <p>The lone-port figure is deliberately not the port's own rate: with nothing to draw against,
     * the machine may only run a recipe at half that rate, which leaves the other half of the tick's
     * budget for what the craft itself does. A second port of the same tier unlocks the full
     * doubling.</p>
     */
    public int maxRecipeCost() {
        if (this.structure == null || this.level == null) {
            return 0;
        }

        // Highest tier present among the energy input ports, and how many of that tier there are.
        // Anything below the top tier contributes nothing: it cannot carry a recipe the top one
        // could not, and counting it would let a pile of cheap ports beat one good one.
        int best = 0;
        int bestCount = 0;
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.ENERGY) {
                continue;
            }
            if (!(this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            int tier = entity.tier().ordinal() + 1;
            if (tier > best) {
                best = tier;
                bestCount = 1;
            } else if (tier == best) {
                bestCount++;
            }
        }
        if (best == 0) {
            return 0;
        }

        int single = GTPortEnergyTiers.transfer(tierOf(best));
        // Two or more of the top tier double it; a lone one only reaches half of that doubling.
        return bestCount >= 2 ? single * 2 : single / 2;
    }

    /** The tier enum for a one-based level, clamped into the table's range. */
    private static MultiblockPortTier tierOf(int level) {
        MultiblockPortTier[] tiers = MultiblockPortTier.values();
        int index = Math.max(0, Math.min(tiers.length - 1, level - 1));
        return tiers[index];
    }

    /**
     * Whether a recipe's base cost fits within what the energy ports can carry.
     *
     * <p>Checked before the craft starts rather than discovered mid-run: a recipe that cannot be
     * powered should never begin, because starting it consumes the inputs and then trips the breaker
     * on the first tick.</p>
     */
    public boolean withinEnergyBudget(int baseCostPerTick) {
        return baseCostPerTick <= 0 || baseCostPerTick <= maxRecipeCost();
    }

    /**
     * Overclock levels this machine should run at.''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
