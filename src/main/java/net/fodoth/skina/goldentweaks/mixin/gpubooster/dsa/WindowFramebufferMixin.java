package net.fodoth.skina.goldentweaks.mixin.gpubooster.dsa;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.util.DSAMode;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MainTarget.class)
public abstract class WindowFramebufferMixin extends RenderTarget {

    @Shadow
    @Final
    public static int DEFAULT_WIDTH;

    @Shadow
    @Final
    public static int DEFAULT_HEIGHT;

    protected WindowFramebufferMixin(boolean useDepth) {
        super(useDepth);
    }

    @Inject(
            method = "createFrameBuffer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$createFramebuffer(
            int width,
            int height,
            CallbackInfo ci
    ) {

        RenderSystem.assertOnRenderThreadOrInit();

        int actualWidth = width <= 0 ? DEFAULT_WIDTH : width;
        int actualHeight = height <= 0 ? DEFAULT_HEIGHT : height;

        this.colorTextureId =
                goldentweaks$allocateColorTexture(actualWidth, actualHeight);

        this.depthBufferId =
                goldentweaks$allocateDepthTexture(actualWidth, actualHeight);

        this.frameBufferId = GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)
                ? GL46C.glCreateFramebuffers()
                : GlStateManager.glGenFramebuffers();

        if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

            GL46C.glNamedFramebufferTexture(
                    this.frameBufferId,
                    GL46C.GL_COLOR_ATTACHMENT0,
                    this.colorTextureId,
                    0
            );

            if (GoldenTweaksClientConfig.canCreateRenderbuffer()) {

                GL46C.glNamedFramebufferRenderbuffer(
                        this.frameBufferId,
                        GL46C.GL_DEPTH_ATTACHMENT,
                        GL46C.GL_RENDERBUFFER,
                        this.depthBufferId
                );

            } else {

                GL46C.glNamedFramebufferTexture(
                        this.frameBufferId,
                        GL46C.GL_DEPTH_ATTACHMENT,
                        this.depthBufferId,
                        0
                );
            }

        } else {

            GlStateManager._glBindFramebuffer(
                    GL46C.GL_FRAMEBUFFER,
                    this.frameBufferId
            );

            GlStateManager._glFramebufferTexture2D(
                    GL46C.GL_FRAMEBUFFER,
                    GL46C.GL_COLOR_ATTACHMENT0,
                    GL46C.GL_TEXTURE_2D,
                    this.colorTextureId,
                    0
            );

            if (GoldenTweaksClientConfig.canCreateRenderbuffer()) {

                GlStateManager._glFramebufferRenderbuffer(
                        GL46C.GL_FRAMEBUFFER,
                        GL46C.GL_DEPTH_ATTACHMENT,
                        GL46C.GL_RENDERBUFFER,
                        this.depthBufferId
                );

            } else {

                GlStateManager._glFramebufferTexture2D(
                        GL46C.GL_FRAMEBUFFER,
                        GL46C.GL_DEPTH_ATTACHMENT,
                        GL46C.GL_TEXTURE_2D,
                        this.depthBufferId,
                        0
                );
            }
        }

        this.viewWidth = actualWidth;
        this.viewHeight = actualHeight;
        this.width = actualWidth;
        this.height = actualHeight;

        this.checkStatus();

        if (!GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

            GlStateManager._glBindFramebuffer(
                    GL46C.GL_FRAMEBUFFER,
                    0
            );
        }

        ci.cancel();
    }

    @Unique
    private int goldentweaks$allocateColorTexture(
            int width,
            int height
    ) {

        int texture = GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)
                ? GL46C.glCreateTextures(GL46C.GL_TEXTURE_2D)
                : GlStateManager._genTexture();

        goldentweaks$configureTexture(texture);

        if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

            GL46C.glTextureStorage2D(
                    texture,
                    1,
                    GL46C.GL_RGBA8,
                    width,
                    height
            );

        } else {

            GlStateManager._bindTexture(texture);

            GlStateManager._texImage2D(
                    GL46C.GL_TEXTURE_2D,
                    0,
                    GL46C.GL_RGBA8,
                    width,
                    height,
                    0,
                    GL46C.GL_RGBA,
                    GL46C.GL_UNSIGNED_BYTE,
                    null
            );
        }

        return texture;
    }

    @Unique
    private int goldentweaks$allocateDepthTexture(
            int width,
            int height
    ) {

        if (GoldenTweaksClientConfig.canCreateRenderbuffer()) {

            int rbo = GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)
                    ? GL46C.glCreateRenderbuffers()
                    : GlStateManager.glGenRenderbuffers();

            if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

                GL46C.glNamedRenderbufferStorage(
                        rbo,
                        GL46C.GL_DEPTH_COMPONENT24,
                        width,
                        height
                );

            } else {

                GlStateManager._glBindRenderbuffer(
                        GL46C.GL_RENDERBUFFER,
                        rbo
                );

                GlStateManager._glRenderbufferStorage(
                        GL46C.GL_RENDERBUFFER,
                        GL46C.GL_DEPTH_COMPONENT24,
                        width,
                        height
                );
            }

            return rbo;
        }

        int texture = GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)
                ? GL46C.glCreateTextures(GL46C.GL_TEXTURE_2D)
                : GlStateManager._genTexture();

        goldentweaks$configureDepthTexture(texture);

        if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

            GL46C.glTextureStorage2D(
                    texture,
                    1,
                    GL46C.GL_DEPTH_COMPONENT24,
                    width,
                    height
            );

        } else {

            GlStateManager._bindTexture(texture);

            GlStateManager._texImage2D(
                    GL46C.GL_TEXTURE_2D,
                    0,
                    GL46C.GL_DEPTH_COMPONENT24,
                    width,
                    height,
                    0,
                    GL46C.GL_DEPTH_COMPONENT,
                    GL46C.GL_FLOAT,
                    null
            );
        }

        return texture;
    }

    @Unique
    private static void goldentweaks$configureTexture(int texture) {

        if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

            GL46C.glTextureParameteri(
                    texture,
                    GL46C.GL_TEXTURE_MIN_FILTER,
                    GL46C.GL_NEAREST
            );

            GL46C.glTextureParameteri(
                    texture,
                    GL46C.GL_TEXTURE_MAG_FILTER,
                    GL46C.GL_NEAREST
            );

            GL46C.glTextureParameteri(
                    texture,
                    GL46C.GL_TEXTURE_WRAP_S,
                    GL46C.GL_CLAMP_TO_EDGE
            );

            GL46C.glTextureParameteri(
                    texture,
                    GL46C.GL_TEXTURE_WRAP_T,
                    GL46C.GL_CLAMP_TO_EDGE
            );

        } else {

            GlStateManager._bindTexture(texture);

            GlStateManager._texParameter(
                    GL46C.GL_TEXTURE_2D,
                    GL46C.GL_TEXTURE_MIN_FILTER,
                    GL46C.GL_NEAREST
            );

            GlStateManager._texParameter(
                    GL46C.GL_TEXTURE_2D,
                    GL46C.GL_TEXTURE_MAG_FILTER,
                    GL46C.GL_NEAREST
            );

            GlStateManager._texParameter(
                    GL46C.GL_TEXTURE_2D,
                    GL46C.GL_TEXTURE_WRAP_S,
                    GL46C.GL_CLAMP_TO_EDGE
            );

            GlStateManager._texParameter(
                    GL46C.GL_TEXTURE_2D,
                    GL46C.GL_TEXTURE_WRAP_T,
                    GL46C.GL_CLAMP_TO_EDGE
            );
        }
    }

    @Unique
    private static void goldentweaks$configureDepthTexture(int texture) {

        goldentweaks$configureTexture(texture);

        if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

            GL46C.glTextureParameteri(
                    texture,
                    GL46C.GL_TEXTURE_COMPARE_MODE,
                    GL46C.GL_NONE
            );

        } else {

            GlStateManager._texParameter(
                    GL46C.GL_TEXTURE_2D,
                    GL46C.GL_TEXTURE_COMPARE_MODE,
                    GL46C.GL_NONE
            );
        }
    }
}