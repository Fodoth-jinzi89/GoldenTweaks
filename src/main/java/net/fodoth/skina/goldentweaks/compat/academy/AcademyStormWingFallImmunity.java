package net.fodoth.skina.goldentweaks.compat.academy;

import com.mohistmc.academy.skill.AcademyAttachments;
import com.mohistmc.academy.skill.PlayerAbilityData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

/**
 * 矢量操作·风暴之翼（storm_wing）解锁后，玩家免疫坠落伤害。
 *
 * <p>注意：这里用 {@code getLearnedSkills().contains(...)} 而不是 {@code hasLearnedSkill(...)}——
 * 后者在开发者模式（{@code devMode}）下恒返回 {@code true}，而 {@code /aim reset} 不会清除 devMode，
 * 会导致重置后仍然免疫坠落伤害。</p>
 */
public final class AcademyStormWingFallImmunity {

    private static final String STORM_WING = "storm_wing";

    private AcademyStormWingFallImmunity() {
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        PlayerAbilityData data = player.getData(AcademyAttachments.PLAYER_ABILITY);
        if (data != null && data.getLearnedSkills().contains(STORM_WING)) {
            event.setCanceled(true);
        }
    }
}
