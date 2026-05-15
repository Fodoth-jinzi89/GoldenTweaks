package net.fodoth.skina.goldentweaks.mixin.shut;

import net.irisshaders.iris.IrisLogging;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CustomUniforms.class)
public class CustomUniformsMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/IrisLogging;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"
            )
    )
    private void goldentweaks$filterUniformWarn(
            IrisLogging instance, String warning, Throwable t
    ) {

        if (warning != null && warning.contains("BIOME_PALE_GARDEN")) {
            return;
        }

        instance.warn(warning, t);
    }
}
