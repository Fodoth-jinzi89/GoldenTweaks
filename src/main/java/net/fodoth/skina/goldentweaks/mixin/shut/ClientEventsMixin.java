package net.fodoth.skina.goldentweaks.mixin.shut;

import net.mcreator.createstuffadditions.events.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientEvents.class)
public class ClientEventsMixin {

    @Inject(
            method = "onTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void goldentweaks$disableFirstPersonRenderers(boolean isPreEvent, CallbackInfo ci) {
        ci.cancel();
    }
}