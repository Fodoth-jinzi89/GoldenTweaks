package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.gui.AspectGuiRenderer;
import thaumcraft.client.gui.ThaumonomiconData;
import thaumcraft.client.gui.ThaumonomiconScreen;
import thaumcraft.common.research.ResearchAspectPageLayout;
import thaumcraft.common.research.ThaumometerScanManager;
import thaumcraft.common.aspects.ItemAspectRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 研究标签页翻页（移植自 TC4Tweaks 的 BrowserPaging）。
 *
 * <p>移植版 {@code drawCategoryTabs} 固定每列画 9 个标签（左右两列共 18 个），
 * 超过 18 个分类时会重叠。这里按每页 18 个标签分页：{@code drawCategoryTabs}
 * 只绘制当前页的分类子列表，并在标签列下方绘制 {@code « / »} 翻页箭头；
 * 点击处理挂到 {@code handleMapCategoryClick}（与标签点击同一坐标体系）。
 * 标签数不超过 18 时完全不生效。</p>
 */
@Mixin(value = ThaumonomiconScreen.class, remap = false)
public abstract class ThaumonomiconScreenMixin {

    @Unique
    private static final int TABS_PER_PAGE = 18;
    @Unique
    private static final int ASPECT_SIZE = 18;
    @Unique
    private static final int ASPECT_ROW_GAP = 19;
    @Unique
    private static final int INFUSION_ASPECTS_PER_ROW = 7;
    @Unique
    private static final int CRUCIBLE_ASPECTS_PER_PAGE = 8;
    @Unique
    private static final long ASPECT_CAROUSEL_INTERVAL_MS = 2000;
    @Unique
    private static final float KNOWLEDGE_ASPECT_SCALE = 0.884F;
    @Unique
    private static final int KNOWLEDGE_ASPECT_ROW_STEP = 53;
    @Unique
    private static final int KNOWLEDGE_COMPONENT_SIZE = 17;
    @Unique
    private static final float KNOWLEDGE_NAME_SCALE = 0.75F;
    @Unique
    private static final float KNOWLEDGE_MAIN_NAME_SCALE = 1.0F;

    /** 左/右翻页箭头的位置（与标签列同一坐标体系，标签列底部 y≈216）。 */
    @Unique
    private static final int PREV_X = -20;
    @Unique
    private static final int NEXT_X = 262;
    @Unique
    private static final int BUTTON_Y = 220;

    @Unique
    private static int gt$tabPage;
    @Unique
    private static long gt$infusionCarouselStart = Util.getMillis();
    @Unique
    private static int gt$infusionCarouselRow;
    @Unique
    private static long gt$crucibleCarouselStart = Util.getMillis();
    @Unique
    private static int gt$crucibleCarouselPage;
    @Unique private static long gt$sourceCarouselStart = Util.getMillis();
    @Unique private static int gt$sourceCarouselPage;
    @Unique private static int gt$sourceCarouselPages = 1;
    @Unique private static Aspect gt$sourceAspect;
    @Unique private static long gt$sourceManualUntil;
    @Unique private static int gt$sourceBoxX;
    @Unique private static int gt$sourceBoxY;
    @Unique private static int gt$sourceBoxWidth;
    @Unique private static int gt$sourceBoxHeight;
    @Unique private static boolean gt$sourceBoxOpen;
    @Unique private final Map<Aspect, List<ItemStack>> gt$sourceItems = new LinkedHashMap<>();
    @Unique private final Map<Aspect, Map<String, Integer>> gt$sourceAmounts = new LinkedHashMap<>();
    @Unique private final Map<Aspect, Iterator<Item>> gt$sourceLoaders = new LinkedHashMap<>();
    @Unique private final Set<Aspect> gt$loadedSources = new HashSet<>();
    @Unique private Aspect gt$componentClickAspect;

    @Shadow private int detailMouseX;
    @Shadow private int detailMouseY;
    @Shadow private int page;
    @Shadow protected abstract List<?> currentDetailPages();
    @Shadow protected abstract void drawLargeAspectTag(GuiGraphics graphics, Aspect aspect, int amount, int x, int y);

    @Redirect(
            method = "drawResearchNodes",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderOutline(IIIII)V")
    )
    private void gt$hideIncompleteResearchHighlight(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        // 老版本 port.152 的 drawResearchNodes 没有 renderOutline；
        // port.239 里该方法唯一的 renderOutline（26x26）就是未完成/可解锁研究的黄色外框，
        // 且颜色已从 0xE8A48E 改为 0xE8B84E，不能再用颜色判断。
        if (GoldenTweaksCommonConfig.REMOVE_THAUMONOMICON_RESEARCH_HIGHLIGHT.get()
                && width == 26 && height == 26) {
            return;
        }
        graphics.renderOutline(x, y, width, height, color);
    }

    @Inject(method = "drawAspectKnowledgePage", at = @At("HEAD"), cancellable = true)
    private void gt$drawAspectKnowledgePage(GuiGraphics graphics, ResearchAspectPageLayout.Page page,
                                             int x, int y, int height, CallbackInfo ci) {
        if (page == null) return;
        List<ResearchAspectPageLayout.Entry> entries = new ArrayList<>(page.entries());
        entries.sort(Comparator.comparingInt((ResearchAspectPageLayout.Entry e) -> gt$tier(e.aspect())).thenComparing(e -> e.aspect().tag()));
        Aspect hovered = null;
        int hoveredY = 0;
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < entries.size(); i++) {
            ResearchAspectPageLayout.Entry entry = entries.get(i);
            int col = i % 2, row = i / 2;
            int ex = x + col * 84, ey = y - 20 + row * KNOWLEDGE_ASPECT_ROW_STEP;
            graphics.pose().pushPose();
            graphics.pose().translate(ex + 16, ey + 16, 0);
            graphics.pose().scale(KNOWLEDGE_ASPECT_SCALE, KNOWLEDGE_ASPECT_SCALE, 1.0F);
            drawLargeAspectTag(graphics, entry.aspect(), entry.amount(), -16, -16);
            graphics.pose().popPose();
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 200.0F);
            graphics.drawString(font, Integer.toString(gt$tier(entry.aspect())), ex + 2, ey + 2, 0xFFFFFF, true);
            graphics.pose().popPose();
            String name = entry.aspect().displayName().getString();
            graphics.pose().pushPose();
            graphics.pose().translate(ex + 16, ey + 36, 0);
            graphics.pose().scale(KNOWLEDGE_MAIN_NAME_SCALE, KNOWLEDGE_MAIN_NAME_SCALE, 1.0F);
            graphics.drawString(font, name, -font.width(name) / 2, 0, 0x507060, false);
            graphics.pose().popPose();
            if (entry.primal()) {
                String primal = Component.translatable("tc.aspect.primal").getString();
                graphics.drawString(font, primal, ex + 56 - font.width(primal) / 2, ey + KNOWLEDGE_COMPONENT_SIZE - 5, 0x507060, false);
            } else if (entry.components().size() >= 2) {
                int componentY = ey + 16 - KNOWLEDGE_COMPONENT_SIZE / 2;
                AspectGuiRenderer.draw(graphics, entry.components().get(0), ex + 36, componentY, KNOWLEDGE_COMPONENT_SIZE, 1.0F);
                AspectGuiRenderer.draw(graphics, entry.components().get(1), ex + 64, componentY, KNOWLEDGE_COMPONENT_SIZE, 1.0F);
                graphics.drawString(font, "+", ex + 56, ey + 14, 0xAAAAAA, false);
                if (this.detailMouseX >= ex + 36 && this.detailMouseX < ex + 36 + KNOWLEDGE_COMPONENT_SIZE
                        && this.detailMouseY >= componentY && this.detailMouseY < componentY + KNOWLEDGE_COMPONENT_SIZE) {
                    this.gt$componentClickAspect = entry.components().get(0);
                } else if (this.detailMouseX >= ex + 64 && this.detailMouseX < ex + 64 + KNOWLEDGE_COMPONENT_SIZE
                        && this.detailMouseY >= componentY && this.detailMouseY < componentY + KNOWLEDGE_COMPONENT_SIZE) {
                    this.gt$componentClickAspect = entry.components().get(1);
                }
                graphics.pose().pushPose();
                graphics.pose().translate(ex + 44, ey + KNOWLEDGE_COMPONENT_SIZE + 13, 0);
                graphics.pose().scale(KNOWLEDGE_NAME_SCALE, KNOWLEDGE_NAME_SCALE, 1.0F);
                graphics.drawString(font, entry.components().get(0).displayName().getString(), -font.width(entry.components().get(0).displayName().getString()) / 2, 0, 0x507060, false);
                graphics.pose().popPose();
                graphics.pose().pushPose();
                graphics.pose().translate(ex + 72, ey + KNOWLEDGE_COMPONENT_SIZE + 13, 0);
                graphics.pose().scale(KNOWLEDGE_NAME_SCALE, KNOWLEDGE_NAME_SCALE, 1.0F);
                graphics.drawString(font, entry.components().get(1).displayName().getString(), -font.width(entry.components().get(1).displayName().getString()) / 2, 0, 0x507060, false);
                graphics.pose().popPose();
            }
            if (this.detailMouseX >= ex + 2 && this.detailMouseX < ex + 30 && this.detailMouseY >= ey + 2 && this.detailMouseY < ey + 30) {
                hovered = entry.aspect(); hoveredY = ey;
            }
        }
        if (hovered != null) gt$drawScannedAspectSources(graphics, hovered, x, hoveredY, height);
        ci.cancel();
    }

    @Inject(method = "renderResearchDetailPages", at = @At("HEAD"))
    private void gt$resetAspectPageInteraction(CallbackInfo ci) {
        gt$sourceBoxWidth = 0;
        gt$sourceBoxHeight = 0;
        gt$sourceBoxOpen = false;
        this.gt$componentClickAspect = null;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void gt$openComponentAspect(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0 || this.gt$componentClickAspect == null) {
            return;
        }
        List<?> pages = this.currentDetailPages();
        for (int i = 0; i < pages.size(); i++) {
            try {
                var method = pages.get(i).getClass().getDeclaredMethod("aspectPage");
                method.setAccessible(true);
                Object aspectPage = method.invoke(pages.get(i));
                if (aspectPage instanceof ResearchAspectPageLayout.Page aspectKnowledge
                        && aspectKnowledge.entries().stream().anyMatch(entry -> entry.aspect() == this.gt$componentClickAspect)) {
                    this.page = i - i % 2;
                    cir.setReturnValue(true);
                    return;
                }
            } catch (ReflectiveOperationException ignored) {
                return;
            }
        }
    }

    @Unique private static int gt$tier(Aspect aspect) {
        if (aspect.isPrimal()) return 1;
        int max = 1;
        for (Aspect component : aspect.components()) max = Math.max(max, gt$tier(component));
        return max + 1;
    }

    @Unique private void gt$drawScannedAspectSources(GuiGraphics graphics, Aspect aspect, int x, int y, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        gt$sourceBoxOpen = true;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);
        this.gt$loadAspectItems(aspect);
        List<ItemStack> all = this.gt$sourceItems.getOrDefault(aspect, List.of());
        Map<String, Integer> amounts = this.gt$sourceAmounts.getOrDefault(aspect, Map.of());
        if (all.isEmpty()) {
            graphics.pose().popPose();
            return;
        }
        int perPage = 25, pages = (all.size() + perPage - 1) / perPage;
        if (gt$sourceAspect != aspect) {
            gt$sourceAspect = aspect;
            gt$sourceCarouselPage = 0;
        }
        gt$sourceCarouselPages = pages;
        long now = Util.getMillis();
        int pageInterval = GoldenTweaksCommonConfig.getThaumonomiconAspectSourcePageInterval();
        if (pages > 1 && GoldenTweaksCommonConfig.isThaumonomiconAspectSourceAutoPage()
                && !Screen.hasShiftDown() && now >= gt$sourceManualUntil && now - gt$sourceCarouselStart >= pageInterval) {
            gt$sourceCarouselStart = now;
            gt$sourceCarouselPage = (gt$sourceCarouselPage + 1) % pages;
        }
        int pageIndex = Math.min(gt$sourceCarouselPage, pages - 1), start = pageIndex * perPage, end = Math.min(start + perPage, all.size());
        int cols = 5, visible = end - start, rows = (visible + cols - 1) / cols;
        int usedCols = Math.min(cols, visible);
        int px = x, py = Math.max(0, y - rows * 18 - 20);
        int boxWidth = (int) Math.ceil((cols * 18 + 6) * 1.1F);
        gt$sourceBoxX = px - 2;
        gt$sourceBoxY = py - 7;
        gt$sourceBoxWidth = boxWidth + 2;
        gt$sourceBoxHeight = rows * 18 + 25;
        graphics.fill(px - 2, py - 7, px + boxWidth, py + rows * 18 + 18, 0xD8000000);
        graphics.renderOutline(px - 2, py - 7, boxWidth + 2, rows * 18 + 25, 0xFF5A4A3A);
        for (int i = start; i < end; i++) {
            ItemStack stack = all.get(i);
            int itemX = px + ((boxWidth - usedCols * 18) / 2) + (i - start) % cols * 18;
            int itemY = py + (i - start) / cols * 18;
            graphics.renderItem(stack, itemX, itemY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, stack, itemX, itemY, Integer.toString(amounts.get(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())));
        }
        graphics.drawCenteredString(Minecraft.getInstance().font,
                Component.translatable("gui.goldentweaks.thaumonomicon.aspect_sources.page", pageIndex + 1, pages, all.size()),
                px + boxWidth / 2, py + rows * 18 + 3, 0xFFFFFF);
        graphics.pose().popPose();
    }

    @Unique
    private void gt$loadAspectItems(Aspect aspect) {
        if (this.gt$loadedSources.contains(aspect)) return;
        Minecraft minecraft = Minecraft.getInstance();
        List<ItemStack> items = this.gt$sourceItems.computeIfAbsent(aspect, ignored -> new ArrayList<>());
        Map<String, Integer> amounts = this.gt$sourceAmounts.computeIfAbsent(aspect, ignored -> new LinkedHashMap<>());
        Iterator<Item> loader = this.gt$sourceLoaders.computeIfAbsent(aspect, ignored -> BuiltInRegistries.ITEM.iterator());
        int remaining = GoldenTweaksCommonConfig.getThaumonomiconAspectSourceItemsPerFrame();
        while (remaining-- > 0 && loader.hasNext()) {
            Item item = loader.next();
            if (item == Items.AIR) continue;
            ItemStack stack = item.getDefaultInstance();
            if (!ThaumometerScanManager.hasBeenScanned(minecraft.player, stack)) continue;
            int amount = ItemAspectRegistry.getObjectTags(stack).amount(aspect);
            if (amount <= 0) continue;
            stack.setCount(1);
            items.add(stack);
            amounts.put(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), amount);
        }
        if (loader.hasNext()) return;
        LinkedHashMap<String, ItemStack> unique = new LinkedHashMap<>();
        for (ItemStack stack : items) unique.putIfAbsent(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack);
        List<ItemStack> sorted = new ArrayList<>(unique.values());
        sorted.sort(Comparator.comparingInt((ItemStack s) -> amounts.get(BuiltInRegistries.ITEM.getKey(s.getItem()).toString())).reversed()
                .thenComparing(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString()));
        this.gt$sourceItems.put(aspect, List.copyOf(sorted));
        this.gt$sourceLoaders.remove(aspect);
        this.gt$loadedSources.add(aspect);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void gt$scrollSourcePages(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (!gt$sourceBoxOpen) {
            return;
        }
        gt$sourceCarouselPage = Mth.clamp(gt$sourceCarouselPage + (scrollY < 0 ? 1 : -1), 0, gt$sourceCarouselPages - 1);
        gt$sourceCarouselStart = Util.getMillis();
        gt$sourceManualUntil = gt$sourceCarouselStart + GoldenTweaksCommonConfig.getThaumonomiconAspectSourcePageInterval();
        cir.setReturnValue(true);
    }

    @Redirect(
            method = "drawInfusionRecipePage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V",
                    ordinal = 0
            )
    )
    private void gt$moveInfusionHaloLeft(GuiGraphics graphics, ResourceLocation texture, int x, int y,
                                          float u, float v, int width, int height, int textureWidth, int textureHeight) {
        graphics.pose().pushPose();
        graphics.pose().translate(-2.0F, 0.0F, 0.0F);
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        graphics.pose().popPose();
    }

    @Redirect(
            method = "drawInfusionRecipePage",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/client/gui/ThaumonomiconScreen;drawAspectCost(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/AspectList;III)V"
            )
    )
    private void gt$drawInfusionAspectsLikeEmi(ThaumonomiconScreen screen, GuiGraphics graphics,
                                                AspectList aspects, int x, int y, int width) {
        List<Aspect> list = aspects.sortedByTag();
        if (list.isEmpty()) {
            return;
        }
        int rows = (list.size() + INFUSION_ASPECTS_PER_ROW - 1) / INFUSION_ASPECTS_PER_ROW;
        int startRow = gt$infusionCarouselStart(rows);
        int shownRows = Math.min(rows, 2);
        int step = gt$infusionWindowStep(list, startRow, shownRows, width);
        for (int i = 0; i < shownRows; i++) {
            int start = (startRow + i) * INFUSION_ASPECTS_PER_ROW;
            int end = Math.min(start + INFUSION_ASPECTS_PER_ROW, list.size());
            int rowY = y - (shownRows - 1 - i) * ASPECT_ROW_GAP;
            gt$drawAspectRow(graphics, aspects, list.subList(start, end), x, rowY, width, step);
        }
    }

    @Redirect(
            method = "drawCrucibleRecipePage",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/client/gui/ThaumonomiconScreen;drawCrucibleAspectCost(Lnet/minecraft/client/gui/GuiGraphics;Lthaumcraft/api/aspects/AspectList;II)V"
            )
    )
    private void gt$drawCrucibleAspectsLikeEmi(ThaumonomiconScreen screen, GuiGraphics graphics,
                                                AspectList aspects, int x, int y) {
        List<Aspect> list = aspects.sortedByTag();
        if (list.isEmpty()) {
            return;
        }
        int pages = (list.size() + CRUCIBLE_ASPECTS_PER_PAGE - 1) / CRUCIBLE_ASPECTS_PER_PAGE;
        int page = gt$crucibleCarouselPage(pages);
        List<Aspect> visible = list.subList(page * CRUCIBLE_ASPECTS_PER_PAGE,
                Math.min((page + 1) * CRUCIBLE_ASPECTS_PER_PAGE, list.size()));
        int rows = visible.size() <= 3 ? 1 : visible.size() <= 6 ? 2 : 3;
        int rowStart = 0;
        for (int row = 0; row < rows; row++) {
            int rowEnd = Math.min(rowStart + (row < 2 ? 3 : 2), visible.size());
            int rowY = y + 128 + (row * 2 - (rows - 1)) * ASPECT_ROW_GAP / 2 - ASPECT_SIZE / 2;
            gt$drawAspectRow(graphics, aspects, visible.subList(rowStart, rowEnd), x + 16, rowY, 80, 24);
            rowStart = rowEnd;
        }
    }

    @Redirect(
            method = "drawCategoryTabs",
            at = @At(value = "INVOKE", target = "Lthaumcraft/client/gui/ThaumonomiconData;categories()Ljava/util/List;")
    )
    private static List<ThaumonomiconData.Category> gt$tabsForCurrentPage() {
        List<ThaumonomiconData.Category> all = ThaumonomiconData.categories();
        int pages = gt$pageCount(all.size());
        if (pages <= 1) {
            return all;
        }

        gt$tabPage = Mth.clamp(gt$tabPage, 0, pages - 1);
        int start = gt$tabPage * TABS_PER_PAGE;
        return all.subList(start, Math.min(start + TABS_PER_PAGE, all.size()));
    }

    @Inject(method = "drawCategoryTabs", at = @At("RETURN"))
    private void gt$drawTabPaging(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (gt$pageCount() <= 1) {
            return;
        }

        guiGraphics.drawString(Minecraft.getInstance().font, "«", PREV_X, BUTTON_Y, 0xFF404040);
        guiGraphics.drawString(Minecraft.getInstance().font, "»", NEXT_X, BUTTON_Y, 0xFF404040);
    }

    @Redirect(
            method = "drawLargeAspectTag",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I")
    )
    private int gt$drawAspectAmountAboveIcon(GuiGraphics graphics, Font font, String amount,
                                              int x, int y, int color, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        int width = graphics.drawString(font, amount, x, y, color, true);
        graphics.pose().popPose();
        return width;
    }

    @Unique
    private static int gt$infusionCarouselStart(int rows) {
        if (rows < 3) {
            return 0;
        }
        long now = Util.getMillis();
        if (now - gt$infusionCarouselStart >= ASPECT_CAROUSEL_INTERVAL_MS) {
            gt$infusionCarouselStart = now;
            gt$infusionCarouselRow = (gt$infusionCarouselRow + 1) % (rows - 1);
        }
        return gt$infusionCarouselRow;
    }

    @Unique
    private static int gt$crucibleCarouselPage(int pages) {
        if (pages < 2) {
            return 0;
        }
        long now = Util.getMillis();
        if (now - gt$crucibleCarouselStart >= ASPECT_CAROUSEL_INTERVAL_MS) {
            gt$crucibleCarouselStart = now;
            gt$crucibleCarouselPage = (gt$crucibleCarouselPage + 1) % pages;
        }
        return gt$crucibleCarouselPage;
    }

    @Unique
    private static int gt$infusionWindowStep(List<Aspect> list, int startRow, int shownRows, int width) {
        int step = Integer.MAX_VALUE;
        for (int i = 0; i < shownRows; i++) {
            int start = (startRow + i) * INFUSION_ASPECTS_PER_ROW;
            int count = Math.min(start + INFUSION_ASPECTS_PER_ROW, list.size()) - start;
            if (count > 1) {
                step = Math.min(step, gt$aspectStep(count, width));
            }
        }
        return step == Integer.MAX_VALUE ? 0 : step;
    }

    @Unique
    private static void gt$drawAspectRow(GuiGraphics graphics, AspectList aspects, List<Aspect> row,
                                          int x, int y, int width, int step) {
        if (row.isEmpty()) {
            return;
        }
        int totalWidth = ASPECT_SIZE + step * (row.size() - 1);
        int startX = x + Math.max(0, (width - totalWidth) / 2);
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < row.size(); i++) {
            Aspect aspect = row.get(i);
            int iconX = startX + i * step;
            AspectGuiRenderer.draw(graphics, aspect, iconX, y, ASPECT_SIZE, 1.0F);
            AspectGuiRenderer.drawCount(graphics, font, aspects.amount(aspect), iconX, y, -1);
        }
    }

    @Unique
    private static int gt$aspectStep(int count, int width) {
        return count <= 1 ? 0 : Math.max(13, Math.min(24, (width - ASPECT_SIZE) / (count - 1)));
    }

    @Inject(method = "handleMapCategoryClick", at = @At("HEAD"), cancellable = true)
    private void gt$handleTabPaging(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (gt$pageCount() <= 1) {
            return;
        }

        if (mouseY < BUTTON_Y - 4 || mouseY > BUTTON_Y + 12) {
            return;
        }

        if (mouseX >= PREV_X - 4 && mouseX <= PREV_X + 10) {
            gt$tabPage = Math.max(0, gt$tabPage - 1);
            cir.setReturnValue(true);
        } else if (mouseX >= NEXT_X - 4 && mouseX <= NEXT_X + 10) {
            gt$tabPage = Math.min(gt$pageCount() - 1, gt$tabPage + 1);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static int gt$pageCount() {
        return gt$pageCount(ThaumonomiconData.categories().size());
    }

    @Unique
    private static int gt$pageCount(int total) {
        return (total + TABS_PER_PAGE - 1) / TABS_PER_PAGE;
    }
}
