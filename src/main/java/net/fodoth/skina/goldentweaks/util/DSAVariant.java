package net.fodoth.skina.goldentweaks.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum DSAVariant implements StringRepresentable {

    CORE("core"),
    ARB("arb");

    public static final Codec<DSAVariant> CODEC =
            StringRepresentable.fromEnum(DSAVariant::values);

    private final String name;
    private final Component text;

    DSAVariant(String name) {
        this.name = name;
        this.text = Component.translatable(
                "config.goldentweaks.dsa.var." + name
        );
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public Component symbol() {
        return this.text;
    }
}