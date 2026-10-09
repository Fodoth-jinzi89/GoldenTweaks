package net.fodoth.skina.goldentweaks.compat.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import dev.emi.emi.search.Query;
import dev.emi.emi.search.QueryType;
import dev.emi.emi.search.RegexModQuery;
import dev.emi.emi.search.RegexNameQuery;
import dev.emi.emi.search.RegexTagQuery;
import dev.emi.emi.search.RegexTooltipQuery;
import dev.emi.emi.search.SearchStack;
import dev.emi.emi.search.TagQuery;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.util.GTState;
import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static net.fodoth.skina.goldentweaks.util.ModPresent.checkIfPresent;

/**
 * EMI 搜索内核：用「1~3 gram 倒排索引 + 位集集合代数」替换 EMI 原版的
 * 「多层集合物化 + 全量逐项扫描」。
 *
 * <p>EMI 1.1.x 的搜索路径有两处昂贵开销：</p>
 * <ol>
 *   <li>{@code EmiSearch#bake()} 用 vanilla {@link SuffixArray} 建 4 张索引，其 {@code generate()}
 *       是比较器驱动的倍增后缀排序，代价约 {@code O(N log N × log min(N,maxLen))}（N = 字符总数），
 *       在大型整合包（本包约 1.1 万个物品、tooltip 字符上百万）里是<b>秒级</b>开销；</li>
 *   <li>{@code EmiSearch.SearchWorker} 每次键入都要 <b>全量扫描</b>所有物品，并且每个 Query 会先把自己
 *       命中的条目整套物化成 identity 集合（{@code SuffixArray.search} 内部还是四层集合），
 *       逐项哈希查找 + 每个 stack 一次 {@code getEmiStacks()} 分配。</li>
 * </ol>
 *
 * <p>本内核的做法：</p>
 * <ul>
 *   <li><b>索引侧</b>：bake 期直接截获 EMI 喂给 {@link SuffixArray} 的字符串（不重算，避免二次生成 tooltip），
 *       建成 {@link GTEmiSubstringIndex}；vanilla 后缀数组的存储与排序可以整体跳过
 *       （见 {@code EmiSearchMixin}）。</li>
 *   <li><b>求值侧</b>：把查询编译成 {@code Or/And/Leaf} 表达式，每个 token 解析成
 *       <b>烘焙栈下标空间的位集</b>；And 就是位与、Or 就是位或、取反就是位非，全程 {@code long[]} 无哈希。
 *       正则项不是先算而是<b>最后在收窄后的候选集上跑</b>，避免对全表跑正则。</li>
 *   <li><b>线程侧</b>：一条常驻守护线程 + 代次号（generation）协作取消，替代「每次键入 new Thread」。</li>
 * </ul>
 *
 * <p><b>语义一致性</b>：查询语法（{@link EmiSearch#TOKENS} + {@link QueryType}）、默认展开
 * （tooltip / mod / tag 是否参与）、结果顺序（保持 source 顺序）、多元素 ingredient 的处理都与 EMI 一致；
 * 不在索引里的 stack 仍然走 EMI 自己的 {@code Query#matchesUnbaked} 回退——那些实现只依赖
 * {@code getName/getId/getTooltipText}，与索引无关，因此跳过 vanilla 索引不会改变它们的行为。</p>
 */
public final class GTEmiSearchEngine {

    private static final int KIND_NAME = 0;
    private static final int KIND_TOOLTIP = 1;
    private static final int KIND_MOD = 2;
    private static final int KIND_ALIAS = 3;
    private static final int KIND_TAG = 4;
    private static final int KIND_REGEX_NAME = 5;
    private static final int KIND_REGEX_TOOLTIP = 6;
    private static final int KIND_REGEX_MOD = 7;
    private static final int KIND_REGEX_TAG = 8;

    private static final Object LOCK = new Object();
    private static final int CANCEL_CHECK_MASK = 511;
    private static final int CACHE_LIMIT = 64;

    private static final Hash.Strategy<EmiStack> IDENTITY = new Hash.Strategy<>() {

        @Override
        public int hashCode(EmiStack stack) {
            return System.identityHashCode(stack);
        }

        @Override
        public boolean equals(EmiStack a, EmiStack b) {
            return a == b;
        }
    };

    private static final Map<SuffixArray<?>, List<String>> CAPTURED_KEYS = new IdentityHashMap<>();
    private static final Map<SuffixArray<?>, List<EmiStack>> CAPTURED_STACKS = new IdentityHashMap<>();

    private static volatile Index index;
    private static volatile boolean capturing;
    private static volatile boolean skipVanillaIndex;
    private static volatile boolean engineActive;

    /** 索引重建代次：求值途中若发生重建就放弃该次求值。 */
    private static volatile long generation;

    private static volatile String pendingQuery;
    private static volatile List<? extends EmiIngredient> pendingSource;
    private static volatile Thread worker;

    /** 去重后的结果日志目标：同一「查询 + 结果数」只打一行，便于确认搜索确实在跑又不刷屏。 */
    private static volatile String loggedResult;

    /** 本轮 bake 是否改用 EMI 自己的索引（JECharacters 会把那几张表换成拼音 Trie）。 */
    private static volatile boolean externalMode;

    private static volatile boolean loggedExternal;

    /** 本轮 bake 是否由本内核接管 tooltip。接管后 tooltip 只进我们自己的 gram 索引，不再投喂 JEChars/原版后缀数组。 */
    private static volatile boolean tooltipHook;

    /** 是否把 tooltip 索引推迟到阶段 2（后台）构建；推迟期间 `$` 类查询暂不命中。 */
    private static volatile boolean deferTooltips;

    private static volatile boolean warnedTooltipPending;

    /** 阶段 1 顺手记下的 tooltip 行（未推迟时使用）。 */
    private static final List<String> TOOLTIP_LINES = new ArrayList<>();
    private static final List<EmiStack> TOOLTIP_STACKS = new ArrayList<>();

    /** 推迟构建时只记 stack，阶段 2 再去生成 tooltip。 */
    private static final List<EmiStack> DEFERRED_TOOLTIP_STACKS = new ArrayList<>();

    private static volatile MethodHandle remiBuildStacks;
    private static volatile MethodHandle remiAppendGroups;
    private static volatile boolean remiBridgeReady;
    private static volatile boolean remiBridgeFailed;

    private GTEmiSearchEngine() {
    }

    // ------------------------------------------------------------------ 配置

    public static boolean fastSearch() {
        return GTState.isReady() && GoldenTweaksClientConfig.EMI_FAST_SEARCH.get();
    }

    /**
     * 装了 JustEnoughCharacters 时，{@code EmiSearch#bake} 里的 {@code new SuffixArray()} 会被它的
     * ASM 换成 {@code me.towdium.jecharacters.utils.FakeArray} —— 那是个 PinIn 拼音 Trie
     * （{@code add} 就是 {@code tree.put}，{@code generate} 是空实现，{@code search} 就是 {@code tree.search}）。
     *
     * <p>也就是说：<b>装了它之后 EMI 的 {@code names/tooltips/mods/aliases} 本身就是拼音索引。</b>
     * 所以这时不再自建倒排索引、也不跳过原版填充，而是直接把 EMI 那几张表当作索引来解析 token：
     * 拼音匹配交给 JECharacters，位集组合与结果物化仍归本内核，两边的长处都保住。</p>
     */
    private static boolean externalIndex() {
        return ModList.get().isLoaded("jecharacters");
    }

    public static boolean skipVanillaIndex() {
        return skipVanillaIndex;
    }

    // ------------------------------------------------------------ 索引构建

    /** 每次 {@code EmiSearch#bake()} 开始时调用，决定这一轮是捕获建索引还是保持 EMI 原样。 */
    public static void beginBake(boolean enabled) {

        externalMode = enabled && externalIndex();
        capturing = enabled && !externalMode;
        skipVanillaIndex = enabled && !externalMode
                && (!GTState.isReady() || GoldenTweaksClientConfig.EMI_SKIP_VANILLA_SEARCH_INDEX.get());

        tooltipHook = enabled;
        deferTooltips = enabled
                && (!GTState.isReady() || GoldenTweaksClientConfig.EMI_DEFER_SEARCH_INDEX.get());

        if (externalMode && !loggedExternal) {
            loggedExternal = true;
            GoldenTweaks.LOGGER.info(
                    "GoldenTweaks: 检测到 JustEnoughCharacters —— 改用它替 EMI 装上的拼音索引解析 token，拼音搜索保留"
            );
        }

        if (deferTooltips) {
            GoldenTweaks.LOGGER.info(
                    "GoldenTweaks: tooltip 索引推迟到重载之后的后台线程构建；完成前 `$` 类搜索暂不命中 tooltip"
            );
        }

        CAPTURED_KEYS.clear();
        CAPTURED_STACKS.clear();
        TOOLTIP_LINES.clear();
        TOOLTIP_STACKS.clear();
        DEFERRED_TOOLTIP_STACKS.clear();
    }

    /** 截获 EMI 原本要写进后缀数组的 (条目, 字符串)；必要时再原样写回，保证关闭本内核时行为不变。 */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void capture(SuffixArray<?> array, Object value, String contents) {

        if (!capturing) {
            return;
        }

        EmiStack stack = stackOf(value);
        if (stack != null) {
            CAPTURED_KEYS.computeIfAbsent(array, key -> new ArrayList<>()).add(contents);
            CAPTURED_STACKS.computeIfAbsent(array, key -> new ArrayList<>()).add(stack);
        }
    }

    private static EmiStack stackOf(Object value) {

        if (value instanceof SearchStack searchStack) {
            return searchStack.stack;
        }
        if (value instanceof EmiStack stack) {
            return stack;
        }
        return null;
    }

    /**
     * 接管 {@code EmiSearch#bake()} 里的 {@code stack.getTooltipText()}。
     *
     * <p>返回空表是<b>有意</b>的：EMI 那一段是
     * {@code for (int i = 1; i < tooltip.size(); i++) tooltips.add(...)}；
     * 返回空表 ⇒ tooltip 既不进原版后缀数组、也不进 JECharacters 的 PinIn trie
     * （后者是那 19.5 s 的主要来源之一）。tooltip 改由我们自己的 gram 索引承接，
     * 见 {@link #buildTooltipIndex}。</p>
     *
     * <p>推迟模式下连 tooltip 都不生成，只记下 stack，交给阶段 2 在后台生成。</p>
     */
    public static List<Component> captureTooltip(EmiStack stack) {

        if (!tooltipHook) {
            return stack.getTooltipText();
        }

        if (deferTooltips) {
            DEFERRED_TOOLTIP_STACKS.add(stack);
            return List.of();
        }

        try {
            List<Component> lines = stack.getTooltipText();
            for (int i = 1; i < lines.size(); i++) {
                Component line = lines.get(i);
                if (line != null) {
                    TOOLTIP_LINES.add(line.getString().toLowerCase());
                    TOOLTIP_STACKS.add(stack);
                }
            }
        } catch (Throwable throwable) {
            GoldenTweaks.LOGGER.error("GoldenTweaks: 生成 tooltip 失败 " + stack, throwable);
        }
        return List.of();
    }

    /**
     * 接管 {@code bake()} 里那次 {@code getItemStack()}。它只用来判断附魔书，
     * 却会给每个 stack 造一个 {@code ItemStack}（27.9k 次）。非附魔书直接返回 {@code EMPTY}
     * —— {@code EMPTY.getItem()} 是 AIR ≠ ENCHANTED_BOOK，语义不变。
     */
    public static ItemStack itemStackForBookCheck(EmiStack stack) {
        return stack.getKey() == Items.ENCHANTED_BOOK ? stack.getItemStack() : ItemStack.EMPTY;
    }

    public static void finishBake(boolean enabled) {

        capturing = false;

        try {
            if (!enabled) {
                index = null;
                engineActive = false;
                return;
            }

            Index built = build(externalMode, deferTooltips);
            index = built;
            engineActive = true;
            invalidate();

            String tooltipSummary = built.tooltips == null ? "推迟到阶段 2" : built.tooltips.keyCount() + " 项";

            if (built.external) {
                GoldenTweaks.LOGGER.info("GoldenTweaks: EMI 搜索内核已接管（token 解析走外部拼音索引），{} 个 stack，tooltip {}",
                        built.size, tooltipSummary);
            } else {
                GoldenTweaks.LOGGER.info(
                        "GoldenTweaks: EMI 搜索索引已重建 —— {} 个 stack；名称 {} 项 / tooltip {} / mod {} 项 / 别名 {} 项",
                        built.size, built.names.keyCount(), tooltipSummary,
                        built.mods.keyCount(), built.aliases.keyCount()
                );
            }

            if (built.tooltips == null) {
                startDeferredTooltipBake(built);
            }
        } catch (Throwable throwable) {
            index = null;
            engineActive = false;
            GoldenTweaks.LOGGER.error(
                    "GoldenTweaks: 构建 EMI 搜索索引失败，本轮搜索不可用；"
                            + "请在配置里把「跳过原版 EMI 搜索索引」关掉后重载资源包以回到 EMI 原生搜索",
                    throwable
            );
        } finally {
            CAPTURED_KEYS.clear();
            CAPTURED_STACKS.clear();
        }
    }

    private static Index build(boolean external, boolean deferTooltips) {

        EmiStack[] stacks = EmiStackList.stacks.toArray(new EmiStack[0]);

        Object2IntMap<EmiStack> byIdentity = new Object2IntOpenCustomHashMap<>(IDENTITY);
        byIdentity.defaultReturnValue(-1);
        Object2IntMap<EmiStack> byValue = new Object2IntOpenHashMap<>();
        byValue.defaultReturnValue(-1);

        for (int i = 0; i < stacks.length; i++) {
            byIdentity.put(stacks[i], i);
            if (!byValue.containsKey(stacks[i])) {
                byValue.put(stacks[i], i);
            }
        }

        GTEmiSubstringIndex tooltips = deferTooltips ? null : buildTooltipIndex(byIdentity);

        if (external) {
            // 名称 / id / mod / 别名 直接查 EMI 那几张表（JECharacters 已把它们换成拼音 Trie）
            return new Index(stacks, byIdentity, byValue, true, null, tooltips, null, null);
        }

        // 别名索引里存的是 stack.copy()，与原对象只有值相等关系，必须按值回落到下标
        return new Index(stacks, byIdentity, null, false,
                buildIndex(EmiSearch.names, byIdentity, null),
                tooltips,
                buildIndex(EmiSearch.mods, byIdentity, null),
                buildIndex(EmiSearch.aliases, byIdentity, byValue));
    }

    /** tooltip 索引统一由我们自己建（不再投喂 JEChars），数据来自 {@link #captureTooltip} 记下的行。 */
    private static GTEmiSubstringIndex buildTooltipIndex(Object2IntMap<EmiStack> byIdentity) {

        GTEmiSubstringIndex.Builder builder = GTEmiSubstringIndex.builder();

        for (int i = 0; i < TOOLTIP_LINES.size(); i++) {
            String line = TOOLTIP_LINES.get(i);
            int stackIndex = byIdentity.getInt(TOOLTIP_STACKS.get(i));
            if (stackIndex >= 0 && !line.isEmpty()) {
                builder.put(line, stackIndex);
            }
        }

        return builder.build();
    }

    /**
     * 阶段 2：在后台线程生成 tooltip 并建好 tooltip 索引，不阻塞 EMI 的重载。
     *
     * <p>只写我们自己的结构，<b>绝不触碰</b> JECharacters 的 {@code FakeArray}（线程安全）。
     * 完成后 swap 进 {@link Index#tooltips} 并 bump 代次，之后 `$` 类搜索自动变完整。</p>
     */
    private static void startDeferredTooltipBake(Index target) {

        List<EmiStack> stacks = List.copyOf(DEFERRED_TOOLTIP_STACKS);

        Thread thread = new Thread(() -> {

            long started = System.currentTimeMillis();

            try {
                GTEmiSubstringIndex.Builder builder = GTEmiSubstringIndex.builder();

                for (EmiStack stack : stacks) {
                    int stackIndex = target.byIdentity.getInt(stack);
                    if (stackIndex < 0) {
                        continue;
                    }
                    List<Component> lines = stack.getTooltipText();
                    if (lines == null) {
                        continue;
                    }
                    for (int i = 1; i < lines.size(); i++) {
                        Component line = lines.get(i);
                        if (line != null) {
                            String text = line.getString().toLowerCase();
                            if (!text.isEmpty()) {
                                builder.put(text, stackIndex);
                            }
                        }
                    }
                }

                GTEmiSubstringIndex built = builder.build();
                target.tooltips = built;
                invalidate();

                GoldenTweaks.LOGGER.info("GoldenTweaks: tooltip 索引已在后台建好（{} 项，{} ms），搜索已完整",
                        built.keyCount(), System.currentTimeMillis() - started);
            } catch (Throwable throwable) {
                GoldenTweaks.LOGGER.error("GoldenTweaks: 后台构建 tooltip 索引失败，`$` 类搜索将不可用", throwable);
            }
        }, "goldentweaks-emi-tooltips");

        thread.setDaemon(true);
        thread.start();
    }

    private static GTEmiSubstringIndex buildIndex(SuffixArray<?> array, Object2IntMap<EmiStack> byIdentity,
                                                  Object2IntMap<EmiStack> byValue) {

        GTEmiSubstringIndex.Builder builder = GTEmiSubstringIndex.builder();

        List<String> keys = CAPTURED_KEYS.remove(array);
        List<EmiStack> stacks = CAPTURED_STACKS.remove(array);

        if (keys == null || stacks == null) {
            return builder.build();
        }

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (key.isEmpty()) {
                continue;
            }

            int stackIndex = byIdentity.getInt(stacks.get(i));
            if (stackIndex < 0 && byValue != null) {
                stackIndex = byValue.getInt(stacks.get(i));
            }
            if (stackIndex >= 0) {
                builder.put(key, stackIndex);
            }
        }

        return builder.build();
    }

    // ------------------------------------------------------------ 搜索调度

    /** @return true 表示已接管本次搜索（调用方应取消 EMI 的原方法）。 */
    public static boolean submit(String query) {

        if (query == null || !engineActive || index == null) {
            return false;
        }

        // 先确认我们真的能为这个查询算出位集。算不出（标签类、取反的正则等走不了位集的）就
        // <b>不</b>取消 EMI 的搜索，让它按原样跑 —— 宁可慢，也绝不能出现「输入什么都没反应」。
        try {
            if (bitsFor(query) == null) {
                return false;
            }
        } catch (Throwable throwable) {
            GoldenTweaks.LOGGER.error("GoldenTweaks: EMI 查询 \"{}\" 预计算失败，交回 EMI 原生搜索", query, throwable);
            return false;
        }

        List<? extends EmiIngredient> source;
        try {
            // REMI 会把 getSearchSource 重定向到它自己的 sourceStacks，这里取到的就是要过滤的那份
            source = EmiScreenManager.getSearchSource();
        } catch (Throwable throwable) {
            GoldenTweaks.LOGGER.error("GoldenTweaks: 取 EMI 搜索源失败，本次交回 EMI 原生搜索", throwable);
            return false;
        }

        synchronized (LOCK) {
            pendingQuery = query;
            pendingSource = source;
            generation++;

            if (worker == null) {
                Thread thread = new Thread(GTEmiSearchEngine::loop, "goldentweaks-emi-search");
                thread.setDaemon(true);
                worker = thread;
                thread.start();
            }

            LOCK.notifyAll();
        }

        return true;
    }

    private static void loop() {

        while (true) {

            String query;
            List<? extends EmiIngredient> source;
            long gen;

            synchronized (LOCK) {
                while (pendingQuery == null) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                query = pendingQuery;
                source = pendingSource;
                gen = generation;
                pendingQuery = null;
                pendingSource = null;
            }

            try {
                runSearch(gen, query, source);
            } catch (Throwable throwable) {
                GoldenTweaks.LOGGER.error("GoldenTweaks: EMI 搜索 \"{}\" 失败", query, throwable);
            }
        }
    }

    private static void runSearch(long gen, String query, List<? extends EmiIngredient> source) {

        Index current = index;
        if (current == null || cancelled(gen)) {
            return;
        }

        // 构造过程里就会算好位集（见 GTBitSetHolder / EmiSearchCompiledQueryMixin），
        // 这样位集只算一次；同时这个实例也是要发布出去的 EmiSearch#compiledQuery。
        EmiSearch.CompiledQuery compiled = new EmiSearch.CompiledQuery(query);
        if (cancelled(gen)) {
            return;
        }

        long[] bits = compiled instanceof GTBitSetHolder holder ? holder.gt$bits() : null;
        if (bits == null) {
            GoldenTweaks.LOGGER.warn("GoldenTweaks: 查询 \"{}\" 位集缺失，本次不发布结果", query);
            return;
        }

        // 空查询：EMI 也是原样返回 source，不过滤、不复制
        if (compiled.fullQuery == null) {
            publish(gen, query, compiled, source);
            return;
        }

        Query fallback = compiled.fullQuery;
        List<EmiIngredient> filtered = new ArrayList<>();

        for (int i = 0; i < source.size(); i++) {

            if ((i & CANCEL_CHECK_MASK) == 0 && cancelled(gen)) {
                return;
            }

            EmiIngredient ingredient = source.get(i);
            EmiStack stack = singleStack(ingredient);
            if (stack == null) {
                continue;
            }

            int stackIndex = current.byIdentity.getInt(stack);
            boolean matched;

            if (stackIndex >= 0 && (stackIndex >>> 6) < bits.length) {
                matched = (bits[stackIndex >>> 6] & (1L << (stackIndex & 63))) != 0;
            } else {
                // 不在索引里的 stack 仍走 EMI 的 matchesUnbaked（与索引无关，行为不变）
                matched = fallback == null || fallback.matchesUnbaked(stack);
            }

            if (matched) {
                filtered.add(ingredient);
            }
        }

        publish(gen, query, compiled, filtered);
    }

    private static void publish(long gen, String query, EmiSearch.CompiledQuery compiled,
                                List<? extends EmiIngredient> filtered) {

        if (cancelled(gen)) {
            return;
        }

        EmiSearch.compiledQuery = compiled;
        EmiSearch.stacks = filtered;

        String key = query + '\u0000' + filtered.size();
        if (!key.equals(loggedResult)) {
            loggedResult = key;
            GoldenTweaks.LOGGER.info("GoldenTweaks: EMI 搜索结果 \"{}\" -> {} 项", query, filtered.size());
        }

        feedRemi(query, filtered);
    }

    private static EmiStack singleStack(EmiIngredient ingredient) {

        if (ingredient instanceof EmiStack stack) {
            return stack;
        }

        List<EmiStack> stacks = ingredient.getEmiStacks();
        return stacks.size() == 1 ? stacks.get(0) : null;
    }

    // ------------------------------------------------------- REMI 显示管线桥

    /**
     * 把结果额外喂给 REMI 的显示管线。
     *
     * <p>REMI 不直接渲染 EMI 的 {@code searchedStacks}：它把该字段重定向成自己的
     * {@code StackManager.displayedStacks}，而那个列表只有在 {@code SearchWorker#run} 里对
     * {@code EmiSearch#apply} 的那次调用被它的 {@code @WrapOperation} 包住时才会被 {@code buildStacks}
     * 更新。本内核取消了那次搜索，所以这里按 REMI 自己那几行的做法原样补上（含 {@code %} 分组查询）。</p>
     *
     * <p>用反射是为了不为一个第三方模组加编译依赖；没装 REMI 时静默跳过。
     * 若 REMI 改了内部方法名，这里会打一条 WARN 并停止尝试——搜索本身不受影响，只是右侧列表不再跟着刷新。</p>
     */
    private static void feedRemi(String query, List<? extends EmiIngredient> filtered) {

        if (!resolveRemiBridge()) {
            return;
        }

        try {
            synchronized (EmiSearch.class) {

                // 已经被更新的一次搜索顶掉，就别再喂旧结果
                if (EmiSearch.stacks != filtered) {
                    return;
                }

                EmiScreenManager.SidebarPanel panel = EmiScreenManager.getSearchPanel();
                if (panel == null || panel.getType() != SidebarType.INDEX) {
                    return;
                }

                List<EmiStack> combined = new ArrayList<>(filtered.size());
                for (EmiIngredient ingredient : filtered) {
                    if (ingredient instanceof EmiStack stack) {
                        combined.add(stack);
                    }
                }

                if (query.startsWith("%") && query.length() > 1) {
                    remiAppendGroups.invoke(query.substring(1), combined);
                }

                remiBuildStacks.invoke(combined);
            }
        } catch (Throwable throwable) {
            if (!remiBridgeFailed) {
                remiBridgeFailed = true;
                GoldenTweaks.LOGGER.warn("GoldenTweaks: 调用 REMI 列表接口失败，右侧列表可能不随搜索刷新", throwable);
            }
        }
    }

    private static boolean resolveRemiBridge() {

        if (remiBridgeReady) {
            return true;
        }
        if (remiBridgeFailed) {
            return false;
        }
        if (!checkIfPresent("com.evandev.remi.integration.emi.StackManager")) {
            remiBridgeFailed = true;   // 没装 REMI：静默关闭
            return false;
        }

        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            Class<?> manager = Class.forName("com.evandev.remi.integration.emi.StackManager");
            Class<?> groups = Class.forName("com.evandev.remi.feature.stackgroup.StackGroupManager");

            remiBuildStacks = lookup.findStatic(manager, "buildStacks",
                    MethodType.methodType(void.class, List.class));
            remiAppendGroups = lookup.findStatic(groups, "appendStacksForMatchingGroups",
                    MethodType.methodType(void.class, String.class, List.class));

            remiBridgeReady = true;
            return true;
        } catch (Throwable throwable) {
            remiBridgeFailed = true;
            GoldenTweaks.LOGGER.warn("GoldenTweaks: 未找到 REMI 的列表接口，右侧列表可能不随搜索刷新", throwable);
            return false;
        }
    }

    /**
     * 为一次查询预计算「命中位集」。
     *
     * <p>由 {@code EmiSearch$CompiledQuery} 构造时调用（见 {@code EmiSearchCompiledQueryMixin}），
     * 内核再用同一个实例的位集去物化结果——位集只算一次。
     * 位集挂在 {@code CompiledQuery} 实例上而不是全局静态，所以并发发起的两次搜索不会互相串台。</p>
     *
     * @return 命中位集；引擎未启用或求值失败时返回 null（调用方交回 EMI 的原实现）
     */
    public static long[] bitsFor(String query) {

        Index current = index;
        if (!engineActive || current == null || query == null) {
            return null;
        }

        long gen = generation;

        try {
            Expr expr = compile(query);

            if (expr == null) {
                Bits everything = new Bits(current.size);
                everything.setAll();
                return everything.words;
            }

            Bits result = eval(expr, current, null, gen);
            return result == null ? null : result.words;
        } catch (Throwable throwable) {
            GoldenTweaks.LOGGER.error("GoldenTweaks: EMI 查询 \"{}\" 求值失败，本次交回 EMI 原生搜索", query, throwable);
            return null;
        }
    }

    /**
     * {@code CompiledQuery#test} 的快速通道。
     *
     * @return 0 = 交回 EMI 处理（未启用 / 该 stack 不在索引里 / 位集缺失），1 = 命中，2 = 不命中
     */
    public static int test(long[] bits, EmiStack stack) {

        if (bits == null) {
            return 0;
        }

        Index current = index;
        if (current == null) {
            return 0;
        }

        int stackIndex = current.byIdentity.getInt(stack);
        // 索引可能在这期间被重建，位集长度对不上就交回 EMI，不要越界
        if (stackIndex < 0 || (stackIndex >>> 6) >= bits.length) {
            return 0;
        }

        return (bits[stackIndex >>> 6] & (1L << (stackIndex & 63))) != 0 ? 1 : 2;
    }

    private static boolean cancelled(long gen) {
        return gen != generation;
    }

    private static void invalidate() {
        generation++;
    }

    // ---------------------------------------------------------- 查询编译

    /** 与 {@code EmiSearch.CompiledQuery} 同构：{@code Or[ And[ ... ] ... ]}，取反挂在节点上。 */
    private static Expr compile(String query) {

        List<Expr> groups = new ArrayList<>();
        List<Expr> current = new ArrayList<>();
        Matcher matcher = EmiSearch.TOKENS.matcher(query);

        while (matcher.find()) {

            String token = matcher.group();
            boolean negated = token.startsWith("-");
            if (negated) {
                token = token.substring(1);
            }

            if (token.isEmpty() || token.equals("&")) {
                continue;
            }

            if (token.equals("|")) {
                if (!current.isEmpty()) {
                    groups.add(group(current, true, false));
                    current = new ArrayList<>();
                }
                continue;
            }

            QueryType type = QueryType.fromString(token);
            current.add(term(type, token.substring(type.prefix.length()), negated));
        }

        if (!current.isEmpty()) {
            groups.add(group(current, true, false));
        }

        if (groups.isEmpty()) {
            return null;
        }
        return groups.size() == 1 ? groups.get(0) : new Group(groups, false, false);
    }

    /** 廉价项（索引直查）排前面，正则才有机会只在收窄后的候选集上跑。 */
    private static Group group(List<Expr> children, boolean all, boolean negated) {
        children.sort(Comparator.comparingInt(Expr::cost));
        return new Group(children, all, negated);
    }

    private static Expr term(QueryType type, String raw, boolean negated) {

        boolean regex = raw.length() > 1 && raw.startsWith("/") && raw.endsWith("/");
        boolean quoted = !regex && raw.length() > 1 && raw.startsWith("\"") && raw.endsWith("\"");
        String pattern = regex || quoted ? raw.substring(1, raw.length() - 1) : raw;

        // 索引里的 key 在 bake 时就已经 toLowerCase 过了（EMI 的 NameQuery / TooltipQuery / ModQuery /
        // AliasQuery 同样是 search(name.toLowerCase())），所以普通子串必须一起小写，否则大写输入永远搜不到。
        // 正则走 CASE_INSENSITIVE、TagQuery 自己会小写，这两类保持原样。
        String indexed = pattern.toLowerCase();

        if (type != QueryType.DEFAULT) {
            if (type == QueryType.MOD) {
                return new Leaf(regex ? KIND_REGEX_MOD : KIND_MOD, regex ? pattern : indexed, negated);
            }
            if (type == QueryType.TOOLTIP) {
                return new Leaf(regex ? KIND_REGEX_TOOLTIP : KIND_TOOLTIP, regex ? pattern : indexed, negated);
            }
            if (type == QueryType.TAG) {
                return new Leaf(regex ? KIND_REGEX_TAG : KIND_TAG, pattern, negated);
            }
            return new Leaf(regex ? KIND_REGEX_NAME : KIND_NAME, regex ? pattern : indexed, negated);
        }

        List<Expr> any = new ArrayList<>();
        if (regex) {
            // 与 EMI 一样，正则展开里没有别名
            any.add(new Leaf(KIND_REGEX_NAME, pattern, false));
            if (EmiConfig.searchTooltipByDefault) {
                any.add(new Leaf(KIND_REGEX_TOOLTIP, pattern, false));
            }
            if (EmiConfig.searchModNameByDefault) {
                any.add(new Leaf(KIND_REGEX_MOD, pattern, false));
            }
            if (EmiConfig.searchTagsByDefault) {
                any.add(new Leaf(KIND_REGEX_TAG, pattern, false));
            }
        } else {
            any.add(new Leaf(KIND_NAME, indexed, false));
            if (EmiConfig.searchTooltipByDefault) {
                any.add(new Leaf(KIND_TOOLTIP, indexed, false));
            }
            if (EmiConfig.searchModNameByDefault) {
                any.add(new Leaf(KIND_MOD, indexed, false));
            }
            if (EmiConfig.searchTagsByDefault) {
                any.add(new Leaf(KIND_TAG, pattern, false));
            }
            any.add(new Leaf(KIND_ALIAS, indexed, false));
        }

        return any.size() == 1 ? any.get(0) : new Group(any, false, negated);
    }

    // ---------------------------------------------------------- 位集求值

    private static Bits eval(Expr expr, Index index, Bits scope, long gen) {

        Bits raw = raw(expr, index, scope, gen);
        if (raw == null || !expr.negated) {
            return raw;
        }

        Bits inverse = new Bits(index.size);
        inverse.setAll();
        inverse.andNot(raw);
        return inverse;
    }

    private static Bits raw(Expr expr, Index index, Bits scope, long gen) {

        if (expr instanceof Leaf leaf) {
            return rawLeaf(leaf, index, scope, gen);
        }

        Group group = (Group) expr;

        if (group.all) {
            Bits current = new Bits(index.size);
            if (scope == null) {
                current.setAll();
            } else {
                current.or(scope);
            }

            for (Expr child : group.children) {
                if (current.isEmpty()) {
                    break;
                }
                if (cancelled(gen)) {
                    return null;
                }
                Bits childBits = eval(child, index, current, gen);
                if (childBits == null) {
                    return null;
                }
                current.and(childBits);
            }
            return current;
        }

        Bits union = new Bits(index.size);
        for (Expr child : group.children) {
            if (cancelled(gen)) {
                return null;
            }
            Bits childBits = eval(child, index, null, gen);
            if (childBits == null) {
                return null;
            }
            union.or(childBits);
        }
        return union;
    }

    private static Bits rawLeaf(Leaf leaf, Index index, Bits scope, long gen) {

        Bits out = new Bits(index.size);

        // 空 pattern（例如只输入了 "$"、"@"、"\"\"" 或 "//"）：
        // EMI 的后缀数组 search("") 会命中全部条目，Pattern.compile("") 的 find() 也恒真，
        // 所以这些类别要按「全中」处理，否则列表会闪成空。
        // 标签类例外——空 pattern 只会命中「带任意标签」的 stack，交给 TagQuery/RegexTagQuery 自己算。
        if (leaf.pattern.isEmpty() && leaf.kind != KIND_TAG && leaf.kind != KIND_REGEX_TAG) {
            out.setAll();
            return out;
        }

        switch (leaf.kind) {
            case KIND_NAME -> resolve(out, index, leaf.pattern, index.names, EmiSearch.names);
            case KIND_TOOLTIP -> resolveTooltip(out, index, leaf.pattern);
            case KIND_MOD -> resolve(out, index, leaf.pattern, index.mods, EmiSearch.mods);
            case KIND_ALIAS -> resolveAlias(out, index, leaf.pattern);
            case KIND_TAG -> {
                Bits cached = tagBits(leaf.pattern, index, gen);
                if (cached == null) {
                    return null;
                }
                out.or(cached);
            }
            default -> {
                // 取反的正则无法从收窄后的候选集反推，只能全表跑
                Bits scanScope = leaf.negated ? null : scope;
                if (!scan(index, scanScope, out, queryOf(leaf, index), gen)) {
                    return null;
                }
            }
        }

        return out;
    }

    /**
     * token → 命中的 stack 位集。
     *
     * <p>外部模式（装了 JECharacters）下查 EMI 自己的表——那几张表已被换成 PinIn 拼音 Trie，
     * 于是拼音匹配由它负责；否则查本内核的 1~3 gram 倒排索引。</p>
     */
    private static void resolve(Bits out, Index index, String token,
                                GTEmiSubstringIndex gram, SuffixArray<SearchStack> external) {

        if (index.external) {
            for (SearchStack searchStack : external.search(token)) {
                int stackIndex = index.byIdentity.getInt(searchStack.stack);
                if (stackIndex >= 0) {
                    out.set(stackIndex);
                }
            }
            return;
        }

        gram.forEachMatch(token, out::set);
    }

    /**
     * tooltip 只走我们自己的 gram 索引（不再投喂 JEChars）。
     *
     * <p>阶段 2 还没建好时按「不命中」处理并提示一次 —— 绝不能算成「全中」，
     * 那会让最初几秒的搜索结果变多且全是错的。</p>
     */
    private static void resolveTooltip(Bits out, Index index, String token) {

        GTEmiSubstringIndex tooltips = index.tooltips;
        if (tooltips == null) {
            if (!warnedTooltipPending) {
                warnedTooltipPending = true;
                GoldenTweaks.LOGGER.info("GoldenTweaks: tooltip 索引尚未建好，本次 `$` 搜索不包含 tooltip 匹配");
            }
            return;
        }

        tooltips.forEachMatch(token, out::set);
    }

    /** 别名表存的是 {@code stack.copy()}，与原对象只有值相等关系，所以外部模式下要额外按值回落。 */
    private static void resolveAlias(Bits out, Index index, String token) {

        if (!index.external) {
            index.aliases.forEachMatch(token, out::set);
            return;
        }

        for (EmiStack stack : EmiSearch.aliases.search(token)) {
            int stackIndex = index.byIdentity.getInt(stack);
            if (stackIndex < 0 && index.byValue != null) {
                stackIndex = index.byValue.getInt(stack);
            }
            if (stackIndex >= 0) {
                out.set(stackIndex);
            }
        }
    }

    private static Bits tagBits(String pattern, Index index, long gen) {

        Bits cached = index.tagBits.get(pattern);
        if (cached != null) {
            return cached;
        }

        TagQuery query;
        try {
            query = new TagQuery(pattern);
        } catch (Throwable throwable) {
            GoldenTweaks.LOGGER.warn("GoldenTweaks: 解析标签查询 \"{}\" 失败", pattern, throwable);
            return new Bits(index.size);
        }

        Bits out = new Bits(index.size);
        for (int i = 0; i < index.size; i++) {
            if ((i & CANCEL_CHECK_MASK) == 0 && cancelled(gen)) {
                return null;
            }
            if (query.matches(index.stacks[i])) {
                out.set(i);
            }
        }

        index.tagBits.put(pattern, out);
        return out;
    }

    private static Query queryOf(Leaf leaf, Index index) {

        String key = leaf.kind + ":" + leaf.pattern;
        Query cached = index.queries.get(key);
        if (cached != null) {
            return cached;
        }

        Query created = switch (leaf.kind) {
            case KIND_REGEX_TOOLTIP -> new RegexTooltipQuery(leaf.pattern);
            case KIND_REGEX_MOD -> new RegexModQuery(leaf.pattern);
            case KIND_REGEX_TAG -> new RegexTagQuery(leaf.pattern);
            default -> new RegexNameQuery(leaf.pattern);
        };

        index.queries.put(key, created);
        return created;
    }

    private static boolean scan(Index index, Bits scope, Bits out, Query query, long gen) {

        if (scope == null) {
            for (int i = 0; i < index.size; i++) {
                if ((i & CANCEL_CHECK_MASK) == 0 && cancelled(gen)) {
                    return false;
                }
                if (query.matches(index.stacks[i])) {
                    out.set(i);
                }
            }
            return true;
        }

        for (int i = scope.nextSetBit(0); i >= 0; i = scope.nextSetBit(i + 1)) {
            if ((i & CANCEL_CHECK_MASK) == 0 && cancelled(gen)) {
                return false;
            }
            if (query.matches(index.stacks[i])) {
                out.set(i);
            }
        }
        return true;
    }

    // ------------------------------------------------------------- 表达式

    private abstract static class Expr {

        final boolean negated;

        Expr(boolean negated) {
            this.negated = negated;
        }

        abstract int cost();
    }

    private static final class Leaf extends Expr {

        final int kind;
        final String pattern;

        Leaf(int kind, String pattern, boolean negated) {
            super(negated);
            this.kind = kind;
            this.pattern = pattern;
        }

        @Override
        int cost() {
            if (this.kind == KIND_TAG) {
                return 1;
            }
            return this.kind >= KIND_REGEX_NAME ? 2 : 0;
        }
    }

    private static final class Group extends Expr {

        final List<Expr> children;
        final boolean all;

        Group(List<Expr> children, boolean all, boolean negated) {
            super(negated);
            this.children = children;
            this.all = all;
        }

        @Override
        int cost() {
            int cost = 0;
            for (Expr child : this.children) {
                cost = Math.max(cost, child.cost());
            }
            return cost;
        }
    }

    // --------------------------------------------------------------- 数据

    /** 构建完成后只读；{@code tagBits}/{@code queries} 仅由搜索线程访问。 */
    private static final class Index {

        final EmiStack[] stacks;
        final int size;
        final Object2IntMap<EmiStack> byIdentity;
        /** 仅外部模式使用：别名表里是 {@code stack.copy()}，只能按值回落。 */
        final Object2IntMap<EmiStack> byValue;
        /** true = token 解析走 EMI 自己的表（可能是 JECharacters 的拼音 Trie）。 */
        final boolean external;
        final GTEmiSubstringIndex names;
        /** tooltip 索引：始终是本内核的 gram 索引；阶段 2 才建好时为 null（见 {@link #deferTooltips}）。 */
        volatile GTEmiSubstringIndex tooltips;
        final GTEmiSubstringIndex mods;
        final GTEmiSubstringIndex aliases;

        final Map<String, Bits> tagBits = new LinkedHashMap<>(16, 0.75F, true) {

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Bits> eldest) {
                return size() > CACHE_LIMIT;
            }
        };

        final Map<String, Query> queries = new LinkedHashMap<>(16, 0.75F, true) {

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Query> eldest) {
                return size() > CACHE_LIMIT;
            }
        };

        Index(EmiStack[] stacks, Object2IntMap<EmiStack> byIdentity, Object2IntMap<EmiStack> byValue,
              boolean external, GTEmiSubstringIndex names, GTEmiSubstringIndex tooltips,
              GTEmiSubstringIndex mods, GTEmiSubstringIndex aliases) {

            this.stacks = stacks;
            this.size = stacks.length;
            this.byIdentity = byIdentity;
            this.byValue = byValue;
            this.external = external;
            this.names = names;
            this.tooltips = tooltips;
            this.mods = mods;
            this.aliases = aliases;
        }
    }

    private static final class Bits {

        final long[] words;

        Bits(int size) {
            this(new long[(size + 63) >>> 6]);
        }

        Bits(long[] words) {
            this.words = words;
        }

        void set(int bit) {
            this.words[bit >>> 6] |= 1L << (bit & 63);
        }

        boolean get(int bit) {
            return (this.words[bit >>> 6] & (1L << (bit & 63))) != 0;
        }

        void setAll() {
            Arrays.fill(this.words, -1L);
        }

        void and(Bits other) {
            long[] a = this.words;
            long[] b = other.words;
            for (int i = 0; i < a.length; i++) {
                a[i] &= b[i];
            }
        }

        void or(Bits other) {
            long[] a = this.words;
            long[] b = other.words;
            for (int i = 0; i < a.length; i++) {
                a[i] |= b[i];
            }
        }

        void andNot(Bits other) {
            long[] a = this.words;
            long[] b = other.words;
            for (int i = 0; i < a.length; i++) {
                a[i] &= ~b[i];
            }
        }

        boolean isEmpty() {
            for (long word : this.words) {
                if (word != 0L) {
                    return false;
                }
            }
            return true;
        }

        int nextSetBit(int from) {

            int word = from >>> 6;
            if (word >= this.words.length) {
                return -1;
            }

            long value = this.words[word] & (-1L << (from & 63));
            while (true) {
                if (value != 0L) {
                    return (word << 6) + Long.numberOfTrailingZeros(value);
                }
                if (++word >= this.words.length) {
                    return -1;
                }
                value = this.words[word];
            }
        }
    }
}
