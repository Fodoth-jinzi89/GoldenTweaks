package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.generator.SteamTunnelPowerPortBlockEntity;
import com.miophas.singularity_iteration.core.api.energy.storage.CableTier;
import com.miophas.singularity_iteration.core.runtime.energy.grid.EnergyNetGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 蒸汽隧道电力端口的电网层级：HV 固定值 -> 按实际输出动态选取。
 *
 * <p>原版 {@code getSourceTier()} 硬编码返回 {@code CableTier.HV}，所以无论涡轮机发多少电，
 * Jade 都显示 HV，电网也只按 HV 的包大小来调度。</p>
 *
 * <p>流体倍率把产出放大后，端口一次性要送出几十万 EU/t，HV 完全不够用。这里改为按
 * {@code rate()}（每 tick 实际可抽取量）选一个刚好够用的档位，Jade 的显示与实际传输能力
 * 就一起对上了。</p>
 */
@Mixin(value = SteamTunnelPowerPortBlockEntity.class, remap = false)
public abstract class MioIcifSteamTunnelPortTierMixin {

    /**
     * 按输出量挑最小够用的档位。
     *
     * <p>档位换算沿用原版的两条规则：{@code sourceTier = CableTier.getTier() + 1}，
     * 而该 sourceTier 对应的包大小是 {@code 8 << (2 * sourceTier)}。这里直接比较
     * {@code rate()} 需要多大的包，取满足条件的最低档。</p>
     */
    @Inject(method = "getSourceTier", at = @At("HEAD"), cancellable = true)
    private void gt$dynamicSourceTier(CallbackInfoReturnable<Integer> cir) {
        SteamTunnelPowerPortBlockEntity self = (SteamTunnelPowerPortBlockEntity) (Object) this;
        if (!self.isPowerSource()) {
            return;
        }

        long rate = self.rate();
        if (rate <= CableTier.HV.getPowerRating() * 64L) {
            // 仍在原版 HV 的能力范围内，保持原行为，不干扰未调倍率的存档。
            return;
        }

        List<CableTier> tiers = CableTier.allTiers();
        for (int index = 0; index < tiers.size(); index++) {
            CableTier tier = tiers.get(index);
            // 与本模组其他端口一致：单包 * 64 包是这一档每 tick 的输送能力。
            long capacity = tier.getPowerRating() * 64L;
            if (capacity >= rate) {
                cir.setReturnValue(EnergyNetGlobal.cableTierToSourceTier(tier));
                return;
            }
        }

        // 超出所有档位：交给原版逻辑（HV）之外的最后一档，避免异常。
        CableTier top = tiers.getLast();
        cir.setReturnValue(EnergyNetGlobal.cableTierToSourceTier(top));
    }
}
