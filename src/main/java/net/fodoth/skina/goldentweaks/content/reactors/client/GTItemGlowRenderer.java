package net.fodoth.skina.goldentweaks.content.reactors.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.fodoth.skina.goldentweaks.content.reactors.GTReactorItems;
import net.fodoth.skina.goldentweaks.content.reactors.GTReactorSolid;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.Random;

/**
 * 物品栏背面发光：沿用神秘时代 {@code SpecialItemRenderer} 的“能量扇”做法（POSITION_COLOR +
 * LIGHTNING_TRANSPARENCY 的三角形），在 GUI 里绘制到物品图标之前，因此位于图标背后。
 */
public final class GTItemGlowRenderer {

    private static final int FAN_COUNT = 10;
    private static final int DISC_SEGMENTS = 24;
    private static final float ICON_CENTER = 8.0F;
    private static final float SCALE = 13.0F;

    private static final float[] FAN_ANGLE = new float[FAN_COUNT];
    private static final float[] FAN_RADIUS = new float[FAN_COUNT];
    private static final float[] FAN_WIDTH = new float[FAN_COUNT];

    static {
        Random random = new Random(245L);
        for (int i = 0; i < FAN_COUNT; i++) {
            FAN_ANGLE[i] = random.nextFloat() * 360.0F;
            FAN_RADIUS[i] = (random.nextFloat() * 20.0F + 5.0F) / 30.0F * SCALE;
            FAN_WIDTH[i] = (random.nextFloat() * 2.0F + 1.0F) / 30.0F * SCALE;
        }
    }

    private static final RenderType GLOW = RenderType.create(
            "goldentweaks_item_glow",
            DefaultVertexFormat.POSITION_COLOR,
            Mode.TRIANGLES,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private GTItemGlowRenderer() {
    }

    public static void render(GuiGraphics graphics, ItemStack stack, int x, int y) {
        int rgb = glowRgb(stack);
        if (rgb < 0) {
            return;
        }

        Matrix4f matrix = graphics.pose().last().pose();
        VertexConsumer buffer = graphics.bufferSource().getBuffer(GLOW);
        float centerX = x + ICON_CENTER;
        float centerY = y + ICON_CENTER;
        float seconds = (Util.getMillis() % 100000L) / 1000.0F;
        float spin = seconds * 14.4F;

        float haloRadius = SCALE * 0.75F + Mth.sin(seconds * 2.0F) * 0.8F;
        drawDisc(buffer, matrix, centerX, centerY, haloRadius, 0.38F, rgb);

        for (int i = 0; i < FAN_COUNT; i++) {
            float pulse = 0.85F + 0.15F * Mth.sin(seconds * 2.6F + i * 1.7F);
            float angle = (FAN_ANGLE[i] + spin) * Mth.DEG_TO_RAD;
            drawFan(buffer, matrix, centerX, centerY, angle,
                    FAN_RADIUS[i] * pulse, FAN_WIDTH[i] * pulse, rgb);
        }
    }

    private static int glowRgb(ItemStack stack) {
        if (stack.is(GTReactorItems.item(GTReactorSolid.CYCLOPARAPHENYLENE_8).get())) {
            return GTReactorSolid.CYCLOPARAPHENYLENE_8.color();
        }
        if (stack.is(GTReactorItems.item(GTReactorSolid.NANO_ACTIVATED_PARTICLES).get())) {
            return GTReactorSolid.NANO_ACTIVATED_PARTICLES.color();
        }
        return -1;
    }

    private static void drawDisc(VertexConsumer buffer, Matrix4f matrix,
                                 float centerX, float centerY, float radius, float alpha, int rgb) {
        int center = color(alpha, rgb);
        int edge = color(0.0F, rgb);
        for (int i = 0; i < DISC_SEGMENTS; i++) {
            float from = i * Mth.TWO_PI / DISC_SEGMENTS;
            float to = (i + 1) * Mth.TWO_PI / DISC_SEGMENTS;
            vertex(buffer, matrix, centerX, centerY, center);
            vertex(buffer, matrix,
                    centerX + Mth.cos(to) * radius, centerY + Mth.sin(to) * radius, edge);
            vertex(buffer, matrix,
                    centerX + Mth.cos(from) * radius, centerY + Mth.sin(from) * radius, edge);
        }
    }

    private static void drawFan(VertexConsumer buffer, Matrix4f matrix,
                                float centerX, float centerY, float angle, float radius, float width, int rgb) {
        int center = color(0.55F, rgb);
        int edge = color(0.0F, rgb);
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);

        triangle(buffer, matrix, centerX, centerY, center, edge,
                -0.866F * radius, -0.5F * radius,
                0.866F * radius, -0.5F * radius, cos, sin);
        triangle(buffer, matrix, centerX, centerY, center, edge,
                0.866F * width, -0.5F * width,
                0.0F, width, cos, sin);
        triangle(buffer, matrix, centerX, centerY, center, edge,
                0.0F, width,
                -0.866F * width, -0.5F * width, cos, sin);
    }

    private static void triangle(VertexConsumer buffer, Matrix4f matrix,
                                 float centerX, float centerY, int centerColor, int edgeColor,
                                 float x0, float y0, float x1, float y1, float cos, float sin) {
        vertex(buffer, matrix, centerX, centerY, centerColor);
        vertex(buffer, matrix, centerX + x0 * cos - y0 * sin, centerY + x0 * sin + y0 * cos, edgeColor);
        vertex(buffer, matrix, centerX + x1 * cos - y1 * sin, centerY + x1 * sin + y1 * cos, edgeColor);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, int color) {
        buffer.addVertex(matrix, x, y, 0.0F).setColor(color);
    }

    private static int color(float alpha, int rgb) {
        return ((int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F) << 24) | rgb;
    }
}
