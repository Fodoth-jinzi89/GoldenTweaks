package net.fodoth.skina.goldentweaks.mixin.feature.ftbquests;

import dev.ftb.mods.ftbquests.quest.ChapterImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@code QuestPanel#addWidgets} 建画布时会按 {@code shouldShowImage} 过滤章节图片：
 *
 * <pre>{@code
 * this.questScreen.selectedChapter.getImages().stream()
 *         .filter(image -> this.questScreen.file.canEdit() || image.shouldShowImage(selfTeamData))
 *         .sorted(...).forEach(image -> this.add(new ChapterImageButton(this, image)));
 * }</pre>
 *
 * 于是「前置任务还没完成」的图片控件根本不会被创建，而任务完成时画布不会重建，
 * 图片就得等切一次编辑模式才出现。这里把这道过滤去掉：图片控件始终创建，
 * 是否绘制交给 {@code ChapterImageButtonItemMixin} 在 {@code draw} 里按
 * {@code shouldShowImage} 决定，这样解锁后下一帧就能显示。
 */
@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel", remap = false)
public abstract class QuestPanelImageWidgetsMixin {

    @Redirect(
            method = "addWidgets",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"
            ),
            remap = false
    )
    private Stream<ChapterImage> gt$keepNotYetUnlockedImages(Stream<ChapterImage> stream,
                                                             Predicate<ChapterImage> predicate) {
        return stream;
    }
}
