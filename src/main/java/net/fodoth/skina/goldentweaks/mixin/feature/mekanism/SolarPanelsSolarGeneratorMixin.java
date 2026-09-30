package net.fodoth.skina.goldentweaks.mixin.feature.mekanism;

import net.fodoth.skina.goldentweaks.compat.mekanism.GTMekanismCompactSpace;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inside a Compact Machines dimension a Mekanism Advanced Generators solar panel ignores the day/night cycle and
 * sky obstruction, and outputs the configured percentage of its peak (see
 * {@code balance.mekanism.compact_space_power_percent}).
 * <p>
 * {@code getBrightnessMultiplier} sits at the front of the production formula for both the normal and the big solar
 * generator ({@code peak * brightness * generationMultiplier}), so overriding it here scales them all at once.
 */
@Mixin(targets = "com.pocky.solarpanels.tile.TileEntitySolarGenerator", remap = false)
public abstract class SolarPanelsSolarGeneratorMixin {

    @Inject(method = "getBrightnessMultiplier", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceBrightness(Level world, CallbackInfoReturnable<Float> cir) {
        if (GTMekanismCompactSpace.isCompact(world)) {
            cir.setReturnValue(GTMekanismCompactSpace.powerFraction());
        }
    }
}
