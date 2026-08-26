package net.fodoth.skina.goldentweaks.mixin.fix.neoecoae;

import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.api.ECOComputationModels;
import cn.dancingsnow.neoecoae.client.all.NEExtraModels;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NEExtraModels.class)
public abstract class NEExtraModelsMixin {
    @Inject(method = "onRegisterExtraModels", at = @At("HEAD"))
    private static void goldentweaks$applyDeferredModels(ModelEvent.RegisterAdditional event, CallbackInfo ci) {
        ECOCellModels.runDeferredRegistration();
        ECOComputationModels.runDeferredRegistration();
    }
}
