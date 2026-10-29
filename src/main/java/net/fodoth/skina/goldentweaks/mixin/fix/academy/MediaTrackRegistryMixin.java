package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.terminal.MediaTrack;
import com.mohistmc.academy.terminal.MediaTrackRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 内置媒体曲目的时长在 {@link MediaTrackRegistry#registerBuiltins()} 里是写死的，而且与随包音频的实际长度
 * 相差很大（例如 {@code only_my_railgun} 标称 99 秒，实际 ogg 约 257 秒），导致进度条提前填满、总时长显示错误。
 *
 * <p>这里在注册时按曲目 id 用实际音频长度（由 ogg 的 granule/采样率换算）替换：</p>
 * <ul>
 *     <li>{@code only_my_railgun} → 257s（4:17）</li>
 *     <li>{@code level5_judgelight} → 265s（4:25）</li>
 *     <li>{@code sisters_noise} → 262s（4:22）</li>
 * </ul>
 */
@Mixin(MediaTrackRegistry.class)
public class MediaTrackRegistryMixin {

    @Redirect(method = "registerBuiltins",
            at = @At(value = "INVOKE",
                    target = "Lcom/mohistmc/academy/terminal/MediaTrackRegistry;register(Lcom/mohistmc/academy/terminal/MediaTrack;)V"))
    private static void gt$fixBuiltinDuration(MediaTrack track) {
        MediaTrackRegistry.register(gt$withRealDuration(track));
    }

    @Unique
    private static MediaTrack gt$withRealDuration(MediaTrack track) {
        int seconds = switch (track.trackId()) {
            case "only_my_railgun" -> 257;
            case "level5_judgelight" -> 265;
            case "sisters_noise" -> 262;
            default -> -1;
        };
        if (seconds <= 0 || seconds == track.durationSeconds()) {
            return track;
        }
        return new MediaTrack(
                track.trackId(), track.nameKey(), track.descKey(), track.tag(),
                track.texture(), track.soundId(), seconds,
                track.externalSource(), track.externalName(), track.externalDescription());
    }
}
