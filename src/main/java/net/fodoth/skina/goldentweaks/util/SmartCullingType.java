package net.fodoth.skina.goldentweaks.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum SmartCullingType implements StringRepresentable {

    OFF("off"),
    BASE("base"),
    SIMD("simd");

    public static final Codec<SmartCullingType> CODEC =
            StringRepresentable.fromEnum(SmartCullingType::values);

    private final String name;
    private final Component text;

    SmartCullingType(String name) {
        this.name = name;
        this.text = Component.translatable(
                "config.goldentweaks.smart_cull." + name
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