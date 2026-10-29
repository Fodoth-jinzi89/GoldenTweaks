package net.fodoth.skina.goldentweaks.mixin.feature.academy;

import com.mohistmc.academy.client.renderer.RailgunBeamRenderer;
import com.mohistmc.academy.entity.RailgunBeamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.compat.academy.GTBeamScale;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 按光束实体携带的粗细倍率（见 {@link GTBeamScale}）缩放可见光束的宽度。
 *
 * <p>{@code render} 里光晕与核心两层都通过 {@code renderBillboardBeam} 绘制，因此直接按倍率
 * 放大该方法的 {@code width} 参数即可同时加粗两层光束。长度已由服务端写入的光束长度决定。</p>
 */
@Mixin(RailgunBeamRenderer.class)
public class RailgunBeamRendererVariantMixin {

    @Unique
    private float gt$beamScale = 1.0F;

    @Inject(method = "render(Lcom/mohistmc/academy/entity/RailgunBeamEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void gt$captureScale(RailgunBeamEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        this.gt$beamScale = ((GTBeamScale) entity).gt$getBeamScale();
    }

    @ModifyArg(method = "render(Lcom/mohistmc/academy/entity/RailgunBeamEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/mohistmc/academy/client/renderer/RailgunBeamRenderer;renderBillboardBeam(Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/Vec3;DLnet/minecraft/world/phys/Vec3;FFFFFI)V"),
            index = 5)
    private float gt$scaleBeamWidth(float width) {
        return this.gt$beamScale == 1.0F ? width : width * this.gt$beamScale;
    }
}
