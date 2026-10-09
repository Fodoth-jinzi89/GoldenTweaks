package net.fodoth.skina.goldentweaks.mixin.fix.aeqc;

import com.github.debris.aeqc.gui.button.BookmarkMissingButton;
import net.fodoth.skina.goldentweaks.compat.ae2.GTAe2CraftConfirmBookmark;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * AEQC 的「收藏缺失项」按钮（合成计划界面左上角工具栏）在纯 EMI 环境里点了没反应：它的
 * {@code onPress} 调 {@code BookmarkMissing#onClick}，而后者引用的是 JEI 内部类
 * （{@code mezz.jei.gui.bookmarks.BookmarkList} / {@code mezz.jei.gui.overlay.bookmarks.BookmarkOverlay}
 * / {@code BookmarkFactory}）。本包的 JEI 是 TMRV 的桩，只有 API 没有这些内部类 —— 于是这个类本身
 * 都可能加载失败，直接 mixin 它的方法并不可靠。
 *
 * <p>所以在<b>按钮构造时</b>就把传进来的 {@code OnPress} 换成我们自己的实现（只依赖 EMI，
 * 不碰任何 JEI 类），从而彻底绕开 {@code BookmarkMissing}。</p>
 */
@Mixin(value = BookmarkMissingButton.class, remap = false)
public abstract class BookmarkMissingButtonMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/gui/widgets/IconButton;<init>(Lnet/minecraft/client/gui/components/Button$OnPress;)V"),
            index = 0)
    private static Button.OnPress goldentweaks$routeToEmi(Button.OnPress original) {
        return button -> GTAe2CraftConfirmBookmark.favoriteMissingIntoEmi();
    }
}
