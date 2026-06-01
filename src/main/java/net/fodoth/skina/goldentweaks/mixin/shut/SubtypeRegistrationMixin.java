package net.fodoth.skina.goldentweaks.mixin.shut;

import mezz.jei.library.load.registration.SubtypeRegistration;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SubtypeRegistration.class)
public class SubtypeRegistrationMixin {

    @Redirect(
            method = "registerSubtypeInterpreter*",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    private void goldentweaks$disableDuplicateInterpreterLog(
            Logger logger,
            String message,
            Object arg1,
            Object arg2
    ) {
        // do nothing
    }
}
