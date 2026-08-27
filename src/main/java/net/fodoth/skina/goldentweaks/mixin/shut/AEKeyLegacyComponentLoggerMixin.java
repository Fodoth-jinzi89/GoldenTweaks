package net.fodoth.skina.goldentweaks.mixin.shut;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "appeng.api.stacks.AEKey$1", remap = false)
public class AEKeyLegacyComponentLoggerMixin {

    @Redirect(
            method = "apply",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V"))
    private void goldentweaks$suppressRemovedNorthstarComponents(Logger logger, String message, Object error) {
        String detail = String.valueOf(error);
        if (!detail.contains("northstar:planet")
                && !detail.contains("northstar:planet_x")
                && !detail.contains("northstar:planet_y")) {
            logger.error(message, error);
        }
    }
}
