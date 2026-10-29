package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.client.MediaPlayerManager;
import com.mohistmc.academy.client.gui.MediaPlayerHudOverlay;
import com.mohistmc.academy.terminal.MediaTrack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 媒体播放器右下角 HUD 的微调：
 *
 * <ul>
 *     <li>进度条与歌曲名上移 2px，读秒 Y 坐标不变：原版布局里歌名在 {@code y+16}、进度条在
 *         {@code y+27..y+29}、读秒在 {@code y+27}。这里把 {@code render} 中两处 {@code GuiGraphics.fill}
 *         （进度条背景与前景）以及第一处 {@code GuiGraphics.drawString}（歌名）的 Y 坐标整体减 2。</li>
 *     <li>读秒改为「当前时间 / 总时长」：把 {@code MediaPlayerManager.formatTime(elapsed)} 的返回值
 *         拼上曲目总时长。</li>
 * </ul>
 */
@Mixin(MediaPlayerHudOverlay.class)
public class MediaPlayerHudOverlayMixin {

    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private static void gt$raiseProgressBar(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1 - 2, x2, y2 - 2, color);
    }

    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
                    ordinal = 0))
    private static int gt$raiseTitle(GuiGraphics graphics, Font font, String text, int x, int y, int color, boolean shadow) {
        return graphics.drawString(font, text, x, y - 2, color, shadow);
    }

    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lcom/mohistmc/academy/client/MediaPlayerManager;formatTime(I)Ljava/lang/String;"))
    private static String gt$currentAndTotal(int elapsedSeconds) {
        MediaTrack track = MediaPlayerManager.getTrack(MediaPlayerManager.getCurrentTrack());
        int total = track != null ? track.durationSeconds() : 0;
        return MediaPlayerManager.formatTime(elapsedSeconds) + " / " + MediaPlayerManager.formatTime(total);
    }
}
