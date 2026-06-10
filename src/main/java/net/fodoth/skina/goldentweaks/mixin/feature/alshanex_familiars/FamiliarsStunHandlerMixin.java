package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.alshanex.alshanex_familiars.Config;
import net.alshanex.alshanex_familiars.event.FamiliarsStunHandler;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksFamiliarCurioHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FamiliarsStunHandler.class)
public abstract class FamiliarsStunHandlerMixin {

    @WrapOperation(
            method = "onLivingDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/alshanex/alshanex_familiars/util/ArmorSetUtils;hasCompleteFamiliarSet(Lnet/minecraft/world/entity/player/Player;)Z"
            ),
            remap = false
    )
    private static boolean goldenTweaks$allowSpellbook(
            Player player, Operation<Boolean> original
    ) {
        return original.call(player)
                || GoldenTweaksFamiliarCurioHelper
                .isWearingInvertedFamiliarSpellbook(player);
    }

    @ModifyVariable(
            method = "onLivingDeath",
            at = @At("STORE"),
            remap = false,
            name = "totalStunDuration")
    private static int goldenTweaks$stunScaling(
            int totalStunDuration
    ) {
        int base =
                Config.STUN_TIME_PER_FAMILIAR.get() * 20;

        int count = totalStunDuration / base;

        double multiplier = 0.0D;

        for (int i = 0; i < count; i++) {
            multiplier += Math.pow(0.8D, i);
        }

        return (int)Math.round(base * multiplier);
    }
}