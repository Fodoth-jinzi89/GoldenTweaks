package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.util.familiars.FamiliarManager;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.alshanex.familiarslib.data.PlayerFamiliarData;
import net.alshanex.familiarslib.registry.AttachmentRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;


@Mixin(FamiliarManager.class)
public class FamiliarManagerMixin {

    @Inject(method = "createFamiliarNBT", at = @At("HEAD"), cancellable = true)
    private static void gt$replaceCreateNBT(
            AbstractSpellCastingPet familiar,
            CallbackInfoReturnable<CompoundTag> cir
    ) {
        CompoundTag nbt = GoldenTweaksConsumableHelper.createFamiliarNBT(familiar);
        cir.setReturnValue(nbt);
    }

    @Inject(method = "applyHealthFromNBT", at = @At("HEAD"), cancellable = true)
    private static void applyHealthFromNBT(AbstractSpellCastingPet familiar, CompoundTag familiarNBT, CallbackInfo ci) {

        float savedHealth = familiarNBT.getFloat("currentHealth");

        if (!familiar.level().isClientSide) {

            float maxHealth = familiar.getMaxHealth();

            familiar.setHealth(Math.min(savedHealth, maxHealth));
        }

        GoldenTweaksConsumableHelper.saveData(familiar);

        ci.cancel();
    }


    @Inject(method = "summonFamiliar", at = @At("TAIL"))
    private static void gt$onSummon(ServerPlayer player, UUID familiarId, CallbackInfo ci) {

        PlayerFamiliarData data =
                player.getData(AttachmentRegistry.PLAYER_FAMILIAR_DATA);

        CompoundTag nbt = data.getFamiliarData(familiarId);
        if (nbt == null) return;

        ServerLevel level = player.serverLevel();
        Entity e = EntityType.byString(nbt.getString("id"))
                .flatMap(type -> Optional.ofNullable(type.create(level)))
                .orElse(null);

        if (!(e instanceof AbstractSpellCastingPet familiar)) return;

        GoldenTweaksConsumableHelper.sync(familiar);
    }

    @Inject(method = "summonSpecificFamiliarAtPosition", at = @At("TAIL"))
    private static void gt$onSummonSpecific(ServerPlayer player, UUID familiarId, int positionIndex, CallbackInfo ci) {

        PlayerFamiliarData data =
                player.getData(AttachmentRegistry.PLAYER_FAMILIAR_DATA);

        CompoundTag nbt = data.getFamiliarData(familiarId);
        if (nbt == null) return;

        ServerLevel level = player.serverLevel();
        Entity e = EntityType.byString(nbt.getString("id"))
                .flatMap(type -> Optional.ofNullable(type.create(level)))
                .orElse(null);

        if (!(e instanceof AbstractSpellCastingPet familiar)) return;

        GoldenTweaksConsumableHelper.sync(familiar);
    }

    @Inject(method = "desummonFamiliar", at = @At("HEAD"))
    private static void gt$saveOnDesummon(ServerPlayer player, UUID familiarId, CallbackInfo ci) {

        ServerLevel level = player.serverLevel();
        Entity entity = level.getEntity(familiarId);

        if (entity instanceof AbstractSpellCastingPet familiar) {

            GoldenTweaksConsumableHelper.saveData(
                    familiar,
                    GoldenTweaksConsumableHelper.getData(familiar)
            );
        }
    }

    @Inject(method = "desummonSpecificFamiliar", at = @At("HEAD"))
    private static void gt$saveSpecific(ServerPlayer player, UUID familiarId, CallbackInfo ci) {

        ServerLevel level = player.serverLevel();
        Entity entity = level.getEntity(familiarId);

        if (entity instanceof AbstractSpellCastingPet familiar) {

            GoldenTweaksConsumableHelper.saveData(
                    familiar,
                    GoldenTweaksConsumableHelper.getData(familiar)
            );
        }
    }

    @Inject(method = "updateFamiliarData", at = @At("HEAD"), cancellable = true)
    private static void gt$update(AbstractSpellCastingPet familiar, CallbackInfo ci) {

        LivingEntity owner = familiar.getSummoner();
        if (!(owner instanceof ServerPlayer player)) return;

        PlayerFamiliarData data =
                player.getData(AttachmentRegistry.PLAYER_FAMILIAR_DATA);

        UUID id = familiar.getUUID();

        if (FamiliarManager.isFamiliarDead(id)) return;
        if (familiar.getHealth() <= 0) return;
        if (!data.hasFamiliar(id)) return;

        try {
            CompoundTag nbt = GoldenTweaksConsumableHelper.createFamiliarNBT(familiar);
            data.addTamedFamiliar(id, nbt);
            GoldenTweaksConsumableHelper.saveData(familiar);
        } catch (Exception e) {
            GoldenTweaks.LOGGER.error("GT sync failed {}", id, e);
        }

        ci.cancel();
    }
}
