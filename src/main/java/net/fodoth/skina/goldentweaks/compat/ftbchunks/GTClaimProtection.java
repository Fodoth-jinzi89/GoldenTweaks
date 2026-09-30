package net.fodoth.skina.goldentweaks.compat.ftbchunks;

import dev.ftb.mods.ftbchunks.FTBChunksExpected;
import dev.ftb.mods.ftbchunks.data.ClaimedChunkManagerImpl;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * AcademyCraft 的技能（电磁炮、熔解射线、定向爆破等）会自行构造并发布 {@link BlockEvent.BreakEvent}，
 * 而不是走原版破坏流程。这里直接在 NeoForge 事件总线上按 FTB Chunks 的规则复核，
 * 保证技能无法破坏他人领地内的受保护方块。
 */
public final class GTClaimProtection {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()
                || !GoldenTweaksCommonConfig.isAcademySkillBlockClaimProtectionEnabled()
                || !(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        ClaimedChunkManagerImpl manager = ClaimedChunkManagerImpl.getInstance();
        if (manager == null) {
            return;
        }

        if (manager.shouldPreventInteraction(player, InteractionHand.MAIN_HAND, event.getPos(),
                FTBChunksExpected.getBlockBreakProtection(), null)) {
            event.setCanceled(true);
        }
    }

    private GTClaimProtection() {
    }
}
