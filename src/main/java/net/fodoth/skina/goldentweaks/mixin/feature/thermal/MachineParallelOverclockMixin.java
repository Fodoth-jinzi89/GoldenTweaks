package net.fodoth.skina.goldentweaks.mixin.feature.thermal;

import cofh.thermal.lib.common.block.entity.MachineBlockEntity;
import net.fodoth.skina.goldentweaks.mixin.feature.thermal.accessor.Reconfigurable4WayBlockEntityInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Thermal 机器超频到 1 tick 以下时，把多出来的速度转成「并行」——同一 tick 内完成多次配方。
 *
 * <p>原版 {@code processStart()} 把每 tick 的进度钳死在 {@code Math.min(baseProcessTick, 配方能耗)}，
 * 于是速度一旦超过配方能耗，配方就固定 1 tick 一个，多出来的部分被直接丢弃。这里在
 * {@code tickServer()} 末尾（此时机器刚好完成一次并重新开始，{@code process == processMax}）
 * 再按 {@code baseProcessTick / processMax - 1} 次重走「完成 → 重开」：每次照常消耗一份能耗、
 * 一份输入并占用产出空间，所以输入 / 产出 / 能量任意一项不足时循环自然停止，
 * 整体吞吐正好等于 {@code floor(速度 / 配方能耗)} 个/tick。</p>
 */
@Mixin(value = MachineBlockEntity.class, remap = false)
public abstract class MachineParallelOverclockMixin {

    @Shadow protected int process;
    @Shadow protected int processMax;
    @Shadow protected int baseProcessTick;

    @Shadow protected abstract int processTick();
    @Shadow protected abstract boolean canProcessStart();
    @Shadow protected abstract boolean canProcessFinish();
    @Shadow protected abstract void processStart();
    @Shadow protected abstract void processFinish();
    @Shadow protected abstract void processOff();

    @Inject(method = "tickServer", at = @At("RETURN"))
    private void gt$parallelOverclock(CallbackInfo ci) {

        int cost = this.processMax;

        // 只在「本 tick 刚好完成一次并重新开始」（process == processMax）且已超频到 1 tick
        // 以下（baseProcessTick > 配方能耗）时接管，其余情况一律保持原版行为。
        if (cost <= 0 || this.process != cost || this.baseProcessTick <= cost) {
            return;
        }

        int extra = this.baseProcessTick / cost - 1;

        for (int i = 0; i < extra; i++) {

            // 推进一个完整配方；能量不足时进度会停在半途，交给下一 tick 继续
            if (this.processTick() <= 0 || this.process > 0) {
                return;
            }
            if (!this.canProcessFinish()) {
                this.processOff();
                return;
            }

            this.processFinish();

            // 与 tickServer 的收尾一致：搬运产出、补充输入后再开下一份
            Reconfigurable4WayBlockEntityInvoker transfer = (Reconfigurable4WayBlockEntityInvoker) (Object) this;
            transfer.gt$transferOutput();
            transfer.gt$transferInput();

            if (!this.canProcessStart()) {
                this.processOff();
                return;
            }
            this.processStart();
        }
    }
}
