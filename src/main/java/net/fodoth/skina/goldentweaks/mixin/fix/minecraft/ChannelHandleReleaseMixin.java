package net.fodoth.skina.goldentweaks.mixin.fix.minecraft;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 音频通道释放的收尾竞态：{@code ChannelAccess$ChannelHandle.release()} 会在音效线程上调用
 * {@code Library.releaseChannel(channel)}，而 {@code Library.releaseChannel} 在两个通道池都找不到该
 * 通道时会抛 {@code IllegalStateException("Tried to release unknown channel")}。
 *
 * <p>当音频引擎被重建/销毁（资源重载、关服，尤其是崩溃后的关服流程）时，早已排队的释放任务会在通道池
 * 清空之后才执行，于是必然抛异常，被 {@code BlockableEventLoop} 记成
 * {@code Error executing task on Sound executor}。这属于「通道已经没了、再释放一次」的良性竞态，
 * 释放本身应当是无操作（幂等）。这里在调用点容忍该异常，避免关服日志出现无意义的报错堆栈。</p>
 */
@Mixin(targets = "net.minecraft.client.sounds.ChannelAccess$ChannelHandle")
public class ChannelHandleReleaseMixin {

    @Redirect(method = "release",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/audio/Library;releaseChannel(Lcom/mojang/blaze3d/audio/Channel;)V"))
    private static void gt$tolerateUnknownChannel(Library library, Channel channel) {
        try {
            library.releaseChannel(channel);
        } catch (IllegalStateException e) {
            if (!"Tried to release unknown channel".equals(e.getMessage())) {
                throw e;
            }
        }
    }
}
