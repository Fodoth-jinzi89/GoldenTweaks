package net.fodoth.skina.goldentweaks.mixin.shut;

import net.mehvahdjukaar.moonlight.core.client.MoonlightHubInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MoonlightHubInfo.class, remap = false)
public class MoonlightHubFetchMixin {

    @Inject(
            method = "fetchFromServer",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void goldentweaks$disableHubFetch(CallbackInfo ci) {
        ci.cancel();
    }
}
