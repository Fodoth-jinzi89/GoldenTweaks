package net.fodoth.skina.goldentweaks.mixin.fix.exspectriments;

import io.github.chromonym.exspectriments.ExspModelLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExspModelLayers.class)
public class ExspModelLayersMixin {

    @Inject(
            method = "register",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void gt$skipRegister(CallbackInfo ci) {
        ci.cancel();
    }
}
