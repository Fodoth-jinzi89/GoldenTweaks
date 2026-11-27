package net.fodoth.skina.goldentweaks.compat.ezunclear;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * 与 EzUnclear 的联动：奇点迭代核反应堆的爆炸改为「先挂起、聊天确认后引爆」。
 *
 * <p>EzUnclear 自己只接管了 Mekanism 裂变堆与龙之进化反应堆。这里复刻它同一套语义，
 * 让核反应堆也具备同样的「手滑不至于直接炸没半个基地」的保护：</p>
 *
 * <ul>
 *   <li>堆芯即将爆炸时不再立即引爆，而是挂起并广播提示；</li>
 *   <li>玩家在聊天栏输入确认口令后，挂起解除，下一 tick 由反应堆原有逻辑正常引爆；</li>
 *   <li>挂起期间反应堆停在 PENDING 状态，不会重复登记、也不会刷屏。</li>
 * </ul>
 *
 * <p>提示与确认口令沿用 EzUnclear 的翻译键，玩家不需要记两套说法。</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class GTEzUnclearPendingBlasts {

    private static final String NOTICE_KEY = "info.ezunclear";
    private static final String CONFIRM_KEY = "info.ezunclear.interact";

    private static final Set<BlockPos> PENDING = new HashSet<>();

    private GTEzUnclearPendingBlasts() {
    }

    /** 只有装了 EzUnclear 才启用，否则完全不影响原版反应堆行为。 */
    public static boolean isActive() {
        return net.neoforged.fml.ModList.get().isLoaded("ezunclear");
    }

    public static boolean defer(ServerLevel level, BlockPos pos) {
        BlockPos key = pos.immutable();
        synchronized (PENDING) {
            if (PENDING.contains(key)) {
                return true;
            }
            PENDING.add(key);
        }

        Component notice = Component.translatable(NOTICE_KEY);
        for (ServerPlayer player : level.players()) {
            player.displayClientMessage(notice, false);
        }
        return true;
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        String confirm = Component.translatable(CONFIRM_KEY).getString();
        if (!event.getRawText().equals(confirm)) {
            return;
        }
        synchronized (PENDING) {
            if (PENDING.isEmpty()) {
                return;
            }
            GoldenTweaks.LOGGER.info("EzUnclear 联动：确认引爆 {} 座核反应堆", PENDING.size());
            PENDING.clear();
        }
    }

    /** 退出存档时清空，避免残留坐标影响下一次进入的世界。 */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        synchronized (PENDING) {
            PENDING.clear();
        }
    }
}
