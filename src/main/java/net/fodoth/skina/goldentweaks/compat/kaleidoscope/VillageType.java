package net.fodoth.skina.goldentweaks.compat.kaleidoscope;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum VillageType implements StringRepresentable {

    PLAINS("plains"),
    DESERT("desert"),
    SAVANNA("savanna"),
    SNOWY("snowy"),
    TAIGA("taiga");

    private final String name;

    VillageType(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

}