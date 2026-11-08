package net.fodoth.skina.goldentweaks.mixin.feature.mioicif;

import com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_MetsSolarGeneratorBase;
import com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_solar_generator;
import com.miophas.singularity_iteration.core.prefab.blockentity.AbstractGeneratorBlockEntity;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifCompactSpace;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scales the output of every MIO-ICIF solar generator inside a Compact Machines dimension by the configured
 * percentage of its daytime peak. This single hook covers both the normal and the SCEX-network-controlled
 * generation paths of all solar tiers.
 */
@Mixin(value = AbstractGeneratorBlockEntity.class, remap = false)
public abstract class MioIcifAbstractGeneratorMixin {

    @Inject(method = "outputScale", at = @At("RETURN"), cancellable = true)
    private void goldentweaks$compactSolarOutputScale(CallbackInfoReturnable<Double> cir) {
        Object self = this;
        if (self instanceof mio_icif_solar_generator || self instanceof mio_icif_MetsSolarGeneratorBase) {
            Level level = ((BlockEntity) self).getLevel();
            if (GTMioIcifCompactSpace.isCompact(level)) {
                cir.setReturnValue(cir.getReturnValue() * GTMioIcifCompactSpace.solarFraction());
            }
        }
    }
}
