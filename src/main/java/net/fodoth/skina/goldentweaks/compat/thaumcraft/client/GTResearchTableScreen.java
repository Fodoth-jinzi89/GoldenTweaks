package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftAdditionalItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.gui.AspectGuiRenderer;
import thaumcraft.common.menu.ResearchTableMenu;
import thaumcraft.common.research.HexUtils;
import thaumcraft.common.research.ResearchNoteData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * GoldenTweaks' replacement for Thaumcraft's research table GUI, using the
 * "research table tweaks" layout and texture (the wide, two-pallet design).
 *
 * <p>Layout (relative to the top-left corner, in GUI pixels) matches the
 * reference {@code ThaumcraftResearchTweaks} UI:
 * <ul>
 *     <li>background 342x219 + player inventory 176x88 at (83, 191), total 342x279,</li>
 *     <li>two aspect pallets (left 12,12 / right 266,12), 4 columns each,</li>
 *     <li>parchment research area at (96, 35), hex grid centred at (171, 110),</li>
 *     <li>scribing-tools slot (91, 10) and notes slot (235, 10),</li>
 *     <li>copy button (207, 6) and usage hint (111, 6).</li>
 * </ul>
 *
 * <p>Interaction mirrors the reference: drag &amp; drop aspects onto hexes to write
 * them, drop one aspect onto another to combine (hold <b>Ctrl</b> to combine up to
 * 10 times), shift-click an aspect to auto-derive it, and click a placed aspect to
 * erase it. All research state remains server-side in {@link ResearchTableMenu}.
 */
public final class GTResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "textures/gui/research_table.png");
    private static final ResourceLocation INVENTORY_BG =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/guiresearchtable2.png");
    private static final ResourceLocation PARCHMENT =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/parchment3.png");
    private static final ResourceLocation HEX =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/hex1.png");
    private static final ResourceLocation HEX_HIGHLIGHT =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/hex2.png");
    private static final ResourceLocation UNKNOWN =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/aspects/_unknown.png");
    private static final ResourceLocation PARTICLES =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles.png");
    private static final ResourceLocation ASPECT_BG =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/aspects/_back.png");

    private static final SoundEvent CAMERA_CLACK = sound("cameraclack");
    private static final SoundEvent ASPECT_SOUND = sound("hhoff");
    private static final SoundEvent COMBINE_SOUND = sound("hhon");
    private static final SoundEvent WRITE_SOUND = sound("write");
    private static final SoundEvent ERASE_SOUND = sound("erase");

    /** Hex grid centre, relative to {@link #leftPos}/{@link #topPos}. */
    private static final int GRID_X = 171;
    private static final int GRID_Y = 110;
    private static final int HEX_SIZE = 9;

    private static final int PALETTE_LEFT_X = 12;
    private static final int PALETTE_RIGHT_X = 266;
    private static final int PALETTE_Y = 12;
    private static final int PALETTE_COLS = 4;
    private static final int PALETTE_CELL = 16;
    /** Visible rows per palette before the list scrolls. */
    private static final int PALETTE_ROWS = 12;

    private static final int COPY_X = 207;
    private static final int COPY_Y = 6;
    private static final int HINT_X = 111;
    private static final int HINT_Y = 6;
    private static final int BUTTON_SIZE = 24;

    /** Maximum aspects combined per ctrl-batch interaction. */
    private static final int BATCH_LIMIT = 10;

    private Aspect draggedAspect;

    /** Rows scrolled off the top of the left/right aspect palettes. */
    private int leftScroll;
    private int rightScroll;

    public GTResearchTableScreen(ResearchTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 342;
        this.imageHeight = 279;
        this.titleLabelX = 2000;
        this.inventoryLabelX = 2000;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.draggedAspect != null) {
            RenderSystem.enableBlend();
            drawAspectIcon(graphics, this.draggedAspect, mouseX - 8, mouseY - 8, 1.0f);
            graphics.flush();
            RenderSystem.disableBlend();
        }
        this.renderPaletteTooltip(graphics, mouseX, mouseY);
        this.renderDuplicateTooltip(graphics, mouseX, mouseY);
        this.renderUsageHintTooltip(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, 342, 219, 342, 245);
        graphics.blit(INVENTORY_BG, this.leftPos + 83, this.topPos + 191, 0, 167, 176, 88, 256, 256);
        this.drawCopyButton(graphics);
        this.drawUsageHint(graphics);
        this.drawPalettes(graphics);
        this.drawResearch(graphics, mouseX, mouseY);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.flush();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /** Discovered aspects, sorted by tier (ascending) then tag (alphabetical). */
    private List<Aspect> palette() {
        List<Aspect> aspects = new ArrayList<>(Aspect.ordered().stream()
                .filter(this.menu::discovered)
                .toList());
        aspects.sort(Comparator.comparingInt(GTResearchTableScreen::tier)
                .thenComparing(Aspect::tag));
        return aspects;
    }

    /** Derivation depth: primals are tier 0, a compound is one above its deepest component. */
    private static int tier(Aspect aspect) {
        if (aspect.isPrimal()) {
            return 0;
        }
        int max = 0;
        for (Aspect component : aspect.components()) {
            max = Math.max(max, tier(component));
        }
        return max + 1;
    }

    private void drawPalettes(GuiGraphics graphics) {
        List<Aspect> all = this.palette();
        int half = (all.size() + 1) / 2;
        this.leftScroll = clampScroll(this.leftScroll, all.subList(0, half).size());
        this.rightScroll = clampScroll(this.rightScroll, all.subList(half, all.size()).size());
        this.drawPalette(graphics, all.subList(0, half), PALETTE_LEFT_X, this.leftScroll);
        this.drawPalette(graphics, all.subList(half, all.size()), PALETTE_RIGHT_X, this.rightScroll);
    }

    private static boolean isCosmicIcon(Aspect aspect) {
        return GTAspectEntry.isCosmic(aspect.tag());
    }

    /**
     * Draws an aspect icon at 16x16. The {@code cosmic} aspect is rendered through
     * its cosmic proxy item (renderblender {@code halo_cosmic} model) instead of
     * the flat aspect texture.
     */
    private static void drawAspectIcon(GuiGraphics graphics, Aspect aspect, int x, int y, float alpha) {
        if (isCosmicIcon(aspect)) {
            graphics.renderItem(GTThaumcraftAdditionalItems.cosmicIconStack(), x, y);
        } else {
            AspectGuiRenderer.draw(graphics, aspect, x, y, 16, alpha);
        }
    }

    private static int clampScroll(int scroll, int size) {
        int maxScroll = Math.max(0, (size + PALETTE_COLS - 1) / PALETTE_COLS - PALETTE_ROWS);
        return Math.max(0, Math.min(scroll, maxScroll));
    }

    private void drawPalette(GuiGraphics graphics, List<Aspect> aspects, int originX, int scroll) {
        int rows = (aspects.size() + PALETTE_COLS - 1) / PALETTE_COLS;
        int firstRow = Math.min(scroll, Math.max(0, rows - PALETTE_ROWS));
        int lastRow = Math.min(rows, firstRow + PALETTE_ROWS);
        for (int row = firstRow; row < lastRow; ++row) {
            for (int col = 0; col < PALETTE_COLS; ++col) {
                int index = row * PALETTE_COLS + col;
                if (index >= aspects.size()) {
                    break;
                }
                Aspect aspect = aspects.get(index);
                int x = this.leftPos + originX + col * PALETTE_CELL;
                int y = this.topPos + PALETTE_Y + (row - firstRow) * PALETTE_CELL;
                int available = this.menu.pool(aspect) + this.menu.bonus(aspect);
                blitTinted(graphics, ASPECT_BG, x - 2, y - 2, 20, 20,
                        0.0f, 0.0f, 32, 32, 32, 32, 1.0f, 1.0f, 1.0f, 1.0f, false);
                drawAspectIcon(graphics, aspect, x, y, available > 0 ? 1.0f : 0.33f);
                AspectGuiRenderer.drawCount(graphics, this.font, available, x, y, available > 0 ? -1 : 0x66FFFFFF);
                this.drawBonusSparkle(graphics, x, y, this.menu.bonus(aspect));
            }
        }
    }

    private void drawCopyButton(GuiGraphics graphics) {
        ResearchNoteData note = ResearchNoteData.read(this.menu.getSlot(1).getItem());
        boolean active = this.menu.canDuplicateResearch() && note != null && note.complete();
        float u = active ? 26.0f : 1.0f;
        graphics.blit(BACKGROUND, this.leftPos + COPY_X, this.topPos + COPY_Y, u, 220.0f,
                BUTTON_SIZE, BUTTON_SIZE, 342, 245);
    }

    private void drawUsageHint(GuiGraphics graphics) {
        graphics.blit(BACKGROUND, this.leftPos + HINT_X, this.topPos + HINT_Y, 51.0f, 220.0f,
                BUTTON_SIZE, BUTTON_SIZE, 342, 245);
    }

    private void drawResearch(GuiGraphics graphics, int mouseX, int mouseY) {
        ResearchNoteData note = ResearchNoteData.read(this.menu.getSlot(1).getItem());
        if (note == null) {
            return;
        }
        graphics.blit(PARCHMENT, this.leftPos + 96, this.topPos + 35, 0, 0, 150, 150, 256, 256);

        Set<HexUtils.Hex> linked = new HashSet<>();
        for (ResearchNoteData.HexEntry hexEntry : note.entries().values()) {
            if (hexEntry.aspect() == null || !this.menu.discovered(hexEntry.aspect())) {
                continue;
            }
            for (int direction = 0; direction < 3; ++direction) {
                ResearchNoteData.HexEntry other = note.entry(hexEntry.hex().neighbour(direction));
                if (other == null || other.aspect() == null
                        || !this.menu.discovered(other.aspect())
                        || !ResearchNoteData.related(hexEntry.aspect(), other.aspect())) {
                    continue;
                }
                linked.add(hexEntry.hex());
                linked.add(other.hex());
                HexUtils.Pixel from = hexEntry.hex().pixel(HEX_SIZE);
                HexUtils.Pixel to = other.hex().pixel(HEX_SIZE);
                this.drawLine(graphics,
                        (double) (this.leftPos + GRID_X) + from.x(),
                        (double) (this.topPos + GRID_Y) + from.y(),
                        (double) (this.leftPos + GRID_X) + to.x(),
                        (double) (this.topPos + GRID_Y) + to.y(),
                        hexEntry.aspect().color(), other.aspect().color());
            }
        }

        HexUtils.Hex hovered = this.mouseHex(mouseX, mouseY);
        for (ResearchNoteData.HexEntry entry : note.entries().values()) {
            HexUtils.Pixel pixel = entry.hex().pixel(HEX_SIZE);
            int x = this.leftPos + GRID_X + (int) Math.round(pixel.x()) - 8;
            int y = this.topPos + GRID_Y + (int) Math.round(pixel.y()) - 8;
            if (!note.complete() && entry.type() != 1) {
                if (entry.hex().equals(hovered)) {
                    blitTinted(graphics, HEX_HIGHLIGHT, x, y, 16, 16,
                            0.0f, 0.0f, 32, 32, 32, 32, 1.0f, 1.0f, 1.0f, 1.0f, true);
                }
                blitTinted(graphics, HEX, x, y, 16, 16,
                        0.0f, 0.0f, 32, 32, 32, 32, 1.0f, 1.0f, 1.0f, 0.25f, false);
            } else if (!note.complete()) {
                this.drawEndpointOrb(graphics, x, y);
            }
            if (entry.aspect() != null && !this.menu.discovered(entry.aspect())) {
                blitTinted(graphics, UNKNOWN, x, y, 16, 16,
                        0.0f, 0.0f, 32, 32, 32, 32, 0.0f, 0.0f, 0.0f, 0.5f, false);
                continue;
            }
            if (entry.aspect() == null) {
                continue;
            }
            if (entry.type() == 2 && !linked.contains(entry.hex())) {
                if (isCosmicIcon(entry.aspect())) {
                    drawAspectIcon(graphics, entry.aspect(), x, y, 1.0f);
                } else {
                    AspectGuiRenderer.drawTinted(graphics, entry.aspect(), x, y, 16, 0x888888, 0.72f);
                }
                continue;
            }
            drawAspectIcon(graphics, entry.aspect(), x, y, 1.0f);
        }
        if (note.complete()) {
            Component complete = Component.translatable("message.thaumcraft.research_note_complete");
            graphics.drawCenteredString(this.font, complete, this.leftPos + GRID_X, this.topPos + 145, -9421167);
        }
    }

    private void drawLine(GuiGraphics graphics, double x1, double y1, double x2, double y2, int startColor, int endColor) {
        int ticks = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.tickCount : 0;
        float pulse = 0.78f + (float) Math.sin((ticks + x1 + y1) * 0.12) * 0.12f;
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        drawGradientQuad(graphics, x1, y1, x2, y2, 2.6f, startColor, endColor, 0.22f + pulse * 0.12f);
        RenderSystem.defaultBlendFunc();
        drawGradientQuad(graphics, x1, y1, x2, y2, 1.15f, startColor, endColor, pulse);
        RenderSystem.defaultBlendFunc();
    }

    private static void drawGradientQuad(GuiGraphics graphics, double x1, double y1, double x2, double y2,
                                         float width, int startColor, int endColor, float alpha) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 0.001) {
            return;
        }
        float offsetX = (float) (-dy / length * width * 0.5);
        float offsetY = (float) (dx / length * width * 0.5);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        addGradientVertex(builder, matrix, (float) x1 - offsetX, (float) y1 - offsetY, startColor, alpha);
        addGradientVertex(builder, matrix, (float) x1 + offsetX, (float) y1 + offsetY, startColor, alpha);
        addGradientVertex(builder, matrix, (float) x2 + offsetX, (float) y2 + offsetY, endColor, alpha);
        addGradientVertex(builder, matrix, (float) x2 - offsetX, (float) y2 - offsetY, endColor, alpha);
        MeshData mesh = builder.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }
    }

    private static void addGradientVertex(BufferBuilder builder, Matrix4f matrix, float x, float y, int color, float alpha) {
        builder.addVertex(matrix, x, y, 0.0f)
                .setColor((float) (color >> 16 & 0xFF) / 255.0f,
                        (float) (color >> 8 & 0xFF) / 255.0f,
                        (float) (color & 0xFF) / 255.0f,
                        Math.max(0.0f, Math.min(1.0f, alpha)));
    }

    private void drawBonusSparkle(GuiGraphics graphics, int x, int y, int bonus) {
        if (bonus <= 0) {
            return;
        }
        int ticks = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.tickCount : 0;
        int sourceX = Math.floorMod(ticks, 16) * 16;
        blitTinted(graphics, PARTICLES, x - 4, y - 4, 16, 16,
                sourceX, 80.0f, 16, 16, 256, 256, 1.0f, 1.0f, 1.0f, 1.0f, true);
        if (bonus > 1) {
            graphics.pose().pushPose();
            graphics.pose().translate(x - 1, y - 1, 210.0f);
            graphics.pose().scale(0.5f, 0.5f, 1.0f);
            graphics.drawString(this.font, Integer.toString(bonus), 0, 0, -1, true);
            graphics.pose().popPose();
        }
    }

    private void drawEndpointOrb(GuiGraphics graphics, int x, int y) {
        int ticks = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.tickCount : 0;
        int frame = Math.floorMod(ticks, 8);
        float red = 0.85f + (float) Math.sin((float) (ticks + x) / 10.0f) * 0.15f;
        float green = 0.85f + (float) Math.sin((float) (ticks + x + y) / 11.0f) * 0.15f;
        float blue = 0.85f + (float) Math.sin((float) (ticks + y) / 12.0f) * 0.15f;
        int sourceX = Math.floorMod(128 + frame * 32, 256);
        blitTinted(graphics, PARTICLES, x, y, 16, 16,
                sourceX, 128.0f, 16, 16, 256, 256, red, green, blue, 1.0f, true);
    }

    private static void blitTinted(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                   float u, float v, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight,
                                   float red, float green, float blue, float alpha, boolean additive) {
        graphics.flush();
        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Matrix4f matrix = graphics.pose().last().pose();
        float minU = u / (float) textureWidth;
        float maxU = (u + (float) sourceWidth) / (float) textureWidth;
        float minV = v / (float) textureHeight;
        float maxV = (v + (float) sourceHeight) / (float) textureHeight;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix, x, y, 0.0f).setUv(minU, minV).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, x, (float) (y + height), 0.0f).setUv(minU, maxV).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, (float) (x + width), (float) (y + height), 0.0f).setUv(maxU, maxV).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, (float) (x + width), y, 0.0f).setUv(maxU, minV).setColor(red, green, blue, alpha);
        MeshData mesh = builder.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }
        if (additive) {
            RenderSystem.defaultBlendFunc();
        }
    }

    private HexUtils.Hex mouseHex(int mouseX, int mouseY) {
        return HexUtils.pixelToHex(mouseX - (this.leftPos + GRID_X), mouseY - (this.topPos + GRID_Y), HEX_SIZE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            this.draggedAspect = null;
            return true;
        }
        if (button == 0) {
            int localX = (int) mouseX - this.leftPos;
            int localY = (int) mouseY - this.topPos;

            ResearchNoteData note = ResearchNoteData.read(this.menu.getSlot(1).getItem());
            if (this.menu.canDuplicateResearch() && note != null && note.complete()
                    && inside(localX, localY, COPY_X, COPY_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                this.sendButton(ResearchTableMenu.BUTTON_DUPLICATE);
                this.play(CAMERA_CLACK, 0.4f);
                return true;
            }

            Aspect clicked = this.paletteAt(localX, localY);
            if (clicked != null) {
                if (hasShiftDown() && !clicked.isPrimal() && clicked.components().size() == 2) {
                    this.combine(clicked.components().get(0), clicked.components().get(1), hasControlDown(), true);
                    return true;
                }
                if (this.menu.pool(clicked) + this.menu.bonus(clicked) <= 0) {
                    return true;
                }
                this.draggedAspect = clicked;
                this.play(ASPECT_SOUND, 0.2f);
                return true;
            }

            if (note != null && !note.complete()) {
                HexUtils.Hex hex = this.mouseHex((int) mouseX, (int) mouseY);
                ResearchNoteData.HexEntry entry = note.entry(hex);
                if (entry != null && entry.type() == 2) {
                    this.sendButton(ResearchTableMenu.encode(hex.q(), hex.r(), null));
                    this.play(COMBINE_SOUND, 0.3f);
                    this.play(ERASE_SOUND, 0.2f);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggedAspect != null) {
            Aspect dropped = this.draggedAspect;
            this.draggedAspect = null;

            ResearchNoteData note = ResearchNoteData.read(this.menu.getSlot(1).getItem());
            if (note != null && !note.complete()) {
                HexUtils.Hex hex = this.mouseHex((int) mouseX, (int) mouseY);
                ResearchNoteData.HexEntry entry = note.entry(hex);
                if (entry != null && entry.type() == 0) {
                    this.sendButton(ResearchTableMenu.encode(hex.q(), hex.r(), dropped));
                    this.play(COMBINE_SOUND, 0.3f);
                    this.play(WRITE_SOUND, 0.2f);
                    return true;
                }
            }

            int localX = (int) mouseX - this.leftPos;
            int localY = (int) mouseY - this.topPos;
            Aspect target = this.paletteAt(localX, localY);
            if (target != null && target != dropped) {
                this.combine(dropped, target, hasControlDown());
                return true;
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int localX = (int) mouseX - this.leftPos;
        int localY = (int) mouseY - this.topPos;
        int paletteWidth = PALETTE_COLS * PALETTE_CELL;
        int paletteHeight = PALETTE_ROWS * PALETTE_CELL;
        boolean overLeft = inside(localX, localY, PALETTE_LEFT_X, PALETTE_Y, paletteWidth, paletteHeight);
        boolean overRight = inside(localX, localY, PALETTE_RIGHT_X, PALETTE_Y, paletteWidth, paletteHeight);
        if (!overLeft && !overRight) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        List<Aspect> all = this.palette();
        int half = (all.size() + 1) / 2;
        int step = (int) Math.signum(verticalAmount);
        int previous;
        if (overLeft) {
            previous = this.leftScroll;
            this.leftScroll = clampScroll(this.leftScroll - step, all.subList(0, half).size());
        } else {
            previous = this.rightScroll;
            this.rightScroll = clampScroll(this.rightScroll - step, all.subList(half, all.size()).size());
        }
        return previous != (overLeft ? this.leftScroll : this.rightScroll);
    }

    /**
     * Sends one (or, when {@code batch} is set, up to {@link #BATCH_LIMIT})
     * combination clicks for {@code first + second}, bounded by the synced pool.
     */
    private void combine(Aspect first, Aspect second, boolean batch) {
        this.combine(first, second, batch, false);
    }

    private void combine(Aspect first, Aspect second, boolean batch, boolean quick) {
        int count = 1;
        if (batch) {
            int a = this.menu.pool(first) + this.menu.bonus(first);
            int b = this.menu.pool(second) + this.menu.bonus(second);
            int max = first == second ? Math.min(a, b) / 2 : Math.min(a, b);
            count = Math.max(1, Math.min(BATCH_LIMIT, max));
        }
        for (int i = 0; i < count; ++i) {
            int id = quick
                    ? ResearchTableMenu.encodeQuickCombination(first, second)
                    : ResearchTableMenu.encodeCombination(first, second);
            this.sendButton(id);
        }
        this.play(CAMERA_CLACK, 0.4f);
        this.play(COMBINE_SOUND, 0.3f);
    }

    private void sendButton(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    @Nullable
    private Aspect paletteAt(int localX, int localY) {
        List<Aspect> all = this.palette();
        int half = (all.size() + 1) / 2;
        Aspect left = this.paletteAt(localX, localY, all.subList(0, half), PALETTE_LEFT_X, this.leftScroll);
        return left != null ? left : this.paletteAt(localX, localY, all.subList(half, all.size()), PALETTE_RIGHT_X, this.rightScroll);
    }

    @Nullable
    private Aspect paletteAt(int localX, int localY, List<Aspect> aspects, int originX, int scroll) {
        if (localX < originX || localY < PALETTE_Y) {
            return null;
        }
        int col = (localX - originX) / PALETTE_CELL;
        int row = (localY - PALETTE_Y) / PALETTE_CELL + scroll;
        if (col < 0 || col >= PALETTE_COLS || row < 0) {
            return null;
        }
        int index = row * PALETTE_COLS + col;
        return index >= 0 && index < aspects.size() ? aspects.get(index) : null;
    }

    private void renderPaletteTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        Aspect aspect = this.paletteAt(mouseX - this.leftPos, mouseY - this.topPos);
        if (aspect == null) {
            return;
        }
        List<Either<FormattedText, TooltipComponent>> elements = new ArrayList<>();
        elements.add(Either.left(aspect.displayName()));
        elements.add(Either.left(Component.literal(Integer.toString(this.menu.pool(aspect) + this.menu.bonus(aspect)))));
        if (this.menu.researcherOne() && !aspect.isPrimal() && aspect.components().size() == 2) {
            List<Aspect> components = aspect.components();
            elements.add(Either.right(new GTAspectRecipeTooltip(components.get(0), components.get(1))));
        }
        graphics.renderComponentTooltipFromElements(this.font, elements, mouseX, mouseY, ItemStack.EMPTY);
    }

    private void renderDuplicateTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        ResearchNoteData note = ResearchNoteData.read(this.menu.getSlot(1).getItem());
        if (!this.menu.canDuplicateResearch() || note == null || !note.complete()
                || !inside(mouseX, mouseY, this.leftPos + COPY_X, this.topPos + COPY_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.thaumcraft.research.copy"));
        lines.add(Component.translatable("gui.thaumcraft.research.copy_materials"));
        AspectList cost = this.menu.duplicateCost();
        for (Aspect aspect : cost.sortedByAmountDescending()) {
            lines.add(Component.literal(aspect.displayName().getString() + ": " + cost.amount(aspect)));
        }
        graphics.renderTooltip(this.font, lines, Optional.empty(), mouseX, mouseY);
    }

    private void renderUsageHintTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!inside(mouseX, mouseY, this.leftPos + HINT_X, this.topPos + HINT_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("goldentweaks.researchtable.usagehint.header"));
        lines.add(Component.translatable("goldentweaks.researchtable.usagehint.description"));
        if (this.menu.researcherOne()) {
            lines.add(Component.translatable("goldentweaks.researchtable.usagehint.research_expertise"));
        }
        graphics.renderTooltip(this.font, lines, Optional.empty(), mouseX, mouseY);
    }

    private void play(SoundEvent sound, float volume) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(sound, volume, 1.0f);
        }
    }

    private static SoundEvent sound(String path) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("thaumcraft", path));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
