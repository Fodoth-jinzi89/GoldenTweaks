package net.fodoth.skina.goldentweaks.mixin.fix.extendedaeplusslots;

import com.extendedae_plus.compat.UpgradeSlotCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * extendedae_plus allocates 2 upgrade slots (5 for ExtendedAE providers) on the AppliedFlux pattern-provider upgrade
 * inventory, which is too few for the pack: the induction / virtual-completion / channel cards plus the
 * result-import card need four (seven for ExtendedAE). Bump the count by two so every card lives in the single
 * shared inventory and therefore works from any of the sidebar slots.
 */
@Mixin(UpgradeSlotCompat.class)
public abstract class UpgradeSlotCompatMixin {

    @Inject(method = "getPatternProviderAppfluxUpgradeSlots(Ljava/lang/Object;)I", at = @At("RETURN"), cancellable = true)
    private static void gt$growAppfluxSlots(Object host, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + 2);
    }

    @Inject(method = "getPatternProviderLocalUpgradeSlots(Ljava/lang/Object;)I", at = @At("RETURN"), cancellable = true)
    private static void gt$growLocalSlots(Object host, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + 2);
    }
}
