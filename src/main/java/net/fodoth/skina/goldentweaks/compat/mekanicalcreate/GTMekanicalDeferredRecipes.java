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
 * 把 mekanicalcreate「模拟室配方目录」的预计算从重载关键路径上**提前**跑掉。
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
 *       物品索引、搜索索引……），这几十秒基本被完全重叠掉；</li>
 *   <li>只完整枚举流体目录（{@code allowFluidProcessing=true}）；物品目录是它的子集，
 *       由插件侧按进程类型派生，省掉第二趟全量枚举；</li>
 *   <li>{@code registerRecipes} 调到 {@code getDisplayRecipes} 时若已算好就直接给出（秒过）；</li>
 *   <li>万一还没算完，就<b>等</b>它（{@link #await}，上限 5 分钟），超时则调用方自己算 ——
 *       等待期间不会重复做同一趟计算。</li>
 * </ol>
 */
public final class GTMekanicalDeferredRecipes {

    /** 正在后台预热；同一份 {@code RecipeManager} 只做一次。 */
    private static final AtomicBoolean RUNNING = new AtomicBoolean();

    /** 预热任务（只算流体目录）；完成时结果已进缓存。 */
    private static volatile CompletableFuture<List<?>> job;

    /** 结果所属的配方管理器与两趟结果。 */
    private static volatile RecipeManager cachedManager;
    private static volatile List<?> cachedFluidRecipes;
    private static volatile List<?> cachedItemRecipes;

    private GTMekanicalDeferredRecipes() {
    }

    public static boolean enabled() {
        return GoldenTweaksClientConfig.MEKANICALCREATE_DEFER_RECIPES.get();
    }

    /** EMI 重载起步时调用：尽量早地把计算丢到后台。 */
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

        CompletableFuture<List<?>> running = job;
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

        if (level == null || level.getRecipeManager() != cachedManager) {
            return null;
        }

        List<?> results = allowFluidProcessing ? cachedFluidRecipes : cachedItemRecipes;
        return (List<T>) results;
    }

    /** 由插件侧从流体目录派生物品目录后回填，避免再跑一趟 {@code getDisplayRecipes(false)}。 */
    public static void storeItemResults(Level level, List<?> items) {

        if (level == null || level.getRecipeManager() != cachedManager) {
            return;
        }

        cachedItemRecipes = items;

        GoldenTweaks.LOGGER.info(
                "GoldenTweaks: mekanicalcreate 物品配方已从流体目录派生（{} 条）", items.size()
        );
    }

    private static void startIfNeeded(Level level) {

        if (level == null || cached(level, true) != null || !RUNNING.compareAndSet(false, true)) {
            return;
        }

        CompletableFuture<List<?>> future = new CompletableFuture<>();
        job = future;

        Thread thread = new Thread(() -> {

            long started = System.currentTimeMillis();

            try {
                List<?> fluidRecipes = compute(level, true);

                cachedFluidRecipes = fluidRecipes;
                cachedItemRecipes = null;
                cachedManager = level.getRecipeManager();

                GoldenTweaks.LOGGER.info(
                        "GoldenTweaks: mekanicalcreate 流体配方已预热完成（{} 条，{} ms），物品目录按需派生",
                        fluidRecipes.size(), System.currentTimeMillis() - started
                );

                future.complete(fluidRecipes);
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
