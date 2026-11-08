package net.fodoth.skina.goldentweaks.mixin.feature.mioicif;

import com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_MetsSolarGeneratorBase;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifCompactSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = mio_icif_MetsSolarGeneratorBase.class, remap = false)
public abstract class MioIcifMetsSolarGeneratorBaseMixin {

    @Inject(method = "calculateSkyLight", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceSkyLight(Level level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (GTMioIcifCompactSpace.isCompact(level)) {
            cir.setReturnValue(1.0F);
        }
    }
}
