package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 魔导手册里「超量内容自动轮播」的计时器。
 *
 * <p>按 key 记录起始时间与当前页，每隔 {@code intervalMs} 前移一页；内容不超过一页时返回 0。
 * 供要素来源浮窗（{@code ResearchPageRenderer#drawScannedAspectSources}）和要素耗费布局
 * （{@code RecipePageLayout#centeredAspectCost / crucibleAspectCost}）共用。</p>
 */
public final class AspectCarousel {

    private static final long DEFAULT_INTERVAL_MS = 2000L;
    private static final Map<String, long[]> STATES = new HashMap<>();

    private AspectCarousel() {
    }

    /** 当前页的起始下标。 */
    public static int start(String key, int total, int pageSize) {
        return start(key, total, pageSize, DEFAULT_INTERVAL_MS);
    }

    public static int start(String key, int total, int pageSize, long intervalMs) {
        if (pageSize <= 0 || total <= pageSize) {
            return 0;
        }
        long interval = Math.max(1L, intervalMs);
        int pages = (total + pageSize - 1) / pageSize;
        long now = Util.getMillis();
        long[] state = STATES.computeIfAbsent(key, ignored -> new long[]{now, 0L});
        long elapsed = now - state[0];
        if (elapsed >= interval) {
            state[0] = now;
            state[1] = (state[1] + Math.max(1L, elapsed / interval)) % pages;
        }
        return (int) (state[1] % pages) * pageSize;
    }

    public static <T> List<T> window(String key, List<T> all, int pageSize) {
        return window(key, all, pageSize, DEFAULT_INTERVAL_MS);
    }

    public static <T> List<T> window(String key, List<T> all, int pageSize, long intervalMs) {
        int start = start(key, all.size(), pageSize, intervalMs);
        return start == 0 ? all : all.subList(start, Math.min(start + pageSize, all.size()));
    }

    /** 用内容本身拼一个稳定 key（同一配方的要素集合每帧不变）。 */
    public static String keyOf(List<?> values) {
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            builder.append(value).append(';');
        }
        return builder.toString();
    }
}
