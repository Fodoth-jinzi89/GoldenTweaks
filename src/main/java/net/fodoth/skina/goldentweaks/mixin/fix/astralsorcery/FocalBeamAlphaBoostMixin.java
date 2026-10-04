package net.fodoth.skina.goldentweaks.mixin.fix.astralsorcery;

import hellfirepvp.astralsorcery.client.effect.vfx.VFXFocalLightBeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 光柱的 alpha 是一路连乘出来的，乘完几乎就没了：
 *
 * <pre>{@code
 * playEffectBeams(...).setAlpha(0.75F).alpha(距离α -> 夜晚α -> PYRAMID)
 * }</pre>
 *
 * 即 {@code 0.75(基础) × sextant(角度，本整合包里我们已垫到 0.75) × distanceAlpha(下限 0.25，
 * 而且是「离得越近越小」：clamp(水平距离/80,0,1)) × visibleAtNight × PYRAMID(出生/消亡时接近 0)}，
 * 实测日志里 {@code render beam alphaI} 只有 1 / 9 / 17 / 30（255 制），也就是 0.4%~12% —— 东西是画出来了，
 * 但在夜色里等于看不见。
 *
 * <p>这里只对**降星点光柱**（{@link VFXFocalLightBeam}）做提亮：只要算出来的 alpha 不是 0
 * （即「正举着星盘」或「柱子里有同星座聚焦水晶石」这两种应有的可见状态），就抬到
 * {@value #GT_MIN_ALPHA}/255 ≈ 0.78，也就是回到作者原本 {@code setAlpha(0.75F)} 的亮度。
 * alpha 为 0 时（没举星盘且没水晶）保持 0，所以「举着星盘就能发现」的行为不变。</p>
 */
@Mixin(targets = "hellfirepvp.astralsorcery.client.effect.EntityVisualFX", remap = false)
public abstract class FocalBeamAlphaBoostMixin {

    private static final int GT_MIN_ALPHA = 200;

    @Inject(method = "getAlphaI", at = @At("RETURN"), cancellable = true, remap = false)
    private void gt$boostFocalBeamAlpha(float pTicks, CallbackInfoReturnable<Integer> cir) {
        int alpha = cir.getReturnValueI();
        if (alpha > 0 && (Object) this instanceof VFXFocalLightBeam) {
            cir.setReturnValue(Math.max(alpha, GT_MIN_ALPHA));
        }
    }
}
