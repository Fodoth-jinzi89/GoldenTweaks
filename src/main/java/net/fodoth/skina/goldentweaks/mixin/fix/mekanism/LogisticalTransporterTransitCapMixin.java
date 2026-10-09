package net.fodoth.skina.goldentweaks.mixin.fix.mekanism;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 单个物流管道方块能同时承载的「在途 stack」数量的兜底上限。
 * <p>
 * 当目标容器（多由精妙存储的超大堆叠引起）无法完成插入时，物品会在管道里越积越多，
 * 实测单个方块曾堆到 1108~3218 个 stack；每多一个 stack，管道 tick 里的路径/插入模拟就多一份，
 * 最终把服务器拖入无限计算并 OOM。
 * <p>
 * 达到上限后拒绝再往该管道塞入新的在途 stack（上游会自然回压，物品留在源容器里）。
 * 正常管道同时在途的 stack 只有个位数，默认上限远高于此，不影响正常物流。
 */
@Mixin(value = LogisticalTransporterBase.class, remap = false)
public abstract class LogisticalTransporterTransitCapMixin {

    @Shadow(remap = false)
    @Final
    protected Int2ObjectMap<TransporterStack> transit;

    @Unique
    private boolean goldentweaks$transitCapWarned = false;

    @Inject(method = "addStack", at = @At("HEAD"), cancellable = true, remap = false)
    private void goldentweaks$capTransit(int id, TransporterStack stack, CallbackInfo ci) {
        int cap = GoldenTweaksCommonConfig.getMekanismTransporterTransitCap();
        if (cap <= 0) {
            return;
        }
        if (transit.size() >= cap) {
            if (!goldentweaks$transitCapWarned) {
                goldentweaks$transitCapWarned = true;
                GoldenTweaks.LOGGER.warn(
                        "[GT/mek] Logistical transporter in-transit cap ({}) reached, refusing new stacks to avoid an entity/job pile-up OOM. Adjust balance.mekanism.transporterTransitCap to change.",
                        cap);
            }
            ci.cancel();
        }
    }
}
