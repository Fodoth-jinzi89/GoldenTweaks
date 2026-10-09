package net.fodoth.skina.goldentweaks.compat.emi;

import dev.emi.emi.registry.EmiStackList;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 把 {@link EmiStackList#reload()} 里那一趟「给所有创造模式标签 buildContents」拆成每个标签一次，
 * 客户端线程一次只做一个标签，不再出现一次 2.65 秒的整段卡顿。
 *
 * <h2>为什么只能"拆帧"，不能"先跳过、之后再重排"</h2>
 * <p>实测（2026-10-06）：</p>
 * <ul>
 *   <li>那一趟恰恰是把 <b>模组注册的物品变体</b> 带进 EMI 索引的<b>唯一</b>来源 ——
 *       例如神秘时代那几根满 vis 的法杖。跳过它，索引里就只剩 {@code EmiStack.of(item)} 的
 *       默认实例（法杖变 0 vis，从 EMI 里拿出来的就是 0 vis）。</li>
 *   <li>进世界之后再改 {@code EmiStackList.stacks}，会与 display 侧（REMI 缓存的搜索源）
 *       持有的 EmiStack 实例脱钩，于是我们的 {@code byIdentity} 全部落空、每个物品都退回
 *       EMI 原生判定 —— 现象就是"搜索又变回特别慢"。</li>
 * </ul>
 * <p>所以这里保持 EMI 自己的顺序与内容（法杖正常、搜索不退化），只把那一趟的耗时摊到多帧。</p>
 */
public final class GTEmiCreativeRegroup {

    private GTEmiCreativeRegroup() {
    }

    public static boolean enabled() {
        return GoldenTweaksClientConfig.EMI_DEFER_CREATIVE_GROUPS.get();
    }

    /** 截获 {@code reload()} 里那次 {@code client.submit(Supplier)}，改成逐个标签提交。 */
    public static CompletableFuture<Map<CreativeModeTab, Collection<ItemStack>>> tabPass(
            Minecraft client, Supplier<?> original) {

        if (!enabled()) {
            return runOriginal(client, original);
        }

        CompletableFuture<Map<CreativeModeTab, Collection<ItemStack>>> future = new CompletableFuture<>();

        Thread thread = new Thread(() -> {

            Map<CreativeModeTab, Collection<ItemStack>> map = new ConcurrentHashMap<>();

            try {
                var context = new CreativeModeTab.ItemDisplayParameters(
                        client.player.connection.enabledFeatures(), false, client.level.registryAccess());

                for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
                    Collection<ItemStack> contents = buildOnClient(client, tab, context);
                    if (contents != null) {
                        map.put(tab, contents);
                    }
                }
            } catch (Throwable throwable) {
                GoldenTweaks.LOGGER.error("GoldenTweaks: 分帧构建创造模式标签内容失败", throwable);
            }

            future.complete(map);
        }, "goldentweaks-emi-tab-pass");

        thread.setDaemon(true);
        thread.start();

        return future;
    }

    /** 单个标签交回客户端线程执行；本方法在后台线程上等它做完。 */
    private static Collection<ItemStack> buildOnClient(Minecraft client, CreativeModeTab tab,
                                                       CreativeModeTab.ItemDisplayParameters context) {

        CompletableFuture<Collection<ItemStack>> future = new CompletableFuture<>();

        client.execute(() -> {
            try {
                tab.buildContents(context);
                future.complete(tab.getSearchTabDisplayItems());
            } catch (Throwable throwable) {
                GoldenTweaks.LOGGER.error("GoldenTweaks: 构建创造模式标签内容失败 " + tab, throwable);
                future.complete(null);
            }
        });

        return future.join();
    }

    /** 引擎关闭时照旧把整段原任务交给客户端线程跑。 */
    private static CompletableFuture<Map<CreativeModeTab, Collection<ItemStack>>> runOriginal(
            Minecraft client, Supplier<?> original) {

        CompletableFuture<Map<CreativeModeTab, Collection<ItemStack>>> future = new CompletableFuture<>();

        client.execute(() -> {
            try {
                @SuppressWarnings("unchecked")
                Map<CreativeModeTab, Collection<ItemStack>> map =
                        (Map<CreativeModeTab, Collection<ItemStack>>) original.get();
                future.complete(map);
            } catch (Throwable throwable) {
                GoldenTweaks.LOGGER.error("GoldenTweaks: 构建创造模式标签内容失败，退回 EMI 原路径", throwable);
                future.complete(Map.of());
            }
        });

        return future;
    }
}
