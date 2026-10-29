package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.client.gui.AcademyScreen;
import com.mohistmc.academy.client.gui.MediaPlayerAppGui;
import com.mohistmc.academy.terminal.MediaTrack;
import java.util.ArrayList;
import java.util.List;
import net.fodoth.skina.goldentweaks.compat.academy.GMMediaPlayerState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 播放器界面右下角新增 4 个按钮：单曲循环 / 列表循环 / 随机播放 / 删除歌曲。
 *
 * <p>布局（控制条第二行，读秒之后横排）：读秒、单曲循环、列表循环、随机播放、删除歌曲。
 * 前三个尺寸 20×17（贴图 50×42，保持比例不变形）、空隙相等；删除按钮 24×20，与前一个间距略大。
 * 三个模式按钮互斥，激活态叠一层与原版按下相同的蓝色；删除按钮按下时叠红色。</p>
 */
@Mixin(MediaPlayerAppGui.class)
public class MediaPlayerAppGuiMixin {

    @Unique
    private static final ResourceLocation GT_SINGLE = ResourceLocation.fromNamespaceAndPath("goldentweaks", "textures/gui/media/single_loop.png");
    @Unique
    private static final ResourceLocation GT_LIST = ResourceLocation.fromNamespaceAndPath("goldentweaks", "textures/gui/media/list_loop.png");
    @Unique
    private static final ResourceLocation GT_SHUFFLE = ResourceLocation.fromNamespaceAndPath("goldentweaks", "textures/gui/media/shuffle.png");
    @Unique
    private static final ResourceLocation GT_DELETE = ResourceLocation.fromNamespaceAndPath("goldentweaks", "textures/gui/media/delete.png");

    @Unique
    private static final int GT_MODE_W = 20;
    @Unique
    private static final int GT_MODE_H = 17;
    @Unique
    private static final int GT_DEL_W = 24;
    @Unique
    private static final int GT_DEL_H = 20;
    @Unique
    private static final int GT_GAP = 6;
    @Unique
    private static final int GT_DEL_GAP = 9;
    @Unique
    private static final int GT_RIGHT = 6;
    @Unique
    private static final int GT_BOTTOM = 6;

    @Unique
    private static final int GT_ACTIVE_COLOR = 0xFF4FC3FF;
    @Unique
    private static final int GT_HOVER_COLOR = 0xFF8FD8F5;
    @Unique
    private static final int GT_DELETE_COLOR = 0xFFFF5050;
    @Unique
    private static final int GT_ORIGINAL_HOVER_PLAY = 855696895;
    @Unique
    private static final int GT_ORIGINAL_HOVER_STOP = 872371541;

    @Unique
    private long gt$deleteFlashUntil = 0L;

    @Shadow
    private List<MediaTrack> visibleTracks;

    @Inject(method = "refreshVisibleTracks", at = @At("TAIL"))
    private void gt$filterHidden(CallbackInfo ci) {
        this.gt$applyHidden();
    }

    @Unique
    private void gt$applyHidden() {
        if (this.visibleTracks.isEmpty()) {
            return;
        }
        List<MediaTrack> kept = new ArrayList<>(this.visibleTracks.size());
        for (MediaTrack track : this.visibleTracks) {
            if (!GMMediaPlayerState.isHidden(track.trackId())) {
                kept.add(track);
            }
        }
        if (kept.size() != this.visibleTracks.size()) {
            this.visibleTracks = List.copyOf(kept);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void gt$renderButtons(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        int guiLeft = ((Screen) (Object) this).width / 2 - 150;
        int guiTop = ((Screen) (Object) this).height / 2 - 105;

        int deleteX = guiLeft + 300 - GT_RIGHT - GT_DEL_W;
        int rowY = guiTop + 210 - GT_BOTTOM - GT_MODE_H;
        int deleteY = rowY + GT_MODE_H / 2 - GT_DEL_H / 2;
        int shuffleX = deleteX - GT_DEL_GAP - GT_MODE_W;
        int listX = shuffleX - GT_GAP - GT_MODE_W;
        int singleX = listX - GT_GAP - GT_MODE_W;

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);

        this.gt$drawButton(graphics, GT_SINGLE, singleX, rowY, GT_MODE_W, GT_MODE_H, mouseX, mouseY,
                GMMediaPlayerState.getMode() == GMMediaPlayerState.Mode.SINGLE, false);
        this.gt$drawButton(graphics, GT_LIST, listX, rowY, GT_MODE_W, GT_MODE_H, mouseX, mouseY,
                GMMediaPlayerState.getMode() == GMMediaPlayerState.Mode.LIST, false);
        this.gt$drawButton(graphics, GT_SHUFFLE, shuffleX, rowY, GT_MODE_W, GT_MODE_H, mouseX, mouseY,
                GMMediaPlayerState.getMode() == GMMediaPlayerState.Mode.SHUFFLE, false);
        this.gt$drawButton(graphics, GT_DELETE, deleteX, deleteY, GT_DEL_W, GT_DEL_H, mouseX, mouseY,
                false, System.currentTimeMillis() < this.gt$deleteFlashUntil);

        graphics.pose().popPose();
    }

    @Unique
    private void gt$drawButton(GuiGraphics graphics, ResourceLocation texture, int x, int y, int w, int h,
                               int mouseX, int mouseY, boolean active, boolean pressed) {
        boolean hovered = AcademyScreen.isHovered(x, y, w, h, mouseX, mouseY);
        graphics.blit(texture, x, y, w, h, 0.0F, 0.0F, 50, 42, 50, 42);
        if (active) {
            gt$border(graphics, x, y, w, h, GT_ACTIVE_COLOR);
        } else if (pressed) {
            gt$border(graphics, x, y, w, h, GT_DELETE_COLOR);
        } else if (hovered) {
            gt$border(graphics, x, y, w, h, GT_HOVER_COLOR);
        }
    }

    @Unique
    private static void gt$border(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void gt$clickButtons(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }
        int guiLeft = ((Screen) (Object) this).width / 2 - 150;
        int guiTop = ((Screen) (Object) this).height / 2 - 105;

        int deleteX = guiLeft + 300 - GT_RIGHT - GT_DEL_W;
        int rowY = guiTop + 210 - GT_BOTTOM - GT_MODE_H;
        int deleteY = rowY + GT_MODE_H / 2 - GT_DEL_H / 2;
        int shuffleX = deleteX - GT_DEL_GAP - GT_MODE_W;
        int listX = shuffleX - GT_GAP - GT_MODE_W;
        int singleX = listX - GT_GAP - GT_MODE_W;

        int mx = (int) mouseX;
        int my = (int) mouseY;

        if (AcademyScreen.isHovered(singleX, rowY, GT_MODE_W, GT_MODE_H, mx, my)) {
            GMMediaPlayerState.setMode(GMMediaPlayerState.Mode.SINGLE);
            cir.setReturnValue(true);
        } else if (AcademyScreen.isHovered(listX, rowY, GT_MODE_W, GT_MODE_H, mx, my)) {
            GMMediaPlayerState.setMode(GMMediaPlayerState.Mode.LIST);
            cir.setReturnValue(true);
        } else if (AcademyScreen.isHovered(shuffleX, rowY, GT_MODE_W, GT_MODE_H, mx, my)) {
            GMMediaPlayerState.setMode(GMMediaPlayerState.Mode.SHUFFLE);
            cir.setReturnValue(true);
        } else if (AcademyScreen.isHovered(deleteX, deleteY, GT_DEL_W, GT_DEL_H, mx, my)) {
            this.gt$deleteFlashUntil = System.currentTimeMillis() + 180L;
            GMMediaPlayerState.deleteCurrent();
            this.gt$applyHidden();
            cir.setReturnValue(true);
        }
    }

    /** 原版播放/停止按钮的悬停高亮：由整块半透明填充改为只描外边框（与新按钮一致）。 */
    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private static void gt$borderInsteadOfFill(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        if (color == GT_ORIGINAL_HOVER_PLAY || color == GT_ORIGINAL_HOVER_STOP) {
            gt$border(graphics, x1, y1, x2 - x1, y2 - y1, GT_ACTIVE_COLOR);
            return;
        }
        graphics.fill(x1, y1, x2, y2, color);
    }

    /** 「选择曲目开始播放 / 载入曲目」提示移到第一行，避免被右下角按钮遮住。 */
    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"))
    private static int gt$raiseHint(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        if (text != null
                && (text.equals(Component.translatable("ac.media.select_hint").getString())
                || text.equals(Component.translatable("ac.media.load_hint").getString()))) {
            y -= 9;
        }
        return graphics.drawString(font, text, x, y, color);
    }
}
