package net.fodoth.skina.goldentweaks.mixin.shut;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.fantasyit.maid_storage_manager.Logger;

@Mixin(value = Logger.class, remap = false)
public class MaidStorageManagerLoggerMixin {

    @Mutable
    @Shadow
    @Final
    public static org.slf4j.Logger logger;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void disableLogger(CallbackInfo ci) {
        logger = org.slf4j.helpers.NOPLogger.NOP_LOGGER;
    }
}
