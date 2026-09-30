package net.fodoth.skina.goldentweaks.compat.academy;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

/**
 * 寂静装备（Silent Gear）的「魔法攻击力」（magic_damage）按档位提升 AcademyCraft 技能的最终伤害。
 * 取主手/副手中的最高值，默认每 5 点增伤 10%。
 */
public final class GTAcademyMagicDamage {

    public static float scaleDamage(Player attacker, float amount) {
        if (attacker == null || !Float.isFinite(amount) || amount <= 0.0F) {
            return amount;
        }

        if (!GoldenTweaksCommonConfig.isAcademyMagicAttackDamageEnabled()
                || !ModList.get().isLoaded("silentgear")) {
            return amount;
        }

        float magic = GearMagicAttack.highest(attacker);
        if (magic <= 0.0F) {
            return amount;
        }

        double perStep = GoldenTweaksCommonConfig.getAcademyMagicAttackPerStep();
        if (perStep <= 0.0D) {
            return amount;
        }

        double multiplier = 1.0D + magic / perStep * GoldenTweaksCommonConfig.getAcademyMagicAttackBonusPerStep();
        if (!Double.isFinite(multiplier) || multiplier <= 1.0D) {
            return amount;
        }

        float scaled = (float) (amount * multiplier);
        return Float.isFinite(scaled) ? scaled : amount;
    }

    private GTAcademyMagicDamage() {
    }
}
