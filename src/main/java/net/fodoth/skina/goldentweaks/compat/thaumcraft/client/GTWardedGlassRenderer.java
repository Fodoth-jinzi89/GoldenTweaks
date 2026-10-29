package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import thaumcraft.common.registry.TCBlocks;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 守卫者玻璃的旧版（thaumcraft port.152）外观：由 GoldenTweaks 逐帧自绘连接纹理面。
 *
 * <p>port.278 起神秘时代把守卫者玻璃的面烘进了区块网格（{@code WardedGlassBakedModel}，连接
 * 纹理经 {@code ModelData} 下发），视觉上是带朝向明暗的区块几何体，且物品模型那一趟
 * （{@code side == null}）直接返回空列表 → 物品栏 / 手持 / 破坏粒子什么都不显示。这里按旧版
 * {@code WardedGlassConnectedRenderer} 的做法还原：只在相机侧、且相邻不是守卫者玻璃的面上
 * 贴对应连接纹理，用不带光照的 {@code POSITION_TEX_COLOR} 在 {@code RenderLevelStageEvent} 里自绘，
 * 不写深度、不剔除背面，按距离由远到近排序。</p>
 *
 * <p>搭配 {@code WardedGlassBakedModelMixin}：方块面交回这里画，无朝向那一趟交还原模型。</p>
 */
public final class GTWardedGlassRenderer {

    /** 每次扫描的取样范围（格），与旧版一致。 */
    private static final int HORIZONTAL_RADIUS = 28;
    private static final int VERTICAL_RADIUS = 20;
    /** 两次扫描的最小间隔（tick）。 */
    private static final int SCAN_INTERVAL_TICKS = 10;
    /** 连接纹理查表：8 位邻接位掩码 → 纹理序号。 */
    private static final int[] CONNECTED_TEXTURE_REF = createConnectedTextureRef();
    private static final ResourceLocation[] TEXTURES = createTextures();
    private static final RenderType[] RENDER_TYPES = createRenderTypes();

    private static List<BlockPos> cachedGlass = List.of();
    private static BlockPos lastCenter;
    private static long nextScanTick;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            cachedGlass = List.of();
            lastCenter = null;
            return;
        }

        BlockPos center = BlockPos.containing(event.getCamera().getPosition());
        long gameTime = level.getGameTime();
        if (lastCenter == null || lastCenter.distSqr(center) > 16.0 || gameTime >= nextScanTick) {
            rescan(level, center, gameTime);
        }

        if (!cachedGlass.isEmpty()) {
            renderGlass(minecraft, level, event.getCamera().getPosition());
        }
    }

    private static void rescan(ClientLevel level, BlockPos center, long gameTime) {
        List<BlockPos> found = new ArrayList<>();
        BlockPos from = center.offset(-HORIZONTAL_RADIUS, -VERTICAL_RADIUS, -HORIZONTAL_RADIUS);
        BlockPos to = center.offset(HORIZONTAL_RADIUS, VERTICAL_RADIUS, HORIZONTAL_RADIUS);

        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (isWardedGlass(level, pos)) {
                found.add(pos.immutable());
            }
        }

        cachedGlass = List.copyOf(found);
        lastCenter = center.immutable();
        nextScanTick = gameTime + SCAN_INTERVAL_TICKS;
    }

    private static void renderGlass(Minecraft minecraft, ClientLevel level, Vec3 camera) {
        List<GlassFace> faces = new ArrayList<>();

        for (BlockPos pos : cachedGlass) {
            if (!isWardedGlass(level, pos)) {
                continue;
            }
            for (Direction face : Direction.values()) {
                if (!isWardedGlass(level, pos.relative(face)) && isFaceOnCameraSide(pos, face, camera)) {
                    faces.add(new GlassFace(pos, face, connectedTexture(level, pos, sideId(face)),
                            distanceToCamera(pos, face, camera)));
                }
            }
        }

        if (faces.isEmpty()) {
            return;
        }

        faces.sort((left, right) -> Double.compare(right.distanceToCamera, left.distanceToCamera));

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Set<RenderType> used = new LinkedHashSet<>();
        for (GlassFace face : faces) {
            RenderType type = RENDER_TYPES[face.texture];
            used.add(type);
            drawFace(buffers.getBuffer(type), face, camera);
        }
        for (RenderType type : used) {
            buffers.endBatch(type);
        }
    }

    /** 只在相机这一侧的画面上面画（旧版同款判断）。 */
    private static boolean isFaceOnCameraSide(BlockPos pos, Direction face, Vec3 camera) {
        double centerX = pos.getX() + 0.5 + face.getStepX() * 0.5;
        double centerY = pos.getY() + 0.5 + face.getStepY() * 0.5;
        double centerZ = pos.getZ() + 0.5 + face.getStepZ() * 0.5;
        double dot = (camera.x - centerX) * face.getStepX()
                + (camera.y - centerY) * face.getStepY()
                + (camera.z - centerZ) * face.getStepZ();
        return dot >= -0.02;
    }

    private static double distanceToCamera(BlockPos pos, Direction face, Vec3 camera) {
        double centerX = pos.getX() + 0.5 + face.getStepX() * 0.5 - camera.x;
        double centerY = pos.getY() + 0.5 + face.getStepY() * 0.5 - camera.y;
        double centerZ = pos.getZ() + 0.5 + face.getStepZ() * 0.5 - camera.z;
        return centerX * centerX + centerY * centerY + centerZ * centerZ;
    }

    private static void drawFace(VertexConsumer buffer, GlassFace face, Vec3 camera) {
        float x0 = (float) (face.pos.getX() - camera.x);
        float x1 = (float) (face.pos.getX() + 1.0 - camera.x);
        float y0 = (float) (face.pos.getY() - camera.y);
        float y1 = (float) (face.pos.getY() + 1.0 - camera.y);
        float z0 = (float) (face.pos.getZ() - camera.z);
        float z1 = (float) (face.pos.getZ() + 1.0 - camera.z);
        Direction normal = face.face;

        switch (normal) {
            case DOWN -> {
                vertex(buffer, normal, x0, y0, z1, 0.0F, 1.0F);
                vertex(buffer, normal, x1, y0, z1, 1.0F, 1.0F);
                vertex(buffer, normal, x1, y0, z0, 1.0F, 0.0F);
                vertex(buffer, normal, x0, y0, z0, 0.0F, 0.0F);
            }
            case UP -> {
                vertex(buffer, normal, x0, y1, z0, 0.0F, 0.0F);
                vertex(buffer, normal, x1, y1, z0, 1.0F, 0.0F);
                vertex(buffer, normal, x1, y1, z1, 1.0F, 1.0F);
                vertex(buffer, normal, x0, y1, z1, 0.0F, 1.0F);
            }
            case NORTH -> {
                vertex(buffer, normal, x1, y0, z0, 0.0F, 1.0F);
                vertex(buffer, normal, x1, y1, z0, 0.0F, 0.0F);
                vertex(buffer, normal, x0, y1, z0, 1.0F, 0.0F);
                vertex(buffer, normal, x0, y0, z0, 1.0F, 1.0F);
            }
            case SOUTH -> {
                vertex(buffer, normal, x0, y0, z1, 0.0F, 1.0F);
                vertex(buffer, normal, x0, y1, z1, 0.0F, 0.0F);
                vertex(buffer, normal, x1, y1, z1, 1.0F, 0.0F);
                vertex(buffer, normal, x1, y0, z1, 1.0F, 1.0F);
            }
            case WEST -> {
                vertex(buffer, normal, x0, y0, z0, 0.0F, 1.0F);
                vertex(buffer, normal, x0, y1, z0, 0.0F, 0.0F);
                vertex(buffer, normal, x0, y1, z1, 1.0F, 0.0F);
                vertex(buffer, normal, x0, y0, z1, 1.0F, 1.0F);
            }
            case EAST -> {
                vertex(buffer, normal, x1, y0, z1, 0.0F, 1.0F);
                vertex(buffer, normal, x1, y1, z1, 0.0F, 0.0F);
                vertex(buffer, normal, x1, y1, z0, 1.0F, 0.0F);
                vertex(buffer, normal, x1, y0, z0, 1.0F, 1.0F);
            }
        }
    }

    /** 旧版不做光照也不做环境光遮蔽，这里固定满亮度、无叠加层。 */
    private static void vertex(VertexConsumer buffer, Direction normal, float x, float y, float z, float u, float v) {
        buffer.addVertex(x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(normal.getStepX(), normal.getStepY(), normal.getStepZ());
    }

    private static int connectedTexture(ClientLevel level, BlockPos pos, int side) {
        boolean[] bitMatrix = new boolean[8];
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (side == 0 || side == 1) {
            bitMatrix[0] = isWardedGlass(level, x - 1, y, z - 1);
            bitMatrix[1] = isWardedGlass(level, x, y, z - 1);
            bitMatrix[2] = isWardedGlass(level, x + 1, y, z - 1);
            bitMatrix[3] = isWardedGlass(level, x - 1, y, z);
            bitMatrix[4] = isWardedGlass(level, x + 1, y, z);
            bitMatrix[5] = isWardedGlass(level, x - 1, y, z + 1);
            bitMatrix[6] = isWardedGlass(level, x, y, z + 1);
            bitMatrix[7] = isWardedGlass(level, x + 1, y, z + 1);
        } else if (side == 2 || side == 3) {
            int toward = side == 2 ? 1 : -1;
            bitMatrix[0] = isWardedGlass(level, x + toward, y + 1, z);
            bitMatrix[1] = isWardedGlass(level, x, y + 1, z);
            bitMatrix[2] = isWardedGlass(level, x - toward, y + 1, z);
            bitMatrix[3] = isWardedGlass(level, x + toward, y, z);
            bitMatrix[4] = isWardedGlass(level, x - toward, y, z);
            bitMatrix[5] = isWardedGlass(level, x + toward, y - 1, z);
            bitMatrix[6] = isWardedGlass(level, x, y - 1, z);
            bitMatrix[7] = isWardedGlass(level, x - toward, y - 1, z);
        } else if (side == 4 || side == 5) {
            int toward = side == 5 ? 1 : -1;
            bitMatrix[0] = isWardedGlass(level, x, y + 1, z + toward);
            bitMatrix[1] = isWardedGlass(level, x, y + 1, z);
            bitMatrix[2] = isWardedGlass(level, x, y + 1, z - toward);
            bitMatrix[3] = isWardedGlass(level, x, y, z + toward);
            bitMatrix[4] = isWardedGlass(level, x, y, z - toward);
            bitMatrix[5] = isWardedGlass(level, x, y - 1, z + toward);
            bitMatrix[6] = isWardedGlass(level, x, y - 1, z);
            bitMatrix[7] = isWardedGlass(level, x, y - 1, z - toward);
        }

        int idBuilder = 0;
        for (int i = 0; i < bitMatrix.length; i++) {
            if (bitMatrix[i]) {
                idBuilder += 1 << i;
            }
        }

        return CONNECTED_TEXTURE_REF[Math.max(0, Math.min(CONNECTED_TEXTURE_REF.length - 1, idBuilder))];
    }

    private static boolean isWardedGlass(ClientLevel level, BlockPos pos) {
        return level.getBlockState(pos).is((Block) TCBlocks.WARDED_GLASS.get());
    }

    private static boolean isWardedGlass(ClientLevel level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).is((Block) TCBlocks.WARDED_GLASS.get());
    }

    private static int sideId(Direction face) {
        return switch (face) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
        };
    }

    private static ResourceLocation[] createTextures() {
        ResourceLocation[] textures = new ResourceLocation[47];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/blocks/warded_glass_" + (i + 1) + ".png");
        }
        return textures;
    }

    /**
     * 旧版用的是裸纹理 + 不受光照影响的面、普通混合、不写深度、不剔除背面，这里用同样的状态组合
     * （神秘时代自己的守卫者覆盖层也用这套纹理 + emissive 着色器）；{@code sortOnUpload} 让同一批里
     * 的四边形按距离由远到近绘制。
     */
    private static RenderType[] createRenderTypes() {
        RenderType[] renderTypes = new RenderType[TEXTURES.length];
        for (int i = 0; i < renderTypes.length; i++) {
            ResourceLocation texture = TEXTURES[i];
            renderTypes[i] = RenderType.create(
                    "goldentweaks_warded_glass_" + (i + 1),
                    DefaultVertexFormat.NEW_ENTITY,
                    Mode.QUADS,
                    1536,
                    false,
                    true,
                    CompositeState.builder()
                            .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                            .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                            .setCullState(RenderStateShard.NO_CULL)
                            .setLightmapState(RenderStateShard.LIGHTMAP)
                            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                            .createCompositeState(false)
            );
        }
        return renderTypes;
    }

    private static int[] createConnectedTextureRef() {
        return new int[]{
                0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
                1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
                0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
                1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
                4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26,
                16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43, 38,
                4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12,
                16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25, 40, 32,
                0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
                1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
                0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
                1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
                4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26,
                7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39, 33,
                4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12,
                7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11
        };
    }

    private record GlassFace(BlockPos pos, Direction face, int texture, double distanceToCamera) {
    }

    private GTWardedGlassRenderer() {
    }
}
