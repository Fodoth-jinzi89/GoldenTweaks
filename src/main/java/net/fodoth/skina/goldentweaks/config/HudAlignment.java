package net.fodoth.skina.goldentweaks.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public enum HudAlignment {
    TOP_LEFT(0.0F, 0.0F),
    TOP_CENTER(0.5F, 0.0F),
    TOP_RIGHT(1.0F, 0.0F),
    CENTER_LEFT(0.0F, 0.5F),
    CENTER(0.5F, 0.5F),
    CENTER_RIGHT(1.0F, 0.5F),
    BOTTOM_LEFT(0.0F, 1.0F),
    BOTTOM_CENTER(0.5F, 1.0F),
    BOTTOM_RIGHT(1.0F, 1.0F);

    public final float horizontal;
    public final float vertical;

    HudAlignment(float horizontal, float vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    @Override
    public @NotNull String toString() {
        return Component.translatable("config.goldentweaks.hud_alignment." + name()).getString();
    }
}
