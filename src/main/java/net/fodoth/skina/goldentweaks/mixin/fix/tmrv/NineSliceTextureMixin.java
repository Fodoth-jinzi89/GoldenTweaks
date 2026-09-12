package net.fodoth.skina.goldentweaks.mixin.fix.tmrv;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import mezz.jei.common.Constants;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * 重写 JEI / TMRV 的九宫格贴图绘制。原实现的三个问题：
 * <ol>
 *   <li>{@code middleHeight = textureWidth - top - bottom} 用了<b>宽</b>来计算竖直中段的
 *       平铺高度，非正方形贴图必然错：{@code scrollbar_marker}（12x15）的 12 像素中段被
 *       挤进 9 像素反复平铺，条纹被切断、错位，即用户看到的「完整滑条从中间切断、上下对调」；</li>
 *   <li>竖直方向是从下往上铺、把余数块画在最上面并采样贴图最下方，上下内容因此对调；</li>
 *   <li>目标比两端切片之和还小时不会像原版那样把切片收敛到目标的一半，而是让四个角重叠、
 *       中段整段不画（10x10 按钮 + 20x20 贴图 6 像素切片就是这种情况）。</li>
 * </ol>
 * 这里改成与原版 {@code GuiGraphics} 九宫格缩放一致的语义：切片先 clamp 到目标的一半，
 * 四角与四条边按 1:1 裁切取样，中段从左上开始 1:1 平铺、余数块放在末尾。
 */
@Mixin(value = DrawableNineSliceTexture.class, remap = false)
public abstract class NineSliceTextureMixin {

    @Shadow
    @Final
    private JeiSpriteUploader spriteUploader;

    @Shadow
    @Final
    private ResourceLocation location;

    @Shadow
    @Final
    private int width;

    @Shadow
    @Final
    private int height;

    @Shadow
    @Final
    private int sliceLeft;

    @Shadow
    @Final
    private int sliceRight;

    @Shadow
    @Final
    private int sliceTop;

    @Shadow
    @Final
    private int sliceBottom;

    /**
     * @author Fodoth_jinzi89
     * @reason 修正九宫格竖直中段平铺高度、平铺方向与切片超出目标时的裁剪，
     *         否则滑条贴图会呈现「从中间切断、上下对调」的破损外观。
     */
    @Overwrite(remap = false)
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int targetWidth, int targetHeight) {
        if (targetWidth <= 0 || targetHeight <= 0) {
            return;
        }

        TextureAtlasSprite sprite = this.spriteUploader.getSprite(this.location);
        int left = Math.min(this.sliceLeft, targetWidth / 2);
        int right = Math.min(this.sliceRight, targetWidth / 2);
        int top = Math.min(this.sliceTop, targetHeight / 2);
        int bottom = Math.min(this.sliceBottom, targetHeight / 2);

        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();
        float uPerPixel = (uMax - uMin) / (float) this.width;
        float vPerPixel = (vMax - vMin) / (float) this.height;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix = guiGraphics.pose().last().pose();

        // 四角：按目标尺寸裁切贴图角落（与原版 blitNineSlicedSprite 一致）
        gt$quad(buffer, matrix, uMin, vMin, uMin + uPerPixel * left, vMin + vPerPixel * top,
                xOffset, yOffset, left, top);
        gt$quad(buffer, matrix, uMax - uPerPixel * right, vMin, uMax, vMin + vPerPixel * top,
                xOffset + targetWidth - right, yOffset, right, top);
        gt$quad(buffer, matrix, uMin, vMax - vPerPixel * bottom, uMin + uPerPixel * left, vMax,
                xOffset, yOffset + targetHeight - bottom, left, bottom);
        gt$quad(buffer, matrix, uMax - uPerPixel * right, vMax - vPerPixel * bottom, uMax, vMax,
                xOffset + targetWidth - right, yOffset + targetHeight - bottom, right, bottom);

        // 四条边与中段：源切片尺寸同样用 clamp 后的边框，保证取样与平铺一一对应
        int sourceMiddleWidth = this.width - left - right;
        int sourceMiddleHeight = this.height - top - bottom;
        int middleWidth = targetWidth - left - right;
        int middleHeight = targetHeight - top - bottom;

        if (middleWidth > 0) {
            if (top > 0) {
                gt$tile(buffer, matrix, uMin + uPerPixel * left, vMin, uPerPixel, vPerPixel,
                        xOffset + left, yOffset, middleWidth, top, sourceMiddleWidth, top);
            }
            if (bottom > 0) {
                gt$tile(buffer, matrix, uMin + uPerPixel * left, vMax - vPerPixel * bottom,
                        uPerPixel, vPerPixel, xOffset + left, yOffset + targetHeight - bottom,
                        middleWidth, bottom, sourceMiddleWidth, bottom);
            }
        }

        if (middleHeight > 0) {
            if (left > 0) {
                gt$tile(buffer, matrix, uMin, vMin + vPerPixel * top, uPerPixel, vPerPixel,
                        xOffset, yOffset + top, left, middleHeight, left, sourceMiddleHeight);
            }
            if (right > 0) {
                gt$tile(buffer, matrix, uMax - uPerPixel * right, vMin + vPerPixel * top,
                        uPerPixel, vPerPixel, xOffset + targetWidth - right, yOffset + top,
                        right, middleHeight, right, sourceMiddleHeight);
            }
        }

        if (middleWidth > 0 && middleHeight > 0) {
            gt$tile(buffer, matrix, uMin + uPerPixel * left, vMin + vPerPixel * top,
                    uPerPixel, vPerPixel, xOffset + left, yOffset + top,
                    middleWidth, middleHeight, sourceMiddleWidth, sourceMiddleHeight);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    /** 从左上角开始 1:1 平铺，最后一块按剩余空间裁切取样（同原版 blitTiledSprite）。 */
    @Unique
    private static void gt$tile(BufferBuilder buffer, Matrix4f matrix, float uStart, float vStart,
                                float uPerPixel, float vPerPixel, int xOffset, int yOffset,
                                int targetWidth, int targetHeight, int sourceWidth, int sourceHeight) {
        if (targetWidth <= 0 || targetHeight <= 0 || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }

        for (int x = 0; x < targetWidth; x += sourceWidth) {
            int tileWidth = Math.min(sourceWidth, targetWidth - x);

            for (int y = 0; y < targetHeight; y += sourceHeight) {
                int tileHeight = Math.min(sourceHeight, targetHeight - y);

                gt$quad(buffer, matrix, uStart, vStart,
                        uStart + uPerPixel * tileWidth, vStart + vPerPixel * tileHeight,
                        xOffset + x, yOffset + y, tileWidth, tileHeight);
            }
        }
    }

    @Unique
    private static void gt$quad(BufferBuilder buffer, Matrix4f matrix, float uMin, float vMin,
                                float uMax, float vMax, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        buffer.addVertex(matrix, (float) x, (float) (y + height), 0.0F).setUv(uMin, vMax);
        buffer.addVertex(matrix, (float) (x + width), (float) (y + height), 0.0F).setUv(uMax, vMax);
        buffer.addVertex(matrix, (float) (x + width), (float) y, 0.0F).setUv(uMax, vMin);
        buffer.addVertex(matrix, (float) x, (float) y, 0.0F).setUv(uMin, vMin);
    }
}
