package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.aspects.ItemAspectRegistry;
import thaumcraft.common.research.ThaumometerScanManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 魔导手册「要素来源」浮窗的完整实现（取代 0.3.0 自带的那份）。
 *
 * <p>行为与旧版 GoldenTweaks 一致：</p>
 * <ul>
 *   <li><b>加载</b>：逐帧扫描 {@code BuiltInRegistries.ITEM}，每帧最多
 *       {@code getThaumonomiconAspectSourceItemsPerFrame()} 个；只保留玩家已扫描过、且该要素含量 &gt; 0 的物品，
 *       扫描完再按物品 id 去重、按含量降序排序并缓存。</li>
 *   <li><b>分页</b>：每页 5 列 × 5 行 = 25 项，底部显示「第 x/y 页，共 n 项」。</li>
 *   <li><b>轮播</b>：开启自动翻页且未按 Shift 时，每 {@code getThaumonomiconAspectSourcePageInterval()} 毫秒翻一页。</li>
 *   <li><b>滚轮</b>：浮窗打开时滚轮翻页，并推迟下一次自动翻页。</li>
 * </ul>
 */
public final class ThaumonomiconAspectSources {

    private static final int COLUMNS = 5;
    private static final int ROWS = 5;
    private static final int PER_PAGE = COLUMNS * ROWS;
    private static final int CELL = 18;

    private static final Map<Aspect, List<ItemStack>> ITEMS = new HashMap<>();
    private static final Map<Aspect, Map<String, Integer>> AMOUNTS = new HashMap<>();
    private static final Map<Aspect, Iterator<Item>> LOADERS = new HashMap<>();
    private static final Set<Aspect> LOADED = new HashSet<>();

    private static Aspect current;
    private static int page;
    private static int pages = 1;
    private static long carouselStart = Util.getMillis();
    private static long manualUntil;
    private static boolean open;

    private ThaumonomiconAspectSources() {
    }

    /** 每帧开始（详情页绘制前）重置「本帧是否正在显示浮窗」。 */
    public static void resetFrame() {
        open = false;
    }

    public static boolean isOpen() {
        return open;
    }

    /** 滚轮翻页；浮窗打开时调用方应吞掉这次滚动。 */
    public static void scroll(double amount) {
        if (amount == 0 || pages <= 1) {
            return;
        }
        page = Mth.clamp(page + (amount < 0 ? 1 : -1), 0, pages - 1);
        carouselStart = Util.getMillis();
        manualUntil = carouselStart + GoldenTweaksCommonConfig.getThaumonomiconAspectSourcePageInterval();
    }

    public static void render(GuiGraphics graphics, Aspect aspect, int x, int y, int height, Player player) {
        if (player == null) {
            return;
        }
        open = true;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);
        try {
            load(aspect, player);
            List<ItemStack> all = ITEMS.getOrDefault(aspect, List.of());
            Map<String, Integer> amounts = AMOUNTS.getOrDefault(aspect, Map.of());
            if (all.isEmpty()) {
                return;
            }

            int pageCount = (all.size() + PER_PAGE - 1) / PER_PAGE;
            if (current != aspect) {
                current = aspect;
                page = 0;
            }
            pages = Math.max(1, pageCount);

            long now = Util.getMillis();
            int interval = GoldenTweaksCommonConfig.getThaumonomiconAspectSourcePageInterval();
            if (pages > 1 && GoldenTweaksCommonConfig.isThaumonomiconAspectSourceAutoPage()
                    && !Screen.hasShiftDown() && now >= manualUntil && now - carouselStart >= interval) {
                carouselStart = now;
                page = (page + 1) % pages;
            }

            int pageIndex = Mth.clamp(page, 0, pages - 1);
            int start = pageIndex * PER_PAGE;
            int end = Math.min(start + PER_PAGE, all.size());
            int visible = end - start;
            int usedColumns = Math.min(COLUMNS, visible);
            int rows = (visible + COLUMNS - 1) / COLUMNS;
            int boxY = Math.max(0, y - rows * CELL - 20);
            int boxWidth = (int) Math.ceil((COLUMNS * CELL + 6) * 1.1F);

            graphics.fill(x - 2, boxY - 7, x + boxWidth, boxY + rows * CELL + 18, 0xD8000000);
            graphics.renderOutline(x - 2, boxY - 7, boxWidth + 2, rows * CELL + 25, 0xFF5A4A3A);

            Font font = Minecraft.getInstance().font;
            for (int i = start; i < end; i++) {
                ItemStack stack = all.get(i);
                int itemX = x + (boxWidth - usedColumns * CELL) / 2 + (i - start) % COLUMNS * CELL;
                int itemY = boxY + (i - start) / COLUMNS * CELL;
                graphics.renderItem(stack, itemX, itemY);
                graphics.renderItemDecorations(font, stack, itemX, itemY,
                        Integer.toString(amounts.getOrDefault(itemId(stack), 0)));
            }

            graphics.drawCenteredString(font,
                    Component.translatable("gui.goldentweaks.thaumonomicon.aspect_sources.page",
                            pageIndex + 1, pages, all.size()),
                    x + boxWidth / 2, boxY + rows * CELL + 3, 0xFFFFFF);
        } finally {
            graphics.pose().popPose();
        }
    }

    private static void load(Aspect aspect, Player player) {
        if (LOADED.contains(aspect)) {
            return;
        }
        List<ItemStack> items = ITEMS.computeIfAbsent(aspect, ignored -> new ArrayList<>());
        Map<String, Integer> amounts = AMOUNTS.computeIfAbsent(aspect, ignored -> new LinkedHashMap<>());
        Iterator<Item> iterator = LOADERS.computeIfAbsent(aspect, ignored -> BuiltInRegistries.ITEM.iterator());

        int budget = GoldenTweaksCommonConfig.getThaumonomiconAspectSourceItemsPerFrame();
        while (budget-- > 0 && iterator.hasNext()) {
            Item item = iterator.next();
            if (item == Items.AIR) {
                continue;
            }
            ItemStack stack = item.getDefaultInstance();
            if (!ThaumometerScanManager.hasBeenScanned(player, stack)) {
                continue;
            }
            int amount = ItemAspectRegistry.getObjectTags(stack).amount(aspect);
            if (amount <= 0) {
                continue;
            }
            stack.setCount(1);
            items.add(stack);
            amounts.put(itemId(stack), amount);
        }

        if (iterator.hasNext()) {
            return;
        }

        LinkedHashMap<String, ItemStack> unique = new LinkedHashMap<>();
        for (ItemStack stack : items) {
            unique.putIfAbsent(itemId(stack), stack);
        }
        List<ItemStack> sorted = new ArrayList<>(unique.values());
        sorted.sort(Comparator.comparingInt(
                        (ItemStack stack) -> amounts.getOrDefault(itemId(stack), 0)).reversed()
                .thenComparing(ThaumonomiconAspectSources::itemId));
        ITEMS.put(aspect, List.copyOf(sorted));
        LOADERS.remove(aspect);
        LOADED.add(aspect);
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }
}
