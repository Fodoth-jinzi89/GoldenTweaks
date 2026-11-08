package net.fodoth.skina.goldentweaks.compat.ftbchunks;

import dev.ftb.mods.ftbchunks.FTBChunksExpected;
import dev.ftb.mods.ftbchunks.data.ClaimedChunkManagerImpl;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

/**
 * AcademyCraft 的技能（电磁炮、熔解射线、定向爆破等）会自行构造并发布 {@link BlockEvent.BreakEvent}，
 * 而不是走原版破坏流程。这里直接在 NeoForge 事件总线上按 FTB Chunks 的规则复核，
 * 保证技能无法破坏他人领地内的受保护方块。
 */
public final class GTClaimProtection {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!event.isCanceled() && isBlockBreakPrevented(event.getPlayer(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    /**
     * 按 FTB Chunks 的「破坏方块」规则判断某个行为者能否破坏该位置。
     * 供自行发布 BreakEvent 的技能，以及绕过事件直接调用 {@code level.destroyBlock} 的弹射物使用。
     */
    public static boolean isBlockBreakPrevented(@Nullable Entity entity, BlockPos pos) {
        if (!(entity instanceof ServerPlayer player)
                || !GoldenTweaksCommonConfig.isAcademySkillBlockClaimProtectionEnabled()) {
            return false;
        }

        ClaimedChunkManagerImpl manager = ClaimedChunkManagerImpl.getInstance();
        if (manager == null) {
            return false;
        }

        return manager.shouldPreventInteraction(player, InteractionHand.MAIN_HAND, pos,
                FTBChunksExpected.getBlockBreakProtection(), null);
    }

    private GTClaimProtection() {
    }
}
