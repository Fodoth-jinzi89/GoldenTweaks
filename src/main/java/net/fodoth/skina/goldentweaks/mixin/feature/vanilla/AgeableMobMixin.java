package net.fodoth.skina.goldentweaks.mixin.feature.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fodoth.skina.goldentweaks.util.GTFrozenAge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin implements GTFrozenAge {
    @Shadow protected int age;
    @Unique private boolean gt$ageFrozen;

    @Override
    public boolean gt$isAgeFrozen() {
        return gt$ageFrozen;
    }

    @Override
    public void gt$setAgeFrozen(boolean frozen) {
        gt$ageFrozen = frozen && age < 0;
    }

    // Only suppress natural aging; AI, movement and breeding cooldowns still run normally.
    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/AgeableMob;setAge(I)V"), require = 2)
    private void gt$holdBabyAge(AgeableMob mob, int nextAge, Operation<Void> original) {
        if (!gt$ageFrozen || age >= 0) original.call(mob, nextAge);
    }

    @Inject(method = "setAge", at = @At("RETURN"))
    private void gt$clearAdultFreeze(int nextAge, CallbackInfo ci) {
        if (age >= 0) gt$ageFrozen = false;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void gt$saveFrozenAge(CompoundTag tag, CallbackInfo ci) {
        if (gt$ageFrozen) tag.putBoolean("goldentweaks:age_frozen", true);
        else tag.remove("goldentweaks:age_frozen");
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void gt$loadFrozenAge(CompoundTag tag, CallbackInfo ci) {
        gt$setAgeFrozen(tag.getBoolean("goldentweaks:age_frozen"));
    }
}
