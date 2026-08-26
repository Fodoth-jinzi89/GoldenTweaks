package net.fodoth.skina.goldentweaks.mixin.fix.ae2cs;

import io.github.lounode.ae2cs.common.init.client.AECSAdditionalModels;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AECSAdditionalModels.class)
public abstract class AECSAdditionalModelsMixin {
    @Inject(method = "onRegisterAdditionalModels", at = @At("HEAD"))
    private static void goldentweaks$refreshAdditionalModels(ModelEvent.RegisterAdditional event, CallbackInfo ci) {
        event.register(AECSAdditionalModels.BROADCASTER_OFF_CORE);
        event.register(AECSAdditionalModels.BROADCASTER_SENDER_CORE_MODEL);
        event.register(AECSAdditionalModels.BROADCASTER_RECEIVER_CORE_MODEL);
        event.register(AECSAdditionalModels.EMITTER_TOP_ON_MODEL);
        event.register(AECSAdditionalModels.EMITTER_TOP_OFF_MODEL);
    }
}
