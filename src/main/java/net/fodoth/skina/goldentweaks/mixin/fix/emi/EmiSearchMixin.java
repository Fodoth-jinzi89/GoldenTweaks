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
 * EMI 搜索节流的接入点（与 AE2 终端共用 {@code searchStartDelay} / {@code searchSpreadDuration}）。
 *
 * <p>接入点选 {@code EmiSearch#update()}：它的方法体就是
 * {@code search(EmiScreenManager.search.getValue())}（javap 确认），"输入变化 → 触发搜索"必经此处；
 * 而 {@code search(String)} 本身不拦（重载、侧栏等入口保持原样），最坏只会"没节流"，不会"搜不出来"。</p>
 *
 * <p><b>只在客户端主线程上节流</b>：EMI 自己在重载线程 / 搜索线程上也会调 {@code update()}
 * （日志实测：`[Thread-82] 记录输入 ''`、`[GoldenTweaks-EMI-Search] 记录输入 ''`）。
 * 如果连那些调用也拦，就会形成"我提交搜索 → 搜索内部又调 update() → 我再当成新输入攒起来 → 再提交"
 * 的自激循环：每秒重启一次搜索，worker 永远被下一次打断、{@code apply()} 永远不执行 ⇒
 * 结果出不来（表现为"搜索不触发/没反应"）+ CPU 被空转打满。所以非主线程一律放行。</p>
 *
 * <p>另外 {@link EmiSearchDebounce} 里还有一道保险：文本与上次已提交的相同时静默吞掉且不重新计时，
 * 双保险确保不会自我循环。</p>
 */
@Mixin(value = EmiSearch.class, remap = false)
public abstract class EmiSearchMixin {

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private static void gt$delaySearch(CallbackInfo ci) {

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft == null || !minecraft.isSameThread()) {
            return;
        }

        EmiSearchDebounce.tick();

        EmiSearchWidget widget = EmiScreenManager.search;

        if (widget != null && EmiSearchDebounce.shouldDelay(widget.getValue())) {
            ci.cancel();
        }
    }
}
