package net.fodoth.skina.goldentweaks.mixin.feature.apotheosisthings;

import net.neoforged.neoforge.event.AnvilUpdateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        targets = "com.chen1335.apotheosisThings.common.EventHandler$GAME",
        remap = false
)
public class DisableSalvageCharmAnvilMixin {

    @Inject(
            method = "AnvilUpdateEvent",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$disableSalvageCharmAnvil(
            AnvilUpdateEvent event,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}
