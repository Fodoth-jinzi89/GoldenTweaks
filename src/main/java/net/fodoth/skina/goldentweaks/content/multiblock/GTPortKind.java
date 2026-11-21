package net.fodoth.skina.goldentweaks.content.multiblock;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

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
    ENERGY("energy");

    /** Id fragment used in block ids and texture paths. */
    private final String id;

    GTPortKind(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    /** Icon texture AMI ships for this kind, or {@code null} if this kind has none. */
    @Nullable
    public ResourceLocation icon() {
        return ResourceLocation.fromNamespaceAndPath("avaritia_more_items",
                "block/multiblock_port/input/" + this.id);
    }
}
