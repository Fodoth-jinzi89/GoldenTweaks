package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.ThaumonomiconAspectCosts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.gui.thaumonomicon.RecipePageLayout;

import java.util.List;

/**
 * 用 {@link ThaumonomiconAspectCosts} 接管要素耗费的槽位排布：
 * 奥术/注魔 2 行 × 7，坩埚每页 8（3/3/2）；两者都带轮播。
 */
@Mixin(value = RecipePageLayout.class, remap = false)
public abstract class RecipePageAspectLayoutMixin {

    @Inject(method = "centeredAspectCost", at = @At("HEAD"), cancellable = true)
    private static void gt$centeredAspectCost(AspectList aspects, int x, int y, int width,
                                              CallbackInfoReturnable<List<RecipePageLayout.AspectSlot>> cir) {
        cir.setReturnValue(ThaumonomiconAspectCosts.centered(aspects, x, y, width));
    }

    @Inject(method = "crucibleAspectCost", at = @At("HEAD"), cancellable = true)
    private static void gt$crucibleAspectCost(AspectList aspects, int pageX, int pageY,
                                              CallbackInfoReturnable<List<RecipePageLayout.AspectSlot>> cir) {
        cir.setReturnValue(ThaumonomiconAspectCosts.crucible(aspects, pageX, pageY));
    }
}
