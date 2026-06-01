package net.fodoth.skina.goldentweaks.mixin.fix.tritium;

import me.zcraft.tconfig.config.TritiumConfig;
import org.craftamethyst.tritium.util.random.TritiumRandomManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TritiumRandomManager.class)
public class TritiumRandomManagerMixin {
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/zcraft/tconfig/config/TritiumConfig;addReloadListener(Ljava/lang/Runnable;)V"
            )
    )
    private static void goldentweaks$preventNullConfig(
            TritiumConfig instance,
            Runnable listener
    ) {
        if (instance != null) {
            instance.addReloadListener(listener);
        }
    }

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/craftamethyst/tritium/util/random/TritiumRandomManager;reloadConfig()V"
            )
    )
    private static void goldentweaks$skipEarlyReload() {

    }
}
