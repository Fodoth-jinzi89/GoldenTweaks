"""Remove the growth ceiling from the energy tiers.

`power()` stopped at the first overflow, so from UXV up the rate came out as
Integer.MAX_VALUE and a dynamo's half of it as a capacity that read back as almost nothing.

The interface stays `int` — that is NeoForge's energy API and cannot be widened — but the
numbers behind it are now `long`, and every conversion back to `int` saturates. The curve
itself has no ceiling: it keeps multiplying by four for as long as anyone adds a tier.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortEnergyTiers.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /** FE per tick a port of this tier accepts and supplies. */
    public static int transfer(MultiblockPortTier tier) {
        return transfer(level(tier));
    }''',
'''    /** FE per tick a port of this tier accepts and supplies, clamped for the energy API. */
    public static int transfer(MultiblockPortTier tier) {
        return clamp(transferLong(level(tier)));
    }

    /**
     * The same figure without a ceiling.
     *
     * <p>{@code long} because the ladder outgrows {@code int} at UXV and the table must keep
     * climbing: 64 × 4ⁿ reaches 2⁶³ around level 30, which is far past the last tier and leaves the
     * whole ladder exact instead of topping out in a saturated plateau.</p>
     */
    public static long transferLong(int level) {
        return BASE_TRANSFER * power(STEP, Math.max(0, level - 1));
    }''', 1)

s = s.replace('''    public static int transfer(int level) {
        return BASE_TRANSFER * power(STEP, Math.max(0, level - 1));
    }''', '')

s = s.replace('''    public static int capacity(int level) {
        return saturatedMultiply(transfer(level), CAPACITY_IN_TICKS);
    }''',
'''    public static int capacity(int level) {
        return clamp(capacityLong(level));
    }

    /** The same figure without a ceiling. */
    public static long capacityLong(int level) {
        return saturatedMultiply(transferLong(level), CAPACITY_IN_TICKS);
    }''', 1)

s = s.replace('''    public static int outputTransfer(int level) {
        return Math.max(1, transfer(level) / 2);
    }''',
'''    public static int outputTransfer(int level) {
        return clamp(outputTransferLong(level));
    }

    public static long outputTransferLong(int level) {
        return Math.max(1L, transferLong(level) / 2);
    }''', 1)

s = s.replace('''    public static int outputCapacity(int level) {
        return Math.max(1, capacity(level) / 2);
    }''',
'''    public static int outputCapacity(int level) {
        return clamp(outputCapacityLong(level));
    }

    public static long outputCapacityLong(int level) {
        return Math.max(1L, capacityLong(level) / 2);
    }''', 1)

s = s.replace('''    /** {@code a × b}, saturated rather than wrapped. */
    private static int saturatedMultiply(int a, int b) {
        long product = (long) a * b;
        return product > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) product;
    }''',
'''    /** {@code a × b}, saturated rather than wrapped. */
    private static long saturatedMultiply(long a, long b) {
        long product = a * b;
        // Overflow wrapped the sign; fall back to the ceiling instead of returning a negative
        // capacity, which the storage would read as "full".
        if (a != 0 && (product / a != b || product < 0)) {
            return Long.MAX_VALUE;
        }
        return product;
    }

    /** A {@code long} figure as the {@code int} the energy API speaks, saturated. */
    private static int clamp(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.max(0, value);
    }''', 1)

s = s.replace('''    private static int power(int base, int exponent) {
        long value = 1L;
        for (int index = 0; index < exponent; index++) {
            value *= base;
            if (value > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) value;
    }''',
'''    private static long power(long base, int exponent) {
        long value = 1L;
        for (int index = 0; index < exponent; index++) {
            if (value > Long.MAX_VALUE / base) {
                return Long.MAX_VALUE;
            }
            value *= base;
        }
        return value;
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("energy curve is unbounded in long")
