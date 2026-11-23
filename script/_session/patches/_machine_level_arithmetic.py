"""Make the machine's own tier arithmetic use our thirteen-level ladder.

`maxRecipeCost` went through AMI's enum, which clamps everything above UV to IX — so a UHV
hatch and a UV one would report the same ceiling. Passing the level directly keeps the
ladder climbing, and the now-unused `tierOf` helper goes away with it.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''        int single = GTPortEnergyTiers.transfer(tierOf(census.topTier()));''',
'''        int single = GTPortEnergyTiers.transfer(census.topTier());''', 1)

s = s.replace('''    /** The tier enum for a one-based level, clamped into the table's range. */
    private static MultiblockPortTier tierOf(int level) {
        MultiblockPortTier[] tiers = MultiblockPortTier.values();
        int index = Math.max(0, Math.min(tiers.length - 1, level - 1));
        return tiers[index];
    }

''', '')

s = s.replace('''     * <p>Capped at the top of the ladder: the boost cannot invent a tier above UV.</p>''',
'''     * <p>Capped at the top of the ladder: the boost cannot invent a tier above MAX.</p>''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("machine tier arithmetic updated")
print("tierOf refs left:", s.count("tierOf("))
