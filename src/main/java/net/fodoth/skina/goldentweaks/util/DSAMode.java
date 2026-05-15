package net.fodoth.skina.goldentweaks.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum DSAMode implements StringRepresentable {

    ALL("all"),
    VBO("vbo"),
    FBO("fbo"),
    OFF("off");

    public static final Codec<DSAMode> CODEC =
            StringRepresentable.fromEnum(DSAMode::values);

    private final String name;
    private final Component text;

    DSAMode(String name) {
        this.name = name;
        this.text = Component.translatable(
                "config.goldentweaks.dsa." + name
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