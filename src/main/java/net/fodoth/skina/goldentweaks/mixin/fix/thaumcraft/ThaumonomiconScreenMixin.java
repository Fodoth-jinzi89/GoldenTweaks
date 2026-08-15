package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.client.gui.ThaumonomiconData;
import thaumcraft.client.gui.ThaumonomiconScreen;

import java.util.List;

/**
 * 研究标签页翻页（移植自 TC4Tweaks 的 BrowserPaging）。
 *
 * <p>移植版 {@code drawCategoryTabs} 固定每列画 9 个标签（左右两列共 18 个），
 * 超过 18 个分类时会重叠。这里按每页 18 个标签分页：{@code drawCategoryTabs}
 * 只绘制当前页的分类子列表，并在标签列下方绘制 {@code « / »} 翻页箭头；
 * 点击处理挂到 {@code handleMapCategoryClick}（与标签点击同一坐标体系）。
 * 标签数不超过 18 时完全不生效。</p>
 */
@Mixin(value = ThaumonomiconScreen.class, remap = false)
public abstract class ThaumonomiconScreenMixin {

    private static final int TABS_PER_PAGE = 18;

    /** 左/右翻页箭头的位置（与标签列同一坐标体系，标签列底部 y≈216）。 */
    private static final int PREV_X = -20;
    private static final int NEXT_X = 262;
    private static final int BUTTON_Y = 220;

    @Unique
    private static int gt$tabPage;

    @Shadow
    private Font font;

    @Redirect(
            method = "drawCategoryTabs",
            at = @At(value = "INVOKE", target = "Lthaumcraft/client/gui/ThaumonomiconData;categories()Ljava/util/List;")
    )
    private static List<ThaumonomiconData.Category> gt$tabsForCurrentPage() {
        List<ThaumonomiconData.Category> all = ThaumonomiconData.categories();
        int pages = gt$pageCount(all.size());
        if (pages <= 1) {
            return all;
        }

        gt$tabPage = Mth.clamp(gt$tabPage, 0, pages - 1);
        int start = gt$tabPage * TABS_PER_PAGE;
        return all.subList(start, Math.min(start + TABS_PER_PAGE, all.size()));
    }

    @Inject(method = "drawCategoryTabs", at = @At("RETURN"))
    private void gt$drawTabPaging(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
        if (gt$pageCount() <= 1) {
            return;
        }

        guiGraphics.drawString(this.font, "«", PREV_X, BUTTON_Y, 0xFF404040);
        guiGraphics.drawString(this.font, "»", NEXT_X, BUTTON_Y, 0xFF404040);
    }

    @Inject(method = "handleMapCategoryClick", at = @At("HEAD"), cancellable = true)
    private void gt$handleTabPaging(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (gt$pageCount() <= 1) {
            return;
        }

        if (mouseY < BUTTON_Y - 4 || mouseY > BUTTON_Y + 12) {
            return;
        }

        if (mouseX >= PREV_X - 4 && mouseX <= PREV_X + 10) {
            gt$tabPage = Math.max(0, gt$tabPage - 1);
            cir.setReturnValue(true);
        } else if (mouseX >= NEXT_X - 4 && mouseX <= NEXT_X + 10) {
            gt$tabPage = Math.min(gt$pageCount() - 1, gt$tabPage + 1);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static int gt$pageCount() {
        return gt$pageCount(ThaumonomiconData.categories().size());
    }

    @Unique
    private static int gt$pageCount(int total) {
        return (total + TABS_PER_PAGE - 1) / TABS_PER_PAGE;
    }
}
