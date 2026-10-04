package net.fodoth.skina.goldentweaks.mixin.fix.ae2cs;

import io.github.lounode.ae2cs.common.block.entity.EntropyVariationReactionChamberBlockEntity;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ae2cs 熵变反应室在安装较多加速卡时会“卡住”（进度条停住、不产出）：
 * 每张加速卡都会让每 tick 能量消耗与运行速率同时翻倍（{@code speedMultiplier = 1 << 卡数}，
 * {@code getEnergyPerTick() = 200 * speedMultiplier}），加速卡偏多时瞬时能量需求过高，
 * 机器会卡在半途。这里加一道“速度过快”的防御：把有效倍率钳制到配置的最大值，
 * 让机器回到已知可用（1 张加速卡，2 倍）的工作区间。把配置的最大值调到 16 可关闭限制。
 */
@Mixin(value = EntropyVariationReactionChamberBlockEntity.class, remap = false)
public abstract class EntropyReactionChamberSpeedMixin {

    @Shadow(remap = false)
    private int speedMultiplier;

    @Inject(method = "onUpgradesChanged", at = @At("TAIL"), remap = false)
    private void goldentweaks$capSpeedMultiplier(CallbackInfo ci) {
        int max = GoldenTweaksCommonConfig.getEntropyReactionMaxSpeedMultiplier();
        if (this.speedMultiplier > max) {
            this.speedMultiplier = max;
        }
    }
}
