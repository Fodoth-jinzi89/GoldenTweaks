"""Make the energy table read our own tier level, not AMI's ordinal.

AMI's enum stops at IX, so every port above UV clamps to it and would share IX's numbers —
six tiers all supplying the same 16 M FE/t. Ours has thirteen levels, so the curve simply
keeps going up.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortEnergyTiers.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;''',
'''import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;''', 1)

s = s.replace('''    /** FE per tick a port of this tier accepts and supplies. */
    public static int transfer(MultiblockPortTier tier) {
        return BASE_TRANSFER * power(STEP, level(tier));
    }''',
'''    /** FE per tick a port of this tier accepts and supplies. */
    public static int transfer(MultiblockPortTier tier) {
        return transfer(level(tier));
    }

    /**
     * The same figure for one of our own tiers.
     *
     * <p>Taken by level rather than through AMI's enum, which stops at IX: every port above UV
     * clamps to that tier, and six tiers answering with IX's rate would make the whole upper half
     * of the ladder pointless.</p>
     */
    public static int transfer(int level) {
        return BASE_TRANSFER * power(STEP, Math.max(0, level - 1));
    }''', 1)

s = s.replace('''    /** FE a port of this tier can hold. */
    public static int capacity(MultiblockPortTier tier) {
        return transfer(tier) * CAPACITY_IN_TICKS;
    }''',
'''    /** FE a port of this tier can hold. */
    public static int capacity(MultiblockPortTier tier) {
        return capacity(level(tier));
    }

    public static int capacity(int level) {
        return saturatedMultiply(transfer(level), CAPACITY_IN_TICKS);
    }''', 1)

s = s.replace('''    public static int outputTransfer(MultiblockPortTier tier) {
        return Math.max(1, transfer(tier) / 2);
    }''',
'''    public static int outputTransfer(MultiblockPortTier tier) {
        return Math.max(1, transfer(tier) / 2);
    }

    public static int outputTransfer(int level) {
        return Math.max(1, transfer(level) / 2);
    }''', 1)

s = s.replace('''    public static int outputCapacity(MultiblockPortTier tier) {
        return Math.max(1, capacity(tier) / 2);
    }''',
'''    public static int outputCapacity(MultiblockPortTier tier) {
        return Math.max(1, capacity(tier) / 2);
    }

    public static int outputCapacity(int level) {
        return Math.max(1, capacity(level) / 2);
    }

    /** {@code a × b}, saturated rather than wrapped. */
    private static int saturatedMultiply(int a, int b) {
        long product = (long) a * b;
        return product > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) product;
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("energy table extended")
