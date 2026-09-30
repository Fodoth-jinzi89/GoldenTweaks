package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTGolemSimplify;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.entities.ai.GolemReturnHomeGoal;
import thaumcraft.common.entities.golem.ThaumcraftGolemEntity;

/**
 * 简化模式下禁止傀儡寻路回家：既然它本来就该站在原点，回家改成直接传送
 * （见 {@code ThaumcraftGolemEntityMixin} 的 {@code customServerAiStep}）。
 */
@Mixin(value = GolemReturnHomeGoal.class, remap = false)
public abstract class GolemReturnHomeGoalMixin {

    @Shadow
    @Final
    private ThaumcraftGolemEntity golem;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void gt$skipWalkHome(CallbackInfoReturnable<Boolean> cir) {
        if (GTGolemSimplify.appliesTo(this.golem)) {
            cir.setReturnValue(false);
        }
    }
}
