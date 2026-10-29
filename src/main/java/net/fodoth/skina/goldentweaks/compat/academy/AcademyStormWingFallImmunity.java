package net.fodoth.skina.goldentweaks.compat.academy;

import com.mohistmc.academy.skill.AcademyAttachments;
import com.mohistmc.academy.skill.PlayerAbilityData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

/**
 * 矢量操作·风暴之翼（storm_wing）解锁后，玩家免疫坠落伤害。
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
        if (data != null && data.hasLearnedSkill(STORM_WING)) {
            event.setCanceled(true);
        }
    }
}
