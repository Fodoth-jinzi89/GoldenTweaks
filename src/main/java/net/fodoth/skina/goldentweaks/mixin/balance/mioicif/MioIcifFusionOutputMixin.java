package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.fusion.FusionBlanket;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 聚变堆产能倍率（默认 ×1,000,000）。
 *
 * <p>原实现在 {@code output} 里把结果夹在 {@code 2.147483647E9}（int 上限），
 * 也就是 2,147,483,647 EU/t。倍率若只作用在结果上，超过该值就会被截回，
 * 所以这里在 TAIL 接管：先按倍率放大，再放开上限。</p>
 *
 * <p>返回值本身是 {@code long}，所以放大后可以安全超过 int 上限；
 * 端到端能真正输出多少，取决于 {@code MioIcifFusionGridLimitMixin} 对各处 int 截断的放开。</p>
 */
@Mixin(value = FusionBlanket.class, remap = false)
public abstract class MioIcifFusionOutputMixin {

    @Inject(method = "output", at = @At("TAIL"), cancellable = true)
    private static void gt$scaleFusionOutput(long neutronPower, double lithiumShare, double fissionEu,
                                             CallbackInfoReturnable<Long> cir) {
        long multiplier = GoldenTweaksCommonConfig.getMioIcifFusionMultiplier();
        if (multiplier <= 1L) {
            return;
        }

        // 复算原始值：原方法返回的是「按 2.147e9 夹紧后的 long」，
        // 夹紧前的热量与 EU 需要重新算一遍才能正确放大。
        double heat = FusionBlanket.thermal(neutronPower, lithiumShare, fissionEu);
        double eu = Math.floor(Math.floor(heat) * 35.0 / 100.0);
        if (eu <= 0.0) {
            return;
        }

        double scaled = Math.floor(eu * (double) multiplier);
        long result = scaled >= (double) Long.MAX_VALUE ? Long.MAX_VALUE : (long) scaled;
        cir.setReturnValue(result);
    }
}
