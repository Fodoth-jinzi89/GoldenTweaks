package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.client.MediaPlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 单曲循环下让播放进度、读秒按曲目总时长取模，这样每循环一轮进度条与「当前时间」都会归零，
 * 而不是一路超过总时长（否则进度条会一直满格、读秒会显示 {@code 4:18 / 4:17} 之类的怪值）。
 *
 * <p>{@code elapsedTicks()} 只被 {@code getProgress()} 与 {@code getElapsedSeconds()} 使用，
 * 因此在这里统一取模即可同时修正两者。</p>
 */
@Mixin(MediaPlayerManager.class)
public class MediaPlayerManagerLoopMixin {

    @Shadow
    private static int currentDurationTicks;

    @Inject(method = "elapsedTicks", at = @At("RETURN"), cancellable = true)
    private static void gt$wrapElapsedForLoop(CallbackInfoReturnable<Integer> cir) {
        int duration = currentDurationTicks;
        if (duration > 0) {
            cir.setReturnValue(cir.getReturnValueI() % duration);
        }
    }
}
