package net.fodoth.skina.goldentweaks.mixin.feature.dangomusic;

import me.sorihiko.dango.DangoClient;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.util.GTState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DangoClient.class, remap = false)
public abstract class DangoClientMixin {

    @Inject(method = "renderMainMenuNowPlaying", at = @At("HEAD"), cancellable = true)
    private static void gt$hideMainMenuOverlay(CallbackInfo ci) {
        if (GTState.isReady() && GoldenTweaksClientConfig.DANGO_STOP_ON_WORLD_LEAVE.get()) {
            ci.cancel();
        }
    }
}
