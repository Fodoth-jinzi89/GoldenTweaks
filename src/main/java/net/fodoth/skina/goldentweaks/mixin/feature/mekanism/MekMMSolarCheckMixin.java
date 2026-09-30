package net.fodoth.skina.goldentweaks.mixin.feature.mekanism;

import net.fodoth.skina.goldentweaks.compat.mekanism.GTMekanismCompactSpace;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.jerry.mekmm.common.util.WorldUtil$SolarCheck", remap = false)
public abstract class MekMMSolarCheckMixin {

    @Final
    @Shadow
    protected Level world;

    @Shadow
    protected float peakMultiplier;

    @Inject(method = "canSeeSun", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceCanSeeSun(CallbackInfoReturnable<Boolean> cir) {
        if (GTMekanismCompactSpace.isCompact(this.world)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getProductionMultiplier", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceProductionMultiplier(CallbackInfoReturnable<Float> cir) {
        if (GTMekanismCompactSpace.isCompact(this.world)) {
            cir.setReturnValue(this.peakMultiplier);
        }
    }
}
