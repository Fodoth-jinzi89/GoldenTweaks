package net.fodoth.skina.goldentweaks.compat.mioicif.emi;

/**
 * The Singularity Iteration constants the UU pages need.
 *
 * <p>Deliberately free of SI types: the numbers here are stable machine facts, and keeping them in a
 * plain class means the EMI plugin can read them without loading anything from the mod.</p>
 */
public final class GTUuMatter {

    /** How much UU the replicator's internal tank holds, in mB. */
    private static final int REPLICATOR_TANK_CAPACITY = 64000;

    private GTUuMatter() {
    }

    public static int tankCapacity() {
        return REPLICATOR_TANK_CAPACITY;
    }
}
