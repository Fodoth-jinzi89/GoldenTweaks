package net.fodoth.skina.goldentweaks.mixin.feature.mekanism;

import net.fodoth.skina.goldentweaks.compat.mekanism.GTMekanismCompactSpace;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mekanism Advanced Generators copies Mekanism's solar panel design in its own package
 * ({@code com.pocky.solarpanels}), so it needs the same Compact Machines handling: inside a compact dimension its
 * solar checks must report sun and their peak multiplier, exactly like Mekanism's own panels.
 * <p>
 * Targets the whole {@code SolarCheck} hierarchy, so both the normal and the big (multi-check) solar generators and
 * every tier are covered - the big variant's checks only override {@code recheckCanSeeSun}.
 */
@Mixin(targets = "com.pocky.solarpanels.tile.TileEntitySolarGenerator$SolarCheck", remap = false)
public abstract class SolarPanelsSolarCheckMixin {

    @Final
    @Shadow
    protected Level world;

    @Final
    @Shadow
    private float peakMultiplier;

    @Inject(method = "canSeeSun", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceCanSeeSun(CallbackInfoReturnable<Boolean> cir) {
        if (GTMekanismCompactSpace.isCompact(this.world)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getGenerationMultiplier", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceGenerationMultiplier(CallbackInfoReturnable<Float> cir) {
        if (GTMekanismCompactSpace.isCompact(this.world)) {
            cir.setReturnValue(this.peakMultiplier);
        }
    }
}
