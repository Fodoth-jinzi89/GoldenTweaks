package net.fodoth.skina.goldentweaks.mixin.shut;

import me.pepperbell.continuity.client.ContinuityClient;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContinuityClient.class)
public class ContinuityClientMixin {

    @Mutable
    @Shadow
    @Final
    public static Logger LOGGER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void disableLogger(CallbackInfo ci) {
        LOGGER = org.slf4j.helpers.NOPLogger.NOP_LOGGER;
    }
}
