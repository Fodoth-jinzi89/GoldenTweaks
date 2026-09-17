package net.fodoth.skina.goldentweaks.compat.emi;

import dev.emi.emi.search.EmiSearch;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EMI 搜索节流：<b>停止输入 N tick 才开始搜</b> + 两次提交至少隔 M tick，搜索本体在后台线程执行。
 *
 * <ul>
 *   <li>{@code searchStartDelay}（默认 20 tick）：停止输入后等这么久才开始搜；</li>
 *   <li>{@code searchSpreadDuration}（默认 20 tick）：两次提交之间的最小间隔；</li>
 *   <li>两者都 0 ⇒ 完全不介入（EMI 原版行为）。</li>
 * </ul>
 *
 * <p><b>只有客户端主线程会进来</b>（mixin 侧用 {@code Minecraft#isSameThread()} 挡掉 EMI 自己的
 * 重载线程/搜索线程 —— 它们内部也会调 {@code search}/{@code update}，不挡就会形成
 * "提交搜索 → 搜索内部又触发 → 再提交" 的自激循环，结果是每秒重搜一次、结果永远出不来）。
 * 另有一道保险：文本与上次已提交的相同 ⇒ 静默吞掉且不重新计时。</p>
 *
 * <p>搜索在单线程 daemon 执行器里调 {@code EmiSearch.search(query)}，主线程只做毫秒比较，
 * 所以不会卡画面；后台执行抛异常时记一条 WARN（否则会静默失败），正常路径不打任何日志。</p>
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
     * @return true = 这次调用先拦下，稍后由 {@link #tick()} 在后台补跑
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
            return true;   // 刚搜过这个文本：静默吞掉，也不重新计时
        }

        if (!query.equals(pending)) {
            pending = query;
            lastInputMillis = Util.getMillis();
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

        EXECUTOR.submit(() -> {
            try {
                EmiSearch.search(query);
            } catch (Throwable t) {
                GoldenTweaks.LOGGER.warn("[EMI 节流] 后台搜索 '{}' 失败", query, t);
            }
        });
    }
}
