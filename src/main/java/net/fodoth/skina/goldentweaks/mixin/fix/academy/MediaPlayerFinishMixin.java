package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.client.MediaPlayerManager;
import net.fodoth.skina.goldentweaks.compat.academy.GMMediaPlayerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 媒体播放器「曲目自然播放结束」钩子。{@code clearFinishedPlayback()} 只在声音实例不再活跃（即自然播完）
 * 时被调用，用户手动停止不会走到这里，因此可据此在列表循环 / 随机播放模式下切到下一首。
 *
 * <p>必须在清理前拿到 {@code currentTrack}，故在 {@code HEAD} 注入。</p>
 */
@Mixin(MediaPlayerManager.class)
public class MediaPlayerFinishMixin {

    @Shadow
    private static String currentTrack;

    @Inject(method = "clearFinishedPlayback", at = @At("HEAD"))
    private static void gt$onTrackFinished(CallbackInfo ci) {
        GMMediaPlayerState.onTrackFinished(currentTrack);
    }
}
