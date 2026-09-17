package net.fodoth.skina.goldentweaks.compat.emi;

import dev.emi.emi.search.EmiSearch;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EMI 搜索的两个节流参数 + <b>后台执行</b>（给 mixin / 客户端 tick 用，本身不是 mixin）。
 *
 * <ol>
 *   <li><b>分担开始间隔</b>（{@code searchStartDelay}，默认 20 tick）：停止输入后等这么久才开始搜，
 *       期间只记住最新文本，不打扰 EMI；</li>
 *   <li><b>分担时长</b>（{@code searchSpreadDuration}，默认 20 tick）：两次真正提交搜索之间至少隔这么久，
 *       把连续输入的负担摊到这段时间上（而不是每敲一字重启一次、也不在停手时一次砸下来）。</li>
 * </ol>
 *
 * <p><b>后台执行</b>：EMI 的 {@code EmiSearch.search(String)} 虽然自己会起一个 daemon 线程去扫条目，
 * 但它被调用的那一刻仍要在调用线程上做 {@code EmiScreenManager.getSearchSource()} 等准备工作 ——
 * 由渲染线程调用时这些就落在主线程上，于是"搜索时整个客户端卡住"。这里把整个
 * {@code EmiSearch.search(query)} 丢进单线程后台执行器（daemon），主线程只做两次时间比较，
 * 所以搜索再慢也不卡画面（跟 EMI 自己的 reload/bake 一样都在后台）。</p>
 *
 * <p>两个参数都设 0 ⇒ 完全回到 EMI 原版行为（不做任何延迟，直接同步调用）。</p>
 */
public final class EmiSearchDebounce {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "GoldenTweaks-EMI-Search");
        thread.setDaemon(true);
        return thread;
    });

    private static String pending;

    private static long lastInputMillis;

    private static long lastSubmitMillis;

    private static int loggedDelayed;

    private static int loggedApplied;

    private EmiSearchDebounce() {
    }

    /**
     * 由 {@code EmiSearch#search(String)} 头部调用（主线程）。
     *
     * @return true = 这次先攒着，等间隔到了由 {@link #tick()} 交给后台
     */
    public static boolean shouldDelay(String query) {

        if (GoldenTweaksClientConfig.SEARCH_START_DELAY_TICKS.get() <= 0
                && GoldenTweaksClientConfig.SEARCH_SPREAD_DURATION_TICKS.get() <= 0) {
            return false;
        }

        if (!query.equals(pending)) {
            pending = query;
            lastInputMillis = Util.getMillis();

            if (loggedDelayed < 5) {
                loggedDelayed++;
                GoldenTweaks.LOGGER.info("[EMI 节流] 记录输入 '{}'（第 {} 次）", query, loggedDelayed);
            }
        }
        return true;
    }

    /** 由客户端 tick / EmiSearch#update / 搜索框 renderWidget 调用（主线程，幂等）。 */
    public static void tick() {

        if (pending == null) {
            return;
        }

        long now = Util.getMillis();
        long startDelay = Math.max(0L, GoldenTweaksClientConfig.SEARCH_START_DELAY_TICKS.get()) * 50L;

        if (now - lastInputMillis < startDelay) {
            return;
        }

        long spread = Math.max(0L, GoldenTweaksClientConfig.SEARCH_SPREAD_DURATION_TICKS.get()) * 50L;

        if (now - lastSubmitMillis < spread) {
            return;
        }

        String query = pending;
        pending = null;
        lastSubmitMillis = now;

        if (loggedApplied < 5) {
            loggedApplied++;
            GoldenTweaks.LOGGER.info("[EMI 节流] 后台提交搜索 '{}'（第 {} 次）", query, loggedApplied);
        }

        EXECUTOR.submit(() -> {
            try {
                EmiSearch.search(query);
            } catch (Throwable t) {
                GoldenTweaks.LOGGER.warn("[EMI 节流] 后台搜索 '{}' 失败", query, t);
            }
        });
    }
}
