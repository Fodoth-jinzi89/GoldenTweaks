package net.fodoth.skina.goldentweaks.mixin.fix.astralsorcery;

import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.function.FXAlphaFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 光柱渲染的最后一处 alpha 门控是坏的：{@code VFXFocalLightBeam#astrolabeFovDistanceAlpha} 用
 *
 * <pre>{@code
 * float currentFov = Mth.lerp(pTicks, gameRenderer.getOldFov(), gameRenderer.getFov());
 * return alphaIn * Mth.clamp(1 - Math.abs(AstrolabeItem.FOV_MODIFIER - currentFov) * 5, 0, 1);
 * }</pre>
 *
 * 想表达的显然是「只有把星盘举起来、视野缩放到位时才显形」（`AstrolabeItem.FOV_MODIFIER = 0.2`）。
 * 但原版 {@code GameRenderer#fov}/{@code oldFov} 存的是**视野倍率**，取值区间 [0.1, 1.5]，
 * 望远镜/星盘这条路径由 {@code AbstractClientPlayer#getFieldOfViewModifier()} 提供；星盘虽然继承
 * {@code SpyglassItem}，却不满足原版 {@code isScoping()} 的判定，倍率始终≈1.0。于是
 * {@code |0.2 - 1.0| * 5 = 4} → clamp 成 0 ⇒ **裸降星点的光柱 alpha 恒为 0**，
 * 无论是否举着星盘都看不见（这也是为什么开关光影毫无变化——它压根没被画出来）。
 * 只有插了星能聚焦水晶石时 {@code displayWithoutAstrolabe = true} 才会跳过这段。
 *
 * <p>这里把该 alpha 函数换成恒等映射：显形与否交给 {@code VFXFocalLightBeam#render} 自己的判定
 * （第一人称 + 正在使用星盘，或插了聚焦水晶石），alpha 则完全由节点侧的
 * sextantAlpha / 距离 / 夜晚 / PYRAMID 决定。</p>
 */
@Mixin(targets = "hellfirepvp.astralsorcery.client.effect.vfx.VFXFocalLightBeam", remap = false)
public abstract class FocalLightBeamFovAlphaMixin {

    @Inject(method = "astrolabeFovDistanceAlpha", at = @At("RETURN"), cancellable = true, remap = false)
    private void gt$dropAstrolabeFovAlphaGate(CallbackInfoReturnable<FXAlphaFunction<?>> cir) {
        FXAlphaFunction<EntityVisualFX> identityAlpha = (fx, alphaIn, pTicks) -> alphaIn;
        cir.setReturnValue(identityAlpha);
    }
}
