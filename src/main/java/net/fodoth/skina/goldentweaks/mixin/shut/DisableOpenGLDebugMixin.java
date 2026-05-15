package net.fodoth.skina.goldentweaks.mixin.shut;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class DisableOpenGLDebugMixin {

    @Inject(
            method = "setErrorCallback",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$disableDebugCallback(CallbackInfo ci) {
        ci.cancel();
    }
}
