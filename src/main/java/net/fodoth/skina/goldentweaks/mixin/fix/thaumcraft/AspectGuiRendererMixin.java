package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.gui.AspectGuiRenderer;

/**
 * 跳过已着色图标（如奇点 singularity）的要素色二次染色。
 * <p>
 * {@link AspectGuiRenderer#draw} 系列方法最终都以 {@code aspect.color()}
 * 作为顶点颜色绘制图标；此处对要素 JSON 中声明了 {@code "tint": false}
 * 的要素返回白色（即不着色），其余要素保持原行为。
 * 要素安瓿/天域之华的物品染色不受影响。
 */
@Mixin(value = AspectGuiRenderer.class, remap = false)
public class AspectGuiRendererMixin {

    @Redirect(
            method = "draw(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/Aspect;IIIF)V",
            at = @At(value = "INVOKE", target = "Lthaumcraft/api/aspects/Aspect;color()I")
    )
    private static int gt$skipUntintedAspectTint(Aspect aspect) {
        return GTAspectEntry.isUntinted(aspect.getTag()) ? 0xFFFFFF : aspect.color();
    }
}
