package net.fodoth.skina.goldentweaks.mixin.gpubooster.rendering;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.util.SIMDAABBTest;
import net.fodoth.skina.goldentweaks.util.SmartCullingType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frustum.class)
public abstract class FrustumMixin {

    @Shadow private double camX;
    @Shadow private double camY;
    @Shadow private double camZ;

    @Shadow @Final private FrustumIntersection intersection;
    @Shadow @Final private Matrix4f matrix;

    // =========================
    // SIMD-friendly SoA storage
    // =========================
    @Unique private float[] goldentweaks$nx;
    @Unique private float[] goldentweaks$ny;
    @Unique private float[] goldentweaks$nz;
    @Unique private float[] goldentweaks$nw;

    // -------------------------
    // init safety
    // -------------------------
    @Unique
    private void goldentweaks$ensurePlanes() {
        if (this.goldentweaks$nx != null) return;

        this.goldentweaks$nx = new float[6];
        this.goldentweaks$ny = new float[6];
        this.goldentweaks$nz = new float[6];
        this.goldentweaks$nw = new float[6];
    }

    // -------------------------
    // inject constructor tail
    // -------------------------
    @Inject(method = "<init>*", at = @At("TAIL"))
    private void goldentweaks$init(CallbackInfo ci) {
        goldentweaks$ensurePlanes();
    }

    // -------------------------
    // frustum update hook
    // -------------------------
    @Inject(method = "calculateFrustum", at = @At("TAIL"))
    private void goldentweaks$setup(Matrix4f pos, Matrix4f proj, CallbackInfo ci) {
        if (GoldenTweaksClientConfig.SMART_CULLING.get() != SmartCullingType.OFF) {
            goldentweaks$setupPlanes(this.matrix);
        }
    }

    // =========================
    // FAST AABB TEST
    // =========================
    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$fastAABBTest(AABB aabb, CallbackInfoReturnable<Boolean> cir) {

        SmartCullingType mode = GoldenTweaksClientConfig.SMART_CULLING.get();
        if (mode == SmartCullingType.OFF) return;

        goldentweaks$ensurePlanes();

        float minX = (float)(aabb.minX - camX);
        float minY = (float)(aabb.minY - camY);
        float minZ = (float)(aabb.minZ - camZ);

        float maxX = (float)(aabb.maxX - camX);
        float maxY = (float)(aabb.maxY - camY);
        float maxZ = (float)(aabb.maxZ - camZ);

        float cx = (minX + maxX) * 0.5F;
        float cy = (minY + maxY) * 0.5F;
        float cz = (minZ + maxZ) * 0.5F;

        float ex = (maxX - minX) * 0.5F;
        float ey = (maxY - minY) * 0.5F;
        float ez = (maxZ - minZ) * 0.5F;

        float radius = Mth.sqrt(ex * ex + ey * ey + ez * ez);

        float[] nx = this.goldentweaks$nx;
        float[] ny = this.goldentweaks$ny;
        float[] nz = this.goldentweaks$nz;
        float[] nw = this.goldentweaks$nw;

        if (mode == SmartCullingType.SIMD) {

            if (!SIMDAABBTest.test(nx, ny, nz, nw, cx, cy, cz, radius)) {
                cir.setReturnValue(false);
                return;
            }

        } else {

            for (int i = 0; i < 6; i++) {
                float dist =
                        nx[i] * cx +
                                ny[i] * cy +
                                nz[i] * cz +
                                nw[i];

                if (dist < -radius) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }

        cir.setReturnValue(
                this.intersection.testAab(
                        minX, minY, minZ,
                        maxX, maxY, maxZ
                )
        );
    }

    // =========================
    // plane computation
    // =========================
    @Unique
    private void goldentweaks$setupPlanes(Matrix4f mat) {

        goldentweaks$ensurePlanes();

        goldentweaks$setPlane(0,
                mat.m03() + mat.m00(),
                mat.m13() + mat.m10(),
                mat.m23() + mat.m20(),
                mat.m33() + mat.m30());

        goldentweaks$setPlane(1,
                mat.m03() - mat.m00(),
                mat.m13() - mat.m10(),
                mat.m23() - mat.m20(),
                mat.m33() - mat.m30());

        goldentweaks$setPlane(2,
                mat.m03() + mat.m01(),
                mat.m13() + mat.m11(),
                mat.m23() + mat.m21(),
                mat.m33() + mat.m31());

        goldentweaks$setPlane(3,
                mat.m03() - mat.m01(),
                mat.m13() - mat.m11(),
                mat.m23() - mat.m21(),
                mat.m33() - mat.m31());

        goldentweaks$setPlane(4,
                mat.m03() + mat.m02(),
                mat.m13() + mat.m12(),
                mat.m23() + mat.m22(),
                mat.m33() + mat.m32());

        goldentweaks$setPlane(5,
                mat.m03() - mat.m02(),
                mat.m13() - mat.m12(),
                mat.m23() - mat.m22(),
                mat.m33() - mat.m32());
    }

    @Unique
    private void goldentweaks$setPlane(int i, float x, float y, float z, float w) {

        float len = Mth.sqrt(x * x + y * y + z * z);
        if (len == 0.0F) {
            goldentweaks$nx[i] = 0;
            goldentweaks$ny[i] = 0;
            goldentweaks$nz[i] = 0;
            goldentweaks$nw[i] = 0;
            return;
        }

        float inv = 1.0F / len;

        goldentweaks$nx[i] = x * inv;
        goldentweaks$ny[i] = y * inv;
        goldentweaks$nz[i] = z * inv;
        goldentweaks$nw[i] = w * inv;
    }
}