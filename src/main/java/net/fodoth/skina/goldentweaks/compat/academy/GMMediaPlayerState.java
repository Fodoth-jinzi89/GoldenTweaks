package net.fodoth.skina.goldentweaks.compat.academy;

import com.mohistmc.academy.client.MediaPlayerManager;
import com.mohistmc.academy.client.media.ExternalMediaManager;
import com.mohistmc.academy.skill.AcademyAttachments;
import com.mohistmc.academy.skill.PlayerAbilityData;
import com.mohistmc.academy.terminal.MediaTrack;
import com.mohistmc.academy.terminal.MediaTrackRegistry;
import net.fodoth.skina.goldentweaks.compat.academy.network.GTMediaUnloadPacket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 媒体播放器的播放模式与曲目列表状态（GoldenTweaks 侧新增）。
 *
 * <ul>
 *     <li>三种模式：单曲循环（默认）、列表循环、随机播放，三者互斥。</li>
 *     <li>「删除歌曲」只把当前播放的曲目从播放器列表里隐藏（不改动磁盘文件）。</li>
 *     <li>非单曲循环时，曲目自然播放结束后自动切到下一首 / 随机一首。</li>
 * </ul>
 */
public final class GMMediaPlayerState {

    public enum Mode {
        SINGLE,
        LIST,
        SHUFFLE
    }

    private static Mode mode = Mode.SINGLE;

    private static final Set<String> HIDDEN = new HashSet<>();

    private static String pendingNext;

    private GMMediaPlayerState() {
    }

    public static Mode getMode() {
        return mode;
    }

    public static void setMode(Mode newMode) {
        if (newMode != null) {
            mode = newMode;
        }
    }

    public static boolean isHidden(String trackId) {
        return trackId != null && HIDDEN.contains(trackId);
    }

    /** 删除当前播放的曲目：停止播放；内置曲目请求服务端从「已加载媒体」移除（可再用 media 物品重新加载），
     *  外部曲目只在客户端列表隐藏（外部曲目本就不是服务端 loadedMedia 的一部分）。 */
    public static void deleteCurrent() {
        String current = MediaPlayerManager.getCurrentTrack();
        if (current == null) {
            return;
        }
        MediaPlayerManager.stop();
        MediaTrack track = MediaPlayerManager.getTrack(current);
        if (track != null && track.external()) {
            HIDDEN.add(current);
        } else {
            PacketDistributor.sendToServer(new GTMediaUnloadPacket(current));
        }
    }

    /** 复刻 MediaPlayerAppGui 的可见曲目列表（内置已解锁 + 外部，去掉被隐藏的）。 */
    public static List<MediaTrack> playlist() {
        List<MediaTrack> out = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return out;
        }
        PlayerAbilityData data = mc.player.getData(AcademyAttachments.PLAYER_ABILITY);
        Set<String> loaded = data.getLoadedMedia();
        for (MediaTrack track : MediaTrackRegistry.getAllTracks()) {
            if (loaded.contains(track.trackId()) && !HIDDEN.contains(track.trackId())) {
                out.add(track);
            }
        }
        for (MediaTrack track : ExternalMediaManager.getTracks()) {
            if (!HIDDEN.contains(track.trackId())) {
                out.add(track);
            }
        }
        return out;
    }

    /** 由 MediaPlayerManager.clearFinishedPlayback 调用（只在自然播放结束时触发）。 */
    public static void onTrackFinished(String endedTrackId) {
        if (mode == Mode.SINGLE || endedTrackId == null) {
            return;
        }
        pendingNext = pickNext(endedTrackId);
    }

    private static String pickNext(String endedTrackId) {
        List<MediaTrack> list = playlist();
        if (list.isEmpty()) {
            return null;
        }
        if (mode == Mode.SHUFFLE) {
            List<String> candidates = new ArrayList<>(list.size());
            for (MediaTrack track : list) {
                if (!track.trackId().equals(endedTrackId)) {
                    candidates.add(track.trackId());
                }
            }
            if (candidates.isEmpty()) {
                return list.get(0).trackId();
            }
            return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        }
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).trackId().equals(endedTrackId)) {
                index = i;
                break;
            }
        }
        return index < 0 ? list.get(0).trackId() : list.get((index + 1) % list.size()).trackId();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (pendingNext == null) {
            return;
        }
        String next = pendingNext;
        pendingNext = null;
        if (MediaPlayerManager.getTrack(next) != null) {
            MediaPlayerManager.play(next);
        }
    }
}
