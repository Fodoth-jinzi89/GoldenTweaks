package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.screen.widget.EmiSearchWidget;
import net.fodoth.skina.goldentweaks.compat.emi.EmiSearchDebounce;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * EMI 搜索节流的"时钟"：搜索框每帧都会渲染，于是借它的 {@code renderWidget} 收尾补跑一次
 * 被攒下的搜索（节流判定与执行都在 {@link EmiSearchDebounce}）。
 *
 * <p><b>必须挂在 {@code EmiSearchWidget} 自己上</b>：它重写了 {@code EditBox#renderWidget} 且不调用
 * {@code super}，所以挂在父类 {@code EditBox} 上的钩子对 EMI 搜索框根本不会触发
 * （症状：延时生效了、但到点不补跑 ⇒ 看起来"延时后就不搜索了"）。</p>
 *
 * <p>{@code renderWidget} 是 {@code EmiSearchWidget} <b>自己声明</b>的方法，注入没有继承问题；
 * 这里也完全不碰输入（{@code setValue}/{@code insertText}），打字显示实时更新。</p>
 */
@Mixin(value = EmiSearchWidget.class, remap = false)
public abstract class EmiSearchWidgetMixin {

    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void gt$flushPendingSearch(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                       CallbackInfo ci) {

        EmiSearchDebounce.tick();
    }
}
