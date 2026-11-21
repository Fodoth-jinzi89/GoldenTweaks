import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# Replace both ad-hoc scans with one shared census.
s = s.replace('''    public int maxOverclock() {
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
    }''',
'''    public int maxOverclock() {
        return energyPortCensus().topTier();
    }

    /**
     * The highest energy input port tier, and how many ports share it.
     *
     * <p>Both the overclock ceiling and the energy budget read this, so it is computed once per
     * query rather than twice. Anything below the top tier contributes nothing to either: it cannot
     * carry what the top one cannot, and counting it would let a pile of cheap ports beat one good
     * one.</p>
     */
    private record EnergyCensus(int topTier, int topCount) {
        static final EnergyCensus NONE = new EnergyCensus(0, 0);
    }

    private EnergyCensus energyPortCensus() {
        if (this.structure == null || this.level == null) {
            return EnergyCensus.NONE;
        }
        int top = 0;
        int count = 0;
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.ENERGY) {
                continue;
            }
            if (!(this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            int tier = entity.tier().ordinal() + 1;
            if (tier > top) {
                top = tier;
                count = 1;
            } else if (tier == top) {
                count++;
            }
        }
        return new EnergyCensus(top, count);
    }''', 1)

s = s.replace('''    public int maxRecipeCost() {
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
    }''',
'''    public int maxRecipeCost() {
        EnergyCensus census = energyPortCensus();
        if (census.topTier() == 0) {
            return 0;
        }
        int single = GTPortEnergyTiers.transfer(tierOf(census.topTier()));
        // Two or more of the top tier double it; a lone one only reaches half of that doubling.
        return census.topCount() >= 2 ? single * 2 : single / 2;
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
