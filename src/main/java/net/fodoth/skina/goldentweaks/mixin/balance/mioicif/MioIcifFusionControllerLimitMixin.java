package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.fusion.FusionControllerBlockEntity;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 放开聚变堆控制器的「电网最高电压截断」。
 *
 * <p>原实现把输出缓冲夹在 {@code 2147483647}（int 上限），倍率放大后会被截回，
 * 表现为"倍率调了却没效果"。这里把所有 int 上限统一替换成按倍率放大的值。</p>
 */
@Mixin(value = FusionControllerBlockEntity.class, remap = false)
public abstract class MioIcifFusionControllerLimitMixin {

    /** 放大后的上限；倍率为 1 时退回原版 2,147,483,647。 */
    private static long gt$limit() {
        long multiplier = GoldenTweaksCommonConfig.getMioIcifFusionMultiplier();
        if (multiplier <= 1L) {
            return 2147483647L;
        }
        long scaled = 2147483647L * multiplier;
        return scaled < 0L ? Long.MAX_VALUE : scaled;
    }

    @ModifyConstant(method = "bufferLimit", constant = @Constant(longValue = 2147483647L))
    private long gt$bufferLimit(long original) {
        return gt$limit();
    }

    /** 存档读取时对 Output / Buffer 两处都做了夹紧。 */
    @ModifyConstant(method = "loadAdditional", constant = @Constant(longValue = 2147483647L))
    private long gt$loadedCap(long original) {
        return gt$limit();
    }
}
