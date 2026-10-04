package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.renderblender.GTCosmicJarRenderQueue;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private static boolean gt$cosmicOverlayFailed;

    @Inject(
            method = "render(Lthaumcraft/common/blockentities/JarBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("RETURN")
    )
    private void gt$renderCosmicLayers(JarBlockEntity jar, float partialTicks, PoseStack pose,
                                       MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
        // renderblender 与 GT 的 cosmic 渲染均为可选叠加层，且本 mixin 不依赖 renderblender 存在
        // （IrisCompat 直接引用其类）；方块实体渲染器抛出的任何异常都会经
        // BlockEntityRenderDispatcher 包装成 ReportedException 直接崩游戏，
        // 因此这里整体兜底：失败时退回原版渲染，绝不向上抛出。
        try {
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
        } catch (Throwable t) {
            if (!gt$cosmicOverlayFailed) {
                gt$cosmicOverlayFailed = true;
                GoldenTweaks.LOGGER.warn("[GT] cosmic jar overlay skipped: {}", t.toString());
            }
        }
    }
}
