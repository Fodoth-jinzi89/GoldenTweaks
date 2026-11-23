"""Move the energy ladder to BigInteger.

`long` was a stopgap: it lasts to about level 30 and then saturates. With BigInteger the
curve is genuinely unbounded — the only conversion left is at the API boundary, where
NeoForge's IEnergyStorage insists on int.

The int accessors stay for those callers, saturating on the way out. Everything that
displays or multiplies uses the BigInteger forms.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortEnergyTiers.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;''',
'''import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;

import java.math.BigInteger;''', 1)

s = s.replace('''    /** FE per tick a port of this tier accepts and supplies, clamped for the energy API. */
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
    }

    /**
     * The same figure for one of our own tiers, clamped for the energy API.
     *
     * <p>Taken by level rather than through AMI's enum, which stops at IX: every port above UV
     * clamps to that tier, and six tiers answering with IX's rate would make the whole upper half
     * of the ladder pointless.</p>
     */
    public static int transfer(int level) {
        return clamp(transferLong(level));
    }''',
'''    /** FE per tick a port of this tier accepts and supplies, clamped for the energy API. */
    public static int transfer(MultiblockPortTier tier) {
        return clamp(transferExact(level(tier)));
    }

    /**
     * The same figure, with no ceiling.
     *
     * <p>{@code BigInteger} rather than {@code long}: the ladder is geometric and has no natural
     * end, and a width that runs out is exactly the plateau this table exists to avoid. The only
     * place a limit belongs is the energy API's {@code int}, which the callers that need it
     * request explicitly.</p>
     */
    public static BigInteger transferExact(int level) {
        return BASE_TRANSFER.multiply(power(STEP, Math.max(0, level - 1)));
    }

    /**
     * The same figure for one of our own tiers, clamped for the energy API.
     *
     * <p>Taken by level rather than through AMI's enum, which stops at IX: every port above UV
     * clamps to that tier, and six tiers answering with IX's rate would make the whole upper half
     * of the ladder pointless.</p>
     */
    public static int transfer(int level) {
        return clamp(transferExact(level));
    }''', 1)

s = s.replace('''    /** FE a port of this tier can hold. */
    public static int capacity(MultiblockPortTier tier) {
        return capacity(level(tier));
    }

    public static int capacity(int level) {
        return clamp(capacityLong(level));
    }

    /** The same figure without a ceiling. */
    public static long capacityLong(int level) {
        return saturatedMultiply(transferLong(level), CAPACITY_IN_TICKS);
    }''',
'''    /** FE a port of this tier can hold. */
    public static int capacity(MultiblockPortTier tier) {
        return capacity(level(tier));
    }

    public static int capacity(int level) {
        return clamp(capacityExact(level));
    }

    /** The same figure without a ceiling. */
    public static BigInteger capacityExact(int level) {
        return transferExact(level).multiply(BigInteger.valueOf(CAPACITY_IN_TICKS));
    }''', 1)

s = s.replace('''    public static int outputTransfer(MultiblockPortTier tier) {
        return Math.max(1, transfer(tier) / 2);
    }

    public static int outputTransfer(int level) {
        return clamp(outputTransferLong(level));
    }

    public static long outputTransferLong(int level) {
        return Math.max(1L, transferLong(level) / 2);
    }''',
'''    public static int outputTransfer(MultiblockPortTier tier) {
        return clamp(outputTransferExact(level(tier)));
    }

    public static int outputTransfer(int level) {
        return clamp(outputTransferExact(level));
    }

    public static BigInteger outputTransferExact(int level) {
        return halve(transferExact(level));
    }''', 1)

s = s.replace('''    public static int outputCapacity(MultiblockPortTier tier) {
        return Math.max(1, capacity(tier) / 2);
    }

    public static int outputCapacity(int level) {
        return clamp(outputCapacityLong(level));
    }

    public static long outputCapacityLong(int level) {
        return Math.max(1L, capacityLong(level) / 2);
    }''',
'''    public static int outputCapacity(MultiblockPortTier tier) {
        return clamp(outputCapacityExact(level(tier)));
    }

    public static int outputCapacity(int level) {
        return clamp(outputCapacityExact(level));
    }

    public static BigInteger outputCapacityExact(int level) {
        return halve(capacityExact(level));
    }

    /** Half, never below one. */
    private static BigInteger halve(BigInteger value) {
        BigInteger half = value.shiftRight(1);
        return half.signum() <= 0 ? BigInteger.ONE : half;
    }''', 1)

s = s.replace('''    /** {@code a × b}, saturated rather than wrapped. */
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
    }''',
'''    /** A figure as the {@code int} the energy API speaks, saturated rather than wrapped. */
    private static int clamp(BigInteger value) {
        return value.min(INT_MAX).max(BigInteger.ZERO).intValue();
    }

    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);''', 1)

s = s.replace('''    /**
     * {@code base} raised to {@code exponent}, saturating instead of overflowing.
     *
     * <p>The top tier is 64 × 4⁸, well inside an int, but a table someone extends later should
     * clamp rather than wrap around into a negative capacity.</p>
     */
    private static long power(long base, int exponent) {
        long value = 1L;
        for (int index = 0; index < exponent; index++) {
            if (value > Long.MAX_VALUE / base) {
                return Long.MAX_VALUE;
            }
            value *= base;
        }
        return value;
    }''',
'''    /** {@code base} raised to {@code exponent}, exactly. */
    private static BigInteger power(BigInteger base, int exponent) {
        return base.pow(exponent);
    }''', 1)

s = s.replace('''    /** Throughput of the lowest tier, in FE per tick. */
    private static final int BASE_TRANSFER = 64;''',
'''    /** Throughput of the lowest tier, in FE per tick. */
    private static final BigInteger BASE_TRANSFER = BigInteger.valueOf(64);''', 1)

s = s.replace('''    /** Multiple applied to each tier above the first. */
    private static final int STEP = 4;''',
'''    /** Multiple applied to each tier above the first. */
    private static final BigInteger STEP = BigInteger.valueOf(4);''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("energy ladder is BigInteger")
