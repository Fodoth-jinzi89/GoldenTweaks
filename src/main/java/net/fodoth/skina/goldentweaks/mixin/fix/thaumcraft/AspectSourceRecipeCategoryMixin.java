package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 「要素来源」页面：将要素源质图标从 x=42 左移 5 像素到 x=37。
 * 要素安瓿的物品偏移由 {@code fix/emi/SlotWidgetMixin} 处理（槽位框不动，仅物品位移）。
 */
@Mixin(targets = "thaumcraft.integration.jei.AspectSourceRecipeCategory", remap = false)
public abstract class AspectSourceRecipeCategoryMixin {

    @ModifyArg(
            method = "draw*",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/client/gui/AspectGuiRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/Aspect;IIIF)V"
            ),
            index = 2
    )
    private int gt$aspectIconLeft(int x) {
        return x - 5;
    }

    @Redirect(
            method = "draw*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"
            )
    )
    private static int gt$drawAspectNameAndSourceCount(GuiGraphics graphics, Font font, Component title,
                                                       int x, int y, int color, boolean shadow) {
        if (!(title.getContents() instanceof TranslatableContents contents)) {
            return graphics.drawString(font, title, x, y, color, shadow);
        }
        Object[] arguments = contents.getArgs();
        if (arguments.length < 2 || !(arguments[0] instanceof Component aspectName)
                || !(arguments[1] instanceof Number sourceCount)) {
            return graphics.drawString(font, title, x, y, color, shadow);
        }
        Component count = Component.literal(Integer.toString(sourceCount.intValue()));
        Component name = Component.literal(aspectName.getString());
        graphics.drawString(font, count, 34 - font.width(count), y, color, shadow);
        return graphics.drawString(font, name, x, y, color, shadow);
    }
}
