package net.fodoth.skina.goldentweaks.mixin.shut;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.dipilodopilasaurus.putapluginit.modfix.JeiCacheFix", remap = false)
public abstract class PapiJeiCacheLoggerMixin {

    @Redirect(
            method = "clearUnsupportedContainers",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"
            )
    )
    private static void gt$suppressUnsupportedContainerWarning(
            Logger logger, String message, Throwable throwable
    ) {
    }
}
