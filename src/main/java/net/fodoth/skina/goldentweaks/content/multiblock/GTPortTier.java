package net.fodoth.skina.goldentweaks.content.multiblock;

import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;

/**
 * The nine voltage tiers a port can be built at.
 *
 * <p>Each tier is its own block and its own item rather than a block state value. That is what lets
 * the inventory name a part "I ULV input bus" instead of a generic "input bus": an item carries no
 * block state, so a tier kept in the state would be invisible once the part is in hand.</p>
 *
 * <p>The enum order matches AMI's own {@link MultiblockPortTier}, which is what the structure scan
 * compares against.</p>
 */
public enum GTPortTier {

    ULV("ulv", "ULV"),
    LV("lv", "LV"),
    MV("mv", "MV"),
    HV("hv", "HV"),
    EV("ev", "EV"),
    IV("iv", "IV"),
    LUV("luv", "LuV"),
    ZPM("zpm", "ZPM"),
    UV("uv", "UV");

    /** Lower-case fragment used in block and item ids. */
    private final String id;
    /** Shorthand the player reads in names, e.g. {@code ULV}. */
    private final String code;

    GTPortTier(String id, String code) {
        this.id = id;
        this.code = code;
    }

    public String id() {
        return this.id;
    }

    public String code() {
        return this.code;
    }

    /** One-based level, matching {@link #id()} and the naming scheme. */
    public int level() {
        return ordinal() + 1;
    }

    /** The AMI tier of the same rank, which the structure scan reads. */
    public MultiblockPortTier ami() {
        return MultiblockPortTier.values()[ordinal()];
    }

    /** Looks a tier up by its one-based level, clamped into range. */
    public static GTPortTier byLevel(int level) {
        GTPortTier[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, level - 1))];
    }

    // ---- capacities, all driven by the level ----

    /** Item slots an item port of this tier provides: the level squared. */
    public int itemSlots() {
        return level() * level();
    }

    /** Stack ceiling per slot: {@code 64 × level}. */
    public int slotLimit() {
        return 64 * level();
    }

    /** Fluid tanks a fluid port provides: one per level. */
    public int fluidTanks() {
        return level();
    }

    /**
     * Capacity of each tank: {@code 8000 mB}, doubling per level.
     *
     * <p>Geometric rather than linear, so the top tier is a real buffer instead of eight times the
     * bottom one: 8000, 16000, 32000 … 2,048,000 mB.</p>
     */
    public int tankCapacity() {
        return 8000 << (level() - 1);
    }

    // ---- active-port throughput ----

    /** Ticks one transfer step takes: 80 for ULV, 20 for LV, then one. */
    public int reachInterval() {
        return switch (this) {
            case ULV -> 80;
            case LV -> 20;
            default -> 1;
        };
    }

    /**
     * Items moved per step.
     *
     * <p>Each tier above ULV is four times the last, starting from a single item — with LV and MV
     * held at the same step length, so MV carries four times LV's amount. IV is a slower but larger
     * step than EV: 64 items every five ticks instead of every second.</p>
     */
    public int reachItems() {
        return switch (this) {
            case ULV, LV -> 1;
            case MV -> 4;
            case HV -> 16;
            case EV, IV, LUV -> 64;
            case ZPM -> 256;
            case UV -> 1024;
        };
    }

    /**
     * Fluid moved per step, in mB.
     *
     * <p>Not the item figure times a thousand: the upper tiers were given their own numbers, and the
     * table is what the ports are balanced against.</p>
     */
    public int reachFluid() {
        return switch (this) {
            case ULV, LV -> 1000;
            case MV -> 4000;
            case HV -> 16000;
            case EV, IV, LUV -> 64000;
            case ZPM -> 256000;
            case UV -> 1024000;
        };
    }
}
