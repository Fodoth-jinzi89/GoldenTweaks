package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import thaumcraft.api.research.ResearchItem;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 研究条目查找缓存（移植自 TC4Tweaks 的 GetResearch / ResearchItemCache）。
 *
 * <p>{@code ResearchCategories.getResearch} 每次调用都要遍历所有研究分类查找，
 * 神秘学之书、配方检查等场景会频繁触发。这里按研究 key 缓存结果，包括未找到的
 * {@code null}（同样省去一次全量遍历）。研究注册/刷新时需调用 {@link #clear()}
 * （见 {@code ResearchCategoriesMixin}）。</p>
 */
public final class ResearchCache {

    private static final ConcurrentHashMap<String, Optional<ResearchItem>> CACHE = new ConcurrentHashMap<>();

    /**
     * @return 缓存命中的研究条目；未命中或缓存被禁用时返回 {@code null}（可能表示"未找到"）
     */
    public static ResearchItem get(String key) {
        if (!GoldenTweaksCommonConfig.isTcResearchCache()) {
            return null;
        }

        Optional<ResearchItem> cached = CACHE.get(key);
        return cached == null ? null : cached.orElse(null);
    }

    /**
     * 将查找结果（可为 {@code null}）存入缓存。
     */
    public static void put(String key, ResearchItem item) {
        if (!GoldenTweaksCommonConfig.isTcResearchCache()) {
            return;
        }

        CACHE.putIfAbsent(key, Optional.ofNullable(item));
    }

    /**
     * 研究条目注册/刷新时清空缓存，防止返回过期的研究数据。
     */
    public static void clear() {
        CACHE.clear();
    }

    private ResearchCache() {
    }
}
