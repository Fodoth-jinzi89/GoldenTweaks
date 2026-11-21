package net.fodoth.skina.goldentweaks.mixin.fix.geckolib;

import net.fodoth.skina.goldentweaks.compat.geckolib.GTGeckoConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "software.bernie.geckolib.loading.math.MathParser", remap = false)
public abstract class MathParserMixin {
    @Inject(method = {"parseJson", "compileMolang", "parseSymbols"}, at = @At("RETURN"), cancellable = true)
    private static void gt$shareConstant(CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(GTGeckoConstants.share(cir.getReturnValue()));
    }
}
