package net.fodoth.skina.goldentweaks.compat.renderblender;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.renderers.blockentity.JarBlockEntityRenderer;
import thaumcraft.common.blockentities.JarBlockEntity;
import thaumcraft.common.config.TCClientConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 源质罐子 cosmic 渲染的延迟队列。
 *
 * <p>光影（Iris shaderpack）启用时，罐子内部的源质立方体与盖上的要素图标走
 * renderblender 的自定义 core shader（cosmic RenderType），该渲染发生在
 * Iris/sodium 的世界渲染管线内，会被直接丢弃（罐内星云与标签图标消失）。
 * 与手持物品相同，绕开方式是延迟到 {@code RenderLevelStageEvent$Stage.AFTER_LEVEL}
 * 阶段统一渲染（此时世界管线已结束，自定义 shader 走 vanilla 路径正常生效）。
 *
 * <p>队列为空时 {@code renderAll()} 直接返回，无渲染开销。
 * 几何通过反射调用 Thaumcraft 私有的 {@code JarBlockEntityRenderer.cube} /
 * {@code orientedQuad}，与原生渲染保持一致。
 */
public final class GTCosmicJarRenderQueue {

    private record JarRenderCall(
            JarBlockEntity jar,
            PoseStack pose,
            int light,
            int overlay,
            Matrix4f projection,
            Matrix4f modelView
    ) {
    }

    private static final List<JarRenderCall> QUEUE = new ArrayList<>();

    private static Method cubeMethod;
    private static Method orientedQuadMethod;
    private static Method atLeastLegacyLightMethod;

    private static final double PI_OVER_360 = Math.PI / 360.0;

    private static final String RENDER_TYPES_CLASS = "net.weibai.renderblender.client.shader.AvaritiaRenderTypes";
    private static final String SHADERS_CLASS = "net.weibai.renderblender.client.shader.AvaritiaShaders";
    private static final String RENDER_UTILS_CLASS = "net.weibai.renderblender.api.utils.RenderUtils";

    private static RenderType cosmicRenderType;
    private static boolean cosmicResolved;

    private static RenderType cosmicNoDepthRenderType;
    private static boolean cosmicNoDepthResolved;

    private GTCosmicJarRenderQueue() {
    }

    public static void enqueue(JarBlockEntity jar, PoseStack pose, int light, int overlay) {
        for (JarRenderCall call : QUEUE) {
            if (call.jar() == jar) {
                return;
            }
        }
        PoseStack poseCopy = new PoseStack();
        poseCopy.last().pose().set(pose.last().pose());
        poseCopy.last().normal().set(pose.last().normal());
        QUEUE.add(new JarRenderCall(
                jar,
                poseCopy,
                light,
                overlay,
                new Matrix4f(RenderSystem.getProjectionMatrix()),
                new Matrix4f(RenderSystem.getModelViewMatrix())
        ));
    }

    public static void renderAll() {
        if (QUEUE.isEmpty()) {
            return;
        }
        // 保存并恢复矩阵，避免遗留状态污染同帧后续渲染（renderblender 队列同款约定）。
        Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        Matrix4f savedMv = new Matrix4f(RenderSystem.getModelViewMatrix());
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        for (JarRenderCall call : QUEUE) {
            RenderSystem.setProjectionMatrix(call.projection(), RenderSystem.getVertexSorting());
            RenderSystem.getModelViewStack().set(call.modelView());
            RenderSystem.applyModelViewMatrix();
            renderEssentiaCube(call, buffers);
        }
        RenderType cosmic = jarCosmicRenderType();
        if (cosmic != null) {
            buffers.endBatch(cosmic);
        }
        for (JarRenderCall call : QUEUE) {
            RenderSystem.setProjectionMatrix(call.projection(), RenderSystem.getVertexSorting());
            RenderSystem.getModelViewStack().set(call.modelView());
            RenderSystem.applyModelViewMatrix();
            renderLabelIcon(call, buffers);
        }
        if (cosmic != null) {
            buffers.endBatch(cosmic);
        }
        RenderSystem.setProjectionMatrix(savedProj, RenderSystem.getVertexSorting());
        RenderSystem.getModelViewStack().set(savedMv);
        RenderSystem.applyModelViewMatrix();
        QUEUE.clear();
    }

    private static void renderEssentiaCube(JarRenderCall call, MultiBufferSource.BufferSource buffers) {
        JarBlockEntity jar = call.jar();
        Aspect aspect = jar.getAspect();
        if (aspect != null && GTAspectEntry.isCosmic(aspect.getTag()) && jar.getAmount() > 0) {
            renderEssentiaCube(aspect, jar, call, buffers);
        }
    }

    /** 与 Thaumcraft {@code JarBlockEntityRenderer.render} 中立方体一致：高度随源质量变化。 */
    private static void renderEssentiaCube(Aspect aspect, JarBlockEntity jar, JarRenderCall call,
                                           MultiBufferSource.BufferSource buffers) {
        RenderType cosmic = jarCosmicRenderType();
        TextureAtlasSprite mask = solidMaskSprite();
        if (cosmic == null || mask == null) {
            return;
        }
        setupCosmicUniforms();
        // cosmic shader 以 texCoord0 采样方块图集、按 mask.r < 0.01 丢弃并乘 mask.r 控制透明度。
        // 立方体使用纯白 mask（r=1），保证整个立方体表面都渲染星空。
        VertexConsumer consumer = mask.wrap(buffers.getBuffer(cosmic));
        float amount = (float) jar.getAmount() / jar.getMaxAmount();
        int color = aspect.color();
        float base = 0.0625f;
        float height = base + 0.625f * amount;
        int light = atLeastLegacyLight(call.light(), 200);
        PoseStack.Pose pose = call.pose().last();
        invokeCube(consumer, pose,
                0.25f, base, 0.25f, 0.75f, height, 0.75f,
                (color >> 16) & 255, (color >> 8) & 255, color & 255, 255, light);
    }

    private static void invokeCube(VertexConsumer consumer, PoseStack.Pose pose,
                                   float x0, float y0, float z0, float x1, float y1, float z1,
                                   int r, int g, int b, int a, int light) {
        try {
            if (cubeMethod == null) {
                cubeMethod = JarBlockEntityRenderer.class.getDeclaredMethod(
                        "cube", VertexConsumer.class, PoseStack.Pose.class,
                        float.class, float.class, float.class, float.class, float.class, float.class,
                        int.class, int.class, int.class, int.class, int.class);
                cubeMethod.setAccessible(true);
            }
            cubeMethod.invoke(null, consumer, pose, x0, y0, z0, x1, y1, z1, r, g, b, a, light);
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.debug("[GT] cosmic jar essentia cube render failed: {}", t.toString());
        }
    }

    /** 与 Thaumcraft {@code renderLabel} 中第二个 orientedQuad（要素图标）一致。 */
    private static void renderLabelIcon(JarRenderCall call, MultiBufferSource.BufferSource buffers) {
        JarBlockEntity jar = call.jar();
        Aspect filter = jar.getFilter();
        if (filter == null || !GTAspectEntry.isCosmic(filter.getTag())) {
            return;
        }
        RenderType cosmic = jarCosmicRenderType();
        TextureAtlasSprite mask = maskSprite(filter.getTag());
        if (cosmic == null || mask == null) {
            return;
        }
        Direction dir = Direction.from3DDataValue(jar.getFacing());
        if (!dir.getAxis().isHorizontal()) {
            dir = Direction.NORTH;
        }
        float rotation = 0.0f;
        if (TCClientConfig.crookedLabels()) {
            rotation = (float) ((filter.tag().hashCode() + jar.getBlockPos().getX() + jar.getFacing()) % 4 - 2);
        }
        setupCosmicUniforms();
        VertexConsumer consumer = mask.wrap(buffers.getBuffer(cosmic));
        invokeOrientedQuad(consumer, call.pose().last(), dir,
                0.41f, 0.168f, 0.3185f, rotation, filter.color(), 255, 15728880);
    }


    private static void invokeOrientedQuad(VertexConsumer consumer, PoseStack.Pose pose, Direction dir,
                                           float x, float y, float w, float h, int color, int alpha, int light) {
        try {
            if (orientedQuadMethod == null) {
                orientedQuadMethod = JarBlockEntityRenderer.class.getDeclaredMethod(
                        "orientedQuad",
                        VertexConsumer.class, PoseStack.Pose.class, Direction.class,
                        float.class, float.class, float.class, float.class,
                        int.class, int.class, int.class);
                orientedQuadMethod.setAccessible(true);
            }
            orientedQuadMethod.invoke(null, consumer, pose, dir, x, y, w, h, color, alpha, light);
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.debug("[GT] cosmic jar label render failed: {}", t.toString());
        }
    }

    private static int atLeastLegacyLight(int light, int min) {
        try {
            if (atLeastLegacyLightMethod == null) {
                atLeastLegacyLightMethod = Class.forName(
                                "thaumcraft.client.renderers.blockentity.LegacyBlockEntityRenderHelper")
                        .getDeclaredMethod("atLeastLegacyLight", int.class, int.class);
                atLeastLegacyLightMethod.setAccessible(true);
            }
            return (int) atLeastLegacyLightMethod.invoke(null, light, min);
        } catch (Throwable t) {
            return light;
        }
    }

    /** 与 renderblender {@code renderCosmicLayer} 相同的 uniform 设置（jar 无物品栈）。 */
    public static void setupCosmicUniforms() {
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

    private static void setUniform(Class<?> shaders, String field, float value) throws Exception {
        AbstractUniform uniform = (AbstractUniform) shaders.getField(field).get(null);
        uniform.set(value);
    }

    /** 标签图标使用的 cosmic mask（方块图集内，与 aspect_icon 模型同一张）。 */
    public static TextureAtlasSprite maskSprite(String tag) {
        try {
            return Minecraft.getInstance().getModelManager()
                    .getAtlas(InventoryMenu.BLOCK_ATLAS)
                    .getSprite(ResourceLocation.fromNamespaceAndPath("goldentweaks", "mask/item/aspect_" + tag));
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.warn("[GT] cosmic label mask lookup failed: {}", t.toString());
            return null;
        }
    }

    /** 源质立方体使用已被方块图集收录的纯白纹理，保证整个立方体表面都渲染星空。 */
    public static TextureAtlasSprite solidMaskSprite() {
        try {
            return Minecraft.getInstance().getModelManager()
                    .getAtlas(InventoryMenu.BLOCK_ATLAS)
                    .getSprite(ResourceLocation.withDefaultNamespace("block/white_concrete"));
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.warn("[GT] cosmic solid mask lookup failed: {}", t.toString());
            return null;
        }
    }

    public static RenderType cosmicRenderType() {
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

    public static RenderType jarCosmicRenderType() {
        return cosmicRenderType();
    }

    /**
     * 与 {@link #cosmicRenderType()} 相同的 cosmic RenderType，但深度测试关闭。
     * <p>延迟到 AFTER_LEVEL 重绘时，深度缓冲里已有罐体前表面的深度，源质立方体
     * 在罐内（深度更大）会被 LEQUAL 剔除而不可见；关掉深度测试即可直接覆盖绘制。
     * 构建失败时回退到普通 cosmic RenderType。
     */
    public static RenderType cosmicNoDepthRenderType() {
        if (!cosmicNoDepthResolved) {
            cosmicNoDepthResolved = true;
            try {
                Class<?> shaders = Class.forName(SHADERS_CLASS);
                net.minecraft.client.renderer.ShaderInstance shader =
                        (net.minecraft.client.renderer.ShaderInstance) shaders.getField("COSMIC_SHADER").get(null);
                Class<?> renderUtils = Class.forName(RENDER_UTILS_CLASS);
                RenderStateShard.EmptyTextureStateShard texture =
                        (RenderStateShard.EmptyTextureStateShard) renderUtils.getField("COSMIC_TEXTURE_ISOLATED").get(null);
                RenderStateShard.LayeringStateShard layering =
                        (RenderStateShard.LayeringStateShard) renderUtils.getField("POLYGON_OFFSET_LAYERING").get(null);
                // 每次绘制时重新读取 COSMIC_SHADER 字段：光影包切换/资源重载会重新注册
                // shader（新 ShaderInstance），不能捕获首次构建时的旧实例（已解绑、绘制无效）。
                RenderStateShard.ShaderStateShard shaderState =
                        new RenderStateShard.ShaderStateShard(() -> {
                            try {
                                return (net.minecraft.client.renderer.ShaderInstance)
                                        Class.forName(SHADERS_CLASS).getField("COSMIC_SHADER").get(null);
                            } catch (Throwable t) {
                                return null;
                            }
                        });
                cosmicNoDepthRenderType = RenderType.create(
                        "goldentweaks:cosmic_no_depth",
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS,
                        2097152, true, false,
                        RenderType.CompositeState.builder()
                                .setShaderState(shaderState)
                                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                                .setCullState(RenderStateShard.NO_CULL)
                                .setLightmapState(RenderStateShard.LIGHTMAP)
                                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                                .setTextureState(texture)
                                .setLayeringState(layering)
                                .createCompositeState(true));
            } catch (Throwable t) {
                GoldenTweaks.LOGGER.warn("[GT] renderblender cosmic no-depth render type unavailable: {}", t.toString());
            }
        }
        return cosmicNoDepthRenderType != null ? cosmicNoDepthRenderType : cosmicRenderType();
    }
}
