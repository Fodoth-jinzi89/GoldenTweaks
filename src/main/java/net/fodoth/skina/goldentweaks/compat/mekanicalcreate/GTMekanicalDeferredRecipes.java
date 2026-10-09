package net.fodoth.skina.goldentweaks.compat.mekanicalcreate;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 把 mekanicalcreate 那两趟「模拟室配方目录」预计算（实测 11.1 秒）从重载关键路径上**提前**跑掉。
 *
 * <h2>为什么不能"算完再补进去"</h2>
 * <p>TMRV 的 {@code RecipeManager#addRecipes} 在装载结束后抛
 * {@code IllegalStateException("Tried to add recipes after registry is locked")} —— 它忠实复刻的是
 * EMI 自己的契约（配方索引重载期一次性烘焙）。也试过算完触发 {@code EmiReloadManager.reloadRecipes()}
 * 重烘焙，副作用大（搜索退化、法杖 vis 变 0）且配方仍未上屏。所以"事后补"这条路是死的。</p>
 *
 * <h2>现在的做法：提前预热 + 必须兑现</h2>
 * <ol>
 *   <li>一进入世界（EMI 重载刚起步、标签同步时）就在后台开始算 —— 这段时间工作量大（插件注册、
 *       物品索引、搜索索引……），这 11 秒基本被完全重叠掉；</li>
 *   <li>{@code registerRecipes} 调到 {@code getDisplayRecipes} 时若已算好就直接给出（秒过，配方正常显示）；</li>
 *   <li>万一还没算完，就<b>等</b>它（{@link #await}，有上限），超时则调用方自己算 ——
 *       <b>任何情况下首次进世界都不会出现"配方为空"</b>。</li>
 * </ol>
 */
public final class GTMekanicalDeferredRecipes {

    /** 正在后台预热；同一份 {@code RecipeManager} 只做一次。 */
    private static final AtomicBoolean RUNNING = new AtomicBoolean();

    /** 预热任务；完成时结果已进缓存。 */
    private static volatile CompletableFuture<Object[]> job;

    /** 结果所属的配方管理器与两趟结果（[0] = item，[1] = fluid）。 */
    private static volatile RecipeManager cachedManager;
    private static volatile Object[] cachedResults;

    private GTMekanicalDeferredRecipes() {
    }

    public static boolean enabled() {
        return GoldenTweaksClientConfig.MEKANICALCREATE_DEFER_RECIPES.get();
    }

    /** EMI 重载起步时调用：尽量早地把两趟计算丢到后台。 */
    public static void prewarm() {

        if (!enabled()) {
            return;
        }

        Level level = Minecraft.getInstance().level;
        if (level != null) {
            startIfNeeded(level);
        }
    }

    /**
     * 取结果：命中缓存直接返回；否则等后台预热（有上限）；再不行返回 {@code null} 由调用方自己算。
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> await(Level level, boolean allowFluidProcessing, long timeoutMs) {

        List<T> ready = cached(level, allowFluidProcessing);
        if (ready != null) {
            return ready;
        }

        CompletableFuture<Object[]> running = job;
        if (running != null) {
            try {
                running.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (Throwable ignored) {
                // 超时/失败都不影响：下面再查一次缓存，否则调用方自己算
            }
            ready = cached(level, allowFluidProcessing);
        }

        return ready;
    }

    /** 缓存命中检查（后台预热算完后，任何一次重载都能立刻命中）。 */
    @SuppressWarnings("unchecked")
    public static <T> List<T> cached(Level level, boolean allowFluidProcessing) {

        Object[] results = cachedResults;
        if (results == null || level == null || level.getRecipeManager() != cachedManager) {
            return null;
        }

        return (List<T>) results[allowFluidProcessing ? 1 : 0];
    }

    private static void startIfNeeded(Level level) {

        if (level == null || cached(level, false) != null || !RUNNING.compareAndSet(false, true)) {
            return;
        }

        CompletableFuture<Object[]> future = new CompletableFuture<>();
        job = future;

        Thread thread = new Thread(() -> {

            long started = System.currentTimeMillis();

            try {
                List<?> itemRecipes = compute(level, false);
                List<?> fluidRecipes = compute(level, true);

                Object[] results = new Object[]{itemRecipes, fluidRecipes};
                cachedResults = results;
                cachedManager = level.getRecipeManager();

                GoldenTweaks.LOGGER.info(
                        "GoldenTweaks: mekanicalcreate 配方已预热完成（{} + {} 条，{} ms）",
                        itemRecipes.size(), fluidRecipes.size(), System.currentTimeMillis() - started
                );

                future.complete(results);
            } catch (Throwable throwable) {
                GoldenTweaks.LOGGER.error("GoldenTweaks: 预热 mekanicalcreate 配方失败，将退回原路径现算", throwable);
                future.complete(null);
            } finally {
                RUNNING.set(false);
            }
        }, "goldentweaks-mekanical-recipes");

        thread.setDaemon(true);
        thread.start();
    }

    private static List<?> compute(Level level, boolean allowFluidProcessing) throws Exception {
        var method = Class.forName("io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver")
                .getMethod("getDisplayRecipes", Level.class, boolean.class);
        return (List<?>) method.invoke(null, level, allowFluidProcessing);
    }
}
