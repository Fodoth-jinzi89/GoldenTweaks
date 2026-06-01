package net.fodoth.skina.goldentweaks.mixin.fix.jaopca;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thelm.jaopca.localization.LocalizationRepoHandler;

@Mixin(value = LocalizationRepoHandler.class, remap = false)
public class LocalizationRepoHandlerMixin {

    @Redirect(
            method = "lambda$reload$0",
            at = @At(
                    value = "FIELD",
                    target = "Lthelm/jaopca/config/ConfigHandler;checkL10nUpdates:Z",
                    opcode = Opcodes.GETSTATIC)
    )
    private static boolean goldentweaks$disableL10nUpdates() {
        return false;
    }
}
