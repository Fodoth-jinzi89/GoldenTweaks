package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.EmiSearchWidget;
import dev.emi.emi.search.EmiSearch;
import net.fodoth.skina.goldentweaks.compat.emi.EmiSearchDebounce;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * EMI 搜索节流接入点（与 AE2 终端共用 {@code searchStartDelay} / {@code searchSpreadDuration}）。
 *
 * <p><b>两个入口都接</b>，因为 EMI 1.1.24 里"输入变化 → 搜索"可能落在任一处：</p>
 * <ul>
 *   <li>{@code EmiSearch#update()}（方法体 = {@code search(EmiScreenManager.search.getValue())}）；</li>
 *   <li>{@code EmiSearch#search(String)}（查询串由参数给出）。</li>
 * </ul>
 *
 * <p><b>都只在客户端主线程生效</b>（{@code Minecraft#isSameThread()}）：EMI 的重载线程、以及我们自己
 * 提交流程里的搜索线程也会调这两个方法（实测日志：{@code [Thread-82]}、{@code [GoldenTweaks-EMI-Search]}），
 * 若一并拦下就会形成"提交 → 内部再触发 → 再提交"的自激循环 ⇒ 每秒重搜一次、worker 每次被打断、
 * {@code apply()} 永不执行 ⇒ 结果出不来且 CPU 空转。非主线程一律 return（原样执行）。</p>
 *
 * <p>配合 {@link EmiSearchDebounce} 里"文本与上次已提交相同时静默吞掉"的保险，
 * 本补丁最坏只会"没节流"，不会"搜不出来"。</p>
 */
@Mixin(value = EmiSearch.class, remap = false)
public abstract class EmiSearchMixin {

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private static void gt$delayUpdate(CallbackInfo ci) {

        if (!gt$onClientThread()) {
            return;
        }

        EmiSearchDebounce.tick();

        EmiSearchWidget widget = EmiScreenManager.search;

        if (widget != null && EmiSearchDebounce.shouldDelay(widget.getValue())) {
            ci.cancel();
        }
    }

    @Inject(method = "search", at = @At("HEAD"), cancellable = true)
    private static void gt$delaySearch(String query, CallbackInfo ci) {

        if (!gt$onClientThread()) {
            return;
        }

        EmiSearchDebounce.tick();

        if (EmiSearchDebounce.shouldDelay(query)) {
            ci.cancel();
        }
    }

    private static boolean gt$onClientThread() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.isSameThread();
    }
}
