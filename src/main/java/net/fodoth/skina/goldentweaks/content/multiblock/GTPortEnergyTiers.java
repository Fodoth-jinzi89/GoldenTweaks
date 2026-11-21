package net.fodoth.skina.goldentweaks.content.multiblock;

import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;

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
    private static final int STEP = 4;

    /** Ticks of full-rate input a port can hold. */
    private static final int CAPACITY_IN_TICKS = 20;

    /** Throughput of the lowest tier, in FE per tick. */
    private static final int BASE_TRANSFER = 64;

    private GTPortEnergyTiers() {
    }

    /** FE per tick a port of this tier accepts and supplies. */
    public static int transfer(MultiblockPortTier tier) {
        return BASE_TRANSFER * power(STEP, level(tier));
    }

    /** FE a port of this tier can hold. */
    public static int capacity(MultiblockPortTier tier) {
        return transfer(tier) * CAPACITY_IN_TICKS;
    }

    /**
     * The same figures for a dynamo hatch, at half the rate and half the store.
     *
     * <p>A generating hatch is meant to be a smaller commitment than a consuming one: it feeds the
     * machine rather than powering a recipe, so it supplies and buffers less for the same tier. The
     * halving applies at every tier, so the ratio between a hatch and a dynamo never changes.</p>
     */
    public static int outputTransfer(MultiblockPortTier tier) {
        return Math.max(1, transfer(tier) / 2);
    }

    public static int outputCapacity(MultiblockPortTier tier) {
        return Math.max(1, capacity(tier) / 2);
    }

    /** Zero-based index of the tier: {@code I} is 0, {@code IX} is 8. */
    private static int level(MultiblockPortTier tier) {
        return Math.max(0, tier.ordinal());
    }

    /**
     * {@code base} raised to {@code exponent}, saturating instead of overflowing.
     *
     * <p>The top tier is 64 × 4⁸, well inside an int, but a table someone extends later should
     * clamp rather than wrap around into a negative capacity.</p>
     */
    private static int power(int base, int exponent) {
        long value = 1L;
        for (int index = 0; index < exponent; index++) {
            value *= base;
            if (value > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) value;
    }
}
