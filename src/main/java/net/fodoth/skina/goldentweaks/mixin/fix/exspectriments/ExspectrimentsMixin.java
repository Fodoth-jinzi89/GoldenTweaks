package net.fodoth.skina.goldentweaks.mixin.fix.exspectriments;

import io.github.chromonym.exspectriments.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Exspectriments.class)
public class ExspectrimentsMixin {

    @Inject(
            method = "onInitialize",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void gt$patchInit(CallbackInfo ci) {

        ExspItemTags.initialize();
        ExspScreenHandlers.initialize();
        ExspParticleTypes.initialize();
        ExspStatusEffects.initialize();
        ExspFluids.initialize();
        ExspBlocks.initialize();
        ExspBlockEntities.initialize();
        ExspItems.initialize();
        ExspServerRecievers.registerReceivers();
        ExspRecipes.initialize();

        Exspectriments.LOGGER.info(
                "[GoldenTweaks] Skipped deprecated ItemSubGroupEvents registration"
        );

        ci.cancel();
    }
}
