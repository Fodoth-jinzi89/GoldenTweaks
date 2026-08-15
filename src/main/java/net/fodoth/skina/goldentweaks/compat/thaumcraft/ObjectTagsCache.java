package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.AspectList;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 物品要素标签缓存（移植自 TC4Tweaks 的 ObjectTagsLagFix / GetObjectTags）。
 *
 * <p>{@code ItemAspectRegistry.getObjectTags} 会递归计算物品的基础要素与配方派生要素，
 * 开销较大。结果只与 {@link Item} 及物品是否携带自定义 components 有关，因此按键
 * {@code (Item, 是否有自定义 components)} 缓存——为简单起见只缓存无自定义 components
 * 的常见情形，带自定义组件的物品直接走原逻辑，保证结果 100% 一致。</p>
 *
 * <p>缓存存内部副本、返回副本，防止调用方修改污染缓存。配方管理器重载时需调用
 * {@link #clear()}（见 {@code ItemAspectRegistryMixin}）。</p>
 */
public final class ObjectTagsCache {

    private static final ConcurrentHashMap<Item, AspectList> CACHE = new ConcurrentHashMap<>();

    /**
     * @return 缓存命中的要素列表副本；未命中、禁用或物品带自定义 components 时返回 {@code null}
     */
    public static AspectList get(ItemStack stack) {
        if (!GoldenTweaksCommonConfig.isTcObjectTagsCache() || !stack.getComponentsPatch().isEmpty()) {
            return null;
        }

        AspectList cached = CACHE.get(stack.getItem());
        return cached == null ? null : cached.copy();
    }

    /**
     * 将计算结果存入缓存。
     */
    public static void put(ItemStack stack, AspectList aspects) {
        if (!GoldenTweaksCommonConfig.isTcObjectTagsCache() || aspects == null || !stack.getComponentsPatch().isEmpty()) {
            return;
        }

        CACHE.putIfAbsent(stack.getItem(), aspects.copy());
    }

    /**
     * 配方管理器重载（datapack / 服务器切换）时清空缓存，防止配方派生要素过期。
     */
    public static void clear() {
        CACHE.clear();
    }

    private ObjectTagsCache() {
    }
}
