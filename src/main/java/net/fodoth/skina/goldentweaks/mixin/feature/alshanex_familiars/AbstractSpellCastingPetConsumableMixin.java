package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableSystem.ConsumableType;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableData;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableHelper;
import net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars.accessor.EntityAccessor;
import net.fodoth.skina.goldentweaks.network.packet.S2CConsumableSyncPacket;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSpellCastingPet.class)
public abstract class AbstractSpellCastingPetConsumableMixin {


    @Inject(method = "getEnragedStacks", at = @At("HEAD"), cancellable = true)
    private void gt$getEnragedStacks(CallbackInfoReturnable<Integer> cir) {

        AbstractSpellCastingPet self = (AbstractSpellCastingPet) (Object) this;

        int value = (int) GoldenTweaksConsumableHelper.getValue(self, ConsumableType.ENRAGED);

        GoldenTweaks.LOGGER.debug("[GT-Consumable] get ENRAGED = {} (entity={})",
                value, self.getId());

        cir.setReturnValue(value);
    }

    @Inject(method = "getArmorStacks", at = @At("HEAD"), cancellable = true)
    private void gt$getArmorStacks(CallbackInfoReturnable<Integer> cir) {

        AbstractSpellCastingPet self = (AbstractSpellCastingPet) (Object) this;

        int value = (int) GoldenTweaksConsumableHelper.getValue(self, ConsumableType.ARMOR);

        GoldenTweaks.LOGGER.debug("[GT-Consumable] get ARMOR = {} (entity={})",
                value, self.getId());

        cir.setReturnValue(value);
    }

    @Inject(method = "getHealthStacks", at = @At("HEAD"), cancellable = true)
    private void gt$getHealthStacks(CallbackInfoReturnable<Integer> cir) {

        AbstractSpellCastingPet self = (AbstractSpellCastingPet) (Object) this;

        int value = (int) GoldenTweaksConsumableHelper.getValue(self, ConsumableType.HEALTH);

        GoldenTweaks.LOGGER.debug("[GT-Consumable] get HEALTH = {} (entity={})",
                value, self.getId());

        cir.setReturnValue(value);
    }

    @Inject(method = "getIsBlocking", at = @At("HEAD"), cancellable = true)
    private void gt$getIsBlocking(CallbackInfoReturnable<Boolean> cir) {

        AbstractSpellCastingPet self = (AbstractSpellCastingPet) (Object) this;

        boolean value = GoldenTweaksConsumableHelper.getValue(self, ConsumableType.BLOCKING) > 0;

        GoldenTweaks.LOGGER.debug("[GT-Consumable] get BLOCKING = {} (entity={})",
                value, self.getId());

        cir.setReturnValue(value);
    }

    // =========================
    // SETTERS
    // =========================

    @Inject(method = "setEnragedStacks", at = @At("HEAD"), cancellable = true)
    private void gt$setEnragedStacks(Integer level, CallbackInfo ci) {

        AbstractSpellCastingPet self = (AbstractSpellCastingPet) (Object) this;

        GoldenTweaks.LOGGER.info("[GT-Consumable] set ENRAGED = {} (entity={})",
                level, self.getId());

        GoldenTweaksConsumableHelper.setValue(self, ConsumableType.ENRAGED, level);

        ci.cancel();
    }

    @Inject(method = "setArmorStacks", at = @At("HEAD"), cancellable = true)
    private void gt$setArmorStacks(Integer level, CallbackInfo ci) {

        AbstractSpellCastingPet self = (AbstractSpellCastingPet) (Object) this;

        GoldenTweaks.LOGGER.info("[GT-Consumable] set ARMOR = {} (entity={})",
                level, self.getId());

        GoldenTweaksConsumableHelper.setValue(self, ConsumableType.ARMOR, level);

        ci.cancel();
    }

    @Inject(method = "setHealthStacks", at = @At("HEAD"), cancellable = true)
    private void gt$setHealthStacks(Integer level, CallbackInfo ci) {

        AbstractSpellCastingPet self = (AbstractSpellCastingPet) (Object) this;

        GoldenTweaks.LOGGER.info("[GT-Consumable] set HEALTH = {} (entity={})",
                level, self.getId());

        GoldenTweaksConsumableHelper.setValue(self, ConsumableType.HEALTH, level);

        ci.cancel();
    }

    @Inject(method = "setIsBlocking", at = @At("HEAD"), cancellable = true)
    private void gt$setIsBlocking(Boolean level, CallbackInfo ci) {

        AbstractSpellCastingPet self = (AbstractSpellCastingPet) (Object) this;

        GoldenTweaks.LOGGER.info("[GT-Consumable] set BLOCKING = {} (entity={})",
                level, self.getId());

        GoldenTweaksConsumableHelper.setValue(
                self,
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
        GoldenTweaks.LOGGER.debug(
                "[GT-Attr] replace applyAttributes entity={}",
                familiar.getId()
        );
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/alshanex/familiarslib/util/familiars/FamiliarAttributesHelper;removeAttributes(Lnet/alshanex/familiarslib/entity/AbstractSpellCastingPet;)V"
            )
    )
    private void gt$redirectRemoveAttributes(AbstractSpellCastingPet familiar) {
        // 这里原作者为什么要 Remove，看不懂
        GoldenTweaksConsumableHelper.applyAttributes(familiar);
    }


    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "    Lnet/alshanex/familiarslib/util/familiars/FamiliarManager;updateFamiliarData(Lnet/alshanex/familiarslib/entity/AbstractSpellCastingPet;)V"
            )
    )
    private void gt$redirectUpdateFamiliarData(AbstractSpellCastingPet familiar) {
        // 这里原作者又为什么要重新 create，离谱啊
    }
}