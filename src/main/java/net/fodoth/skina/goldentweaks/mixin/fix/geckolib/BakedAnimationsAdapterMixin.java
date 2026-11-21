package net.fodoth.skina.goldentweaks.mixin.fix.geckolib;

import net.fodoth.skina.goldentweaks.compat.geckolib.GTGeckoConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "software.bernie.geckolib.loading.json.typeadapter.BakedAnimationsAdapter", remap = false)
public abstract class BakedAnimationsAdapterMixin {
    @Inject(method = "lambda$buildKeyframeStack$0", at = @At("RETURN"), cancellable = true)
    private static void gt$shareEasingConstant(CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(GTGeckoConstants.share(cir.getReturnValue()));
    }
}
