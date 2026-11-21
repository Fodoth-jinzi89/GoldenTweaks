import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/emi/GTEmiSearchEngine.java'
s = io.open(p, encoding='utf-8').read()
orig = s

old = '''    /**
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
    }'''

new = '''    /**
     * 每次 tick 最多烘焙这么多条 tooltip。
     *
     * <p>取值偏小：一次 {@code getTooltipText} 会触发整个 Tooltip 事件链，其它模组的处理器可能做
     * 不轻的工作，逐个来既能摊平开销，也不会让玩家在加载后感到卡顿。</p>
     */
    private static final int TOOLTIP_BAKE_PER_TICK = 8;

    /**
     * 正在进行的 tooltip 烘焙任务，没有则为 {@code null}。
     *
     * <p>必须在<b>主线程</b>执行，不能像早期版本那样丢到后台线程：{@code getTooltipText} 会触发
     * {@code ItemTooltip} 事件，不少模组的处理器并不是线程安全的（例如 cuisinedelight 会往一个
     * 共享的 {@code HashSet} 里写）。在后台线程调用会和主线程并发修改同一个容器，损坏 HashMap 的
     * 内部结构并抛 {@code ClassCastException}。分帧执行既避开了这个并发问题，又不会阻塞。</p>
     */
    @Nullable
    private static TooltipBake pendingTooltipBake;

    /** 一个分帧进行的 tooltip 烘焙任务。 */
    private static final class TooltipBake {

        private final Index target;
        private final List<EmiStack> stacks;
        private final GTEmiSubstringIndex.Builder builder = GTEmiSubstringIndex.builder();
        private final long started = System.currentTimeMillis();
        private int cursor;
    }

    /**
     * 阶段 2：在主线程分帧生成 tooltip 并建好 tooltip 索引。
     *
     * <p>完成后 swap 进 {@link Index#tooltips} 并 bump 代次，之后 `$` 类搜索自动变完整。</p>
     */
    private static void startDeferredTooltipBake(Index target) {
        TooltipBake bake = new TooltipBake();
        bake.target = target;
        bake.stacks = List.copyOf(DEFERRED_TOOLTIP_STACKS);
        pendingTooltipBake = bake;
    }

    /**
     * 推进当前的 tooltip 烘焙任务，由客户端 tick 驱动。
     *
     * <p>每 tick 处理一小批，处理完就停；下一 tick 继续，直到整批结束。</p>
     */
    public static void tickTooltipBake() {
        TooltipBake bake = pendingTooltipBake;
        if (bake == null) {
            return;
        }

        try {
            int budget = TOOLTIP_BAKE_PER_TICK;
            while (budget-- > 0 && bake.cursor < bake.stacks.size()) {
                EmiStack stack = bake.stacks.get(bake.cursor++);
                int stackIndex = bake.target.byIdentity.getInt(stack);
                if (stackIndex < 0) {
                    continue;
                }
                List<Component> lines = stack.getTooltipText();
                if (lines == null) {
                    continue;
                }
                // 第 0 行是物品名，已经在名称索引里，这里从 1 开始取附加信息。
                for (int i = 1; i < lines.size(); i++) {
                    Component line = lines.get(i);
                    if (line != null) {
                        String text = line.getString().toLowerCase();
                        if (!text.isEmpty()) {
                            bake.builder.put(text, stackIndex);
                        }
                    }
                }
            }

            if (bake.cursor < bake.stacks.size()) {
                return;
            }

            GTEmiSubstringIndex built = bake.builder.build();
            bake.target.tooltips = built;
            invalidate();
            pendingTooltipBake = null;

            GoldenTweaks.LOGGER.info("GoldenTweaks: tooltip 索引已建好（{} 项，{} ms），搜索已完整",
                    built.keyCount(), System.currentTimeMillis() - bake.started);
        } catch (Throwable throwable) {
            // 放弃这一轮：索引不完整总好过每 tick 反复报同一个错。
            pendingTooltipBake = null;
            GoldenTweaks.LOGGER.error("GoldenTweaks: 构建 tooltip 索引失败，`$` 类搜索将不可用", throwable);
        }
    }'''

if old not in s:
    print('MISS: block not found')
else:
    s = s.replace(old, new, 1)
    s = s.replace('import net.neoforged.fml.ModList;',
                  'import net.neoforged.fml.ModList;\nimport org.jetbrains.annotations.Nullable;', 1)
    io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
    print('changed')
