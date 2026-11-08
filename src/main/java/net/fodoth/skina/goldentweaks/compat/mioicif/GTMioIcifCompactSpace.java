package net.fodoth.skina.goldentweaks.compat.mioicif;

import dev.compactmods.machines.api.dimension.CompactDimension;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.level.Level;

/**
 * Helper for letting MIO-ICIF solar and wind machines run inside a Compact Machines dimension,
 * where there is no sky, no day/night cycle and no free wind.
 */
public final class GTMioIcifCompactSpace {

    private GTMioIcifCompactSpace() {
    }

    public static boolean isCompact(Level level) {
        return level != null && CompactDimension.isLevelCompact(level);
    }

    public static float solarFraction() {
        return GoldenTweaksCommonConfig.getMioIcifCompactSpaceSolarFraction();
    }

    public static float windFraction() {
        return GoldenTweaksCommonConfig.getMioIcifCompactSpaceWindFraction();
    }
}
