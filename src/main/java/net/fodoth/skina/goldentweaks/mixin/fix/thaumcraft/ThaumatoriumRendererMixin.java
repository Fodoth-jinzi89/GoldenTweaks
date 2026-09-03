package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import com.mojang.blaze3d.vertex.PoseStack;
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

    @Redirect(method = "render(Lthaumcraft/common/blockentities/ThaumatoriumBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void gt$liftDisplayItem(PoseStack poseStack, float x, float y, float z) {
        poseStack.translate(x, y + 3.0F / 16.0F, z);
    }

    @Redirect(method = "render(Lthaumcraft/common/blockentities/ThaumatoriumBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void gt$halveDisplayItem(PoseStack poseStack, float x, float y, float z) {
        poseStack.scale(x * 0.5F, y * 0.5F, z * 0.5F);
    }
}
