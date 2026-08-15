package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.renderers.blockentity.JarBlockEntityRenderer;
import thaumcraft.common.blockentities.JarBlockEntity;

/**
 * 源质罐子内部的源质块（立方体）对声明了 {@code "cosmic": true} 的要素
 * （如 dense）改用 renderblender 的 cosmic RenderType 渲染，使罐内源质表面
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
    private static final double PI_OVER_360 = Math.PI / 360.0;

    @Unique
    private static final String RENDER_TYPES_CLASS = "net.weibai.renderblender.client.shader.AvaritiaRenderTypes";
    @Unique
    private static final String SHADERS_CLASS = "net.weibai.renderblender.client.shader.AvaritiaShaders";

    @Unique
    private static RenderType cosmicRenderType;
    @Unique
    private static boolean cosmicResolved;

    @Redirect(
            method = "render(Lthaumcraft/common/blockentities/JarBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private static VertexConsumer gt$cosmicEssentiaCube(MultiBufferSource buffers, RenderType type, JarBlockEntity jar) {
        Aspect aspect = jar.getAspect();
        if (aspect != null && GTAspectEntry.isCosmic(aspect.getTag())) {
            RenderType cosmic = cosmicRenderType();
            if (cosmic != null) {
                setupCosmicUniforms();
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
    private static VertexConsumer gt$cosmicLabelIcon(MultiBufferSource buffers, RenderType type, JarBlockEntity jar) {
        Aspect aspect = jar.getFilter();
        if (aspect != null && GTAspectEntry.isCosmic(aspect.getTag())) {
            RenderType cosmic = cosmicRenderType();
            TextureAtlasSprite mask = maskSprite(aspect.getTag());
            if (cosmic != null && mask != null) {
                setupCosmicUniforms();
                return mask.wrap(buffers.getBuffer(cosmic));
            }
        }
        return buffers.getBuffer(type);
    }

    /** 与 renderblender {@code renderCosmicLayer} 相同的 uniform 设置（jar 无物品栈）。 */
    @Unique
    private static void setupCosmicUniforms() {
        try {
            Class<?> shaders = Class.forName(SHADERS_CLASS);
            ClientLevel level = Minecraft.getInstance().level;
            Player player = Minecraft.getInstance().player;
            float time = (float) (level != null ? level.getGameTime() % 2147483647L : 0L);
            float yaw = player != null ? (float) (player.getYRot() * PI_OVER_360) : 0.0F;
            float pitch = player != null ? (float) (-player.getXRot() * PI_OVER_360) : 0.0F;
            setUniform(shaders, "cosmicTime", time);
            setUniform(shaders, "cosmicBgColor", 0.0F);
            setUniform(shaders, "cosmicYaw", yaw);
            setUniform(shaders, "cosmicPitch", pitch);
            setUniform(shaders, "cosmicExternalScale", 1.0F);
            setUniform(shaders, "cosmicOpacity", 2.0F);
            AbstractUniform uvs = (AbstractUniform) shaders.getField("cosmicUVs").get(null);
            uvs.set((float[]) shaders.getField("COSMIC_UVS").get(null));
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.warn("[GT] renderblender cosmic uniform setup failed: {}", t.toString());
        }
    }

    @Unique
    private static void setUniform(Class<?> shaders, String field, float value) throws Exception {
        AbstractUniform uniform = (AbstractUniform) shaders.getField(field).get(null);
        uniform.set(value);
    }

    /** 标签图标使用的 cosmic mask（方块图集内，与 aspect_icon 模型同一张）。 */
    @Unique
    private static TextureAtlasSprite maskSprite(String tag) {
        try {
            return Minecraft.getInstance().getModelManager()
                    .getAtlas(InventoryMenu.BLOCK_ATLAS)
                    .getSprite(ResourceLocation.fromNamespaceAndPath("goldentweaks", "mask/item/aspect_" + tag));
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.warn("[GT] cosmic label mask lookup failed: {}", t.toString());
            return null;
        }
    }

    @Unique
    private static RenderType cosmicRenderType() {
        if (!cosmicResolved) {
            cosmicResolved = true;
            try {
                Class<?> renderTypes = Class.forName(RENDER_TYPES_CLASS);
                cosmicRenderType = (RenderType) renderTypes.getField("COSMIC").get(null);
            } catch (Throwable t) {
                GoldenTweaks.LOGGER.warn("[GT] renderblender cosmic render type unavailable: {}", t.toString());
            }
        }
        return cosmicRenderType;
    }
}
