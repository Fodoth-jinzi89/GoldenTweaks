package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftAdditionalItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.gui.AspectGuiRenderer;

/**
 * 1) 跳过已着色图标（如奇点 singularity）的要素色二次染色。
 * 2) 声明了 {@code "cosmic": true} 的要素（如 dense）图标改由 cosmic 渲染：
 *    在 GUI 中通过隐藏代理物品绘制，覆盖魔导手册、JEI 要素配方等所有
 *    经由 {@link AspectGuiRenderer#draw} 绘制的界面。
 * <p>
 * {@link AspectGuiRenderer#draw} 系列方法最终都以 {@code aspect.color()}
 * 作为顶点颜色绘制图标；此处对要素 JSON 中声明了 {@code "tint": false}
 * 的要素返回白色（即不着色），其余要素保持原行为。
 * 要素安瓿/天域之华的物品染色不受影响。
 */
@Mixin(value = AspectGuiRenderer.class, remap = false)
public class AspectGuiRendererMixin {

    /** 所有 4 参/6 参 draw 最终都进入该 6 参方法。 */
    @Inject(
            method = "draw(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/Aspect;IIIF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gt$cosmicIcon(GuiGraphics graphics, Aspect aspect, int x, int y, int size, float alpha, CallbackInfo ci) {
        if (GTAspectEntry.isCosmic(aspect.getTag())) {
            renderCosmicIcon(graphics, x, y, size);
            ci.cancel();
        }
    }

    @Inject(
            method = "drawTinted(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/Aspect;IIIIF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gt$cosmicIconTinted(GuiGraphics graphics, Aspect aspect, int x, int y, int size, int color, float alpha, CallbackInfo ci) {
        if (GTAspectEntry.isCosmic(aspect.getTag())) {
            renderCosmicIcon(graphics, x, y, size);
            ci.cancel();
        }
    }

    /**
     * 渲染 cosmic 代理物品并缩放到 {@code size} 像素。
     * <p>注意：NeoForge 1.21.1 的 {@link GuiGraphics#renderItem(ItemStack, int, int, int, int)}
     * 是 {@code (stack, x, y, seed, z)}，并没有宽高重载，必须手动缩放 pose。
     */
    private static void renderCosmicIcon(GuiGraphics graphics, int x, int y, int size) {
        ItemStack stack = GTThaumcraftAdditionalItems.denseIconStack();
        graphics.pose().pushPose();
        graphics.pose().translate(x + size / 2.0F, y + size / 2.0F, 0.0F);
        graphics.pose().scale(size / 16.0F, size / 16.0F, 1.0F);
        graphics.pose().translate(-8.0F, -8.0F, 0.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    @Redirect(
            method = "draw(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/Aspect;IIIF)V",
            at = @At(value = "INVOKE", target = "Lthaumcraft/api/aspects/Aspect;color()I")
    )
    private static int gt$skipUntintedAspectTint(Aspect aspect) {
        return GTAspectEntry.isUntinted(aspect.getTag()) ? 0xFFFFFF : aspect.color();
    }
}
