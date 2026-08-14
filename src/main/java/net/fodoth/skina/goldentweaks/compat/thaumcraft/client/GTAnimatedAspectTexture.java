package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.nio.ByteBuffer;

/**
 * An {@link AbstractTexture} that plays a vertical sprite-sheet animation
 * (frame height == image width).
 * <p>
 * Thaumcraft renders aspect icons through {@code RenderSystem.setShaderTexture}
 * (a plain {@code SimpleTexture}), which does not honour {@code .mcmeta}
 * animation blocks — only atlas sprites do. Registering this texture under the
 * aspect's image resource location makes every {@code blit} of that icon
 * animated. Texture storage uses the classic {@code glTexImage2D} path for
 * maximum compatibility (including Vulkan-backed GL renderers).
 */
@OnlyIn(Dist.CLIENT)
public final class GTAnimatedAspectTexture extends AbstractTexture {

    private final NativeImage[] frames;
    private final int frameWidth;
    private final int frameCount;
    private int currentFrame;

    public GTAnimatedAspectTexture(NativeImage source) {
        this.frameWidth = source.getWidth();
        this.frameCount = source.getHeight() / this.frameWidth;
        this.frames = new NativeImage[this.frameCount];
        for (int i = 0; i < this.frameCount; i++) {
            this.frames[i] = new NativeImage(this.frameWidth, this.frameWidth, false);
            for (int x = 0; x < this.frameWidth; x++) {
                for (int y = 0; y < this.frameWidth; y++) {
                    this.frames[i].setPixelRGBA(x, y, source.getPixelRGBA(x, y + i * this.frameWidth));
                }
            }
        }
        source.close();

        this.id = GL11.glGenTextures();
        RenderSystem.bindTexture(this.id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, this.frameWidth, this.frameWidth,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        uploadFrame(0);
    }

    @Override
    public void load(ResourceManager resourceManager) {
        // Texture is built from a preloaded NativeImage; nothing to load here.
    }

    @Override
    public void bind() {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(this::bindInner);
        } else {
            this.bindInner();
        }
    }

    private void bindInner() {
        RenderSystem.bindTexture(this.id);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_CLAMP_TO_EDGE);
    }

    /** Advances to the next frame and re-uploads it on the render thread. */
    public void tick() {
        this.currentFrame = (this.currentFrame + 1) % this.frameCount;
        RenderSystem.recordRenderCall(() -> uploadFrame(this.currentFrame));
    }

    private void uploadFrame(int frameIndex) {
        RenderSystem.bindTexture(this.id);
        this.frames[frameIndex].upload(0, 0, 0, false);
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            GoldenTweaks.LOGGER.warn("GL error after aspect frame upload (frame {}): {}.", frameIndex, Integer.toHexString(error));
        }
    }

    @Override
    public void close() {
        for (NativeImage frame : this.frames) {
            frame.close();
        }
        GL11.glDeleteTextures(this.id);
        super.close();
    }
}
