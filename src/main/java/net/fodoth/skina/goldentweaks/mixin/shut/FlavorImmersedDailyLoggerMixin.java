package net.fodoth.skina.goldentweaks.mixin.shut;

import com.flavor_immersed_daily.gameplay.CropHarvestHandler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CropHarvestHandler.class, remap = false)
public class FlavorImmersedDailyLoggerMixin {

    @Redirect(
            method = "onRightClickBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;info(Ljava/lang/String;[Ljava/lang/Object;)V"
            )
    )
    private static void suppressInfo(Logger logger, String message, Object[] arguments) {
    }

    @Redirect(
            method = "onRightClickBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    private static void suppressInfo(Logger logger, String message, Object first, Object second) {
    }
}
