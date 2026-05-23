package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.util.familiars.FamiliarManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableData;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(FamiliarManager.class)
public class FamiliarManagerMixin {

    @Inject(method = "createFamiliarNBT", at = @At("HEAD"), cancellable = true)
    private static void gt$replaceCreateNBT(
            AbstractSpellCastingPet familiar,
            CallbackInfoReturnable<CompoundTag> cir
    ) {
        CompoundTag nbt = new CompoundTag();

        familiar.saveWithoutId(nbt);

        nbt.putFloat("currentHealth", familiar.getHealth());
        nbt.putFloat("baseMaxHealth", familiar.getBaseMaxHealth());
        nbt.putString("id", EntityType.getKey(familiar.getType()).toString());

        if (familiar.hasCustomName()) {
            nbt.putString("customName", Objects.requireNonNull(familiar.getCustomName()).getString());
        }

        GoldenTweaksConsumableData data = new GoldenTweaksConsumableData(nbt);

        GoldenTweaksConsumableHelper.saveData(familiar, data);

        cir.setReturnValue(nbt);
    }

    @Inject(method = "applyHealthFromNBT", at = @At("HEAD"), cancellable = true)
    private static void applyHealthFromNBT(AbstractSpellCastingPet familiar, CompoundTag familiarNBT, CallbackInfo ci) {

        float savedHealth = familiarNBT.getFloat("currentHealth");

        GoldenTweaksConsumableData data =
                GoldenTweaksConsumableHelper.getData(familiar);

        GoldenTweaksConsumableHelper.saveData(familiar, data);

        if (!familiar.level().isClientSide) {

            float maxHealth = familiar.getMaxHealth();

            familiar.setHealth(Math.min(savedHealth, maxHealth));
        }

        ci.cancel();
    }
}
