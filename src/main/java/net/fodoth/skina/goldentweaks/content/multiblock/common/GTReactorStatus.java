package net.fodoth.skina.goldentweaks.content.multiblock.common;

/**
 * Why the chemical kettle is currently not working.
 *
 * <p>Ordered by how the GUI reports it, and kept as an explicit state rather than a set of
 * independent booleans: the status line shows exactly one of these, so the machine has to settle on
 * one reason. The first matching case wins when several apply at once, most specific first — a
 * power trip is reported before the plain "switched off" it also implies.</p>
 */
public enum GTReactorStatus {

    /** Assembled and switched on, with nothing to do. */
    IDLE,

    /** Assembled, switched on, and in the middle of a reaction. */
    PROCESSING,

    /** Switched off by the player. */
    OFF,

    /** Switched off because a tick's energy could not be paid. */
    NO_ENERGY,

    /** Switched off because the shape around the machine is gone. */
    BROKEN_STRUCTURE,

    /** Switched off for a reason this enum does not model. */
    OTHER,

    /** Switch is on and the machine is looking for work. */
    STARTING,

    /**
     * Every recipe the ports could match costs more than the energy hatches can carry.
     *
     * <p>Appended rather than inserted: the ordinal is what the status packet carries, so reordering
     * would silently relabel a running machine's status on clients with the old build.</p>
     */
    INSUFFICIENT_ENERGY_HATCH
}
