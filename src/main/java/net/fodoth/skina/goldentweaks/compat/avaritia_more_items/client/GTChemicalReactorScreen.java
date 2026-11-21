package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMenu;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTEnergyFormat;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTRecipeLocks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionProducts;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStacks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactorStatus;
import net.fodoth.skina.goldentweaks.network.packet.C2SMachineNamePacket;
import net.fodoth.skina.goldentweaks.network.packet.C2SMachineOverclockPacket;
import net.fodoth.skina.goldentweaks.network.packet.C2SMachineParallelPacket;
import net.fodoth.skina.goldentweaks.util.GTDurationFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Screen of the basic chemical reaction kettle.
 *
 * <p>Every widget is drawn from the single {@code gt_multiblock_machine} sheet: the panel occupies
 * {@code (0, 0)..(210, 178)} and the sprites sit underneath it. Sprites keep transparent padding so
 * that their cells line up with the widgets they belong to, which is why the icons are always
 * blitted at their full cell size rather than at their visible pixel bounds.</p>
 *
 * <p>Two independent pieces of state drive the look of a button. The cell background follows the
 * mouse being held down, and the icon drawn on top follows the machine state the button toggles
 * (or, for the refresh and dismantle buttons, their own momentary or armed state).</p>
 */
public class GTChemicalReactorScreen extends AbstractContainerScreen<GTChemicalReactorMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "textures/gui/gt_multiblock_machine.png");

    private static final int PANEL_WIDTH = 210;
    private static final int PANEL_HEIGHT = 178;

    private static final int TITLE_X = 15;
    private static final int TITLE_Y = 16;
    private static final int TITLE_WIDTH = 179;
    private static final int TITLE_HEIGHT = 9;

    /** Gap left between the end of a scrolling text and its next copy. */
    private static final int MARQUEE_GAP = 24;
    /** Scrolling speed; slow enough to read at 179 px of width. */
    private static final double MARQUEE_PIXELS_PER_SECOND = 28.0D;
    /** Rest before each lap, so the beginning of the text is readable every time it comes round. */
    private static final long MARQUEE_PAUSE_MILLIS = 1200L;

    /** The status line sits directly under the title, on the same left margin. */
    private static final int STATUS_Y = 27;
    private static final int STATUS_HEIGHT = 9;

    /**
     * The output list occupies the free band between the status line and the player inventory.
     *
     * <p>Its width stops short of the energy trough, so the two never overlap.</p>
     */
    private static final int LIST_X = 15;
    private static final int LIST_Y = 38;
    /**
     * The output band is wide and short: 140% of the old width at two thirds of its height.
     *
     * <p>The extra width reaches past where the energy trough starts, which is fine because the
     * trough sits well below it — the two no longer share a row once the band is this shallow.</p>
     */
    private static final int LIST_WIDTH = 178;
    private static final int LIST_HEIGHT = 32;
    private static final int ROW_HEIGHT = 16;
    private static final int SLOT_SIZE = 16;
    /** Secondary lines are drawn smaller so two of them fit beside a 16 px slot. */
    private static final float ROW_TEXT_SCALE = 0.7F;
    private static final int NAME_COLOUR = 0xFFAA00;
    private static final int RATE_COLOUR = 0xAAAAAA;

    private static final int ENERGY_X = 141;
    private static final int ENERGY_Y = 80;
    /** Full width of the trough, as laid out in the sheet: 4 + 19 cells of 3 px. */
    private static final int ENERGY_WIDTH = 61;
    private static final int ENERGY_HEIGHT = 11;
    private static final int ENERGY_SPRITE_U = 80;
    private static final int ENERGY_SPRITE_V = 178;
    private static final int ENERGY_FIRST_CELL_WIDTH = 4;
    private static final int ENERGY_CELL_STEP = 3;

    private static final int ADDON_X = 8;
    private static final int ADDON_Y = 79;
    private static final int ADDON_WIDTH = 18;
    private static final int ADDON_HEIGHT = 13;
    private static final int ADDON_STEP = 18;
    private static final int ADDON_COUNT = 4;
    private static final int ADDON_SPRITE_U = 44;
    private static final int ADDON_SPRITE_PRESSED_U = 62;
    private static final int ADDON_SPRITE_V = 178;
    private static final int ADDON_ICON_V_LOCK = 191;
    private static final int ADDON_ICON_V_DESTROY = 204;
    private static final int ADDON_ICON_V_BATCH = 217;
    /**
     * The isolate icons sit on the sprite row directly below the batch pair.
     *
     * <p>Each icon is a 13 px tall cell and the rows start 13 apart (191, 204, 217, 230), so the
     * next cell starts at 230 even though its artwork only occupies the middle of that cell. Using
     * the artwork's own top row instead would draw the icon flush against the button's upper edge
     * while every other icon sits centred.</p>
     */
    private static final int ADDON_ICON_V_ISOLATE = 230;
    /** The two states reuse the same two sprite columns as the other buttons. */
    private static final int ADDON_ICON_U_ISOLATE_OFF = ADDON_SPRITE_U;
    private static final int ADDON_ICON_U_ISOLATE_ON = ADDON_SPRITE_PRESSED_U;

    private static final int SIDE_X = 177;
    private static final int SIDE_WIDTH = 22;
    private static final int SIDE_HEIGHT = 11;
    private static final int SIDE_STEP = 14;
    private static final int SIDE_REFRESH_Y = 95;
    private static final int SIDE_RUNNING_Y = 109;
    private static final int SIDE_DISMANTLE_Y = 123;
    private static final int SIDE_SPRITE_U = 0;
    private static final int SIDE_SPRITE_PRESSED_U = 22;
    private static final int SIDE_SPRITE_V = 178;
    private static final int SIDE_ICON_V_REFRESH = 189;
    private static final int SIDE_ICON_V_RUNNING = 200;
    private static final int SIDE_ICON_V_DISMANTLE = 211;

    /**
     * The side pane, drawn outside the machine panel at its top right.
     *
     * <p>One sprite holds both states: the collapsed tab is the top left 22 by 26, and the expanded
     * panel is the 62 by 41 block directly below it. Expanding therefore starts by revealing more of
     * the same artwork rather than swapping pictures, which is also how the reference panel behaves —
     * the collapsed tab and the expanded panel's corner line up exactly.</p>
     */
    private static final ResourceLocation SIDEBAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "textures/gui/cc_1.png");
    /** Origin: flush with the panel's right edge, at its top. Never overlaps the panel itself. */
    private static final int SIDEBAR_X = PANEL_WIDTH;
    private static final int SIDEBAR_Y = 0;
    private static final int SIDEBAR_MIN_WIDTH = 22;
    private static final int SIDEBAR_MIN_HEIGHT = 26;
    private static final int SIDEBAR_MAX_WIDTH = 62;
    private static final int SIDEBAR_MAX_HEIGHT = 41;
    /** Where the expanded artwork sits in the sheet. */
    private static final int SIDEBAR_EXPANDED_V = 26;
    /** Pixels per frame, matching the reference panel's own expansion speed. */
    private static final int SIDEBAR_EXPAND_SPEED = 8;
    /**
     * Where the control circuit's icon goes inside the pane.
     *
     * <p>The current artwork draws no slot of its own, so these are the places the frame leaves
     * for it: the collapsed tab keeps its icon in the upper part of the small square, and the
     * expanded panel in the wide band above its title bar. Each state gets its own offset because
     * they are not at the same distance from the pane's corner, and the artwork is revealed from
     * that corner as the pane opens.</p>
     */
    private static final int SIDEBAR_SLOT_COLLAPSED_X = 1;
    private static final int SIDEBAR_SLOT_COLLAPSED_Y = 5;
    private static final int SIDEBAR_SLOT_EXPANDED_X = 4;
    private static final int SIDEBAR_SLOT_EXPANDED_Y = 6;
    private static final int SIDEBAR_SLOT_SIZE = 16;

    /**
     * The readout beside the icon, right of it and left-aligned.
     *
     * <p>Text starts two pixels past the icon's right edge so the two do not touch, and the first
     * line is nudged down to sit level with the icon's upper half rather than its very top.</p>
     */
    private static final int SIDEBAR_TEXT_X = SIDEBAR_SLOT_EXPANDED_X + SIDEBAR_SLOT_SIZE + 2;
    private static final int SIDEBAR_TEXT_Y = SIDEBAR_SLOT_EXPANDED_Y + 1;
    /** The two lines stop above the artwork's input band, which starts at {@link #SIDEBAR_FIELD_Y}. */
    /** Line pitch at {@link #SIDEBAR_TEXT_SCALE}: the font is nine units tall, so ten clears it. */
    private static final int SIDEBAR_TEXT_LINE_HEIGHT = 10;
    /** Smaller than a list row: the pane is a side note and this has to fit beside the icon. */
    private static final float SIDEBAR_TEXT_SCALE = 0.65F;
    /** Dark grey, matching the panel's own captions. */
    private static final int SIDEBAR_TEXT_COLOUR = 0x404040;
    /** The circuit this pane describes, as written on the item. */
    private static final String SIDEBAR_OVERCLOCK_RATIO = "4/2";

    /** Vertical gap between the two panes' collapsed tabs. */
    private static final int SIDEBAR_STACK_STEP = SIDEBAR_MIN_HEIGHT + 2;
    /** The parallel pane is nudged up by this much, so the two tabs read as one column. */
    private static final int PARALLEL_PANE_OFFSET_Y = -2;

    /**
     * The ceiling's input box, drawn by the artwork on its third band.
     *
     * <p>The black bar in the sprite is the field itself, so nothing here draws a frame: the box is
     * placed inside it and left unbordered, exactly like the port window's own fields.</p>
     */
    private static final int SIDEBAR_FIELD_X = 5;
    private static final int SIDEBAR_FIELD_Y = 25;
    /** Exactly the artwork's black band: x 5..54, y 51..59 of the expanded block. */
    private static final int SIDEBAR_FIELD_WIDTH = 50;
    private static final int SIDEBAR_FIELD_HEIGHT = 9;
    private static final int SIDEBAR_FIELD_TEXT_COLOUR = 0xFFFFFF;
    /** Matches the port window: dimmer while the box is not the one being edited. */
    private static final int SIDEBAR_FIELD_INACTIVE_COLOUR = 0x8A8A8A;
    /** Longest accepted text; an integer never needs more than this. */
    private static final int SIDEBAR_FIELD_LIMIT = 11;

    private static final int WIDGET_LOCK = 0;
    private static final int WIDGET_DESTROY = 1;
    private static final int WIDGET_BATCH = 2;
    private static final int WIDGET_ISOLATE = 3;
    private static final int WIDGET_REFRESH = 4;
    private static final int WIDGET_RUNNING = 5;
    private static final int WIDGET_DISMANTLE = 6;
    private static final int WIDGET_COUNT = 7;

    /** The renaming box, drawn over the title strip. */
    private EditBox nameField;

    private final boolean[] held = new boolean[WIDGET_COUNT];

    /** The dismantle button needs two clicks; the first one only arms the icon. */
    private boolean dismantleArmed;

    /**
     * One side pane: its own reveal state, its own box.
     *
     * <p>The two panes share their artwork and their layout, so they share the drawing code; only
     * the icon, the two lines and the number they edit differ. Keeping the animated size per pane is
     * what lets them be opened one at a time, which is how the reference behaves too.</p>
     */
    private static final class Pane {
        boolean open;
        boolean fullyOpen;
        int width = SIDEBAR_MIN_WIDTH;
        int height = SIDEBAR_MIN_HEIGHT;
        @Nullable
        EditBox field;
        /** Value sent to the server and not yet read back. */
        @Nullable
        Integer pending;

        void step() {
            if (this.open) {
                this.width = Math.min(SIDEBAR_MAX_WIDTH, this.width + SIDEBAR_EXPAND_SPEED);
                this.height = Math.min(SIDEBAR_MAX_HEIGHT, this.height + SIDEBAR_EXPAND_SPEED);
            } else {
                this.width = Math.max(SIDEBAR_MIN_WIDTH, this.width - SIDEBAR_EXPAND_SPEED);
                this.height = Math.max(SIDEBAR_MIN_HEIGHT, this.height - SIDEBAR_EXPAND_SPEED);
            }
            this.fullyOpen = this.open
                    && this.width == SIDEBAR_MAX_WIDTH
                    && this.height == SIDEBAR_MAX_HEIGHT;
        }

        boolean collapsed() {
            return this.width == SIDEBAR_MIN_WIDTH && this.height == SIDEBAR_MIN_HEIGHT;
        }
    }

    /** Overclock pane, then parallel pane, top to bottom. */
    private final Pane overclockPane = new Pane();
    private final Pane parallelPane = new Pane();
    private final List<Pane> panes = List.of(this.overclockPane, this.parallelPane);

    /** Scroll target and its eased value, both in rows, for the product list. */
    private double scrollTarget;
    private double scrollCurrent;

    public GTChemicalReactorScreen(GTChemicalReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.nameField = new EditBox(this.font, this.leftPos + TITLE_X, this.topPos + TITLE_Y,
                TITLE_WIDTH, this.font.lineHeight, Component.translatable("gui.goldentweaks.chemical_reactor.name"));
        this.nameField.setMaxLength(C2SMachineNamePacket.NAME_LIMIT);
        this.nameField.setBordered(false);
        this.nameField.setTextColor(0xFFFFFF);
        this.nameField.setTextColorUneditable(0x8A8A8A);
        this.nameField.setValue(this.menu.getDisplayName().getString());
        addRenderableWidget(this.nameField);

        this.overclockPane.field = makePaneField(
                "gui.goldentweaks.chemical_reactor.sidebar.max",
                this.menu.getOverclockLimit());
        this.parallelPane.field = makePaneField(
                "gui.goldentweaks.chemical_reactor.sidebar.parallel.max",
                this.menu.getParallelLimit());
    }

    /**
     * Keeps the ceiling box showing what the machine will end up storing.
     *
     * <p>While a value is in flight the box keeps the sent text, because the machine still reports
     * the previous one for a few ticks and echoing it back is what made the box flash. Once the
     * machine agrees the pending flag clears and normal echoing resumes.</p>
     */
    private void syncOverclockEcho() {
        echoPane(this.overclockPane, this.menu.getOverclockLimit());
        echoPane(this.parallelPane, this.menu.getParallelLimit());
    }

    /** Writes a pane's stored number into its box, holding any value still in flight. */
    private void echoPane(Pane pane, int stored) {
        if (pane.field == null || pane.field.isFocused()) {
            return;
        }
        if (pane.pending != null) {
            if (stored != pane.pending) {
                return;
            }
            pane.pending = null;
        }
        // A stored value out of range is shown as the range, not as itself: removing an energy hatch
        // lowers the ceiling, and the box must not keep offering a number the machine would refuse.
        if (pane == this.overclockPane && stored > 0) {
            int ceiling = this.menu.getOverclockCeiling();
            if (stored > ceiling) {
                stored = ceiling;
            }
        }
        String shown = String.valueOf(stored);
        if (!shown.equals(pane.field.getValue())) {
            pane.field.setValue(shown);
        }
    }

    /**
     * Builds a side pane's input box.
     *
     * <p>Unbordered: the artwork draws the bar the box sits in. Two text colours, matching the port
     * window — white while being edited, a dimmer grey otherwise.</p>
     */
    private EditBox makePaneField(String labelKey, int initial) {
        EditBox box = new EditBox(this.font, 0, 0,
                SIDEBAR_FIELD_WIDTH, SIDEBAR_FIELD_HEIGHT,
                Component.translatable(labelKey));
        box.setMaxLength(SIDEBAR_FIELD_LIMIT);
        box.setBordered(false);
        box.setTextColor(SIDEBAR_FIELD_TEXT_COLOUR);
        box.setTextColorUneditable(SIDEBAR_FIELD_INACTIVE_COLOUR);
        box.setValue(String.valueOf(initial));
        // Typing only updates the box; the server hears about it on commit.
        box.setResponder(value -> {
        });
        box.setVisible(false);
        addRenderableWidget(box);
        return box;
    }

    /** Sends the typed name if it differs from what the machine reports. */
    private void commitName() {
        if (this.nameField == null) {
            return;
        }
        String typed = this.nameField.getValue();
        String shown = this.menu.getDisplayName().getString();
        if (!typed.equals(shown)) {
            PacketDistributor.sendToServer(
                    new C2SMachineNamePacket(this.menu.getMachinePos(), typed));
        }
    }

    /**
     * Sends the typed ceiling, if it differs from what the machine reports.
     *
     * <p>Anything that does not parse as an integer is sent as {@code -1}, the unlimited value.
     * That is the field's own default, so a garbled entry leaves the machine as it was rather than
     * with a ceiling nobody meant.</p>
     */
    private void commitOverclock() {
        commitPane(this.overclockPane, this.menu.getOverclockLimit(), -1);
        commitPane(this.parallelPane, this.menu.getParallelLimit(), 0);
    }

    /**
     * Reads one pane's box and sends it.
     *
     * <p>{@code fallback} is the value a garbled entry becomes, and also the floor: anything below
     * it is clamped rather than rejected, so the box always shows what will actually be stored. A
     * value the box and the machine disagree on is exactly what made the control look dead.</p>
     */
    private void commitPane(Pane pane, int stored, int fallback) {
        if (pane.field == null) {
            return;
        }
        int typed;
        try {
            typed = Integer.parseInt(pane.field.getValue().trim());
        } catch (NumberFormatException unparseable) {
            typed = fallback;
        }
        if (typed < fallback) {
            typed = fallback;
        }
        // Capped at what the pane displays, so the box can never hold a number the machine would
        // silently reduce anyway. The overclock pane's own top is the hatches' reachable count, and
        // its -1 means "automatic" — that sentinel is exempt, as it is below any real ceiling.
        if (pane == this.overclockPane && typed >= 0) {
            int ceiling = this.menu.getOverclockCeiling();
            if (typed > ceiling) {
                typed = ceiling;
            }
        }

        String normalised = String.valueOf(typed);
        if (!normalised.equals(pane.field.getValue())) {
            pane.field.setValue(normalised);
        }

        // Always sent and always recorded: comparing with the stored value alone skips the case that
        // matters, where the text clamps to what the machine already has, or where a correction
        // follows a value that is still in flight.
        pane.pending = typed;
        PacketDistributor.sendToServer(pane == this.overclockPane
                ? new C2SMachineOverclockPacket(this.menu.getMachinePos(), typed)
                : new C2SMachineParallelPacket(this.menu.getMachinePos(), typed));
    }

    @Override
    public void onClose() {
        commitName();
        commitOverclock();
        super.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        renderEnergyTrough(graphics);
        renderAddonButtons(graphics);
        renderSideButtons(graphics);
        renderSidebar(graphics);
    }

    /** Advances every pane's reveal by one frame. */
    private void updateSidebarSize() {
        for (Pane pane : this.panes) {
            pane.step();
        }
    }

    /**
     * Advances the pane once per frame, before anything reads its size.
     *
     * <p>Not inside {@code renderBg}: EMI asks for this screen's bounds in its own background pass,
     * which runs before {@code renderBg} does. Stepping the size while drawing would therefore hand
     * EMI the previous frame's size every time, and the panels would lag the artwork during the
     * animation.</p>
     */
    @Override
    public void containerTick() {
        super.containerTick();
        updateSidebarSize();
    }

    private void renderSidebar(GuiGraphics graphics) {
        for (int index = 0; index < this.panes.size(); index++) {
            renderPane(graphics, this.panes.get(index), index);
        }
    }

    /**
     * Top edge of a pane, in panel coordinates.
     *
     * <p>The second pane is not merely stacked below the first: it is pushed down by however far
     * the first one has opened. Otherwise an expanding pane would draw over its neighbour, and the
     * two would overlap for the whole animation.</p>
     */
    private int paneTop(int index) {
        int top = SIDEBAR_Y + index * SIDEBAR_STACK_STEP;
        if (index > 0) {
            top += PARALLEL_PANE_OFFSET_Y;
            // Everything the pane above has grown past its collapsed height pushes this one down.
            for (int above = 0; above < index; above++) {
                top += this.panes.get(above).height - SIDEBAR_MIN_HEIGHT;
            }
        }
        return top;
    }

    private void renderPane(GuiGraphics graphics, Pane pane, int index) {
        int x = this.leftPos + SIDEBAR_X;
        int y = this.topPos + paneTop(index);

        // The box lives on the artwork's third band, so it only appears once the pane has grown far
        // enough to reveal that band. Any earlier and it would sit on the collapsed tab.
        boolean bandVisible = pane.width >= SIDEBAR_FIELD_X + SIDEBAR_FIELD_WIDTH
                && pane.height >= SIDEBAR_FIELD_Y + SIDEBAR_FIELD_HEIGHT;
        if (pane.field != null) {
            pane.field.setVisible(bandVisible);
            if (bandVisible) {
                pane.field.setX(x + SIDEBAR_FIELD_X);
                pane.field.setY(y + SIDEBAR_FIELD_Y);
            }
        }

        if (pane.collapsed()) {
            // Settled shut: draw the collapsed tab from its own corner of the sheet, so it looks
            // exactly like the reference rather than like a crop of the expanded one.
            graphics.blit(SIDEBAR_TEXTURE, x, y, 0, 0,
                    SIDEBAR_MIN_WIDTH, SIDEBAR_MIN_HEIGHT);
            renderPaneIcon(graphics, pane, x + SIDEBAR_SLOT_COLLAPSED_X,
                    y + SIDEBAR_SLOT_COLLAPSED_Y);
            return;
        }
        // Anywhere in between, reveal the expanded artwork up to the animated size.
        graphics.blit(SIDEBAR_TEXTURE, x, y, 0, SIDEBAR_EXPANDED_V, pane.width, pane.height);
        // The expanded slot only exists once the artwork has grown past its corner; drawing it
        // earlier would put the icon over the collapsed tab's own slot.
        if (pane.width >= SIDEBAR_SLOT_EXPANDED_X + SIDEBAR_SLOT_SIZE
                && pane.height >= SIDEBAR_SLOT_EXPANDED_Y + SIDEBAR_SLOT_SIZE) {
            renderPaneIcon(graphics, pane, x + SIDEBAR_SLOT_EXPANDED_X,
                    y + SIDEBAR_SLOT_EXPANDED_Y);
            renderPaneReadout(graphics, pane, x, y);
        }
    }

    /** The pane's own icon: the 4/2 overclock circuit or the parallel one. */
    private void renderPaneIcon(GuiGraphics graphics, Pane pane, int x, int y) {
        graphics.renderItem(pane == this.overclockPane ? overclockIcon() : parallelIcon(), x, y);
    }

    /**
     * The two figures beside the icon: the circuit's ratio, then its ceiling.
     *
     * <p>Only drawn once the pane is open. The collapsed tab is 22 px wide and its icon already
     * takes 16 of them, so there is no room for text there at all — it would spill out of the
     * frame.</p>
     *
     * <p>Both lines share the list's 0.7 scale rather than the full font size: at 1.0 a single
     * Chinese line is wider than the space between the icon and the frame, and the pane is a
     * side note, not a place to shout.</p>
     */
    private void renderPaneReadout(GuiGraphics graphics, Pane pane, int originX, int originY) {
        int textX = originX + SIDEBAR_TEXT_X;
        int textY = originY + SIDEBAR_TEXT_Y;

        // Both panes are two fixed lines: a title, then a caption naming what the box edits. Neither
        // shows the stored number — the box beneath already does.
        boolean overclock = pane == this.overclockPane;
        Component first = Component.translatable(overclock
                ? "gui.goldentweaks.chemical_reactor.sidebar.ratio"
                : "gui.goldentweaks.chemical_reactor.sidebar.parallel.title",
                overclock ? ratioLabel() : Component.empty());
        // The overclock pane reports the count its hatches actually pay for; the parallel pane's
        // caption is fixed wording.
        Component second = overclock
                ? Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.max",
                        String.valueOf(this.menu.getReachableOverclock()))
                : Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.parallel.max");
        second = overclock
                // The live reachable count, not a fixed phrase: it is exactly what the hatches pay for.
                ? Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.max",
                        String.valueOf(this.menu.getReachableOverclock()))
                : second;

        graphics.pose().pushPose();
        graphics.pose().translate(textX, textY, 0.0F);
        graphics.pose().scale(SIDEBAR_TEXT_SCALE, SIDEBAR_TEXT_SCALE, 1.0F);
        graphics.drawString(this.font, first, 0, 0, SIDEBAR_TEXT_COLOUR, false);
        // A full line below the first: at this scale the font is nine units tall, so anything less
        // would overlap.
        graphics.drawString(this.font, second,
                0, SIDEBAR_TEXT_LINE_HEIGHT, SIDEBAR_TEXT_COLOUR, false);
        graphics.pose().popPose();
    }

    /**
     * The ratio this machine overclocks at, always {@code 4/2}.
     *
     * <p>A fixed marking, not a live figure. Every overclock step quadruples the power bill and
     * halves the time, so the ratio is the same 4/2 whatever step the run happens to be on — the
     * steps shown beside it already count how many times it was applied. Scaling this label with the
     * step count would claim the machine overclocks at a different rate than it does.</p>
     */
    private Component ratioLabel() {
        return Component.literal(SIDEBAR_OVERCLOCK_RATIO);
    }

    /** A stand-in stack for the 4/2 overclock control circuit, so no real stack is needed. */
    private static ItemStack overclockIcon() {
        return new ItemStack(GTMultiblockBlocks.CONTROL_CIRCUIT_OVERCLOCK_4_2.get());
    }

    /** A stand-in stack for the parallel control circuit. */
    private static ItemStack parallelIcon() {
        return new ItemStack(GTMultiblockBlocks.CONTROL_CIRCUIT_PARALLEL_1.get());
    }

    /** The pane whose icon is under the cursor, or {@code null}. */
    @Nullable
    private Pane paneIconAt(double mouseX, double mouseY) {
        double x = mouseX - this.leftPos;
        double y = mouseY - this.topPos;
        for (int index = 0; index < this.panes.size(); index++) {
            Pane pane = this.panes.get(index);
            boolean collapsed = pane.collapsed();
            int slotX = collapsed ? SIDEBAR_SLOT_COLLAPSED_X : SIDEBAR_SLOT_EXPANDED_X;
            int slotY = collapsed ? SIDEBAR_SLOT_COLLAPSED_Y : SIDEBAR_SLOT_EXPANDED_Y;
            if (!collapsed
                    && (pane.width < slotX + SIDEBAR_SLOT_SIZE
                    || pane.height < slotY + SIDEBAR_SLOT_SIZE)) {
                continue;
            }
            if (inside(x, y, SIDEBAR_X + slotX, paneTop(index) + slotY,
                    SIDEBAR_SLOT_SIZE, SIDEBAR_SLOT_SIZE)) {
                return pane;
            }
        }
        return null;
    }

    /** The pane whose input box is under the cursor, or {@code null}. */
    @Nullable
    private Pane paneFieldAt(double mouseX, double mouseY) {
        for (Pane pane : this.panes) {
            if (pane.field != null && pane.field.isVisible()
                    && pane.field.isMouseOver(mouseX, mouseY)) {
                return pane;
            }
        }
        return null;
    }

    /**
     * The area this screen occupies, side pane included.
     *
     * <p>EMI lays its own panels out around the space a screen reports, so the pane has to be in
     * there or the recipe list is drawn straight over it. Both the animated size and the panel
     * itself are reported, in screen coordinates.</p>
     */
    public int sidebarRight() {
        int widest = SIDEBAR_MIN_WIDTH;
        for (Pane pane : this.panes) {
            widest = Math.max(widest, pane.width);
        }
        return this.leftPos + SIDEBAR_X + widest;
    }

    public int sidebarBottom() {
        int lowest = this.imageHeight;
        for (int index = 0; index < this.panes.size(); index++) {
            lowest = Math.max(lowest, paneTop(index) + this.panes.get(index).height);
        }
        return this.topPos + lowest;
    }

    public int panelLeft() {
        return this.leftPos;
    }

    public int panelTop() {
        return this.topPos;
    }

    private void renderEnergyTrough(GuiGraphics graphics) {
        int cells = this.menu.getEnergyCells();
        if (cells <= 0) {
            return;
        }
        int width = ENERGY_FIRST_CELL_WIDTH + (cells - 1) * ENERGY_CELL_STEP;
        graphics.blit(TEXTURE, this.leftPos + ENERGY_X, this.topPos + ENERGY_Y,
                ENERGY_SPRITE_U, ENERGY_SPRITE_V, width, ENERGY_HEIGHT);
    }

    private void renderAddonButtons(GuiGraphics graphics) {
        for (int index = 0; index < ADDON_COUNT; index++) {
            int x = this.leftPos + ADDON_X + index * ADDON_STEP;
            int y = this.topPos + ADDON_Y;
            int backgroundU = this.held[index] ? ADDON_SPRITE_PRESSED_U : ADDON_SPRITE_U;
            graphics.blit(TEXTURE, x, y, backgroundU, ADDON_SPRITE_V, ADDON_WIDTH, ADDON_HEIGHT);

            if (index == WIDGET_ISOLATE) {
                // Its two icons live on their own sprite row and differ by column, not by the
                // pressed/resting pair the other three use.
                int iconU = this.menu.isIsolateInput() ? ADDON_ICON_U_ISOLATE_ON : ADDON_ICON_U_ISOLATE_OFF;
                graphics.blit(TEXTURE, x, y, iconU, ADDON_ICON_V_ISOLATE, ADDON_WIDTH, ADDON_HEIGHT);
                continue;
            }

            boolean alternate = switch (index) {
                case WIDGET_LOCK -> this.menu.isLockRecipe();
                case WIDGET_DESTROY -> this.menu.isVoidOutput();
                default -> !this.menu.isBatchMode();
            };
            int iconV = switch (index) {
                case WIDGET_LOCK -> ADDON_ICON_V_LOCK;
                case WIDGET_DESTROY -> ADDON_ICON_V_DESTROY;
                default -> ADDON_ICON_V_BATCH;
            };
            int iconU = alternate ? ADDON_SPRITE_PRESSED_U : ADDON_SPRITE_U;
            graphics.blit(TEXTURE, x, y, iconU, iconV, ADDON_WIDTH, ADDON_HEIGHT);
        }
    }

    private void renderSideButtons(GuiGraphics graphics) {
        renderSideButton(graphics, WIDGET_REFRESH, SIDE_REFRESH_Y,
                this.held[WIDGET_REFRESH] ? 1 : 0, SIDE_ICON_V_REFRESH);
        // Sprite column 0 holds the green arrow and column 1 the yellow pause mark, so a running
        // machine shows the arrow and a stopped one the pause mark.
        renderSideButton(graphics, WIDGET_RUNNING, SIDE_RUNNING_Y,
                this.menu.isRunning() ? 0 : 1, SIDE_ICON_V_RUNNING);
        renderSideButton(graphics, WIDGET_DISMANTLE, SIDE_DISMANTLE_Y,
                this.dismantleArmed ? 1 : 0, SIDE_ICON_V_DISMANTLE);
    }

    private void renderSideButton(GuiGraphics graphics, int widget, int y, int iconColumn, int iconV) {
        int x = this.leftPos + SIDE_X;
        int top = this.topPos + y;
        int backgroundU = this.held[widget] ? SIDE_SPRITE_PRESSED_U : SIDE_SPRITE_U;
        graphics.blit(TEXTURE, x, top, backgroundU, SIDE_SPRITE_V, SIDE_WIDTH, SIDE_HEIGHT);
        graphics.blit(TEXTURE, x, top, iconColumn * SIDE_WIDTH, iconV, SIDE_WIDTH, SIDE_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // The name is an editable box now, so it is drawn by the widget pass rather than here.
        // Painting it here as well would show the old name underneath the field being typed into.
        renderStatusLine(graphics);
    }

    /**
     * Draws text inside {@code availableWidth}, scrolling it when it is wider.
     *
     * <p>Shared by everything that can overflow: the machine name, the status line — whose
     * {@code elapsed / total} pair grows with the recipe — and the product rows. Scrolling rather
     * than clipping keeps the information readable instead of silently dropping its tail.</p>
     *
     * <p>The offset comes from the wall clock, so the speed does not depend on the frame rate, and
     * the copy drawn one cycle behind makes the loop seamless. Coordinates are whatever the current
     * pose uses, which is why the scaled product rows can call this unchanged.</p>
     */
    private void drawMarquee(GuiGraphics graphics, Component text, int x, int y,
                            int availableWidth, int colour) {
        int textWidth = this.font.width(text);
        if (textWidth <= availableWidth) {
            graphics.drawString(this.font, text, x, y, colour, false);
            return;
        }

        int cycle = textWidth + MARQUEE_GAP;
        int offset = marqueeOffset(cycle);
        graphics.drawString(this.font, text, x - offset, y, colour, false);
        graphics.drawString(this.font, text, x - offset + cycle, y, colour, false);
    }

    /**
     * How far the text has scrolled into its current lap.
     *
     * <p>Each lap holds still for {@link #MARQUEE_PAUSE_MILLIS} and then travels one full cycle,
     * which makes the start of the text readable every time it comes round.</p>
     */
    private static int marqueeOffset(int cycle) {
        long lapMillis = Math.max(1L, Math.round(cycle / MARQUEE_PIXELS_PER_SECOND * 1000.0D));
        long phase = net.minecraft.Util.getMillis() % (MARQUEE_PAUSE_MILLIS + lapMillis);
        if (phase < MARQUEE_PAUSE_MILLIS) {
            return 0;
        }
        double travelled = (phase - MARQUEE_PAUSE_MILLIS) / 1000.0D * MARQUEE_PIXELS_PER_SECOND;
        return (int) Math.round(travelled % cycle);
    }

    /**
     * The "running" line, with the channel appended when one is known.
     *
     * <p>One machine can serve several channel groups, so naming the active one is what tells the
     * player which production line the progress belongs to. A craft that began before any channel
     * existed has none, and the line then reads exactly as it did before this feature.</p>
     */
    private Component processingLine() {
        Component progress = GTDurationFormat.format(this.menu.getRecipeProgress());
        Component length = GTDurationFormat.format(this.menu.getRecipeLength());
        Integer channel = this.menu.getActiveChannel();
        return channel == null
                ? Component.translatable("gui.goldentweaks.chemical_reactor.status.processing",
                        progress, length)
                : Component.translatable("gui.goldentweaks.chemical_reactor.status.processing_channel",
                        progress, length, channel);
    }

    /**
     * Draws the machine state directly under the title.
     *
     * <p>The colour carries the meaning as much as the words do: anything red needs the player's
     * attention, yellow is work in progress, green and white are healthy. While a reaction runs the
     * line also counts up, so the player can tell a slow recipe from a stalled one.</p>
     */
    private void renderStatusLine(GuiGraphics graphics) {
        GTReactorStatus status = this.menu.getStatus();
        Component text = switch (status) {
            case IDLE -> Component.translatable("gui.goldentweaks.chemical_reactor.status.idle");
            case PROCESSING -> processingLine();
            case OFF -> Component.translatable("gui.goldentweaks.chemical_reactor.status.off");
            case NO_ENERGY -> Component.translatable("gui.goldentweaks.chemical_reactor.status.no_energy");
            case BROKEN_STRUCTURE ->
                    Component.translatable("gui.goldentweaks.chemical_reactor.status.broken_structure");
            // STARTING is reserved for later; until something sets it, it reads as the idle case.
            case STARTING, OTHER ->
                    Component.translatable("gui.goldentweaks.chemical_reactor.status.other");
            // The message names the figure the cheapest rejected recipe asked for, so the player
            // knows how much more power to install instead of only that something is missing.
            case INSUFFICIENT_ENERGY_HATCH -> Component.translatable(
                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch",
                    GTEnergyFormat.formatExpandedWithUnit(this.menu.getRefusedCost(), "FE/t"));
        };

        int colour = switch (status) {
            // Yellow is the resting state: the machine is healthy but doing nothing.
            case IDLE -> 0xFFFF55;
            // Green is the only state in which work is actually happening.
            case PROCESSING -> 0x55FF55;
            case STARTING -> 0xFFFFFF;
            // Everything else is a stop the player has to act on.
            case OFF, NO_ENERGY, BROKEN_STRUCTURE, OTHER, INSUFFICIENT_ENERGY_HATCH -> 0xFF5555;
        };

        graphics.enableScissor(this.leftPos + TITLE_X, this.topPos + STATUS_Y,
                this.leftPos + TITLE_X + TITLE_WIDTH, this.topPos + STATUS_Y + STATUS_HEIGHT);
        // Scrolls rather than clips: the processing line carries two durations and can outgrow the
        // panel, and a cut-off time would be worse than none.
        drawMarquee(graphics, text, TITLE_X, STATUS_Y, TITLE_WIDTH, colour);
        graphics.disableScissor();

        if (status == GTReactorStatus.PROCESSING) {
            renderProducts(graphics);
        }
    }

    /**
     * Lists what the running reaction will produce, one entry per row.
     *
     * <p>The figures arrive from the server already resolved, so this method only has to lay them
     * out: the stack on the left, its name and per-craft amount above the rate below. Rows are
     * clipped to the band and can be scrolled with the wheel, because a reaction may produce more
     * than fits.</p>
     */
    private void renderProducts(GuiGraphics graphics) {
        List<GTReactionProducts.Line> lines = this.products();
        if (lines.isEmpty()) {
            return;
        }

        // Scissor is in window coordinates and ignores the pose; everything drawn below uses panel
        // coordinates, because renderLabels has already translated the pose to the panel origin.
        graphics.enableScissor(this.leftPos + LIST_X, this.topPos + LIST_Y,
                this.leftPos + LIST_X + LIST_WIDTH, this.topPos + LIST_Y + LIST_HEIGHT);

        double offset = this.scroll() * ROW_HEIGHT;
        for (int index = 0; index < lines.size(); index++) {
            int rowY = (int) Math.round(LIST_Y + index * (double) ROW_HEIGHT - offset);
            // Skip rows that are entirely outside the band; the scissor would clip them anyway, but
            // this avoids laying out text nobody can see.
            if (rowY + ROW_HEIGHT <= LIST_Y || rowY >= LIST_Y + LIST_HEIGHT) {
                continue;
            }
            renderProductRow(graphics, lines.get(index), rowY);
        }

        graphics.disableScissor();
        renderListScrollbar(graphics, lines.size());
    }

    private void renderProductRow(GuiGraphics graphics, GTReactionProducts.Line line, int rowY) {
        GTReactionStack stack = line.stack();
        this.renderProductIcon(graphics, stack, LIST_X, rowY);

        int textX = LIST_X + SLOT_SIZE + 2;
        // The row is drawn scaled, so the width it may use is converted back into unscaled units;
        // otherwise a long name would scroll past the visible edge instead of stopping at it.
        int textWidth = (int) ((LIST_WIDTH - SLOT_SIZE - 4) / ROW_TEXT_SCALE);

        // Nested inside the list's own scissor, so a scrolling row stays off the icon: without this
        // the text would slide across the slot as it travels.
        graphics.enableScissor(this.leftPos + textX, this.topPos + rowY,
                this.leftPos + LIST_X + LIST_WIDTH, this.topPos + rowY + ROW_HEIGHT);
        graphics.pose().pushPose();
        graphics.pose().translate(textX, rowY + 1, 0.0F);
        graphics.pose().scale(ROW_TEXT_SCALE, ROW_TEXT_SCALE, 1.0F);
        drawMarquee(graphics, productName(stack, line.amountPerCraft()), 0, 0,
                textWidth, NAME_COLOUR);
        // The second line starts a full line below the first: at this scale the font is nine units
        // tall, so anything less would overlap.
        drawMarquee(graphics, rateText(line), 0, 9, textWidth, RATE_COLOUR);
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    /**
     * Draws one output stack.
     *
     * <p>Items are drawn directly, which needs no optional mod. Fluids, chemicals and AE keys go
     * through the same EMI conversion the recipe pages use, so a key renders exactly as it does
     * there; that path is skipped when EMI is absent rather than failing the whole screen.</p>
     */
    private void renderProductIcon(GuiGraphics graphics, GTReactionStack stack, int x, int y) {
        if (stack.kind() == GTReactionStack.Kind.ITEM) {
            ItemStack item = stack.toItemStack();
            if (!item.isEmpty()) {
                graphics.renderItem(item, x, y);
            }
            return;
        }

        if (!ModList.get().isLoaded("emi")) {
            return;
        }
        var level = Minecraft.getInstance().level;
        var ingredient = GTReactionStacks.toEmiIngredient(
                stack, level == null ? null : level.registryAccess());
        if (ingredient != null) {
            ingredient.render(graphics, x, y, 0.0F);
        }
    }

    /** Name and per-craft amount, e.g. {@code Coal × 8} or {@code Water 1000 mB}. */
    private static Component productName(GTReactionStack stack, long amount) {
        Component name = switch (stack.kind()) {
            case ITEM -> stack.toItemStack().getHoverName();
            case FLUID -> stack.toFluidStack().getHoverName();
            // Chemicals and AE keys carry no portable display name here, so the id is the honest
            // fallback instead of an empty label.
            case CHEMICAL, AE -> Component.literal(stack.id().getPath());
        };
        // Amounts are grouped for the same reason rates are: a large batch is unreadable otherwise.
        String size = GTEnergyFormat.formatExpanded(amount);
        return stack.kind() == GTReactionStack.Kind.ITEM
                ? Component.literal("").append(name).append(" × " + size)
                : Component.literal("").append(name).append(" " + size + " mB");
    }

    /** Production rate in the unit the machine already uses: per second. */
    private static Component rateText(GTReactionProducts.Line line) {
        String rate = formatRate(line.perSecond());
        return line.stack().kind() == GTReactionStack.Kind.ITEM
                ? Component.literal(rate + " / 秒")
                : Component.literal(rate + " mB / 秒");
    }

    /**
     * Formats a rate with the precision its magnitude deserves.
     *
     * <p>One integer digit keeps two decimals, two digits keep one, and three or more are rounded to
     * a whole number: {@code 1.25}, {@code 12.5}, {@code 125}. More precision than that is noise at
     * this size, and fewer digits would make a slow reaction read as zero.</p>
     *
     * <p>A large rate is grouped the way the player's language groups numbers, so
     * {@code 100000000} reads as {@code 100,000,000} instead of an unbroken run of digits.</p>
     */
    private static String formatRate(double rate) {
        // Round to the finest precision first: the tier depends on how many integer digits the value
        // ends up with, and rounding can change that (9.999 becomes 10, which is a two digit rate).
        double rounded = Math.round(rate * 100.0D) / 100.0D;
        double magnitude = Math.abs(rounded);
        int integerDigits = magnitude < 1.0D ? 1 : (int) Math.floor(Math.log10(magnitude)) + 1;
        int decimals = integerDigits <= 1 ? 2 : integerDigits == 2 ? 1 : 0;

        if (decimals == 0) {
            return GTEnergyFormat.formatExpanded(Math.round(rounded));
        }

        String text = String.format(java.util.Locale.ROOT, "%." + decimals + "f", rounded);
        int dot = text.indexOf('.');
        if (dot < 0) {
            return GTEnergyFormat.formatExpanded(Math.round(rounded));
        }
        long whole = Long.parseLong(text.substring(0, dot));
        // Grouping applies to the integer half only; the fraction is kept exactly as formatted.
        return GTEnergyFormat.formatExpanded(whole) + text.substring(dot);
    }

    /** Scrollbar for the output list, drawn only when the rows do not all fit. */
    private void renderListScrollbar(GuiGraphics graphics, int rowCount) {
        int visibleRows = Math.max(1, LIST_HEIGHT / ROW_HEIGHT);
        if (rowCount <= visibleRows) {
            return;
        }

        // Panel coordinates: renderLabels' pose is already shifted to the panel origin.
        int barX = LIST_X + LIST_WIDTH - 3;
        int barY = LIST_Y;
        int barHeight = LIST_HEIGHT;
        graphics.fill(barX, barY, barX + 2, barY + barHeight, 0xFF373737);

        int markerHeight = Math.max(8, barHeight * visibleRows / rowCount);
        int travel = barHeight - markerHeight;
        int maxScroll = rowCount - visibleRows;
        int markerY = barY + (maxScroll <= 0
                ? 0
                : (int) Math.round(travel * (Math.round(this.scrollTarget)) / (double) maxScroll));
        graphics.fill(barX, markerY, barX + 2, markerY + markerHeight, 0xFFC6C6C6);
    }

    /** Current product list for this machine, read from the client cache. */
    private List<GTReactionProducts.Line> products() {
        BlockPos pos = this.menu.getMachinePos();
        return pos == null ? List.of() : GTReactionProducts.get(pos.asLong());
    }

    /**
     * Scroll position, eased toward the target so the wheel glides instead of jumping.
     *
     * <p>The visible rows are computed from the real list length, so a short list cannot be scrolled
     * past its end.</p>
     */
    private double scroll() {
        int rowCount = this.products().size();
        int visibleRows = Math.max(1, LIST_HEIGHT / ROW_HEIGHT);
        double max = Math.max(0, rowCount - visibleRows);
        this.scrollTarget = Math.max(0.0D, Math.min(max, this.scrollTarget));
        this.scrollCurrent += (this.scrollTarget - this.scrollCurrent) * 0.35D;
        return this.scrollCurrent;
    }

    /** Wheel over the list area scrolls the product rows. */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.menu.getStatus() == GTReactorStatus.PROCESSING
                && mouseX >= this.leftPos + LIST_X
                && mouseX < this.leftPos + LIST_X + LIST_WIDTH
                && mouseY >= this.topPos + LIST_Y
                && mouseY < this.topPos + LIST_Y + LIST_HEIGHT) {
            this.scrollTarget -= scrollY;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * Draws the item tooltip, which the container screen does not do on its own here.
     *
     * <p>This build of {@code AbstractContainerScreen.render} never calls its own
     * {@code renderTooltip}: the deferred tooltip is only flushed by
     * {@code Screen.renderWithTooltip}, which container screens do not go through. A container
     * screen therefore has to ask for the tooltip explicitly, otherwise nothing shows up over the
     * slots at all.</p>
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.nameField != null && !this.nameField.isFocused()) {
            String shown = this.menu.getDisplayName().getString();
            if (!shown.equals(this.nameField.getValue())) {
                this.nameField.setValue(shown);
            }
        }
        syncOverclockEcho();
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.menu.getCarried().isEmpty()
                && this.hoveredSlot != null
                && this.hoveredSlot.hasItem()) {
            ItemStack stack = this.hoveredSlot.getItem();
            graphics.renderTooltip(this.font, this.getTooltipFromContainerItem(stack),
                    stack.getTooltipImage(), stack, mouseX, mouseY);
        }
        renderButtonTooltip(graphics, mouseX, mouseY);
    }

    /** Explains whichever button the cursor is over; the icons alone are not self-evident. */
    private void renderButtonTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        Component label = buttonLabelAt(mouseX, mouseY);
        if (label != null) {
            graphics.renderTooltip(this.font, label, mouseX, mouseY);
            return;
        }

        // Only the icon itself explains itself; the rest of a pane is just a container, so it shows
        // no tooltip at all.
        Pane iconPane = paneIconAt(mouseX, mouseY);
        if (iconPane != null) {
            graphics.renderTooltip(this.font, Component.translatable(iconPane == this.overclockPane
                            ? "gui.goldentweaks.chemical_reactor.sidebar.overclock"
                            : "gui.goldentweaks.chemical_reactor.sidebar.parallel"),
                    mouseX, mouseY);
            return;
        }

        // Each box names what it edits, so the two panes' identical layouts stay tellable apart.
        Pane fieldPane = paneFieldAt(mouseX, mouseY);
        if (fieldPane != null) {
            graphics.renderTooltip(this.font, Component.translatable(
                            fieldPane == this.overclockPane
                                    ? "gui.goldentweaks.chemical_reactor.sidebar.overclock_limit"
                                    : "gui.goldentweaks.chemical_reactor.sidebar.parallel_limit"),
                    mouseX, mouseY);
            return;
        }

        // The trough has no text of its own, so its numbers are only readable here.
        if (inside(mouseX - this.leftPos, mouseY - this.topPos,
                ENERGY_X, ENERGY_Y, ENERGY_WIDTH, ENERGY_HEIGHT)) {
            graphics.renderTooltip(this.font,
                    energyTooltip().stream().map(Component::getVisualOrderText).toList(),
                    mouseX, mouseY);
        }
    }

    /** Stored and capacity, plus the total the input ports can accept per tick. */
    private List<Component> energyTooltip() {
        return List.of(
                Component.translatable("gui.goldentweaks.chemical_reactor.energy",
                        GTEnergyFormat.formatWithUnit(this.menu.getEnergyStored(), "FE"),
                        GTEnergyFormat.formatWithUnit(this.menu.getEnergyCapacity(), "FE")),
                Component.translatable("gui.goldentweaks.chemical_reactor.energy_input",
                        GTEnergyFormat.formatWithUnit(this.menu.getInputEnergyRate(), "FE/t")));
    }

    @Nullable
    private Component buttonLabelAt(double mouseX, double mouseY) {
        int widget = widgetAt(mouseX, mouseY);
        return switch (widget) {
            case WIDGET_LOCK -> lockTooltip();
            case WIDGET_DESTROY -> Component.translatable(this.menu.isVoidOutput()
                    ? "gui.goldentweaks.chemical_reactor.void.on"
                    : "gui.goldentweaks.chemical_reactor.void.off");
            case WIDGET_BATCH -> Component.translatable(this.menu.isBatchMode()
                    ? "gui.goldentweaks.chemical_reactor.batch.on"
                    : "gui.goldentweaks.chemical_reactor.batch.off");
            case WIDGET_ISOLATE -> Component.translatable(this.menu.isIsolateInput()
                    ? "gui.goldentweaks.chemical_reactor.isolate.on"
                    : "gui.goldentweaks.chemical_reactor.isolate.off");
            case WIDGET_REFRESH -> Component.translatable("gui.goldentweaks.chemical_reactor.refresh");
            case WIDGET_RUNNING -> Component.translatable(this.menu.isRunning()
                    ? "gui.goldentweaks.chemical_reactor.running.on"
                    : "gui.goldentweaks.chemical_reactor.running.off");
            case WIDGET_DISMANTLE -> Component.translatable(this.dismantleArmed
                    ? "gui.goldentweaks.chemical_reactor.dismantle.confirm"
                    : "gui.goldentweaks.chemical_reactor.dismantle");
            default -> null;
        };
    }

    /**
     * The lock button's tooltip: its state, plus the pinned recipe id once there is one.
     *
     * <p>The id is appended only while a recipe is actually pinned. Saying "locked, nothing pinned
     * yet" would be noise — the state line already covers that, and the id appears as soon as a
     * craft starts. The id is drawn in grey so it reads as reference data rather than a label.</p>
     */
    private Component lockTooltip() {
        Component state = Component.translatable(this.menu.isLockRecipe()
                ? "gui.goldentweaks.chemical_reactor.lock.on"
                : "gui.goldentweaks.chemical_reactor.lock.off");

        ResourceLocation pinned = GTRecipeLocks.get(this.menu.getMachinePos().asLong());
        if (pinned == null) {
            return state;
        }
        return Component.empty()
                .append(state)
                .append(Component.literal(": "))
                .append(Component.literal(pinned.toString())
                        .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // A click anywhere but the name box commits it, so a rename lands without closing the window.
        if (this.nameField != null && !this.nameField.isMouseOver(mouseX, mouseY)) {
            commitName();
        }
        // A click outside every pane box commits them all, the same way the name box behaves.
        if (paneFieldAt(mouseX, mouseY) == null) {
            commitOverclock();
        }
        // Only the icon toggles the pane. The rest of it is a container the player may want to
        // click through — the ceiling box lives in there, and a click meant for it must not also
        // fold the pane away.
        Pane clicked = paneIconAt(mouseX, mouseY);
        if (button == 0 && clicked != null) {
            clicked.open = !clicked.open;
            return true;
        }
        if (button == 0) {
            int widget = widgetAt(mouseX, mouseY);
            if (widget >= 0) {
                this.held[widget] = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Commits the name on Enter, which is what a player expects from a text box.
     *
     * <p>Escape keeps its usual meaning of closing the window, so the name is still committed by
     * {@link #onClose} on the way out.</p>
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 257 || keyCode == 335)
                && this.nameField != null && this.nameField.isFocused()) {
            commitName();
            // Through the screen: it owns the focus pointer, and clearing only the field leaves it
            // pointing here, which makes the field unclickable afterwards.
            this.setFocused(null);
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            for (Pane pane : this.panes) {
                if (pane.field != null && pane.field.isFocused()) {
                    commitOverclock();
                    this.setFocused(null);
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int pressed = firstHeldWidget();
            if (pressed >= 0) {
                this.held[pressed] = false;
                if (widgetAt(mouseX, mouseY) == pressed) {
                    onButton(pressed);
                }
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void onButton(int widget) {
        switch (widget) {
            case WIDGET_LOCK -> click(GTChemicalReactorMenu.BUTTON_TOGGLE_LOCK);
            case WIDGET_DESTROY -> click(GTChemicalReactorMenu.BUTTON_TOGGLE_VOID);
            case WIDGET_BATCH -> click(GTChemicalReactorMenu.BUTTON_TOGGLE_BATCH);
            case WIDGET_ISOLATE -> click(GTChemicalReactorMenu.BUTTON_TOGGLE_ISOLATE);
            case WIDGET_REFRESH -> click(GTChemicalReactorMenu.BUTTON_REFRESH);
            case WIDGET_RUNNING -> click(GTChemicalReactorMenu.BUTTON_TOGGLE_RUNNING);
            case WIDGET_DISMANTLE -> {
                if (this.dismantleArmed) {
                    this.dismantleArmed = false;
                    click(GTChemicalReactorMenu.BUTTON_DISMANTLE);
                } else {
                    this.dismantleArmed = true;
                }
            }
            default -> {
            }
        }
    }

    private void click(int menuButton) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, menuButton);
        }
    }

    private int firstHeldWidget() {
        for (int widget = 0; widget < WIDGET_COUNT; widget++) {
            if (this.held[widget]) {
                return widget;
            }
        }
        return -1;
    }

    private int widgetAt(double mouseX, double mouseY) {
        double x = mouseX - this.leftPos;
        double y = mouseY - this.topPos;

        for (int index = 0; index < ADDON_COUNT; index++) {
            if (inside(x, y, ADDON_X + index * ADDON_STEP, ADDON_Y, ADDON_WIDTH, ADDON_HEIGHT)) {
                return index;
            }
        }
        if (inside(x, y, SIDE_X, SIDE_REFRESH_Y, SIDE_WIDTH, SIDE_HEIGHT)) {
            return WIDGET_REFRESH;
        }
        if (inside(x, y, SIDE_X, SIDE_RUNNING_Y, SIDE_WIDTH, SIDE_HEIGHT)) {
            return WIDGET_RUNNING;
        }
        if (inside(x, y, SIDE_X, SIDE_DISMANTLE_Y, SIDE_WIDTH, SIDE_HEIGHT)) {
            return WIDGET_DISMANTLE;
        }
        return -1;
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }
}
