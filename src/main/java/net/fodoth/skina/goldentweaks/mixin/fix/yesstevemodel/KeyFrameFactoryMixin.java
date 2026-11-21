package net.fodoth.skina.goldentweaks.mixin.fix.yesstevemodel;

import net.fodoth.skina.goldentweaks.compat.yesstevemodel.GTYsmLinearFrames;
import net.fodoth.skina.goldentweaks.compat.yesstevemodel.GTYsmSplineFrames;
import net.fodoth.skina.goldentweaks.compat.yesstevemodel.GTYsmStepFrames;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.elfmcys.yesstevemodel.OO0o0ooO0oOoOO00o0O0OOo0", remap = false)
public interface KeyFrameFactoryMixin {
    @Inject(method = "oOo0OO0O0o000OO0O000oo0o(Ljava/util/List;)Lcom/elfmcys/yesstevemodel/O0o0o00O00oOOoOo0OOo0O0o;",
            at = @At("RETURN"), cancellable = true)
    private static void gt$shareStepFrame(CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(GTYsmStepFrames.share(cir.getReturnValue()));
    }

    @Inject(method = "oOoo00O0o0oO0o0oO00OO0O0(Ljava/util/List;I)Lcom/elfmcys/yesstevemodel/OoO0O00OoOOo00OoOoO0o0O0;",
            at = @At("RETURN"), cancellable = true)
    private static void gt$shareSplineFrame(CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(GTYsmSplineFrames.share(cir.getReturnValue()));
    }

    @Inject(method = "oOo0OO0O0o000OO0O000oo0o(Ljava/util/List;I)Lcom/elfmcys/yesstevemodel/OoO0O00OoOOo00OoOoO0o0O0;",
            at = @At("RETURN"), cancellable = true)
    private static void gt$shareLinearFrame(CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(GTYsmLinearFrames.share(cir.getReturnValue()));
    }
}
