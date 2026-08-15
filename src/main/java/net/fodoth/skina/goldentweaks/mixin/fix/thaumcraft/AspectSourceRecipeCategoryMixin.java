package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 「要素来源」页面：将要素源质图标从 x=42 左移 5 像素到 x=37。
 * 要素安瓿的物品偏移由 {@code fix/emi/SlotWidgetMixin} 处理（槽位框不动，仅物品位移）。
 */
@Mixin(targets = "thaumcraft.integration.jei.AspectSourceRecipeCategory", remap = false)
public abstract class AspectSourceRecipeCategoryMixin {

    @ModifyArg(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/client/gui/AspectGuiRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/Aspect;IIIF)V"
            ),
            index = 2
    )
    private int gt$aspectIconLeft(int x) {
        return x - 5;
    }
}
