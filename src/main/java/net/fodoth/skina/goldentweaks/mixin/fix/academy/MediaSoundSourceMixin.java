package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import net.minecraft.sounds.SoundSource;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 媒体播放器的声音实例（{@code MediaPlayerManager$MediaSoundInstance}）把音源类别写死成了
 * {@code SoundSource.MASTER}，因此只受「主音量」影响、不随其它音量滑块变化。
 *
 * <p>这里把 {@code super(...)} 里传入的 {@code SoundSource.MASTER} 改为 {@code SoundSource.RECORDS}
 * （「音符盒/唱片机」音量类别），让媒体播放器的音量跟随全局「音符盒/唱片机」音量设置
 * （媒体播放器自身的音量滑块仍作为额外倍率生效）。</p>
 */
@Mixin(targets = "com.mohistmc.academy.client.MediaPlayerManager$MediaSoundInstance")
public class MediaSoundSourceMixin {

    @Redirect(method = "<init>",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/sounds/SoundSource;MASTER:Lnet/minecraft/sounds/SoundSource;",
                    opcode = Opcodes.GETSTATIC))
    private static SoundSource gt$followRecordVolume() {
        return SoundSource.RECORDS;
    }
}
