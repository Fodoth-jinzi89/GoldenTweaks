package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.generator.SteamTunnelBlockEntity;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 蒸汽隧道（涡轮机）流体倍率（默认 ×10）。
 *
 * <p><b>只放大「每步发电量」，不放大「每步耗汽量」。</b>这是本改动的核心：</p>
 *
 * <p>原版一拍最多跑 4 步，每步耗 1024 蒸汽、发 {@code stepEu} 电。若把耗汽也 ×10
 * （每步 10240），一台 1680 蒸汽/t 的反应堆连一步都凑不满，输出会从 840 直接掉到 0——
 * 比不改还糟。反过来只放大发电量，同样的蒸汽就能发出成倍的电，符合「蒸汽全消耗、
 * 输出 x10」的预期。</p>
 *
 * <p>{@code stepEu = 512 + 各转子加成}，转子加成最高 3×160=480。若只把基底 512 乘 10，
 * 满配转子时是 {@code 5120+480 = 5600}，相对原来的 {@code 992} 只有 5.65 倍。
 * 在 {@code validate()} 的 TAIL 对整个字段乘倍率，才能保证任何转子配置都是精确 10 倍；
 * 该方法每 20 tick 重算并重新赋值，所以每次乘的都是刚算出的原值，不会累积。</p>
 *
 * <p>配套放大：蒸汽箱与水箱容量、内部储电上限、以及 {@code MAX_STEP_EU} 基底
 * （电力端口的容量与包数由后者推导，会自动跟上）。</p>
 */
@Mixin(value = SteamTunnelBlockEntity.class, remap = false)
public abstract class MioIcifSteamTunnelMixin {

    /** 每步发电量；由 {@code validate()} 按转子重算。 */
    @Shadow
    private long stepEu;

    private static long gt$multiplier() {
        return GoldenTweaksCommonConfig.getMioIcifSteamTunnelMultiplier();
    }

    private static int gt$scaleInt(int original) {
        long scaled = original * gt$multiplier();
        return scaled > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) scaled;
    }

    private static long gt$scaleLong(long original) {
        long scaled = original * gt$multiplier();
        return scaled < 0L ? Long.MAX_VALUE : scaled;
    }

    /**
     * 每步发电量整体 ×倍率。
     *
     * <p>{@code validate()} 每次都会把 {@code stepEu} 重新赋成 {@code 512 + 转子加成}，
     * 因此在 TAIL 乘一次即可，既不漏掉转子，也不会反复累积。</p>
     */
    @Inject(method = "validate", at = @At("TAIL"))
    private void gt$scaleStepEu(CallbackInfo ci) {
        long multiplier = gt$multiplier();
        if (multiplier <= 1L) {
            return;
        }
        long scaled = this.stepEu * multiplier;
        this.stepEu = scaled < 0L ? Long.MAX_VALUE : scaled;
    }

    /** 构造时的初始每步发电量，避免刚放下的那一拍出现一次掉档。 */
    @ModifyConstant(method = "<init>", constant = @Constant(longValue = 512L))
    private static long gt$initialStepEu(long original) {
        return gt$scaleLong(original);
    }

    /** 蒸汽箱 16,000 -> ×倍率。{@code FluidTank} 容量构造时定死，只能改这里。 */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 16000))
    private static int gt$steamTank(int original) {
        return gt$scaleInt(original);
    }

    /** 冷凝水箱 2,000 -> ×倍率。 */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 2000))
    private static int gt$waterTank(int original) {
        return gt$scaleInt(original);
    }

    /** 对外上报的罐容量必须与实际 {@code FluidTank} 一致，否则界面与管道会按旧值判断。 */
    @ModifyConstant(method = "getTankCapacity", constant = @Constant(intValue = 16000))
    private int gt$reportedSteamCapacity(int original) {
        return gt$scaleInt(original);
    }

    @ModifyConstant(method = "getTankCapacity", constant = @Constant(intValue = 2000))
    private int gt$reportedWaterCapacity(int original) {
        return gt$scaleInt(original);
    }

    /**
     * 内部储电上限 10,000 -> ×倍率，否则产得再多也存不下、会当场卡住。
     */
    @ModifyConstant(method = "tick", constant = @Constant(longValue = 10000L))
    private static long gt$energyBuffer(long original) {
        return gt$scaleLong(original);
    }

    /** {@code MAX_STEP_EU} / {@code MAX_ENERGY} 的基底，静态初始化时算一次。 */
    @ModifyConstant(method = "<clinit>", constant = @Constant(longValue = 512L))
    private static long gt$maxStepBase(long original) {
        return gt$scaleLong(original);
    }

    /** {@code MAX_ENERGY = 10,000 + 3 × MAX_STEP_EU} 的基底。 */
    @ModifyConstant(method = "<clinit>", constant = @Constant(longValue = 10000L))
    private static long gt$maxEnergyBase(long original) {
        return gt$scaleLong(original);
    }
}
