package net.fodoth.skina.goldentweaks.mixin.shut;

import net.minecraft.client.renderer.texture.SpriteLoader;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteLoader.class)
public class SpriteLoaderMixin {

    @Mutable
    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void silenceLogger(CallbackInfo ci) {
        LOGGER = org.slf4j.helpers.NOPLogger.NOP_LOGGER;
    }
}