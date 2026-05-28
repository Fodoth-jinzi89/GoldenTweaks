package net.fodoth.skina.goldentweaks.mixin.fix.cfwinfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
        targets = "com.jopgood.cfwinfo.client.ClientSetup"
)
public class ClientSetupMixin {

    @WrapOperation(
            method = "onClientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/jopgood/cfwinfo/common/config/CommonConfig;isMessagesEnabled()Z"
            )
    )
    private static boolean goldentweaks$preventConfigCrash(
            Operation<Boolean> original
    ) {

        try {
            return original.call();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}