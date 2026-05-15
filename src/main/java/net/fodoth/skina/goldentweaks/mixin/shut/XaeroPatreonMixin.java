package net.fodoth.skina.goldentweaks.mixin.shut;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.lib.patreon.Patreon;

@Mixin(Patreon.class)
public class XaeroPatreonMixin {

    @Inject(
            method = "checkPatreon",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$disablePatreonCheck(CallbackInfo ci) {
        ci.cancel();
    }
}
