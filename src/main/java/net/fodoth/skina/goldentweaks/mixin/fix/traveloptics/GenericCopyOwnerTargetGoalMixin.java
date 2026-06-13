package net.fodoth.skina.goldentweaks.mixin.fix.traveloptics;

import io.redspace.ironsspellbooks.entity.mobs.goals.GenericCopyOwnerTargetGoal;
import net.fodoth.skina.goldentweaks.mixin.fix.traveloptics.accessor.TargetGoalAccessor;
import net.fodoth.skina.goldentweaks.util.FamiliarCompat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(value = GenericCopyOwnerTargetGoal.class, remap = false)
public abstract class GenericCopyOwnerTargetGoalMixin {

    @Shadow
    @Final
    private Supplier<Entity> ownerGetter;

    @Inject(
            method = "canUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$blockFriendlyCopy(CallbackInfoReturnable<Boolean> cir) {

        Entity ownerEntity = this.ownerGetter.get();

        if (!(ownerEntity instanceof Mob ownerMob)) {
            return;
        }

        LivingEntity target = ownerMob.getTarget();

        if (target == null) {
            return;
        }

        Mob mob = ((TargetGoalAccessor) this).goldentweaks$getMob();

        if (FamiliarCompat.areFriendly(mob, target)
                || FamiliarCompat.areFriendly(ownerMob, target)) {

            cir.setReturnValue(false);
        }
    }
}