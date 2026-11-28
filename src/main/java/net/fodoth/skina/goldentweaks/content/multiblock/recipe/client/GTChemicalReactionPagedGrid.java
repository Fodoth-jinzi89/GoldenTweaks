package net.fodoth.skina.goldentweaks.content.multiblock.recipe.client;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.runtime.EmiDrawContext;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionStack;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortAmounts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * A paged 3 by 3 slot grid with its own scrollbar.
 *
 * <p>EMI has no scroll widget of its own — its only scrollable grid lives in the JEI compatibility
 * layer and is not a public API — so this widget pages its own contents.</p>
 *
 * <p><b>The slots belong to EMI, not to this widget.</b> Drawing a {@code SlotWidget} by hand looks
 * identical but is inert: EMI resolves hover, tooltips and R/U only through widgets it owns, so a
 * hand-drawn slot never shows an item tooltip and never answers a recipe lookup. The page therefore
 * registers a fixed block of slots once and only changes what each one points at, which keeps the
 * widget list stable (EMI has no way to remove a widget) while giving every slot full interaction.</p>
 *
 * <p>The scrollbar is deliberately plain rectangles in the panel's own colours rather than a
 * texture: the reference layout only marks where it goes, and a hard-drawn texture would be the one
 * thing a resource pack could not restyle.</p>
 *
 * <p>Wheel and drag are not delivered by EMI to widgets at all, so
 * {@code GTChemicalReactionScrollMixin} forwards those.</p>
 */
public class GTChemicalReactionPagedGrid extends Widget {

    private static final int TRACK_COLOUR = 0xFF373737;
    private static final int TRACK_BORDER_COLOUR = 0xFF8B8B8B;
    private static final int MARKER_COLOUR = 0xFFC6C6C6;
    private static final int MARKER_MIN_HEIGHT = 14;
    private static final int FRAME_FILL_COLOUR = 0xFF8B8B8B;
    private static final int FRAME_BORDER_COLOUR = 0xFF373737;

    private final List<EmiIngredient> stacks;
    /** The data-pack entries behind {@link #stacks}, one for one; they carry the kind and amount. */
    private final List<GTReactionStack> entries;
    private final int x;
    private final int y;
    private final List<PagedSlot> slots = new ArrayList<>();

    private int page;

    public GTChemicalReactionPagedGrid(List<EmiIngredient> stacks, List<GTReactionStack> entries,
                                       int x, int y, WidgetHolder holder) {
        this.stacks = List.copyOf(stacks);
        this.entries = List.copyOf(entries);
        this.x = x;
        this.y = y;

        // The box has to be registered before the slots: EMI renders widgets in insertion order, so
        // adding it afterwards would paint the frame over the items.
        holder.add(new GridFrame());

        for (int index = 0; index < GTChemicalReactionLayout.pageSize(); index++) {
            int column = index % GTChemicalReactionLayout.COLUMNS;
            int row = index / GTChemicalReactionLayout.COLUMNS;
            PagedSlot slot = holder.add(new PagedSlot(
                    this.x + column * GTChemicalReactionLayout.SLOT,
                    this.y + row * GTChemicalReactionLayout.SLOT));
            // No per-item frame: the whole grid sits in one box, as on the aspect-source page.
            slot.drawBack(false);
            this.slots.add(slot);
        }
        applyPage();
    }

    public int page() {
        return this.page;
    }

    public int pageCount() {
        return GTChemicalReactionLayout.pageCount(this.stacks.size());
    }

    /** The whole grid plus its scrollbar, so a hover anywhere on the block is recognised. */
    @Override
    public Bounds getBounds() {
        return new Bounds(this.x, this.y,
                GTChemicalReactionLayout.GRID_WIDTH + 2 + GTChemicalReactionLayout.SCROLLBAR_WIDTH,
                GTChemicalReactionLayout.GRID_HEIGHT);
    }

    /** Scrollbar column only; the scroll mixin uses this so wheel events over the slots work too. */
    public Bounds scrollbarBounds() {
        return new Bounds(scrollbarX(), this.y,
                GTChemicalReactionLayout.SCROLLBAR_WIDTH, GTChemicalReactionLayout.GRID_HEIGHT);
    }

    /**
     * True when every stack fits on one page.
     *
     * <p>Only decides whether the wheel has anything to do; the scrollbar is drawn either way so the
     * layout does not shift between a recipe that fits and one that does not.</p>
     */
    public boolean fits() {
        return this.stacks.size() <= GTChemicalReactionLayout.pageSize();
    }

    /**
     * Moves by whole pages, clamped.
     *
     * <p>The caller accumulates fractional wheel movement, so this only ever sees integers.</p>
     */
    public boolean scrollPages(int delta) {
        int target = Math.max(0, Math.min(pageCount() - 1, this.page + delta));
        if (target == this.page) {
            return false;
        }
        this.page = target;
        applyPage();
        return true;
    }

    /** Jumps to the page under the given widget-local Y, or does nothing outside the track. */
    public boolean scrollTo(double localX, double localY) {
        if (fits() || !scrollbarBounds().contains((int) localX, (int) localY)) {
            return false;
        }

        double progress = (localY - this.y) / (double) GTChemicalReactionLayout.GRID_HEIGHT;
        int target = (int) Math.round(progress * (pageCount() - 1));
        target = Math.clamp(target, 0, pageCount() - 1);
        if (target == this.page) {
            return false;
        }
        this.page = target;
        applyPage();
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // The slots draw themselves through EMI; only the scrollbar belongs to this widget.
        // Drawn even when everything fits: a grid that sometimes has a bar and sometimes does not
        // makes the page jump around, so the bar stays put and simply has nothing to scroll.
        renderScrollbar(graphics);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0 || !getBounds().contains(mouseX, mouseY)) {
            return false;
        }
        // Clicking the track jumps; clicking the slots is left to EMI so R/U keeps working there.
        return scrollTo(mouseX, mouseY);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int barX = scrollbarX();
        int barY = this.y;
        int barWidth = GTChemicalReactionLayout.SCROLLBAR_WIDTH;
        int barHeight = GTChemicalReactionLayout.GRID_HEIGHT;

        // One pixel border plus a dark track, mirroring the vanilla scrollbar's proportions.
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, TRACK_BORDER_COLOUR);
        graphics.fill(barX + 1, barY + 1, barX + barWidth - 1, barY + barHeight - 1, TRACK_COLOUR);

        int innerHeight = barHeight - 2;
        // The marker expresses how much of the list is on screen. Comparing against the page size
        // rather than the raw count keeps it full-height for any single page, including a grid with
        // only one or two entries, where the count would otherwise produce a marker shorter than the
        // track and imply more content than there is.
        int total = Math.max(this.stacks.size(), GTChemicalReactionLayout.pageSize());
        int markerHeight = Math.clamp(Math.round(innerHeight * (float) GTChemicalReactionLayout.pageSize()
                / (float) total), MARKER_MIN_HEIGHT, innerHeight);
        int travel = innerHeight - markerHeight;
        int markerY = barY + 1 + (pageCount() <= 1
                ? 0
                : Math.round(travel * (float) this.page / (float) (pageCount() - 1)));

        graphics.fill(barX + 2, markerY, barX + barWidth - 2, markerY + markerHeight, MARKER_COLOUR);
    }

    private int scrollbarX() {
        return this.x + GTChemicalReactionLayout.GRID_WIDTH + 2;
    }

    /** Points every registered slot at the current page's entry, or at nothing past the end. */
    private void applyPage() {
        int start = this.page * GTChemicalReactionLayout.pageSize();
        for (int index = 0; index < this.slots.size(); index++) {
            int stackIndex = start + index;
            boolean present = stackIndex < this.stacks.size();
            this.slots.get(index).setContent(
                    present ? this.stacks.get(stackIndex) : EmiStack.EMPTY,
                    present ? this.entries.get(stackIndex) : null);
        }
    }

    /**
     * The single box behind a whole grid.
     *
     * <p>Drawn as one inset rectangle instead of nine slot frames: with an arbitrary number of
     * stacks, separate frames read as a wall of boxes and make it hard to see how many there
     * actually are. This mirrors the aspect-source page, whose item list is also one large box.</p>
     *
     * <p>It renders first among the grid's widgets, so the items sit on top of it rather than under
     * it.</p>
     */
    private final class GridFrame extends Widget {

        private GridFrame() {
        }

        @Override
        public Bounds getBounds() {
            return new Bounds(GTChemicalReactionPagedGrid.this.x, GTChemicalReactionPagedGrid.this.y,
                    GTChemicalReactionLayout.GRID_WIDTH, GTChemicalReactionLayout.GRID_HEIGHT);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int left = GTChemicalReactionPagedGrid.this.x;
            int top = GTChemicalReactionPagedGrid.this.y;
            int right = left + GTChemicalReactionLayout.GRID_WIDTH;
            int bottom = top + GTChemicalReactionLayout.GRID_HEIGHT;

            // Dark well plus a light border, the same two-tone the vanilla slots use.
            graphics.fill(left, top, right, bottom, FRAME_FILL_COLOUR);
            graphics.fill(left, top, right, top + 1, FRAME_BORDER_COLOUR);
            graphics.fill(left, bottom - 1, right, bottom, FRAME_BORDER_COLOUR);
            graphics.fill(left, top, left + 1, bottom, FRAME_BORDER_COLOUR);
            graphics.fill(right - 1, top, right, bottom, FRAME_BORDER_COLOUR);
        }
    }

    /**
     * A slot whose content changes as the page turns.
     *
     * <p>{@code SlotWidget} reads its ingredient through {@code getStack()} for drawing, tooltips and
     * recipe lookups alike, so overriding that one method keeps every one of those paths correct
     * without touching EMI's internals.</p>
     */
    private static final class PagedSlot extends SlotWidget {

        private EmiIngredient content = EmiStack.EMPTY;

        /** The data-pack entry this cell shows, or {@code null} past the end of the list. */
        private GTReactionStack entry;

        private PagedSlot(int x, int y) {
            super(EmiStack.EMPTY, x, y);
        }

        private void setContent(EmiIngredient content, @Nullable GTReactionStack entry) {
            this.content = content;
            this.entry = entry;
        }

        @Override
        public EmiIngredient getStack() {
            return this.content;
        }

        /**
         * Adds the amount label for every kind EMI does not label itself.
         *
         * <p>EMI draws the count as part of the stack, but only its item flavour does so:
         * {@code ItemEmiStack.render} prints it while the fluid, chemical and AE flavours draw an
         * icon and nothing else. Essentia is a stack of this mod's own and prints nothing either.</p>
         *
         * <p>The text comes from the shared port formatter, which delegates to AE2's own amount
         * format when AE2 is present, so the badge matches the machine's port window for the same
         * quantity. Essentia and items are counted in whole units and get the item shape; the bulk
         * kinds get the fluid shape, which appends the unit.</p>
         */
        @Override
        public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.drawOverlay(graphics, mouseX, mouseY, delta);

            GTReactionStack stack = this.entry;
            if (stack == null || stack.kind() == GTReactionStack.Kind.ITEM) {
                // Items are already labelled by EMI, and drawing a second label would double it.
                return;
            }

            long amount = stack.amount();
            // A single unit prints no label, exactly as an item stack of one behaves.
            if (amount <= 1) {
                return;
            }

            boolean counted = stack.kind() == GTReactionStack.Kind.ESSENTIA;
            EmiRenderHelper.renderAmount(
                    EmiDrawContext.wrap(graphics),
                    getBounds().x(),
                    getBounds().y(),
                    Component.literal(GTPortAmounts.number(amount, counted)));
        }
    }
}
