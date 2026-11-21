package net.fodoth.skina.goldentweaks.mixin.fix.yesstevemodel;

import net.fodoth.skina.goldentweaks.compat.yesstevemodel.GTYsmConstants;
import net.fodoth.skina.goldentweaks.compat.yesstevemodel.GTYsmVectors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.elfmcys.yesstevemodel.oOO0000OO00oO0000OooO0O0", remap = false)
public abstract class AnimationValuesMixin {
    @Inject(method = "oOo0OO0O0o000OO0O000oo0o(Z)V", at = @At("RETURN"))
    private void gt$shareNumericVectors(boolean rotation, CallbackInfo ci) {
        GTYsmVectors.compact(this);
    }

    @Inject(method = "oOo0OO0O0o000OO0O000oo0o(Lcom/elfmcys/yesstevemodel/o0oOo00o0oOOOOOo00o0O0oo;DZZ)Lcom/elfmcys/yesstevemodel/o0oOo00o0oOOOOOo00o0O0oo;",
            at = @At("RETURN"), cancellable = true)
    private void gt$shareConstant(CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(GTYsmConstants.share(cir.getReturnValue()));
    }
}
