package net.fodoth.skina.goldentweaks.mixin.shut;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.cazsius.solcarrot.tracking.FoodInstance", remap = false)
public abstract class SolCarrotFoodLoggerMixin {

    @Redirect(
            method = "decode",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"
            )
    )
    private static void gt$suppressInvalidFoodWarning(
            Logger logger, String message, Object itemId
    ) {
    }
}
