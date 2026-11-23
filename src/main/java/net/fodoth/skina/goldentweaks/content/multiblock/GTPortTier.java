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

    ULV("ulv", "ULV", 0xFF5555),
    LV("lv", "LV", 0xAAAAAA),
    MV("mv", "MV", 0xFFAA00),
    HV("hv", "HV", 0xFFFF55),
    EV("ev", "EV", 0x555555),
    IV("iv", "IV", 0x5555FF),
    LUV("luv", "LuV", 0xFF55FF),
    ZPM("zpm", "ZPM", 0x55FFFF),
    UV("uv", "UV", 0x00AA00),
    // Past AMI's own ladder, which ends at IX. These are ours alone: the structure scan clamps
    // them to IX (see #ami), because a port that the machine accepts as a tier IX hatch is still
    // a hatch, and refusing to assemble around one would help nobody.
    UHV("uhv", "UHV", 0x8B0000),
    UEV("uev", "UEV", 0x800080),
    UIV("uiv", "UIV", 0x020DAA),
    UMV("umv", "UMV", 0x6B4423),
    UXV("uxv", "UXV", 0x00FFFF),
    MAX("max", "MAX", 0xFFFFFF);

    /** Lower-case fragment used in block and item ids. */
    private final String id;
    /** Shorthand the player reads in names, e.g. {@code ULV}. */
    private final String code;
    /**
     * The tier's colour, as {@code 0xRRGGBB}.
     *
     * <p>Plain RGB rather than {@code ChatFormatting}: that enum has sixteen fixed colours and no
     * brown or cyan, so UMV and UXV could only be approximated there. Owning the value here also
     * means the ring on the block, the code in the item name and the line in Jade are all drawn
     * from one number instead of three tables that can drift.</p>
     */
    private final int colour;

    GTPortTier(String id, String code, int colour) {
        this.id = id;
        this.code = code;
        this.colour = colour;
    }

    public String id() {
        return this.id;
    }

    public String code() {
        return this.code;
    }

    /**
     * The tier's colour as {@code 0xRRGGBB}, without an alpha channel.
     *
     * <p>MAX is the exception: it walks the spectrum, so what comes back depends on the moment.
     * That is only useful for text, which is rebuilt each frame — a block's colour is baked into
     * its mesh, so the MAX ring animates through a texture instead and asks for white here.</p>
     */
    public int colour() {
        if (this == MAX) {
            return spectrum();
        }
        return this.colour;
    }

    /**
     * A hue that advances with the clock, roughly one full turn every few seconds.
     *
     * <p>Written by hand rather than through {@code java.awt.Color}: that class drags AWT in, which
     * a dedicated server has no business loading just to pick a colour.</p>
     */
    private static int spectrum() {
        float turn = (System.currentTimeMillis() % SPECTRUM_PERIOD_MS) / (float) SPECTRUM_PERIOD_MS;
        return hsvToRgb(turn, 1.0F, 1.0F);
    }

    /** One full cycle of the spectrum, in milliseconds. */
    private static final long SPECTRUM_PERIOD_MS = 1000L;

    /** {@code h, s, v} in {@code 0..1} to {@code 0xRRGGBB}. */
    private static int hsvToRgb(float h, float s, float v) {
        float scaled = (h - (float) Math.floor(h)) * 6.0F;
        int sector = (int) scaled;
        float fraction = scaled - sector;
        float p = v * (1.0F - s);
        float q = v * (1.0F - s * fraction);
        float t = v * (1.0F - s * (1.0F - fraction));
        float r;
        float g;
        float b;
        switch (sector % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return (Math.round(r * 255.0F) << 16)
                | (Math.round(g * 255.0F) << 8)
                | Math.round(b * 255.0F);
    }

    /** One-based level, matching {@link #id()} and the naming scheme. */
    public int level() {
        return ordinal() + 1;
    }

    /**
     * The AMI tier of the same rank, which the structure scan reads.
     *
     * <p>Clamped to AMI's top: its enum ends at IX, and the tiers above UV exist only on our side.
     * Reporting IX keeps them usable as structure parts, which is the only thing this bridge is
     * for.</p>
     */
    public MultiblockPortTier ami() {
        MultiblockPortTier[] all = MultiblockPortTier.values();
        return all[Math.min(all.length - 1, ordinal())];
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

    /**
     * Tanks a chemical hopper provides.
     *
     * <p>Same count as a fluid hatch, so the two are interchangeable as structure parts and the
     * "cells" readout means the same thing in both windows.</p>
     */
    public int chemicalTanks() {
        return level();
    }

    /**
     * Capacity of each chemical tank, in Mekanism units.
     *
     * <p>A tenth of the fluid hatch's tank: chemicals are handled in the same order of magnitude as
     * fluids by Mekanism machinery, and a hopper is meant to be the tighter buffer of the two.</p>
     */
    public int chemicalTankCapacity() {
        return (8000 << (level() - 1)) / 10;
    }

    /** Essentia tanks an essentia port provides: the item port's cell count, per the same rule. */
    public int essentiaTanks() {
        return itemSlots();
    }

    /**
     * Capacity of each essentia tank, in essentia units.
     *
     * <p>The item figure exactly — 64 × level — because a tank of essentia is meant to hold the
     * same count as a slot of items, so a recipe asking for "three of this aspect" reads the same
     * as one asking for three ingots.</p>
     */
    public int essentiaTankCapacity() {
        return slotLimit();
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
            case UHV -> 4096;
            case UEV -> 16384;
            case UIV -> 65536;
            case UMV -> 262144;
            case UXV -> 1048576;
            case MAX -> Integer.MAX_VALUE;
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
            case UHV -> 4096000;
            case UEV -> 16384000;
            case UIV -> 65536000;
            case UMV -> 262144000;
            case UXV -> Integer.MAX_VALUE / 2;
            case MAX -> Integer.MAX_VALUE;
        };
    }
}
