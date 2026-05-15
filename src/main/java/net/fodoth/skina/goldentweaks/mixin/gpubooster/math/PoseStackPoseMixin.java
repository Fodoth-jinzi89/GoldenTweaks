package net.fodoth.skina.goldentweaks.mixin.gpubooster.math;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.util.math.GTMatrix3f;
import net.fodoth.skina.goldentweaks.util.math.GTMatrix4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PoseStack.Pose.class)
public abstract class PoseStackPoseMixin {

    @Mutable
    @Shadow
    @Final
    Matrix3f normal;

    @Mutable
    @Shadow
    @Final
    Matrix4f pose;

    @Inject(
            method = "<init>(Lorg/joml/Matrix4f;Lorg/joml/Matrix3f;)V",
            at = @At("TAIL")
    )
    private void goldentweaks$initWithFastMath(
            Matrix4f poseMatrix,
            Matrix3f normalMatrix,
            CallbackInfo ci
    ) {

        if (GoldenTweaksClientConfig.doFastMath()) {

            this.normal = new GTMatrix3f(normalMatrix);
            this.pose = new GTMatrix4f(poseMatrix);
        }
    }
}