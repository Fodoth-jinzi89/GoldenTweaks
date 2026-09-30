package net.fodoth.skina.goldentweaks.mixin.feature.mekanism;

import com.jerry.meklm.common.tile.machine.TileEntityLargeSolarNeutronActivator;
import mekanism.common.util.WorldUtils;
import net.fodoth.skina.goldentweaks.compat.mekanism.GTMekanismCompactSpace;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityLargeSolarNeutronActivator.class, remap = false)
public abstract class MekMMLargeSolarNeutronActivatorMixin {

    @Redirect(
            method = "recalculateProductionRate",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/common/util/WorldUtils;getSunBrightness(Lnet/minecraft/world/level/Level;F)F"
            )
    )
    private float goldentweaks$compactSpaceFullBrightness(Level level, float partialTicks) {
        return GTMekanismCompactSpace.isCompact(level) ? 1.0F : WorldUtils.getSunBrightness(level, partialTicks);
    }
}
