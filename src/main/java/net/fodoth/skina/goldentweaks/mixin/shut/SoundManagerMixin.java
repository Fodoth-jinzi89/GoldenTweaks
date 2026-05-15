package net.fodoth.skina.goldentweaks.mixin.shut;

import net.minecraft.client.sounds.SoundManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    @Mutable
    @Shadow
    @Final
    static Logger LOGGER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void silenceLogger(CallbackInfo ci) {
        LOGGER = org.slf4j.helpers.NOPLogger.NOP_LOGGER;
    }



}
