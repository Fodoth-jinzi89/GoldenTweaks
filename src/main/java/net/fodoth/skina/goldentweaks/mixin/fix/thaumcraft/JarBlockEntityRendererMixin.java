package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.compat.renderblender.GTCosmicJarRenderQueue;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    @Inject(
            method = "render(Lthaumcraft/common/blockentities/JarBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("RETURN")
    )
    private void gt$renderCosmicLayers(JarBlockEntity jar, float partialTicks, PoseStack pose,
                                       MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
        Aspect aspect = jar.getAspect();
        Aspect filter = jar.getFilter();
        if ((aspect == null || !GTAspectEntry.isCosmic(aspect.getTag()))
                && (filter == null || !GTAspectEntry.isCosmic(filter.getTag()))) {
            return;
        }
        if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
            if (aspect != null && GTAspectEntry.isCosmic(aspect.getTag())) {
                bufferSource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
            }
            if (filter != null && GTAspectEntry.isCosmic(filter.getTag())) {
                bufferSource.endBatch(RenderType.entityTranslucent(filter.image()));
            }
        }
        GTCosmicJarRenderQueue.enqueue(jar, pose, light, overlay);
        if (!IrisCompat.isShaderPackEnabled()) {
            GTCosmicJarRenderQueue.renderAll();
        }
    }
}
