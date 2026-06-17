package net.fodoth.skina.goldentweaks.mixin.fix.createsubmarine;

import com.maxenonyme.createsubmarine.submarine.client.DeepSeasWelcomeScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeepSeasWelcomeScreen.class)
public class DeepSeasWelcomeScreenMixin {

    @Inject(
            method = "onScreenOpening",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$disableWelcomeScreen(
            ScreenEvent.Opening event,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}