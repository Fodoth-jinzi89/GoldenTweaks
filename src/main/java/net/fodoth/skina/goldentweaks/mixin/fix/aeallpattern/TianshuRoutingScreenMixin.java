package net.fodoth.skina.goldentweaks.mixin.fix.aeallpattern;

import io.github.langqi99.aeallpattern.tianshu.TianshuRoutingScreen;
import net.fodoth.skina.goldentweaks.mixin.fix.aeallpattern.accessor.AbstractContainerScreenAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TianshuRoutingScreen.class)
public abstract class TianshuRoutingScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void goldentweaks$extendDownward(CallbackInfo ci) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this;
        accessor.goldentweaks$setImageHeight(accessor.goldentweaks$getImageHeight() + 20);
    }
}
