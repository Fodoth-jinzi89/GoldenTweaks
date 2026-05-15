package net.fodoth.skina.goldentweaks.gpubooster.api;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.gpubooster.api.controller.dsa.DSAController;
import net.fodoth.skina.goldentweaks.gpubooster.client.renderer.gl.VertexBufferCache;
import net.fodoth.skina.goldentweaks.util.DSAMode;
import org.lwjgl.opengl.GL46;

/**
 * OpenGL abstraction layer for GPU Booster (GL46-only)
 */
public final class GTGL {

    /**
     * Thread-local currently bound VertexFormat
     */
    public static final ThreadLocal<VertexFormat> CURRENT_VERTEX_FORMAT =
            ThreadLocal.withInitial(() -> null);

    public static final ThreadLocal<Integer> CURRENT_PROGRAM =
            ThreadLocal.withInitial(() -> 0);

    private GTGL() {}

    /* ------------------------------------------------------------
     * VAO
     * ------------------------------------------------------------ */

    public static int makeVAO() {

        RenderSystem.assertOnRenderThreadOrInit();

        VertexFormat format = CURRENT_VERTEX_FORMAT.get();

        if (format != null) {
            return VertexBufferCache.getVAO(format);
        }

        return DSAController.get().createVAO();
    }

    /* ------------------------------------------------------------
     * FBO
     * ------------------------------------------------------------ */

    public static int makeFBO() {

        RenderSystem.assertOnRenderThreadOrInit();

        return GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)
                ? DSAController.get().createFBO()
                : GL46.glGenFramebuffers();
    }

    public static void bindFramebuffer(int fbo) {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, fbo);
    }

    public static void unbindFramebuffer() {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, 0);
    }

    public static void deleteFramebuffer(int fbo) {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glDeleteFramebuffers(fbo);
    }

    /* ------------------------------------------------------------
     * Texture
     * ------------------------------------------------------------ */

    public static int make2DTexture() {

        RenderSystem.assertOnRenderThreadOrInit();

        return GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)
                ? DSAController.get().createTexture()
                : TextureUtil.generateTextureId();
    }

    public static void deleteTexture(int texture) {

        RenderSystem.assertOnRenderThreadOrInit();

        TextureUtil.releaseTextureId(texture);
    }

    /* ------------------------------------------------------------
     * Renderbuffer (RBO)
     * ------------------------------------------------------------ */

    public static int createRBO() {

        RenderSystem.assertOnRenderThreadOrInit();

        return GL46.glCreateRenderbuffers();
    }

    public static int genRBO() {

        RenderSystem.assertOnRenderThreadOrInit();

        return GL46.glGenRenderbuffers();
    }

    public static void bindRBO(int id) {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glBindRenderbuffer(GL46.GL_RENDERBUFFER, id);
    }

    public static void storageRBO(int width, int height) {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glRenderbufferStorage(
                GL46.GL_RENDERBUFFER,
                GL46.GL_DEPTH24_STENCIL8,
                width,
                height
        );
    }

    public static void storageNamedRBO(
            int rbo,
            int width,
            int height
    ) {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glNamedRenderbufferStorage(
                rbo,
                GL46.GL_DEPTH24_STENCIL8,
                width,
                height
        );
    }

    /* ------------------------------------------------------------
     * Framebuffer attachment
     * ------------------------------------------------------------ */

    public static void framebufferRBO(int rbo) {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glFramebufferRenderbuffer(
                GL46.GL_FRAMEBUFFER,
                GL46.GL_DEPTH_ATTACHMENT,
                GL46.GL_RENDERBUFFER,
                rbo
        );
    }

    public static void framebufferNamedRBO(
            int fbo,
            int rbo
    ) {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glNamedFramebufferRenderbuffer(
                fbo,
                GL46.GL_DEPTH_ATTACHMENT,
                GL46.GL_RENDERBUFFER,
                rbo
        );
    }

    /* ------------------------------------------------------------
     * Texture Barrier
     * ------------------------------------------------------------ */

    public static void textureBarrier() {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glTextureBarrier();
    }

    /* ------------------------------------------------------------
     * Delete
     * ------------------------------------------------------------ */

    public static void deleteRBO(int rbo) {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glDeleteRenderbuffers(rbo);
    }
}