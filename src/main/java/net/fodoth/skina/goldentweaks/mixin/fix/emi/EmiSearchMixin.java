package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.search.EmiSearch;
import net.fodoth.skina.goldentweaks.compat.emi.EmiSearchDebounce;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把 GT 的搜索节流套到 EMI 上（与 AE2 终端共用 {@code searchStartDelay} / {@code searchSpreadDuration}）。
 *
 * <ul>
 *   <li>{@code search(String)}：节流点本身 —— 间隔不够就把这次查询攒住（{@link EmiSearchDebounce}）；</li>
 *   <li>{@code update()}：<b>不拦</b>，只在头部顺带叫一次 {@code tick()}（EMI 自己在刷新时
 *       会把攒下的查询补跑掉，等于多一个时钟）。</li>
 * </ul>
 *
 * <p>输入框的 {@code setValue}/{@code insertText} 一律不碰（它们同时负责更新输入框显示，
 * 拦了会让打字像卡住）。阈值 0 = 完全回到原版。</p>
 */
@Mixin(value = EmiSearch.class, remap = false)
public abstract class EmiSearchMixin {

    @Inject(method = "search", at = @At("HEAD"), cancellable = true)
    private static void gt$delaySearch(String query, CallbackInfo ci) {

        if (EmiSearchDebounce.shouldDelay(query)) {
            ci.cancel();
        }
    }

    @Inject(method = "update", at = @At("HEAD"))
    private static void gt$tickOnUpdate(CallbackInfo ci) {

        EmiSearchDebounce.tick();
    }
}
