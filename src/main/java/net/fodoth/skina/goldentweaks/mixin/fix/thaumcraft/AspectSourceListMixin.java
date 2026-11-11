package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.ThaumonomiconAspectSources;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.gui.thaumonomicon.ResearchPageRenderer;

/**
 * 用 {@link ThaumonomiconAspectSources} 完全接管「要素来源」浮窗的绘制
 * （0.3.0 自带的那套分页/加载不可靠，这里整体替换）。
 */
@Mixin(value = ResearchPageRenderer.class, remap = false)
public abstract class AspectSourceListMixin {

    @Inject(method = "drawScannedAspectSources", at = @At("HEAD"), cancellable = true)
    private void gt$renderAspectSourceList(GuiGraphics graphics, Aspect aspect, int x, int y, int height,
                                           Player player, CallbackInfo ci) {
        ThaumonomiconAspectSources.render(graphics, aspect, x, y, height, player);
        ci.cancel();
    }
}
