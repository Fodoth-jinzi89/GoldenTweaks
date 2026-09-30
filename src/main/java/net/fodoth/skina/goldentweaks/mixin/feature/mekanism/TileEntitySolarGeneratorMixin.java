package net.fodoth.skina.goldentweaks.mixin.feature.mekanism;

import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.fodoth.skina.goldentweaks.compat.mekanism.GTMekanismCompactSpace;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntitySolarGenerator.class, remap = false)
public abstract class TileEntitySolarGeneratorMixin {

    @Inject(method = "getBrightnessMultiplier", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$compactSpaceBrightness(Level world, CallbackInfoReturnable<Float> cir) {
        if (GTMekanismCompactSpace.isCompact(world)) {
            cir.setReturnValue(GTMekanismCompactSpace.powerFraction());
        }
    }
}
