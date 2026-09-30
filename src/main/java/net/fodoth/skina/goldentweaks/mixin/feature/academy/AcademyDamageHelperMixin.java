package net.fodoth.skina.goldentweaks.mixin.feature.academy;

import com.mohistmc.academy.skill.AcademyDamageHelper;
import net.fodoth.skina.goldentweaks.compat.academy.GTAcademyMagicDamage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 所有玩家技能伤害最终都会经过 {@link AcademyDamageHelper#hurt}，在这里按寂静装备的魔法攻击力放大最终伤害。
 */
@Mixin(AcademyDamageHelper.class)
public class AcademyDamageHelperMixin {

    @Unique
    private static final ThreadLocal<Boolean> gt$scaling = new ThreadLocal<>();

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private static void gt$scaleMagicAttackDamage(ServerPlayer attacker, Entity target, DamageSource source, float amount,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (attacker == null || Boolean.TRUE.equals(gt$scaling.get())) {
            return;
        }

        float scaled = GTAcademyMagicDamage.scaleDamage(attacker, amount);
        if (scaled == amount) {
            return;
        }

        gt$scaling.set(Boolean.TRUE);
        try {
            cir.setReturnValue(AcademyDamageHelper.hurt(attacker, target, source, scaled));
        } finally {
            gt$scaling.set(null);
        }
    }
}
