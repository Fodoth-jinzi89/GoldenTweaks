package net.fodoth.skina.goldentweaks.mixin.fix.astralsorcery;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 服务端「发现降星点」也卡着游标角度：{@code FocalPointManager#onPlayerTick} 里对每个可能是降星点的星座做
 *
 * <pre>{@code
 * if (!progress.hasSeenFocalPoint(cst) && Math.abs(angle - ctx.getConstellationHandler().getAngle(cst)) < 6F) {
 *     if (this.isPlayerLookingAtFocalPoint(sPlayer, focalPointData, cst)) { ... memorize + 提示消息 ... }
 * }
 * }</pre>
 *
 * 只有星盘游标落在该星座当前角度的 ±6° 内才可能记忆成功，配合上面那层「按角度隐形」，玩家必须一路滚轮
 * 扫角度才能找到降星点。这里把该 lambda 里那个唯一的 {@code Math.abs(...)} 短路成 0，
 * 于是角度条件恒成立——举着星盘、看向降星点光柱即可发现；原有的
 * {@code !hasSeenFocalPoint} 与 {@code isPlayerLookingAtFocalPoint}（朝向 + 视线无遮挡）判定保持不变。
 */
@Mixin(targets = "hellfirepvp.astralsorcery.common.focal.FocalPointManager", remap = false)
public abstract class FocalPointManagerDiscoveryMixin {

    @Redirect(
            method = "lambda$onPlayerTick$1",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(F)F"),
            require = 0,
            remap = false
    )
    private float gt$ignoreMarkerAngleForDiscovery(float value) {
        return 0F;
    }
}
