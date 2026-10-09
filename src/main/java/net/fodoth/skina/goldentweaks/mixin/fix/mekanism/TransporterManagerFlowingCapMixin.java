package net.fodoth.skina.goldentweaks.mixin.fix.mekanism;

import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

/**
 * Mekanism 物流管道与「超大堆叠」容器（如精妙存储的堆叠升级 omega 桶，单槽可达上千万）
 * 交互时，{@code TransporterManager.getPredictedInsert -> predictFlowing} 会遍历「正流向同一目标」
 * 的整份 {@code flowingStacks} 集合，对每个 stack 逐个 {@code simulateInsert}（含 ItemStack 分配）。
 * 一旦某个目标被卡住、流向它的 stack 堆积到成千上万（实测单方块曾达 1108~3218 个），
 * 单个 tick 就会在这个循环里无限膨胀，堆内存每秒上涨数百 MB 直到开服 OOM。
 * <p>
 * 这里给「单个目标点的在途 stack 集合」加上上限：超出上限的 stack 不再登记进预测缓存。
 * 该表仅用于插入预判模拟（不影响真实物品移动、不丢物品），因此封顶只会让极端的病态网络
 * 停止无限展开，而正常网络（在途 stack 远低于上限）完全不受影响。
 */
@Mixin(value = TransporterManager.class, remap = false)
public abstract class TransporterManagerFlowingCapMixin {

    @Shadow(remap = false)
    @Final
    private static Map<GlobalPos, Set<TransporterStack>> flowingStacks;

    @Unique
    private static boolean goldentweaks$flowCapWarned = false;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true, remap = false)
    private static void goldentweaks$capFlowingStacks(Level level, TransporterStack stack, CallbackInfo ci) {
        int cap = GoldenTweaksCommonConfig.getMekanismTransporterFlowingCap();
        if (cap <= 0) {
            return;
        }
        Set<TransporterStack> set = flowingStacks.get(GlobalPos.of(level.dimension(), BlockPos.of(stack.getDest())));
        if (set != null && set.size() >= cap) {
            if (!goldentweaks$flowCapWarned) {
                goldentweaks$flowCapWarned = true;
                GoldenTweaks.LOGGER.warn(
                        "[GT/mek] Logistical transporter flowing-stack cap ({}) reached, throttling path simulation to protect the server from OOM. Adjust balance.mekanism.transporterFlowingCap to change.",
                        cap);
            }
            ci.cancel();
        }
    }
}
