package net.fodoth.skina.goldentweaks.mixin.shut;

import foundry.veil.impl.client.render.dynamicbuffer.VanillaShaderCompiler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VanillaShaderCompiler.class)
public class VanillaShaderCompilerMixin {

    @Redirect(
            method = "compileShader",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    private void goldenTweaks$noShaderErrorLog(Logger instance, String s, Object path, Object t) {
    }
}
