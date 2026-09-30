package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTGolemSimplify;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.entities.ai.GolemHarvestGoal;
import thaumcraft.common.entities.golem.ThaumcraftGolemEntity;

/**
 * 简化模式下的收获/伐木核心：不必走到目标旁边再动手。
 *
 * <p>收获目标本来就是以傀儡原点为中心、按 {@code workRange()} 取整成方形范围检索的
 * （{@code findMatureCrop}/{@code findLog}），所以只需要让"是否已经够得着"恒为真：
 * 目标一被发现就直接原地收获，{@code findWorkPath} 与整套寻路/重试逻辑都不会再被执行。</p>
 */
@Mixin(value = GolemHarvestGoal.class, remap = false)
public abstract class GolemHarvestGoalMixin {

    @Shadow
    @Final
    private ThaumcraftGolemEntity golem;

    @Inject(method = "withinHarvestReach", at = @At("HEAD"), cancellable = true)
    private void gt$harvestAtRange(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (GTGolemSimplify.appliesTo(this.golem)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 伐木核心挑选目标时要求旁边存在可站立点（{@code findLog} 里的空值判断）。
     * 简化模式下傀儡不移动，这个前提不再成立，直接放行让范围内所有原木都可被选中。
     */
    @Inject(method = "workStandPosition", at = @At("HEAD"), cancellable = true)
    private void gt$skipStandPosition(BlockPos target, CallbackInfoReturnable<BlockPos> cir) {
        if (GTGolemSimplify.appliesTo(this.golem)) {
            cir.setReturnValue(this.golem.blockPosition());
        }
    }
}
