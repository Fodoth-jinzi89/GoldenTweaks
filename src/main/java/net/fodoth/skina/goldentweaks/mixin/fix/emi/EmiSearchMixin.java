package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.search.EmiSearch;
import net.fodoth.skina.goldentweaks.compat.emi.EmiSearchDebounce;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把 GT 的「搜索触发阈值」套到 EMI 的搜索上（与 AE2 终端共用
 * {@code GoldenTweaksConfig.search_trigger_threshold}，默认 10 tick = 0.5s）。
 *
 * <p>EMI 搜索框的 responder 最终会调到 {@link EmiSearch#search(String)}；在这里把请求攒住，
 * 等停手满阈值再由渲染钩子补跑一次 —— 每敲一个字符就重排一次搜索（大包里本来就要几百 ms，
 * 而且它同时触发 EMI 自己的后缀数组搜索与各种搜索扩展）的问题就没了。</p>
 *
 * <p>只拦 {@code search}，<b>不碰</b> {@code update()}、也不碰输入框的 {@code setValue}：
 * 输入与光标实时更新，只有"跑搜索"这一步被推迟。阈值设 0 即完全回到 EMI 原版行为。</p>
 */
@Mixin(value = EmiSearch.class, remap = false)
public abstract class EmiSearchMixin {

    @Inject(method = "search", at = @At("HEAD"), cancellable = true)
    private static void gt$delaySearch(String query, CallbackInfo ci) {

        if (EmiSearchDebounce.shouldDelay(query)) {
            ci.cancel();
        }
    }
}
