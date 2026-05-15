package net.fodoth.skina.goldentweaks.mixin.shut;

import dev.kosmx.playerAnim.minecraftApi.codec.AnimationCodecs;
import org.spongepowered.asm.mixin.Mixin;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnimationCodecs.class)
public class AnimationCodecsMixin {

    @Redirect(
            method = "deserialize(Ljava/lang/String;Ljava/util/function/Supplier;)Ljava/util/Collection;",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Throwable;)V"
            )
    )
    private static void goldentweaks$disableInfoLog(Logger instance, String msg, Throwable throwable) {
        // do nothing
    }



    @Redirect(
            method = "deserialize(Ljava/lang/String;Ljava/util/function/Supplier;)Ljava/util/Collection;",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V"
            )
    )
    private static void goldentweaks$disableErrorLog(Logger instance, String msg, Throwable throwable) {
        // do nothing
    }
}
