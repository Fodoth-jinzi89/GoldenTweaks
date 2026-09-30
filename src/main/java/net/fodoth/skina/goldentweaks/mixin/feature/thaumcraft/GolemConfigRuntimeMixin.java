package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTGolemSimplify;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.entities.golem.GolemConfigRuntime;

/**
 * 简化模式下统一检索间隔为 1 秒（20 tick）。
 *
 * <p>{@code searchIntervalTicks} 是傀儡工作节奏的唯一来源：核心工作间隔
 * （{@code ThaumcraftGolemEntity.coreWorkInterval}）与收获/伐木/钓鱼核心的重新搜索间隔
 * （{@code GolemHarvestGoal.canUse}、{@code GolemFishGoal}）都取自它。守卫与屠夫核心
 * 不使用这套工作循环，因此不受影响。</p>
 */
@Mixin(value = GolemConfigRuntime.class, remap = false)
public abstract class GolemConfigRuntimeMixin {

    @Inject(method = "searchIntervalTicks", at = @At("HEAD"), cancellable = true)
    private static void gt$simplifiedInterval(CallbackInfoReturnable<Integer> cir) {
        if (GTGolemSimplify.isEnabled()) {
            cir.setReturnValue(GTGolemSimplify.INTERVAL_TICKS);
        }
    }
}
