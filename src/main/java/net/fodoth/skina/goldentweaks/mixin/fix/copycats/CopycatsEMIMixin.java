package net.fodoth.skina.goldentweaks.mixin.fix.copycats;

import com.copycatsplus.copycats.compat.recipe_viewers.CopycatsEMI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CopycatsEMI.class)
public class CopycatsEMIMixin {

    @Inject(
            method = "refreshItemList",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$disableRefresh(CallbackInfo ci) {
        ci.cancel();
    }
}
