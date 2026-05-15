package net.fodoth.skina.goldentweaks.mixin.gpubooster.math;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.gpubooster.api.GTGL;
import net.fodoth.skina.goldentweaks.util.math.GTMatrix4f;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL46;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin {

    @Shadow @Final @Nullable public Uniform MODEL_VIEW_MATRIX;
    @Shadow @Final @Nullable public Uniform PROJECTION_MATRIX;
    @Shadow @Final @Nullable public Uniform TEXTURE_MATRIX;
    @Shadow @Final @Nullable public Uniform SCREEN_SIZE;
    @Shadow @Final @Nullable public Uniform COLOR_MODULATOR;
    @Shadow @Final @Nullable public Uniform GLINT_ALPHA;
    @Shadow @Final @Nullable public Uniform FOG_START;
    @Shadow @Final @Nullable public Uniform FOG_END;
    @Shadow @Final @Nullable public Uniform FOG_COLOR;
    @Shadow @Final @Nullable public Uniform FOG_SHAPE;
    @Shadow @Final @Nullable public Uniform LINE_WIDTH;
    @Shadow @Final @Nullable public Uniform GAME_TIME;
    @Shadow @Final private int programId;

    @Shadow
    public abstract void setSampler(String name, Object sampler);

    @Redirect(
            method = "apply",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/shaders/Uniform;uploadInteger(II)V"
            )
    )
    private void goldentweaks$dsSamplerUpload(int location, int value) {

        GL46.glProgramUniform1i(
                this.programId,
                location,
                value
        );
    }



    @Inject(
            method = "apply",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/shaders/ProgramManager;glUseProgram(I)V",
                    shift = At.Shift.AFTER
            )
    )
    private void goldentweaks$cacheProgram(CallbackInfo ci) {

        GTGL.CURRENT_PROGRAM.set(this.programId);
    }

    @Inject(
            method = "setDefaultUniforms",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$fastUniforms(
            VertexFormat.Mode drawMode,
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            Window window,
            CallbackInfo ci
    ) {

        if (!GoldenTweaksClientConfig.doFastMath()) {
            return;
        }

        for (int i = 0; i < 12; ++i) {
            this.setSampler(
                    "Sampler" + i,
                    RenderSystem.getShaderTexture(i)
            );
        }

        if (this.MODEL_VIEW_MATRIX != null) {
            this.MODEL_VIEW_MATRIX.set(
                    new GTMatrix4f(modelViewMatrix)
            );
        }

        if (this.PROJECTION_MATRIX != null) {
            this.PROJECTION_MATRIX.set(
                    new GTMatrix4f(projectionMatrix)
            );
        }

        if (this.COLOR_MODULATOR != null) {
            this.COLOR_MODULATOR.set(
                    RenderSystem.getShaderColor()
            );
        }

        if (this.GLINT_ALPHA != null) {
            this.GLINT_ALPHA.set(
                    RenderSystem.getShaderGlintAlpha()
            );
        }

        if (this.FOG_START != null) {
            this.FOG_START.set(
                    RenderSystem.getShaderFogStart()
            );
        }

        if (this.FOG_END != null) {
            this.FOG_END.set(
                    RenderSystem.getShaderFogEnd()
            );
        }

        if (this.FOG_COLOR != null) {
            this.FOG_COLOR.set(
                    RenderSystem.getShaderFogColor()
            );
        }

        if (this.FOG_SHAPE != null) {
            this.FOG_SHAPE.set(
                    RenderSystem.getShaderFogShape().getIndex()
            );
        }

        if (this.TEXTURE_MATRIX != null) {
            this.TEXTURE_MATRIX.set(
                    new GTMatrix4f(RenderSystem.getTextureMatrix())
            );
        }

        if (this.GAME_TIME != null) {
            this.GAME_TIME.set(
                    RenderSystem.getShaderGameTime()
            );
        }

        if (this.SCREEN_SIZE != null) {
            this.SCREEN_SIZE.set(
                    (float) window.getWidth(),
                    (float) window.getHeight()
            );
        }

        if (this.LINE_WIDTH != null
                && (drawMode == VertexFormat.Mode.LINES
                || drawMode == VertexFormat.Mode.LINE_STRIP)) {

            this.LINE_WIDTH.set(
                    RenderSystem.getShaderLineWidth()
            );
        }

        RenderSystem.setupShaderLights(
                (ShaderInstance) (Object) this
        );

        ci.cancel();
    }

    @Inject(
            method = "clear",
            at = @At("TAIL")
    )
    private void goldentweaks$clearProgram(CallbackInfo ci) {

        GTGL.CURRENT_PROGRAM.set(0);
    }
}