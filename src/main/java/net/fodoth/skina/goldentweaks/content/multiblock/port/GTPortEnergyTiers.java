package net.fodoth.skina.goldentweaks.content.multiblock.port;

import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;

import java.math.BigInteger;

/**
 * Capacity and throughput of an energy port, per tier.
 *
 * <p>The tiers are AMI's own — {@code I} through {@code IX} — so a machine's structure matching,
 * which counts ports by tier, stays untouched. Only the numbers are ours.</p>
 *
 * <p>AMI's table is linear: 100, 400, 800, 1200 … 300. That made the cheap tiers unusable with
 * recipes costing a few hundred FE per tick and gave no meaningful headroom further up, so the
 * curve is geometric here instead: tier I is 64 FE/t, and every tier above it multiplies by four.
 * Capacity always holds {@value #CAPACITY_IN_TICKS} ticks' worth of that rate, so a port can run a
 * recipe of that length on its own store.</p>
 */
public final class GTPortEnergyTiers {

    /** Multiple applied to each tier above the first. */
    private static final BigInteger STEP = BigInteger.valueOf(4);

    /** Ticks of full-rate input a port can hold. */
    private static final int CAPACITY_IN_TICKS = 20;

    /** Throughput of the lowest tier, in FE per tick. */
    private static final BigInteger BASE_TRANSFER = BigInteger.valueOf(64);

    private GTPortEnergyTiers() {
    }

    /** FE per tick a port of this tier accepts and supplies, clamped for the energy API. */
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
    }

    /** FE a port of this tier can hold. */
    public static int capacity(MultiblockPortTier tier) {
        return capacity(level(tier));
    }

    public static int capacity(int level) {
        return clamp(capacityExact(level));
    }

    /** The same figure without a ceiling. */
    public static BigInteger capacityExact(int level) {
        return transferExact(level).multiply(BigInteger.valueOf(CAPACITY_IN_TICKS));
    }

    /**
     * The same figures for a dynamo hatch, at half the rate and half the store.
     *
     * <p>A generating hatch is meant to be a smaller commitment than a consuming one: it feeds the
     * machine rather than powering a recipe, so it supplies and buffers less for the same tier. The
     * halving applies at every tier, so the ratio between a hatch and a dynamo never changes.</p>
     */
    public static int outputTransfer(MultiblockPortTier tier) {
        return clamp(outputTransferExact(level(tier)));
    }

    public static int outputTransfer(int level) {
        return clamp(outputTransferExact(level));
    }

    public static BigInteger outputTransferExact(int level) {
        return halve(transferExact(level));
    }

    public static int outputCapacity(MultiblockPortTier tier) {
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
    }

    /** A figure as the {@code int} the energy API speaks, saturated rather than wrapped. */
    private static int clamp(BigInteger value) {
        return value.min(INT_MAX).max(BigInteger.ZERO).intValue();
    }

    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    /** Zero-based index of the tier: {@code I} is 0, {@code IX} is 8. */
    private static int level(MultiblockPortTier tier) {
        return tier.ordinal();
    }

    /** {@code base} raised to {@code exponent}, exactly. */
    private static BigInteger power(BigInteger base, int exponent) {
        return base.pow(exponent);
    }
}
