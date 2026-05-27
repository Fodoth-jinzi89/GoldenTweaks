package net.fodoth.skina.goldentweaks.mixin.fix.cdg;

import com.jesz.createdieselgenerators.CDGSpriteShifts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fix: prevent CDGSpriteShifts from initializing during <clinit>
 * because Create/Catnip sprite stitching is not thread-safe during parallel mod loading.
 */
@Mixin(CDGSpriteShifts.class)
public class CDGSpriteShiftsMixin {

    /**
     * Cancel static initialization entirely.
     * We will re-trigger initialization safely during client setup.
     */
    @Inject(method = "<clinit>", at = @At("HEAD"), cancellable = true)
    private static void goldenTweaks$cancelClinit(CallbackInfo ci) {
        ci.cancel();
    }
}