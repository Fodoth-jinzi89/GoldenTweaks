package net.fodoth.skina.goldentweaks.mixin.shut;

import com.mojang.realmsclient.RealmsAvailability;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RealmsAvailability.class)
public class RealmsAvailabilityLoggerMixin {

    @Mutable
    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void silenceLogger(CallbackInfo ci) {
        LOGGER = org.slf4j.helpers.NOPLogger.NOP_LOGGER;
    }
}
