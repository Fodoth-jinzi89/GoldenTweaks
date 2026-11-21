package net.fodoth.skina.goldentweaks.mixin.fix.yesstevemodel;

import net.fodoth.skina.goldentweaks.compat.yesstevemodel.GTYsmConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.elfmcys.yesstevemodel.O0O0oOOOOo00o0o00o0o00oO", remap = false)
public abstract class NumericParserMixin {
    @Inject(method = "oOo0OO0O0o000OO0O000oo0o(D)Lcom/elfmcys/yesstevemodel/o0oOo00o0oOOOOOo00o0O0oo;",
            at = @At("RETURN"), cancellable = true)
    private void gt$shareConstant(double value, CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(GTYsmConstants.share(cir.getReturnValue()));
    }
}
