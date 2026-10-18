package net.fodoth.skina.goldentweaks.mixin.fix.createsubmarine;

import com.maxenonyme.createsubmarine.submarine.client.DeepSeasUpdateScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeepSeasUpdateScreen.class)
public class DeepSeasUpdateScreenMixin {

    @Inject(
            method = "onScreenOpening",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$disableUpdateScreen(
            ScreenEvent.Opening event,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}
