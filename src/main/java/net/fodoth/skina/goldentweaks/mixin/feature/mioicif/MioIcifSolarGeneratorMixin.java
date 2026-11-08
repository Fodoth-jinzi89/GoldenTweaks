package net.fodoth.skina.goldentweaks.mixin.feature.mioicif;

import com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_solar_generator;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifCompactSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = mio_icif_solar_generator.class, remap = false)
public abstract class MioIcifSolarGeneratorMixin {

    @Inject(method = "canGenerate", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceCanGenerate(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (GTMioIcifCompactSpace.isCompact(level)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "scexCurrentRate", at = @At("HEAD"), cancellable = true)
    private static void goldentweaks$compactSpaceScexRate(Level level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (GTMioIcifCompactSpace.isCompact(level)) {
            cir.setReturnValue(1.0F);
        }
    }
}
