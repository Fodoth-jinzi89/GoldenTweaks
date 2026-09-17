package net.fodoth.skina.goldentweaks.compat.emi;

import dev.emi.emi.search.EmiSearch;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.Util;

/**
 * EMI 搜索防抖的共享状态（给 mixin 用，本身不是 mixin）。
 *
 * <p>要点：防抖必须打在<b>搜索函数</b>上（{@link EmiSearch#search(String)}），<b>不能</b>拦
 * {@code EditBox#setValue} —— 输入框自己的文本也是在 {@code setValue}/{@code insertText} 里更新的，
 * 拦掉它会让输入框要等阈值才显示新字符（打字像卡住；中文走 IME/IMBlocker 的输入路径绕开了那个 hook，
 * 所以只有英文明显卡）。这里只推迟"跑搜索"，输入、光标、候选全都能立刻更新。</p>
 */
public final class EmiSearchDebounce {

    private static String pending;

    private static long lastInputMillis;

    private static boolean applying;

    private EmiSearchDebounce() {
    }

    /**
     * @return true 表示这次搜索请求先攒起来、暂不执行
     */
    public static boolean shouldDelay(String query) {

        int threshold = GoldenTweaksClientConfig.SEARCH_TRIGGER_THRESHOLD.get();

        if (applying || threshold <= 0) {
            return false;
        }

        if (query.equals(pending)) {
            return true;
        }

        pending = query;
        lastInputMillis = Util.getMillis();
        return true;
    }

    /**
     * 由每一帧的渲染钩子调用：停手满 {@code SEARCH_TRIGGER_THRESHOLD} 个 tick（阈值 × 50ms）后，
     * 把攒下的最后一次查询真正交给 EMI。
     */
    public static void tick() {

        if (pending == null || applying) {
            return;
        }

        int threshold = GoldenTweaksClientConfig.SEARCH_TRIGGER_THRESHOLD.get();

        if (threshold <= 0) {
            pending = null;
            return;
        }

        if (Util.getMillis() - lastInputMillis < Math.max(1L, threshold) * 50L) {
            return;
        }

        String query = pending;
        pending = null;
        applying = true;
        try {
            EmiSearch.search(query);
        } finally {
            applying = false;
        }
    }
}
