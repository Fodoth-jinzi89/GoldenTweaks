package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import thaumcraft.api.crafting.CrucibleRecipe;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 坩埚配方按 hash 查找的缓存（移植自 TC4Tweaks 的 FindCrucibleRecipe /
 * CrucibleRecipeByHash）。
 *
 * <p>{@code ThaumcraftApi.getCrucibleRecipeFromHash} 每次调用都全量遍历配方列表，
 * 坩埚下药/倾倒时会被频繁调用。这里按 {@link CrucibleRecipe#hash} 惰性缓存命中结果；
 * 配方本身是共享只读对象，直接缓存引用即可。</p>
 */
public final class CrucibleRecipeCache {

    private static final ConcurrentHashMap<Integer, CrucibleRecipe> CACHE = new ConcurrentHashMap<>();

    /**
     * @return 缓存命中的坩埚配方；未命中或缓存被禁用时返回 {@code null}
     */
    public static CrucibleRecipe get(int hash) {
        if (!GoldenTweaksCommonConfig.isTcCrucibleRecipeCache()) {
            return null;
        }

        return CACHE.get(hash);
    }

    /**
     * 将命中的坩埚配方按 hash 存入缓存。
     */
    public static void put(int hash, CrucibleRecipe recipe) {
        if (!GoldenTweaksCommonConfig.isTcCrucibleRecipeCache() || recipe == null) {
            return;
        }

        CACHE.putIfAbsent(hash, recipe);
    }

    private CrucibleRecipeCache() {
    }
}
