package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.fodoth.skina.goldentweaks.compat.academy.GMMediaPlayerState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 媒体播放器默认单曲循环。
 *
 * <p>媒体声音实例 {@code MediaPlayerManager$MediaSoundInstance} 构造时把 {@code looping} 设成了 false，
 * 因此只播一遍。这里给它加一个 {@code isLooping() = true} 的覆写（该方法继承自
 * {@code AbstractSoundInstance}，目标类自身未声明，故以「新增方法」的方式覆盖），使
 * {@code SoundEngine} 按自动循环处理：</p>
 * <ul>
 *     <li>内置曲目：静态缓冲走 {@code Channel.setLooping(true)}、流式走 {@code SoundBufferLibrary}
 *         的循环流，均无缝循环；</li>
 *     <li>外部曲目：模组自己的 {@code getStream} 实现忽略了 {@code looping} 参数，这里在循环模式下改用
 *         {@link LoopingAudioStream} 包裹原文件流，使其同样循环。</li>
 * </ul>
 */
@Mixin(targets = "com.mohistmc.academy.client.MediaPlayerManager$MediaSoundInstance")
public class MediaSoundLoopMixin {

    @Shadow
    @Final
    private Path externalSource;

    public boolean isLooping() {
        // 单曲循环模式下由声音引擎循环当前曲目；列表循环 / 随机播放不循环，播完由
        // GMMediaPlayerState 切到下一首。
        return GMMediaPlayerState.getMode() == GMMediaPlayerState.Mode.SINGLE;
    }

    @Inject(method = "getStream", at = @At("HEAD"), cancellable = true)
    private void gt$loopExternalStream(SoundBufferLibrary buffers, Sound sound, boolean looping,
                                       CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
        if (!looping || this.externalSource == null) {
            return;
        }

        cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
            try {
                return new LoopingAudioStream(JOrbisAudioStream::new, Files.newInputStream(this.externalSource));
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, Util.ioPool()));
    }
}
