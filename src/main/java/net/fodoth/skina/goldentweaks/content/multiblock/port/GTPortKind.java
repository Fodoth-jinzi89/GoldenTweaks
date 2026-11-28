package net.fodoth.skina.goldentweaks.content.multiblock.port;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * What a port carries, and which icon and storage it uses.
 *
 * <p>A port's kind is fixed by its block instance rather than by its block state: an item port is
 * registered once and never becomes a fluid port, so keeping the kind out of the state avoids nine
 * extra properties that could never change.</p>
 */
public enum GTPortKind {

    ITEM("item"),
    FLUID("fluid"),
    ENERGY("energy"),
    /**
     * A Mekanism chemical hopper.
     *
     * <p>Its own kind rather than a variant of {@link #FLUID}: chemicals are a separate registry
     * with a separate capability, and the two cannot share a handler. The art calls it a hopper
     * because the name is about the part, not the contents.</p>
     */
    CHEMICAL("chemical"),
    /**
     * A Thaumcraft essentia tank.
     *
     * <p>Also its own kind: essentia is addressed by {@code Aspect} and carried through Thaumcraft's
     * own transport interface, which shares nothing with fluids beyond the idea of a tank.</p>
     */
    ESSENTIA("essentia"),
    /**
     * A general-purpose assembly: one storage that carries every kind at once.
     *
     * <p>Not a variant of any of the above. The others each hold one thing, and a machine's matcher
     * asks each of its ports for that one thing; this holds a slice of all five, the way AMI's
     * all-type storage cell does. It is measured in bytes and types rather than slots or tanks, and
     * its window reports those two figures instead of the cells count.</p>
     *
     * <p>Appended last: {@code writeEnum}/{@code readEnum} encode by ordinal, so inserting above
     * would renumber every kind that is already in a save or a packet.</p>
     */
    GENERAL("general");

    /** Id fragment used in block ids and texture paths. */
    private final String id;

    GTPortKind(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    /** Whether this kind is one of ours, rather than one AMI also ships art for. */
    public boolean isExtended() {
        return this == CHEMICAL || this == ESSENTIA || this == GENERAL;
    }

    /**
     * Whether a port of this kind can hold essentia.
     *
     * <p>Asked instead of comparing against {@code ESSENTIA} alone: the general assembly holds
     * every kind at once, so a port that can take essentia is not necessarily an essentia port.
     * Written once here rather than repeated at each of the five places the machine walks its
     * typed ports.</p>
     */
    public boolean holdsEssentia() {
        return this == ESSENTIA || this == GENERAL;
    }

    /** Whether a port of this kind can hold chemicals. See {@link #holdsEssentia()}. */
    public boolean holdsChemical() {
        return this == CHEMICAL || this == GENERAL;
    }

    /** Icon texture AMI ships for this kind, or {@code null} if this kind has none. */
    public @NotNull ResourceLocation icon() {
        if (isExtended()) {
            // Our own sheet, in arts/multiblock/port.
            return ResourceLocation.fromNamespaceAndPath("goldentweaks",
                    "block/multiblock/port/input_" + this.id);
        }
        return ResourceLocation.fromNamespaceAndPath("avaritia_more_items",
                "block/multiblock_port/input/" + this.id);
    }
}
