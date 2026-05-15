package net.fodoth.skina.goldentweaks.mixin.shut;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.raritycore.RarityCore;

@Mixin(RarityCore.class)
public class RarityCoreLoggerMixin {

    @Mutable
    @Shadow
    @Final
    public static Logger LOGGER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void disableLogger(CallbackInfo ci) {
        LOGGER = org.slf4j.helpers.NOPLogger.NOP_LOGGER;
    }
}
