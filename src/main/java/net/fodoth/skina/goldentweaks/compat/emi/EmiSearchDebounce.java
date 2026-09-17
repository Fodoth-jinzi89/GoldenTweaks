package net.fodoth.skina.goldentweaks.compat.emi;

import dev.emi.emi.search.EmiSearch;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EMI 搜索节流：<b>停止输入 N tick 才开始搜</b> + 提交之间至少隔 M tick，且整个搜索在<b>后台线程</b>跑。
 *
 * <p>只有 {@code EmiSearch#update()}（方法体就是 {@code search(EmiScreenManager.search.getValue())}）
 * 被拦；{@code EmiSearch#search(String)} 之类的其它入口<b>一律不动</b>，
 * 所以本类即使判断出错，最坏结果也只是"没节流"，绝不会出现"搜不出来"。</p>
 *
 * <ul>
 *   <li>{@code searchStartDelay}：停止输入后等多少 tick 才开始搜（默认 20）；</li>
 *   <li>{@code searchSpreadDuration}：两次提交之间至少隔多少 tick（默认 20）；</li>
 *   <li>两者都 0 ⇒ 完全不介入（原版行为）。</li>
 * </ul>
 *
 * <p>真正的搜索在单线程 daemon 执行器里调用 {@code EmiSearch.search(query)} —— 主线程只做毫秒比较，
 * 所以不会卡画面。异常吞成一条 WARN，不会变成崩溃。</p>
 */
public final class EmiSearchDebounce {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "GoldenTweaks-EMI-Search");
        thread.setDaemon(true);
        return thread;
    });

    private static String pending;

    private static String lastSubmitted;

    private static long lastInputMillis;

    private static long lastSubmitMillis;

    private EmiSearchDebounce() {
    }

    /**
     * 由 {@code EmiSearch#update()} 头部调用（主线程）。
     *
     * @return true = 这次 update 先拦下，稍后由 {@link #tick()} 在后台补跑
     */
    public static boolean shouldDelay(String query) {

        if (query == null) {
            return false;
        }

        int start = GoldenTweaksClientConfig.SEARCH_START_DELAY_TICKS.get();
        int spread = GoldenTweaksClientConfig.SEARCH_SPREAD_DURATION_TICKS.get();

        if (start <= 0 && spread <= 0) {
            return false;
        }

        if (query.equals(lastSubmitted)) {
            return true;   // 这个文本刚搜过：静默吞掉，也不重新计时（否则会变成每秒重搜一次）
        }

        if (!query.equals(pending)) {
            pending = query;
            lastInputMillis = Util.getMillis();
            GoldenTweaks.LOGGER.debug("[EMI 节流] 记录输入 '{}'（{} tick 后开始搜）", query, start);
        }
        return true;
    }

    /** 时钟（客户端 tick / EMI 更新 / 搜索框渲染都会叫）：到点把攒下的文本丢给后台。 */
    public static void tick() {

        if (pending == null) {
            return;
        }

        long now = Util.getMillis();

        if (now - lastInputMillis < Math.max(0L, GoldenTweaksClientConfig.SEARCH_START_DELAY_TICKS.get()) * 50L) {
            return;
        }

        if (now - lastSubmitMillis < Math.max(0L, GoldenTweaksClientConfig.SEARCH_SPREAD_DURATION_TICKS.get()) * 50L) {
            return;
        }

        String query = pending;
        pending = null;
        lastSubmitted = query;
        lastSubmitMillis = now;

        GoldenTweaks.LOGGER.debug("[EMI 节流] 后台提交搜索 '{}'", query);

        EXECUTOR.submit(() -> {
            try {
                EmiSearch.search(query);
            } catch (Throwable t) {
                GoldenTweaks.LOGGER.warn("[EMI 节流] 后台搜索 '{}' 失败", query, t);
            }
        });
    }
}
