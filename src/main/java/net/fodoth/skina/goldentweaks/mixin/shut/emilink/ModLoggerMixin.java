package net.fodoth.skina.goldentweaks.mixin.shut.emilink;

import org.chatterjay.emiextend.util.ModLogger;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModLogger.class)
public class ModLoggerMixin {

    @Mutable
    @Shadow
    @Final
    private static Logger LOG;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void silenceLogger(CallbackInfo ci) {
        LOG = org.slf4j.helpers.NOPLogger.NOP_LOGGER;
    }
}
