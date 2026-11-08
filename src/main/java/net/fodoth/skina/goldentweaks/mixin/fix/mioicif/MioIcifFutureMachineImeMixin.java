package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.miophas.singularity_iteration.common.client.screen.mio_icif_gui_future_elc;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 期货机界面每帧先把所有输入框 {@code setVisible(false)}，再在绘制当前页时把用到的重新设为可见。
 * IMBlocker 在 {@code EditBox#setVisible} 的 TAIL 里重新判定文本焦点：这一隐藏会让它判定输入法
 * 失去焦点并与下一帧的恢复反复切换，表现为输入框聚焦时输入法窗口闪烁。
 *
 * <p>这里只跳过「隐藏当前聚焦输入框」这一帧操作；随后当前页的绘制仍会把它设回可见，
 * 因此渲染结果不变，但焦点状态不再被反复撤销。</p>
 */
@Mixin(value = mio_icif_gui_future_elc.class, remap = false)
public abstract class MioIcifFutureMachineImeMixin {

    @Redirect(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setVisible(Z)V"))
    private void gt$keepFocusedInputVisible(EditBox box, boolean visible) {
        if (!visible && (box.isFocused() || box == ((Screen) (Object) this).getFocused())) {
            return;
        }
        box.setVisible(visible);
    }
}
