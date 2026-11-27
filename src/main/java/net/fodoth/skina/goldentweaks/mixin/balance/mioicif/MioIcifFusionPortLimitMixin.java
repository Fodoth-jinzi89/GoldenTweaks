package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.fusion.FusionPowerPortBlockEntity;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 聚变堆电源端口的「电网最高电压截断」放开。
 *
 * <p>端口原本把容量、单次可吸收空间都钉在 {@code 2147483647}（int 上限），
 * 而且 tier 分级上限 {@code MAX_TIER = 11} 对应的单 tick 承载
 * （{@code packet(11) * 64 = 2,147,483,648}）刚好等于这个上限。
 * 倍率一旦大于 1，输出就会被这两处同时卡住，表现为"改了配置没反应"。</p>
 *
 * <p>这里把容量与分级上限一起放大；倍率为 1 时保持原版数值不变。</p>
 */
@Mixin(value = FusionPowerPortBlockEntity.class, remap = false)
public abstract class MioIcifFusionPortLimitMixin {

    private static boolean gt$scaling() {
        return GoldenTweaksCommonConfig.getMioIcifFusionMultiplier() > 1L;
    }

    private static long gt$capacity() {
        long multiplier = GoldenTweaksCommonConfig.getMioIcifFusionMultiplier();
        if (multiplier <= 1L) {
            return 2147483647L;
        }
        long scaled = 2147483647L * multiplier;
        return scaled < 0L ? Long.MAX_VALUE : scaled;
    }

    /**
     * 端口内部容量（构造器传给父类）：放开 21 亿。
     *
     * <p>必须是 static：注入点在 {@code super()} 调用之前，此时 {@code this} 还是
     * {@code uninitializedThis}，调用实例方法会让 JVM 校验直接失败
     * （{@code VerifyError: Bad type on operand stack}）。</p>
     */
    @ModifyConstant(
            method = "<init>",
            constant = @Constant(longValue = 2147483647L))
    private static long gt$portCapacity(long original) {
        return gt$capacity();
    }

    /** tick 里计算「还能塞多少」的上限。 */
    @ModifyConstant(
            method = "tick",
            constant = @Constant(longValue = 2147483647L))
    private static long gt$portSpace(long original) {
        return gt$capacity();
    }

    /**
     * 放开分级上限：原逻辑最高到 11 级，单 tick 只能过 2,147,483,648 EU。
     * 放大后按需继续升档（每档 ×4）；封顶 25 避免 {@code 8L << 2*tier} 移位溢出。
     */
    @Inject(method = "tierFor", at = @At("TAIL"), cancellable = true)
    private static void gt$raiseTierCap(long perTick, CallbackInfoReturnable<Integer> cir) {
        if (!gt$scaling()) {
            return;
        }
        int tier = 5;
        while (tier < 25 && gt$packet(tier) * 64L < perTick) {
            tier++;
        }
        cir.setReturnValue(tier);
    }

    /** 与源码 {@code packet(int)} 一致：8L << (2 * tier)。 */
    private static long gt$packet(int tier) {
        long shifted = 8L << (2 * tier);
        return shifted <= 0L ? Long.MAX_VALUE : shifted;
    }
}
