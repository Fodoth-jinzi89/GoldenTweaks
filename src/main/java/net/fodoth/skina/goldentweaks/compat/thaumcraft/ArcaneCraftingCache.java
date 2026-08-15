package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.research.TCResearchManager;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * 奥术合成缓存（移植自 TC4Tweaks 的 ArcaneCraftingHistory）。
 *
 * <p>以"最近命中的配方"为粒度的 LRU 缓存：查找时先按使用时间从新到旧用与原版一致的
 * 检查（研究 + matches）验证缓存项，命中即返回，避免在配方数量庞大的整合包中每次
 * 合成都全量遍历奥术配方列表。shift 批量合成同一配方时收益最大。</p>
 *
 * <p>用 ThreadLocal 保证不同线程（服务端/客户端）互不干扰；每次命中/存入都会重新校验
 * matches，因此语义与原版完全一致，只是改变了搜索顺序，是纯优化。</p>
 */
public final class ArcaneCraftingCache {

    private static final ThreadLocal<LinkedList<IArcaneRecipe>> RECENT_RECIPES = ThreadLocal.withInitial(LinkedList::new);

    /**
     * 在缓存中查找与当前合成网格匹配的奥术配方。
     *
     * @return 命中的配方；未命中或缓存被禁用时返回 {@code null}
     */
    public static IArcaneRecipe find(Container container, Player player) {
        if (GoldenTweaksCommonConfig.getArcaneCraftingCacheSize() <= 0) {
            return null;
        }

        LinkedList<IArcaneRecipe> recent = RECENT_RECIPES.get();
        Iterator<IArcaneRecipe> iterator = recent.iterator();
        while (iterator.hasNext()) {
            IArcaneRecipe recipe = iterator.next();
            if (matches(recipe, container, player)) {
                iterator.remove();
                recent.addFirst(recipe);
                return recipe;
            }
        }
        return null;
    }

    /**
     * 将命中的配方存入缓存（按配置上限裁剪）。
     */
    public static void add(IArcaneRecipe recipe) {
        int maxSize = GoldenTweaksCommonConfig.getArcaneCraftingCacheSize();
        if (maxSize <= 0) {
            return;
        }

        LinkedList<IArcaneRecipe> recent = RECENT_RECIPES.get();
        recent.addFirst(recipe);
        while (recent.size() > maxSize) {
            recent.removeLast();
        }
    }

    private static boolean matches(IArcaneRecipe recipe, Container container, Player player) {
        return TCResearchManager.has(player, recipe.getResearch())
                && recipe.matches(container, player.level(), player);
    }

    private ArcaneCraftingCache() {
    }
}
