import io

# ---------------- machine: the boost-aware tier ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** The tier enum for a one-based level, clamped into the table's range. */''',
'''    /**
     * The tier the machine effectively runs at, with the dual-hatch boost counted.
     *
     * <p>A lone top-tier hatch is only allowed half of its own rate, while a pair reaches the full
     * doubling — which is exactly one tier's worth of throughput. Reporting the effective tier is
     * what makes the number in the tooltip agree with the power figure beside it: a pair of tier I
     * hatches reads as II, because that is what it supplies.</p>
     *
     * <p>Capped at the top of the ladder: the boost cannot invent a tier above UV.</p>
     */
    public int effectiveEnergyTier() {
        EnergyCensus census = energyPortCensus();
        if (census.topTier() == 0) {
            return 0;
        }
        int tier = census.topTier();
        if (census.topCount() >= 2) {
            tier++;
        } else {
            tier--;
        }
        return Math.max(1, Math.min(MultiblockPortTier.values().length, tier));
    }

    /** The tier enum for a one-based level, clamped into the table's range. */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('machine:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- provider: send the effective tier ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeProvider.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''        root.putInt(ENERGY_TIER, machine.maxOverclock());''',
'''        // The effective tier, not the raw one: a dual-hatch pair supplies a tier's worth more, and
        // the line sits directly above the power figure it has to agree with.
        root.putInt(ENERGY_TIER, machine.effectiveEnergyTier());''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('provider:', 'changed' if s2 != orig2 else 'NO CHANGE')
