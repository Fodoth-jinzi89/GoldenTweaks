package net.fodoth.skina.goldentweaks.mixin.gpubooster.dsa;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.gpubooster.api.GTGL;
import net.fodoth.skina.goldentweaks.gpubooster.api.controller.dsa.DSAController;
import net.fodoth.skina.goldentweaks.util.DSAMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.opengl.NVTextureBarrier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

@Mixin(RenderTarget.class)
public abstract class FramebufferMixin {

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    public int viewWidth;

    @Shadow
    public int viewHeight;

    @Shadow
    public int frameBufferId;

    @Shadow
    protected int colorTextureId;

    @Shadow
    protected int depthBufferId;

    @Shadow
    public int filterMode;

    @Shadow
    @Final
    public boolean useDepth;

    @Shadow
    protected abstract void setFilterMode(int filterMode, boolean force);

    @Shadow
    public abstract void checkStatus();

    @Shadow
    public abstract void clear(boolean getError);

    @Shadow
    public abstract void unbindRead();

    @Shadow
    public abstract void unbindWrite();

    @Inject(
            method = "createBuffers",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$initNamed(
            int width,
            int height,
            boolean getError,
            CallbackInfo ci
    ) {
        RenderSystem.assertOnRenderThreadOrInit();

        int max = RenderSystem.maxSupportedTextureSize();

        if (width <= 0 || width > max || height <= 0 || height > max) {
            throw new IllegalArgumentException(
                    "Window " + width + "x" + height
                            + " size out of bounds (max. size: " + max + ")"
            );
        }

        this.width = width;
        this.height = height;
        this.viewWidth = width;
        this.viewHeight = height;

        this.frameBufferId = GTGL.makeFBO();
        this.colorTextureId = GTGL.make2DTexture();

        /*
         * Depth attachment
         */
        if (this.useDepth) {
            if (GoldenTweaksClientConfig.canCreateRenderbuffer()) {

                if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

                    this.depthBufferId = GTGL.createRBO();

                    GTGL.storageNamedRBO(
                            this.depthBufferId,
                            width,
                            height
                    );

                } else {

                    this.depthBufferId = GTGL.genRBO();

                    GTGL.bindRBO(this.depthBufferId);

                    GTGL.storageRBO(width, height);
                }

            } else {

                this.depthBufferId = GTGL.make2DTexture();

                if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

                    DSAController.get().textureParameter(
                            this.depthBufferId,
                            GL46C.GL_TEXTURE_MIN_FILTER,
                            GL46C.GL_NEAREST
                    );

                    DSAController.get().textureParameter(
                            this.depthBufferId,
                            GL46C.GL_TEXTURE_MAG_FILTER,
                            GL46C.GL_NEAREST
                    );

                    DSAController.get().textureParameter(
                            this.depthBufferId,
                            GL46C.GL_TEXTURE_COMPARE_MODE,
                            GL46C.GL_NONE
                    );

                    DSAController.get().textureParameter(
                            this.depthBufferId,
                            GL46C.GL_TEXTURE_WRAP_S,
                            GL46C.GL_CLAMP_TO_EDGE
                    );

                    DSAController.get().textureParameter(
                            this.depthBufferId,
                            GL46C.GL_TEXTURE_WRAP_T,
                            GL46C.GL_CLAMP_TO_EDGE
                    );

                    DSAController.get().textureStorage(
                            this.depthBufferId,
                            1,
                            GL46C.GL_DEPTH_COMPONENT32F,
                            this.viewWidth,
                            this.viewHeight
                    );

                } else {

                    GL46C.glBindTexture(GL46C.GL_TEXTURE_2D, this.depthBufferId);

                    GL46C.glTexParameteri(
                            GL46C.GL_TEXTURE_2D,
                            GL46C.GL_TEXTURE_MIN_FILTER,
                            GL46C.GL_NEAREST
                    );

                    GL46C.glTexParameteri(
                            GL46C.GL_TEXTURE_2D,
                            GL46C.GL_TEXTURE_MAG_FILTER,
                            GL46C.GL_NEAREST
                    );

                    GL46C.glTexParameteri(
                            GL46C.GL_TEXTURE_2D,
                            GL46C.GL_TEXTURE_COMPARE_MODE,
                            GL46C.GL_NONE
                    );

                    GL46C.glTexParameteri(
                            GL46C.GL_TEXTURE_2D,
                            GL46C.GL_TEXTURE_WRAP_S,
                            GL46C.GL_CLAMP_TO_EDGE
                    );

                    GL46C.glTexParameteri(
                            GL46C.GL_TEXTURE_2D,
                            GL46C.GL_TEXTURE_WRAP_T,
                            GL46C.GL_CLAMP_TO_EDGE
                    );

                    GL46C.glTexImage2D(
                            GL46C.GL_TEXTURE_2D,
                            0,
                            GL46C.GL_DEPTH_COMPONENT32F,
                            this.viewWidth,
                            this.viewHeight,
                            0,
                            GL46C.GL_DEPTH_COMPONENT,
                            GL46C.GL_FLOAT,
                            (IntBuffer) null
                    );
                }
            }
        }

        this.setFilterMode(GL46C.GL_NEAREST, true);

        /*
         * Color texture
         */
        if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

            DSAController.get().textureParameter(
                    this.colorTextureId,
                    GL46C.GL_TEXTURE_WRAP_S,
                    GL46C.GL_CLAMP_TO_EDGE
            );

            DSAController.get().textureParameter(
                    this.colorTextureId,
                    GL46C.GL_TEXTURE_WRAP_T,
                    GL46C.GL_CLAMP_TO_EDGE
            );

            DSAController.get().textureStorage(
                    this.colorTextureId,
                    1,
                    GL46C.GL_RGBA8,
                    this.viewWidth,
                    this.viewHeight
            );

            DSAController.get().namedFramebufferTexture(
                    this.frameBufferId,
                    GL46C.GL_COLOR_ATTACHMENT0,
                    this.colorTextureId,
                    0
            );

        } else {

            GL46C.glBindTexture(GL46C.GL_TEXTURE_2D, this.colorTextureId);

            GL46C.glTexParameteri(
                    GL46C.GL_TEXTURE_2D,
                    GL46C.GL_TEXTURE_WRAP_S,
                    GL46C.GL_CLAMP_TO_EDGE
            );

            GL46C.glTexParameteri(
                    GL46C.GL_TEXTURE_2D,
                    GL46C.GL_TEXTURE_WRAP_T,
                    GL46C.GL_CLAMP_TO_EDGE
            );

            GL46C.glTexImage2D(
                    GL46C.GL_TEXTURE_2D,
                    0,
                    GL46C.GL_RGBA8,
                    this.viewWidth,
                    this.viewHeight,
                    0,
                    GL46C.GL_RGBA,
                    GL46C.GL_UNSIGNED_BYTE,
                    (IntBuffer) null
            );

            GL46C.glBindFramebuffer(
                    GL46C.GL_FRAMEBUFFER,
                    this.frameBufferId
            );

            GL46C.glFramebufferTexture2D(
                    GL46C.GL_FRAMEBUFFER,
                    GL46C.GL_COLOR_ATTACHMENT0,
                    GL46C.GL_TEXTURE_2D,
                    this.colorTextureId,
                    0
            );
        }

        /*
         * Attach depth
         */
        if (this.useDepth) {

            if (GoldenTweaksClientConfig.canCreateRenderbuffer()) {

                if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

                    GTGL.framebufferNamedRBO(
                            this.frameBufferId,
                            this.depthBufferId
                    );

                } else {

                    GL46C.glBindFramebuffer(
                            GL46C.GL_FRAMEBUFFER,
                            this.frameBufferId
                    );

                    GTGL.framebufferRBO(this.depthBufferId);
                }

            } else {

                if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

                    DSAController.get().namedFramebufferTexture(
                            this.frameBufferId,
                            GL46C.GL_DEPTH_ATTACHMENT,
                            this.depthBufferId,
                            0
                    );

                } else {

                    GL46C.glBindFramebuffer(
                            GL46C.GL_FRAMEBUFFER,
                            this.frameBufferId
                    );

                    GL46C.glFramebufferTexture2D(
                            GL46C.GL_FRAMEBUFFER,
                            GL46C.GL_DEPTH_ATTACHMENT,
                            GL46C.GL_TEXTURE_2D,
                            this.depthBufferId,
                            0
                    );
                }
            }
        }

        this.checkStatus();

        this.clear(getError);

        if (!GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {
            this.unbindRead();
        }

        ci.cancel();
    }

    @Inject(
            method = "setFilterMode*",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$setTexFilter(
            int filterMode,
            boolean force,
            CallbackInfo ci
    ) {
        RenderSystem.assertOnRenderThreadOrInit();

        if (force || filterMode != this.filterMode) {

            this.filterMode = filterMode;

            if (GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {

                DSAController.get().textureParameter(
                        this.colorTextureId,
                        GL46C.GL_TEXTURE_MIN_FILTER,
                        filterMode
                );

                DSAController.get().textureParameter(
                        this.colorTextureId,
                        GL46C.GL_TEXTURE_MAG_FILTER,
                        filterMode
                );

            } else {

                GL46C.glBindTexture(
                        GL46C.GL_TEXTURE_2D,
                        this.colorTextureId
                );

                GL46C.glTexParameteri(
                        GL46C.GL_TEXTURE_2D,
                        GL46C.GL_TEXTURE_MIN_FILTER,
                        filterMode
                );

                GL46C.glTexParameteri(
                        GL46C.GL_TEXTURE_2D,
                        GL46C.GL_TEXTURE_MAG_FILTER,
                        filterMode
                );

                GL46C.glBindTexture(GL46C.GL_TEXTURE_2D, 0);
            }
        }

        ci.cancel();
    }

    @Inject(
            method = "destroyBuffers",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$deleteBuffers(CallbackInfo ci) {

        RenderSystem.assertOnRenderThreadOrInit();

        if (!GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {
            this.unbindRead();
            this.unbindWrite();
        }

        if (this.depthBufferId > -1) {

            if (GoldenTweaksClientConfig.canCreateRenderbuffer()) {
                GTGL.deleteRBO(this.depthBufferId);
            } else {
                TextureUtil.releaseTextureId(this.depthBufferId);
            }

            this.depthBufferId = -1;
        }

        if (this.colorTextureId > -1) {
            TextureUtil.releaseTextureId(this.colorTextureId);
            this.colorTextureId = -1;
        }

        if (this.frameBufferId > -1) {

            if (!GoldenTweaksClientConfig.hasDSA(DSAMode.FBO)) {
                GTGL.unbindFramebuffer();
            }

            GL46C.glDeleteFramebuffers(this.frameBufferId);

            this.frameBufferId = -1;
        }

        ci.cancel();
    }

    @Inject(
            method = "blitToScreen*",
            at = @At("HEAD")
    )
    private void goldentweaks$texBarrier(
            int width,
            int height,
            boolean disableBlend,
            CallbackInfo ci
    ) {
        if (GoldenTweaksClientConfig.TEX_BARRIER.get()
                && GL.getCapabilities().GL_NV_texture_barrier) {

            NVTextureBarrier.glTextureBarrierNV();
        }
    }
}