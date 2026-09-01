package net.fodoth.skina.goldentweaks.mixin.fix.ae2helpers;

import appeng.menu.me.crafting.CraftConfirmMenu;
import net.fodoth.skina.goldentweaks.compat.ae2helpers.WcwtPendingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftConfirmMenu.class, remap = false)
public abstract class CraftConfirmMenuMixin {

    @Inject(method = "startJob", at = @At("HEAD"))
    private void gt$activateWcwtPending(CallbackInfo ci) {
        if (((CraftConfirmMenu) (Object) this).isClientSide()) {
            WcwtPendingHelper.activateStaged();
        }
    }
}
