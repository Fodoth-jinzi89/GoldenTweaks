package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.ThaumonomiconAspectSources;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.client.gui.ThaumonomiconScreen;

/**
 * 「要素来源」浮窗的每帧状态重置与滚轮翻页（配合 {@link AspectSourceListMixin}）。
 */
@Mixin(value = ThaumonomiconScreen.class, remap = false)
public abstract class AspectSourceListScreenMixin {

    @Inject(method = "renderResearchDetailPages", at = @At("HEAD"))
    private void gt$resetAspectSourceList(GuiGraphics graphics, CallbackInfo ci) {
        ThaumonomiconAspectSources.resetFrame();
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void gt$scrollAspectSourceList(double mouseX, double mouseY, double scrollX, double scrollY,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (ThaumonomiconAspectSources.isOpen() && scrollY != 0) {
            ThaumonomiconAspectSources.scroll(scrollY);
            cir.setReturnValue(true);
        }
    }
}
