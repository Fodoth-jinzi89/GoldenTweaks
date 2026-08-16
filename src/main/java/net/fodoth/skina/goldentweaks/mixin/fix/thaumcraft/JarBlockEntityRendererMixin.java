package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fodoth.skina.goldentweaks.compat.renderblender.GTCosmicJarRenderQueue;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.weibai.renderblender.client.compat.IrisCompat;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.renderers.blockentity.JarBlockEntityRenderer;
import thaumcraft.common.blockentities.JarBlockEntity;

/**
 * 源质罐子内部的源质块（立方体）对声明了 {@code "cosmic": true} 的要素
 * （如 cosmic）改用 renderblender 的 cosmic RenderType 渲染，使罐内源质表面
 * 呈现 cosmic 星云效果；非 cosmic 要素保持原样。
 * <p>
 * {@link JarBlockEntityRenderer#render} 中源质立方体是唯一一次
 * {@code MultiBufferSource.getBuffer} 调用（标签在独立的 renderLabel 中绘制），
 * 因此只在 render() 主体内重定向这一次即可。
 * <p>
 * 通过反射访问 renderblender 的 {@code AvaritiaRenderTypes.COSMIC} 与
 * {@code AvaritiaShaders} uniform（该库不在编译类路径上）；renderblender
 * 缺失时自动降级为原样渲染。
 */
@Mixin(value = JarBlockEntityRenderer.class, remap = false)
public class JarBlockEntityRendererMixin {

    @Redirect(
            method = "render(Lthaumcraft/common/blockentities/JarBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private static VertexConsumer gt$cosmicEssentiaCube(MultiBufferSource buffers, RenderType type,
                                                        JarBlockEntity jar, float partialTicks, PoseStack pose,
                                                        MultiBufferSource renderBuffers, int light, int overlay) {
        Aspect aspect = jar.getAspect();
        if (aspect != null && GTAspectEntry.isCosmic(aspect.getTag())) {
            if (IrisCompat.isShaderPackEnabled()) {
                GTCosmicJarRenderQueue.enqueue(jar, pose, light, overlay);
                return buffers.getBuffer(type);
            }
            RenderType cosmic = GTCosmicJarRenderQueue.cosmicRenderType();
            if (cosmic != null) {
                GTCosmicJarRenderQueue.setupCosmicUniforms();
                return buffers.getBuffer(cosmic);
            }
        }
        return buffers.getBuffer(type);
    }

    /** 标签上的要素图标（renderLabel 中第二处 getBuffer：entityTranslucent(aspect.image())）也改用 cosmic。 */
    @Redirect(
            method = "renderLabel(Lthaumcraft/common/blockentities/JarBlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
                    ordinal = 1)
    )
    private static VertexConsumer gt$cosmicLabelIcon(MultiBufferSource buffers, RenderType type,
                                                     JarBlockEntity jar, PoseStack pose,
                                                     MultiBufferSource renderBuffers, int light) {
        Aspect aspect = jar.getFilter();
        if (aspect != null && GTAspectEntry.isCosmic(aspect.getTag())) {
            if (IrisCompat.isShaderPackEnabled()) {
                GTCosmicJarRenderQueue.enqueue(jar, pose, light, 0);
                // 保留标签原始图标；星空层由 AFTER_BLOCK_ENTITIES 队列叠加绘制。
                return buffers.getBuffer(type);
            }
            RenderType cosmic = GTCosmicJarRenderQueue.cosmicRenderType();
            TextureAtlasSprite mask = GTCosmicJarRenderQueue.maskSprite(aspect.getTag());
            if (cosmic != null && mask != null) {
                GTCosmicJarRenderQueue.setupCosmicUniforms();
                return mask.wrap(buffers.getBuffer(cosmic));
            }
        }
        return buffers.getBuffer(type);
    }
}
