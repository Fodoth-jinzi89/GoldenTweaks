package net.fodoth.skina.goldentweaks.mixin.fix.silentgear;

import dev.emi.emi.registry.EmiStackList;
import net.silentchaos512.gear.api.item.GearItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// EMI adds every registered item to its index independently of creative tabs,
// so the base gear items would still show up even with an empty gear tab.
@Mixin(value = EmiStackList.class, remap = false)
public class SilentGearEmiHiderMixin {

    @Inject(method = "isHiddenFromRecipeViewers", at = @At("HEAD"), cancellable = true)
    private static <T> void gt$hideGearItems(T value, CallbackInfoReturnable<Boolean> cir) {
        if (value instanceof GearItem) {
            cir.setReturnValue(true);
        }
    }
}
