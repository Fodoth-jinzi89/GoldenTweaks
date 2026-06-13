package net.fodoth.skina.goldentweaks.mixin.fix.traveloptics;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IMagicSummon.class)
public interface IMagicSummonMixin {

    @Inject(
            method = "isAlliedHelper",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$isAlliedHelper(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        IMagicSummon self = (IMagicSummon) this;

        Entity ownerEntity = self.getSummoner();
        if (!(ownerEntity instanceof LivingEntity owner)) {
            return;
        }

        // 魔灵主人
        if (entity == owner) {
            cir.setReturnValue(true);
            return;
        }

        // 魔灵本体
        if (entity instanceof AbstractSpellCastingPet pet) {
            LivingEntity petOwner = pet.getSummoner();

            if (petOwner != null &&
                    (petOwner == owner
                            || petOwner.isAlliedTo(owner)
                            || owner.isAlliedTo(petOwner))) {

                cir.setReturnValue(true);
            }
        }
    }
}
