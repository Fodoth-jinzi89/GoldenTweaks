package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableSystem.ConsumableType;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSpellCastingPet.class)
public abstract class AbstractSpellCastingPetConsumableMixin {

    @Inject(method = "getEnragedStacks", at = @At("HEAD"), cancellable = true)
    private void gt$getEnragedStacks(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(
                (int) GoldenTweaksConsumableHelper.getValue(
                        (AbstractSpellCastingPet) (Object) this,
                        ConsumableType.ENRAGED
                )
        );
    }

    @Inject(method = "getArmorStacks", at = @At("HEAD"), cancellable = true)
    private void gt$getArmorStacks(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(
                (int) GoldenTweaksConsumableHelper.getValue(
                        (AbstractSpellCastingPet) (Object) this,
                        ConsumableType.ARMOR
                )
        );
    }

    @Inject(method = "getHealthStacks", at = @At("HEAD"), cancellable = true)
    private void gt$getHealthStacks(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(
                (int) GoldenTweaksConsumableHelper.getValue(
                        (AbstractSpellCastingPet) (Object) this,
                        ConsumableType.HEALTH
                )
        );
    }

    @Inject(method = "getIsBlocking", at = @At("HEAD"), cancellable = true)
    private void gt$getIsBlocking(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(
                GoldenTweaksConsumableHelper.getValue(
                        (AbstractSpellCastingPet) (Object) this,
                        ConsumableType.BLOCKING
                ) > 0
        );
    }

    @Inject(method = "setEnragedStacks", at = @At("HEAD"), cancellable = true)
    private void gt$setEnragedStacks(Integer level, CallbackInfo ci) {
        GoldenTweaksConsumableHelper.setValue(
                (AbstractSpellCastingPet) (Object) this,
                ConsumableType.ENRAGED,
                level
        );

        ci.cancel();
    }

    @Inject(method = "setArmorStacks", at = @At("HEAD"), cancellable = true)
    private void gt$setArmorStacks(Integer level, CallbackInfo ci) {
        GoldenTweaksConsumableHelper.setValue(
                (AbstractSpellCastingPet) (Object) this,
                ConsumableType.ARMOR,
                level
        );

        ci.cancel();
    }

    @Inject(method = "setHealthStacks", at = @At("HEAD"), cancellable = true)
    private void gt$setHealthStacks(Integer level, CallbackInfo ci) {
        GoldenTweaksConsumableHelper.setValue(
                (AbstractSpellCastingPet) (Object) this,
                ConsumableType.HEALTH,
                level
        );

        ci.cancel();
    }

    @Inject(method = "setIsBlocking", at = @At("HEAD"), cancellable = true)
    private void gt$setIsBlocking(Boolean level, CallbackInfo ci) {
        GoldenTweaksConsumableHelper.setValue(
                (AbstractSpellCastingPet) (Object) this,
                ConsumableType.BLOCKING,
                level ? 1 : 0
        );

        ci.cancel();
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/alshanex/familiarslib/util/familiars/FamiliarAttributesHelper;applyAttributes(Lnet/alshanex/familiarslib/entity/AbstractSpellCastingPet;)V"
            )
    )
    private void gt$redirectApplyAttributes(AbstractSpellCastingPet familiar) {
        GoldenTweaksConsumableHelper.applyAttributes(familiar);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/alshanex/familiarslib/util/familiars/FamiliarAttributesHelper;removeAttributes(Lnet/alshanex/familiarslib/entity/AbstractSpellCastingPet;)V"
            )
    )
    private void gt$redirectRemoveAttributes(AbstractSpellCastingPet familiar) {
        GoldenTweaksConsumableHelper.applyAttributes(familiar);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/alshanex/familiarslib/util/familiars/FamiliarManager;updateFamiliarData(Lnet/alshanex/familiarslib/entity/AbstractSpellCastingPet;)V"
            )
    )
    private void gt$redirectUpdateFamiliarData(AbstractSpellCastingPet familiar) {
        GoldenTweaksConsumableHelper.applyAttributes(familiar);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/alshanex/familiarslib/entity/AbstractSpellCastingPet;heal(F)V"
            )
    )
    private void goldenTweaks$changeBedHeal(AbstractSpellCastingPet instance, float v) {
        float healAmount = Math.max(instance.getMaxHealth() * 0.025F, 5.0F);
        instance.heal(healAmount);
    }
}