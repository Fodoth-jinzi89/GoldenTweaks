package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.screen.widget.EmiSearchWidget;
import net.fodoth.skina.goldentweaks.compat.emi.EmiSearchDebounce;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * EMI 搜索防抖的"时钟"：EMI 搜索框每帧都会渲染，于是借它的 {@code renderWidget} 收尾补跑一次
 * 被攒下的搜索（真正的防抖判定与执行在 {@link EmiSearchDebounce}）。
 *
 * <p><b>注意这里刻意不做任何输入拦截</b>：{@code EditBox#setValue}/{@code insertText} 同时负责更新
 * 输入框自己的文本，拦掉它会让打字要等阈值才显示（"打英文非常卡"就是这么来的）；
 * 防抖打在 {@code EmiSearch.search(String)} 上（见 {@code fix/emi/EmiSearchMixin}）。</p>
 *
 * <p>为什么要 mixin 原版 {@link EditBox} 而不是 {@code EmiSearchWidget}：{@code renderWidget} 虽然被
 * {@code EmiSearchWidget} 重写过，但为了拿准 EMI 自己的"帧节奏"这里挂在父类上并用 {@code instanceof}
 * 守卫成"只对 EMI 搜索框生效"（非 EMI 输入框只多一次判断）。</p>
 */
@Mixin(EditBox.class)
public abstract class EmiSearchWidgetMixin {

    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void gt$flushDelayedSearch(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                      CallbackInfo ci) {

        if (!(((Object) this) instanceof EmiSearchWidget)) {
            return;
        }

        EmiSearchDebounce.tick();
    }
}
