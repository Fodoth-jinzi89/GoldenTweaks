package net.fodoth.skina.goldentweaks.compat.emi;

import dev.emi.emi.search.EmiSearch;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.Util;

/**
 * EMI 搜索的<b>节流</b>（前沿 + 尾沿）+ 兜底时钟，给 mixin / 客户端 tick 用（本身不是 mixin）。
 *
 * <p>规则：两次真正执行搜索之间至少间隔 {@code SEARCH_TRIGGER_THRESHOLD} 个 tick（阈值 × 50ms）。</p>
 * <ul>
 *   <li><b>前沿</b>：距上次搜索已经隔够 ⇒ 立刻放行（第一下打字/停手后再打，跟版一样跟手）；</li>
 *   <li><b>尾沿</b>：间隔不够 ⇒ 只攒<b>最后一次</b>文本，由 {@link #tick()} 到点补跑，
 *       保证停手后一定能搜到最新文本。</li>
 * </ul>
 *
 * <p><b>{@link #tick()} 有三个独立来源</b>（同一份状态、幂等，谁在跑都行）：
 * 客户端 {@code ClientTickEvent.Post}（最可靠，不依赖任何 UI 路径）、EMI 搜索框的
 * {@code renderWidget}、以及 {@code EmiSearch.update()} 的头部。之前两次"延时后不搜索"都是
 * 因为只挂了一个可能不触发的时钟。</p>
 *
 * <p>前若干次节流/补跑会打 INFO 日志（带查询串），方便在客户端日志里确认这条链路是否真的在跑。</p>
 */
public final class EmiSearchDebounce {

    private static final int LOG_LIMIT = 5;

    private static String pending;

    private static long lastRunMillis;

    private static boolean applying;

    private static int delayedCount;

    private static int appliedCount;

    private EmiSearchDebounce() {
    }

    /**
     * @return true 表示这次请求先攒着（稍后由 {@link #tick()} 用最新文本补跑）
     */
    public static boolean shouldDelay(String query) {

        int threshold = GoldenTweaksClientConfig.SEARCH_TRIGGER_THRESHOLD.get();

        if (applying || threshold <= 0) {
            return false;
        }

        if (pending == null && Util.getMillis() - lastRunMillis >= intervalMillis(threshold)) {
            lastRunMillis = Util.getMillis();
            return false;
        }

        pending = query;

        if (delayedCount < LOG_LIMIT) {
            delayedCount++;
            GoldenTweaks.LOGGER.info("[EMI 节流] 攒下查询 '{}'（第 {} 次；阈值 {} tick）",
                    query, delayedCount, threshold);
        }
        return true;
    }

    /** 由客户端 tick / 搜索框渲染 / EmiSearch#update 调用：攒下的请求到点就补跑一次。 */
    public static void tick() {

        if (pending == null || applying) {
            return;
        }

        int threshold = GoldenTweaksClientConfig.SEARCH_TRIGGER_THRESHOLD.get();

        if (threshold > 0 && Util.getMillis() - lastRunMillis < intervalMillis(threshold)) {
            return;
        }

        String query = pending;
        pending = null;

        if (appliedCount < LOG_LIMIT) {
            appliedCount++;
            GoldenTweaks.LOGGER.info("[EMI 节流] 补跑查询 '{}'（第 {} 次）", query, appliedCount);
        }
        run(query);
    }

    private static long intervalMillis(int threshold) {
        return Math.max(1L, threshold) * 50L;
    }

    private static void run(String query) {
        lastRunMillis = Util.getMillis();
        applying = true;
        try {
            EmiSearch.search(query);
        } finally {
            applying = false;
        }
    }
}
