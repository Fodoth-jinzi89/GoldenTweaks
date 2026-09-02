package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import com.mojang.math.Axis;
import thaumcraft.client.renderers.blockentity.ThaumatoriumBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ThaumatoriumBlockEntityRenderer.class, remap = false)
public abstract class ThaumatoriumRendererMixin {
    @Redirect(method = "render(Lthaumcraft/common/blockentities/ThaumatoriumBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;"))
    private org.joml.Quaternionf gt$swapNorthSouth(Axis axis, float degrees) {
        return axis.rotationDegrees(degrees == 0.0F ? 180.0F : degrees == 180.0F ? 0.0F : degrees);
    }
}
