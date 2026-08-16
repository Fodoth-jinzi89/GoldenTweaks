package net.fodoth.skina.goldentweaks.mixin.fix.iris;

import net.irisshaders.iris.gl.uniform.FloatSupplier;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.uniforms.IrisExclusiveUniforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IrisExclusiveUniforms.class, remap = false)
public abstract class IrisExclusiveUniformsMixin {

    @Inject(method = "addIrisExclusiveUniforms", at = @At("TAIL"))
    private static void gt$addEndFlashUniforms(UniformHolder uniforms, CallbackInfo ci) {
        FloatSupplier noEndFlash = () -> 0.0F;
        uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "endFlashIntensity", noEndFlash);
        uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "previousEndFlashIntensity", noEndFlash);
    }
}
