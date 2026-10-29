package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.client.MediaPlayerManager;
import com.mojang.blaze3d.audio.Channel;
import java.util.Map;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess.ChannelHandle;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 媒体播放器「暂停」是把底层 OpenAL 通道 {@code Channel::pause}，但原版在关闭任意界面时
 * （{@code Minecraft.setScreen(null)}）会调用 {@code SoundManager.resume()} →
 * {@code SoundEngine.resume() → executeOnChannels(Channel::unpause)}，把**所有**通道一并解暂停，
 * 于是已暂停的媒体通道被误恢复播放，直到媒体播放器下一个客户端 tick 重新暂停为止，表现为
 * 「暂停状态下开关背包会响一下」。
 *
 * <p>这里在 {@code SoundEngine.resume} 末尾把媒体播放器自己的通道重新暂停。调用与 resume 排在同一条
 * 声音执行器队列上、紧随其后执行，因此实际可闻间隙可忽略；只在媒体播放器确实处于暂停态时生效，
 * 不影响其它音效。</p>
 */
@Mixin(SoundEngine.class)
public class SoundEngineResumeMixin {

    @Shadow
    @Final
    private Map<SoundInstance, ChannelHandle> instanceToChannel;

    @Inject(method = "resume", at = @At("TAIL"))
    private void gt$keepPausedMediaPlayerPaused(CallbackInfo ci) {
        if (!MediaPlayerManager.isPaused()) {
            return;
        }

        for (Map.Entry<SoundInstance, ChannelHandle> entry : this.instanceToChannel.entrySet()) {
            if ("com.mohistmc.academy.client.MediaPlayerManager$MediaSoundInstance".equals(entry.getKey().getClass().getName())) {
                entry.getValue().execute(Channel::pause);
            }
        }
    }
}
