package net.fodoth.skina.goldentweaks.compat.mekanism;

import dev.compactmods.machines.api.dimension.CompactDimension;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.level.Level;

/**
 * Helper for letting Mekanism (and its add-ons') solar/wind machines run inside a Compact Machines dimension,
 * where there is no sky and no day/night cycle, and scaling their output down.
 */
public final class GTMekanismCompactSpace {

    private GTMekanismCompactSpace() {
    }

    public static boolean isCompact(Level level) {
        return level != null && CompactDimension.isLevelCompact(level);
    }

    public static float powerFraction() {
        return GoldenTweaksCommonConfig.getMekanismCompactSpacePowerFraction();
    }
}
