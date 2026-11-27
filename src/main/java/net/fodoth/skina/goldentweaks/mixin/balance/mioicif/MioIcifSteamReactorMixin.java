package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.reactor.mio_icif_steam_reactor;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 蒸汽反应堆流体倍率（默认 ×10）。
 *
 * <p>只放大两个水箱的容量，不碰换算比例。原版的每周期产量由
 * {@code min(储汽余量, 水 × 100) / 40} 决定，而这一拍能产多少汽正好被「储汽余量」卡住：
 * 满水 16,000 时预算算出 40,000 热量，产汽上限却是 128,000 的汽箱余量。
 * 所以把水、汽两个容量同时 ×10，每周期产汽就从 128,000 变成 1,280,000，
 * 耗水随之从 1,280 变成 12,800——两者一起放大，比例仍是 1 水 : 100 汽。</p>
 *
 * <p>反过来若只放大循环预算而不放大容量，产汽会被旧汽箱直接截断，等于没有任何效果。</p>
 */
@Mixin(value = mio_icif_steam_reactor.class, remap = false)
public abstract class MioIcifSteamReactorMixin {

    /** 倍率；为 1 时所有注入都退回原值。 */
    private static long gt$multiplier() {
        return GoldenTweaksCommonConfig.getMioIcifSteamReactorMultiplier();
    }

    private static int gt$scale(int original) {
        long scaled = original * gt$multiplier();
        return scaled > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) scaled;
    }

    /**
     * 水箱 16,000 -> ×倍率。{@code FluidTank} 的容量在构造时定死，只能改这里。
     *
     * <p>处理器是 static：这条常量出现在 {@code super()} 调用之前，此时 {@code this} 尚未
     * 初始化，调用实例方法会触发 {@code VerifyError}。</p>
     */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 16000))
    private static int gt$waterCapacity(int original) {
        return gt$scale(original);
    }

    /** 汽箱 128,000 -> ×倍率。 */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 128000))
    private static int gt$steamCapacity(int original) {
        return gt$scale(original);
    }

    /** 界面与流体能力上报的输入罐容量。 */
    @ModifyConstant(method = "inputFluidCapacity", constant = @Constant(intValue = 16000))
    private int gt$inputCapacity(int original) {
        return gt$scale(original);
    }

    /** 界面与流体能力上报的输出罐容量。 */
    @ModifyConstant(method = "outputFluidCapacity", constant = @Constant(intValue = 128000))
    private int gt$outputCapacity(int original) {
        return gt$scale(original);
    }

    /** 储汽余量，决定单周期最多能产多少汽，是实际的产量上限。 */
    @ModifyConstant(method = "steamRoom", constant = @Constant(intValue = 128000))
    private int gt$steamRoom(int original) {
        return gt$scale(original);
    }

    /** 向相邻机器推送蒸汽的单次上限，跟着放大才不会成为新的瓶颈。 */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 8000))
    private static int gt$pushAmount(int original) {
        return gt$scale(original);
    }
}
