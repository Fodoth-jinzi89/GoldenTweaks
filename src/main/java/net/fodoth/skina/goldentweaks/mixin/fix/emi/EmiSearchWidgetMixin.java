package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.screen.widget.EmiSearchWidget;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把 GT 的「搜索触发阈值」也套到 EMI 的搜索框上（本来只作用于 AE2 终端，见
 * {@code MEStorageScreenSearchMixin}）：输入后先攒着，等 {@code SEARCH_TRIGGER_THRESHOLD} 个 tick
 * 没有新输入再真正触发搜索，避免在一个 400+ 模组的包里每敲一个字就重排一次列表。
 *
 * <p><b>为什么要 mixin 原版的 {@link EditBox} 而不是 {@code EmiSearchWidget}：</b>
 * {@code EmiSearchWidget} 只重写了 {@code renderWidget}，文本赋值用的 {@code setValue(String)}
 * 是它从 {@code EditBox} 继承的。Mixin 的 {@code @Inject} 只匹配**目标类自己声明**的方法，
 * 打在 {@code EmiSearchWidget} 上会直接找不到目标（Critical injection failure），
 * 所以这里挂在声明它的 {@code EditBox} 上，再用 {@code instanceof} 守卫成"只对 EMI 搜索框生效"。</p>
 *
 * <ul>
 *   <li>阈值 0 = 不延迟，行为同原版 EMI。</li>
 *   <li>同值 {@code setValue} 放行（EMI 的 {@code EmiSearchWidget#update()} 靠 {@code setValue(getValue())}
 *       在数据重载后刷新搜索，不能拦）。</li>
 *   <li>计时用 {@link Util#getMillis()}（阈值 × 50ms），这样在没进世界、{@code level == null}
 *       的菜单界面里也照样生效。</li>
 *   <li>非 EMI 的输入框只多一次 {@code instanceof} 判断。</li>
 * </ul>
 */
@Mixin(EditBox.class)
public abstract class EmiSearchWidgetMixin {

    @Unique
    private String gt$pendingSearch;

    @Unique
    private long gt$lastInputMillis;

    @Unique
    private boolean gt$applyingSearch;

    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    private void gt$delaySearch(String value, CallbackInfo ci) {

        if (!(((Object) this) instanceof EmiSearchWidget self)) {
            return;
        }

        if (gt$applyingSearch || GoldenTweaksClientConfig.SEARCH_TRIGGER_THRESHOLD.get() == 0) {
            return;
        }

        if (value.equals(self.getValue())) {
            return;
        }

        if (value.equals(gt$pendingSearch)) {
            ci.cancel();
            return;
        }

        gt$pendingSearch = value;
        gt$lastInputMillis = Util.getMillis();
        ci.cancel();
    }

    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void gt$applyDelayedSearch(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                       CallbackInfo ci) {

        if (!(((Object) this) instanceof EmiSearchWidget self) || gt$pendingSearch == null) {
            return;
        }

        long delayMillis = Math.max(1L, GoldenTweaksClientConfig.SEARCH_TRIGGER_THRESHOLD.get()) * 50L;

        if (Util.getMillis() - gt$lastInputMillis < delayMillis) {
            return;
        }

        String value = gt$pendingSearch;
        gt$pendingSearch = null;
        gt$applyingSearch = true;
        try {
            self.setValue(value);
        } finally {
            gt$applyingSearch = false;
        }
    }
}
