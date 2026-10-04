package net.fodoth.skina.goldentweaks.compat.astralsorcery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataManager;
import hellfirepvp.astralsorcery.common.focal.node.FocalPointNode;
import hellfirepvp.astralsorcery.common.item.AstrolabeItem;
import hellfirepvp.astralsorcery.common.lib.types.SyncDataTypesAS;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.level.DayTimeHelper;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 降星点光柱：GoldenTweaks 自己画一根。
 *
 * <p>Astral Sorcery 自己的光柱（{@code VFXFocalLightBeam}）在本整合包里链路是通的——对象生成、进入
 * 绘制、alpha 也正常（日志实测 {@code render beam alphaI≥200}）——但屏幕上始终没有像素：它走的是
 * AS 自定义 core shader（{@code ShadersAS.position_color_tex_alpha}）＋自定义 RenderType，
 * 再叠上这个包里的 Sodium 系 / Iris / Veil / sable 等一套渲染替换，以及 `shut_up_gl_error` 把 GL 报错
 * 吞掉，属于只能上机才能继续查的问题。这里不改上游，改用**纯原版渲染类型**（{@code RenderType.lightning()}，
 * POSITION_COLOR + QUADS + 加法混合）在 {@code RenderLevelStageEvent} 里自绘，绕开 AS 的特效管线，
 * 保证玩家能看见。
 *
 * <p>显示条件与 AS 的原设计保持一致：只在 AS 的夜里、且（正举着星盘 **或** 柱子里已接入同星座聚焦水晶石）
 * 时出现，距离越远越淡。可用客户端配置 {@code misc.astralsorceryFocalColumn} 关闭。</p>
 */
public final class GTFocalPointColumnRenderer {

    /** 超过这个水平距离就不画（AS 自己大约是 80 格）。 */
    private static final double VISIBLE_RANGE = 96.0D;
    /** 光柱半径（格）。 */
    private static final float COLUMN_RADIUS = 0.6F;
    /** 光柱从地表往下延伸的格数（与 AS 一致：地表下方 20 格起）。 */
    private static final int COLUMN_BELOW = 20;
    /** 光柱超出地表的高度（格）。 */
    private static final int COLUMN_ABOVE = 90;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        if (!GoldenTweaksClientConfig.doAstralSorceryFocalColumn()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;
        if (level == null || player == null || mc.isPaused()) {
            return;
        }

        // 只在 AS 自己的夜里显示（和它光柱的 alpha 条件一致）
        float night = DayTimeHelper.getCurrentDaytimeDistribution(level);
        if (night <= 0F) {
            return;
        }

        boolean scoping = AstrolabeItem.isUsingAstrolabe(player);
        List<FocalPointNode> nodes = SyncDataManager.getInstance()
                .getClientData(SyncDataTypesAS.FOCAL_POINT)
                .getNodes(level, new Vector3(player), VISIBLE_RANGE);
        if (nodes.isEmpty()) {
            return;
        }

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vb = buffers.getBuffer(RenderType.lightning());

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        Vec3 cam = event.getCamera().getPosition();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = pose.last().pose();

        for (FocalPointNode node : nodes) {
            // 与 AS 一致：没举星盘、柱子里也没有聚焦水晶石就不显形
            if (!scoping && node.getFocalPosition().isEmpty()) {
                continue;
            }

            BlockPos column = node.getPos().toBlockPos(0);
            double dx = column.getX() + 0.5D - player.getX();
            double dz = column.getZ() + 0.5D - player.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (horizontal > VISIBLE_RANGE) {
                continue;
            }

            float alpha = (float) Mth.clamp(1.0D - horizontal / VISIBLE_RANGE, 0.35D, 1.0D) * night;
            if (!scoping) {
                alpha *= 0.6F; // 只靠水晶石时不那么亮
            }

            int surfaceY = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, column).getY();
            float x = column.getX() + 0.5F;
            float z = column.getZ() + 0.5F;
            int color = Mth.clamp((int) (alpha * 255.0F), 0, 255);
            drawColumn(vb, matrix, x, z, surfaceY - COLUMN_BELOW, surfaceY + COLUMN_ABOVE, color);
        }

        pose.popPose();
        buffers.endBatch(RenderType.lightning());
    }

    /** 两个互相垂直的竖直四边形，正反两面各发一次（原版 `lightning` 渲染类型开了背面剔除）。 */
    private static void drawColumn(VertexConsumer vb, Matrix4f matrix, float x, float z, int y0, int y1, int alpha) {
        float radius = COLUMN_RADIUS;
        float fy0 = y0;
        float fy1 = y1;

        // X 方向的面
        quad(vb, matrix, x - radius, fy0, z, x + radius, fy0, z, x + radius, fy1, z, x - radius, fy1, z, alpha);
        quad(vb, matrix, x + radius, fy0, z, x - radius, fy0, z, x - radius, fy1, z, x + radius, fy1, z, alpha);
        // Z 方向的面
        quad(vb, matrix, x, fy0, z - radius, x, fy0, z + radius, x, fy1, z + radius, x, fy1, z - radius, alpha);
        quad(vb, matrix, x, fy0, z + radius, x, fy0, z - radius, x, fy1, z - radius, x, fy1, z + radius, alpha);
    }

    private static void quad(VertexConsumer vb, Matrix4f matrix,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int alpha) {
        vb.addVertex(matrix, x1, y1, z1).setColor(235, 240, 255, alpha);
        vb.addVertex(matrix, x2, y2, z2).setColor(235, 240, 255, alpha);
        vb.addVertex(matrix, x3, y3, z3).setColor(235, 240, 255, alpha);
        vb.addVertex(matrix, x4, y4, z4).setColor(235, 240, 255, alpha);
    }

    private GTFocalPointColumnRenderer() {
    }
}
