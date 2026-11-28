package net.fodoth.skina.goldentweaks.content.multiblock.common;

import java.math.BigInteger;

/**
 * Exact per-tick energy carried alongside an AMI matched recipe.
 *
 * <p>AMI stores {@code energyPerTick} as an {@code int}, which a large parallel multiplier or a
 * high overclock can exceed. The exact figure is therefore recorded beside the record instead of
 * being saturated into it.</p>
 *
 * <p>Declared here rather than next to the mixin that implements it: {@code goldentweaks.mixins.json}
 * claims the whole {@code net.fodoth.skina.goldentweaks.mixin} package, and Mixin refuses to load a
 * class from there as an ordinary class. Anything that is referenced directly — an interface our own
 * code casts to — has to live outside that package.</p>
 */
public interface GTExactEnergy {

    /** Records the exact figure for this recipe. */
    void gt$setExactEnergy(BigInteger exact);

    /** The exact per-tick cost, falling back to the record's own {@code int}. */
    BigInteger gt$exactEnergy();
}
