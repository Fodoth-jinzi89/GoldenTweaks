package net.fodoth.skina.goldentweaks.compat.dangomusic;

import me.sorihiko.dango.music.MusicPlayer;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public final class GTDangoMusicEvents {

    private GTDangoMusicEvents() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        if (GoldenTweaksClientConfig.DANGO_STOP_ON_WORLD_LEAVE.get()) {
            MusicPlayer.INSTANCE.stop();
        }
    }
}
