package net.fodoth.skina.goldentweaks.mixin.feature.mekanism;

import mekanism.api.math.MathUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.fodoth.skina.goldentweaks.compat.mekanism.GTMekanismCompactSpace;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inside a Compact Machines dimension a wind generator ignores height and sky visibility, and always outputs
 * the configured percentage of {@code windGenerationMax} (the maximum generation the generator can ever reach).
 */
@Mixin(value = TileEntityWindGenerator.class, remap = false)
public abstract class TileEntityWindGeneratorMixin {

    @Inject(method = "getMultiplier", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceMultiplier(CallbackInfoReturnable<Double> cir) {
        //Height and sky are irrelevant here; just keep the generator active so it produces anything at all.
        if (GTMekanismCompactSpace.isCompact(((BlockEntity) (Object) this).getLevel())) {
            cir.setReturnValue(1.0D);
        }
    }

    @Inject(method = "getCurrentGeneration", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceGeneration(CallbackInfoReturnable<Long> cir) {
        if (GTMekanismCompactSpace.isCompact(((BlockEntity) (Object) this).getLevel())) {
            double max = MekanismGeneratorsConfig.generators.windGenerationMax.get();
            cir.setReturnValue(MathUtils.clampToLong(max * GTMekanismCompactSpace.powerFraction()));
        }
    }
}
