package net.fodoth.skina.goldentweaks.mixin.fix.traveloptics;

import io.redspace.ironsspellbooks.entity.mobs.goals.GenericHurtByTargetGoal;
import net.fodoth.skina.goldentweaks.mixin.fix.traveloptics.accessor.TargetGoalAccessor;
import net.fodoth.skina.goldentweaks.util.FamiliarCompat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GenericHurtByTargetGoal.class, remap = false)
public abstract class GenericHurtByTargetGoalMixin {

    @Inject(
            method = "canUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$ignoreFriendlyFire(CallbackInfoReturnable<Boolean> cir) {

        Mob mob = ((TargetGoalAccessor) this).goldentweaks$getMob();

        LivingEntity attacker = mob.getLastHurtByMob();

        if (attacker != null
                && FamiliarCompat.areFriendly(mob, attacker)) {
            cir.setReturnValue(false);
        }
    }
}