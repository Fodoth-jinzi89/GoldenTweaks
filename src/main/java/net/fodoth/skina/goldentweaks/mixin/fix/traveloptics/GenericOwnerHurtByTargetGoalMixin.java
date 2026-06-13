package net.fodoth.skina.goldentweaks.mixin.fix.traveloptics;

import io.redspace.ironsspellbooks.entity.mobs.goals.GenericOwnerHurtByTargetGoal;
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

@Mixin(value = GenericOwnerHurtByTargetGoal.class, remap = false)
public abstract class GenericOwnerHurtByTargetGoalMixin {


    @Final
    @Shadow
    private Supplier<Entity> owner;

    @Shadow
    private LivingEntity ownerLastHurtBy;

    @Inject(
            method = "canUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$ignoreFriendlyFire(CallbackInfoReturnable<Boolean> cir) {

        Entity ownerEntity = this.owner.get();

        if (!(ownerEntity instanceof LivingEntity livingOwner)) {
            return;
        }

        LivingEntity attacker = this.ownerLastHurtBy;

        if (attacker == null) {
            return;
        }

        Mob mob = ((TargetGoalAccessor) this).goldentweaks$getMob();

        if (FamiliarCompat.areFriendly(mob, attacker)
                || FamiliarCompat.areFriendly(livingOwner, attacker)) {

            cir.setReturnValue(false);
        }
    }
}
