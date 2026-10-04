package net.fodoth.skina.goldentweaks.network;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端保存的「各玩家右键拾取设定」。
 * <p>
 * 拾取设定是客户端的，玩家登录后由客户端同步过来；这里按玩家 UUID 保存并发时读取。
 * 未同步过的玩家（例如没有装本模组的客户端）使用 {@link PickupSettings#DEFAULT}，
 * 由于默认不启用 Lootr 快速拾取，不会影响他们的正常交互。
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class PickupSettingsManager {

    private static final Map<UUID, PickupSettings> SETTINGS = new ConcurrentHashMap<>();

    public static void update(UUID player, PickupSettings settings) {
        SETTINGS.put(player, settings.clamped());
    }

    public static PickupSettings get(UUID player) {
        PickupSettings settings = SETTINGS.get(player);
        return settings != null ? settings : PickupSettings.DEFAULT;
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SETTINGS.remove(player.getUUID());
        }
    }

    private PickupSettingsManager() {
    }
}
