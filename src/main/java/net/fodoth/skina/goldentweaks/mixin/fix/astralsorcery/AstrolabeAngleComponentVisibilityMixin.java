package net.fodoth.skina.goldentweaks.mixin.fix.astralsorcery;

import hellfirepvp.astralsorcery.common.constellation.BaseConstellation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 降星点光柱是「按角度隐形」的：{@code BasicFocalPointNode#tickEffects} 里
 *
 * <pre>{@code
 * if (AstrolabeItem.isUsingAstrolabe(pl)) {
 *     return alphaIn * Math.max(cmp.getAngleVisibility(cst, requiredAngle), minMultiplier);
 * }
 * if (focusCrystal != null) return alphaIn * minMultiplier;
 * return 0F;
 * }</pre>
 *
 * 而 {@code getAngleVisibility = clamp(1 - |游标角度 - 该星座当前角度| / 8, 0, 1)}，游标不对齐时是 0，
 * 于是裸降星点（没插星能聚焦水晶石）即便举着星盘也全透明——只能靠反复滚轮扫角度去碰那 8° 的窗口。
 *
 * <p>这里给可见度加一个下限：只要玩家举着星盘（上面那个 if 成立才会调用到本方法），
 * 光柱就至少有 {@value #GT_MIN_VISIBILITY} 的可见度，扫到精确角度时仍然是 1.0（更亮）。</p>
 *
 * <p>本方法在整包中只有 {@code BasicFocalPointNode} 这一个调用点，改动范围仅限于光柱渲染的 alpha，
 * 不影响服务端的发现判定、也不影响插了聚焦水晶石的情形。想让它完全无视角度，把下限改成 {@code 1F} 即可。</p>
 */
@Mixin(targets = "hellfirepvp.astralsorcery.common.component.AstrolabeAngleComponent", remap = false)
public abstract class AstrolabeAngleComponentVisibilityMixin {

    private static final float GT_MIN_VISIBILITY = 0.75F;

    @Inject(method = "getAngleVisibility", at = @At("RETURN"), cancellable = true, remap = false)
    private void gt$keepColumnVisibleWhileScoping(BaseConstellation cst, float cstAngle, CallbackInfoReturnable<Float> cir) {
        Float visibility = cir.getReturnValue();
        if (visibility == null || visibility < GT_MIN_VISIBILITY) {
            cir.setReturnValue(GT_MIN_VISIBILITY);
        }
    }
}
