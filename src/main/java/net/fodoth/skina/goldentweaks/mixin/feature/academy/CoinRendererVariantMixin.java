package net.fodoth.skina.goldentweaks.mixin.feature.academy;

import com.mohistmc.academy.client.entity.CoinRenderer;
import com.mohistmc.academy.world.entity.CoinEntity;
import net.fodoth.skina.goldentweaks.compat.academy.GTThermalRailgunCoins;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 飞行中的硬币按来源硬币渲染：热力硬币用其自身的贴图（每种金属不同颜色），学院币保持原贴图。
 */
@Mixin(CoinRenderer.class)
public class CoinRendererVariantMixin {

    @Unique
    private CoinEntity gt$renderingCoin;

    @Inject(method = "render(Lcom/mohistmc/academy/world/entity/CoinEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void gt$captureCoin(CoinEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                                MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        this.gt$renderingCoin = entity;
    }

    @ModifyArg(method = "render(Lcom/mohistmc/academy/world/entity/CoinEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"),
            index = 0)
    private ResourceLocation gt$variantTexture(ResourceLocation original) {
        return GTThermalRailgunCoins.entityTexture(this.gt$renderingCoin, original);
    }
}
