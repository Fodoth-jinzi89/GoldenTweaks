package net.fodoth.skina.goldentweaks.mixin.fix.renderblender;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fodoth.skina.goldentweaks.compat.renderblender.GTCosmicJarRenderQueue;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.weibai.renderblender.client.model.loader.CosmicBakeModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 第一人称手持物品的 cosmic 层延迟重绘时改用无深度测试的渲染类型。
 * <p>
 * Iris 用 z 压缩投影（世界投影 × scale(1,1,0.125)）把手部写进 gbuffer，
 * 物品在合成后的深度缓冲里深度被压缩；延迟到 AFTER_LEVEL 重绘的星空层
 * 用世界投影、处于真实深度，LEQUAL 深度测试下会被物品自身的压缩深度遮挡，
 * 表现为星空渲染在物品背后、随视场角变化忽隐忽现。
 * 这里对第一人称上下文改用 {@link GTCosmicJarRenderQueue#cosmicNoDepthRenderType()}
 * （无深度测试），星空直接覆盖在手持物品上。第三人称与展示框等仍走
 * 世界渲染上下文、深度一致，保持原渲染类型。
 */
@Mixin(value = CosmicBakeModel.class, remap = false)
public class CosmicRenderLayerDepthMixin {

    @Redirect(
            method = "renderCosmicLayer(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
                    ordinal = 0)
    )
    private VertexConsumer gt$noDepthFirstPersonLayer(MultiBufferSource buffers, RenderType type,
                                                      ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                                                      MultiBufferSource renderBuffers, int light, int overlay) {
        if ((context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                && GTCosmicJarRenderQueue.cosmicNoDepthRenderType() != null) {
            return buffers.getBuffer(GTCosmicJarRenderQueue.cosmicNoDepthRenderType());
        }
        return buffers.getBuffer(type);
    }
}
