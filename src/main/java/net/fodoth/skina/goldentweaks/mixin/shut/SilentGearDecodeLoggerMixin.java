package net.fodoth.skina.goldentweaks.mixin.shut;

import net.silentchaos512.gear.core.DataResourceManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DataResourceManager.class, remap = false)
public class SilentGearDecodeLoggerMixin {

    @Redirect(
            method = "tryDecode",
            at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Lorg/apache/logging/log4j/Marker;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void suppressDecodeInfo(Logger logger, Marker marker, String message,
                                    Object type, Object id, Object pack) {
    }
}
