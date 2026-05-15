package net.fodoth.skina.goldentweaks.mixin.gpubooster.math;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.util.math.GTMatrix4f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BufferUploader.class)
public abstract class BufferUploaderMixin {

    @Redirect(
            method = "_drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;getModelViewMatrix()Lorg/joml/Matrix4f;"
            )
    )
    private static Matrix4f goldentweaks$getFastModelViewMat() {

        Matrix4f matrix = RenderSystem.getModelViewMatrix();

        return GoldenTweaksClientConfig.doFastMath()
                ? new GTMatrix4f(matrix)
                : matrix;
    }

    @Redirect(
            method = "_drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;getProjectionMatrix()Lorg/joml/Matrix4f;"
            )
    )
    private static Matrix4f goldentweaks$getFastProjectionMat() {

        Matrix4f matrix = RenderSystem.getProjectionMatrix();

        return GoldenTweaksClientConfig.doFastMath()
                ? new GTMatrix4f(matrix)
                : matrix;
    }
}