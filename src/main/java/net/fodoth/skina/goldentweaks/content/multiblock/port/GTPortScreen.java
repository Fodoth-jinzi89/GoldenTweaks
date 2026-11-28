package net.fodoth.skina.goldentweaks.content.multiblock.port;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.EmiConfig;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.client.GTButton;
import net.fodoth.skina.goldentweaks.content.multiblock.client.GTEchoField;
import net.fodoth.skina.goldentweaks.content.multiblock.client.GTEnergyTrough;
import net.fodoth.skina.goldentweaks.content.multiblock.client.GTScrollRail;
import net.fodoth.skina.goldentweaks.content.multiblock.client.GTStackIcon;
import net.fodoth.skina.goldentweaks.network.packet.C2SPortTextPacket;
import net.fodoth.skina.goldentweaks.network.packet.C2SPortTogglePacket;
import net.fodoth.skina.goldentweaks.util.GTNumberInput;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTEnergyFormat;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionStack;

/**
 * Shared frame of every port window.
 *
 * <p>Three sheets exist, and each has its own subclass: {@code gt_port_io} for item and fluid
 * ports (a 3 by 3 storage grid), {@code gt_port_energy} for energy ports (an upright trough where
 * that grid would be), and {@code gt_port_io_general} for a general assembly, which draws both
 * halves and carries a third switch plate.</p>
 *
 * <p>Only the middle and the switches differ. The name strip, the three text fields, the run
 * switch, the player's slots, the scrolling and the EMI hover are the same everywhere and live
 * here; a subclass supplies its sheet, its middle, its switches and its caption lines.</p>
 *
 * <p>Positions come from the artwork rather than being computed, so each sheet keeps its own
 * numbers. Where a plate sits is a question this class asks and the subclass answers, and the
 * drawing, the click handler and the tooltip all go through that one answer — which is what stops
 * a plate from being painted in one place and answering the cursor in another.</p>
 *
 * <p>Contents are drawn the way EMI draws a recipe's slots: an icon with its amount on the corner.
 * The grid is a fixed 3 by 3 viewport onto however many entries the port holds, so it scrolls on
 * the wheel and its rail jumps on a click, the same interaction the recipe pages offer.</p>
 */
public abstract class GTPortScreen extends AbstractContainerScreen<GTPortMenu> {

    // ---- panel measurements, all in sheet pixels ----

    /** The three free-form fields. */
    protected static final int FIELD_X = 9;
    protected static final int FIELD_WIDTH = 61;
    protected static final int FIELD_HEIGHT = 9;
    protected static final int FIELD_MODE_Y = 33;
    protected static final int FIELD_CHANNEL_Y = 55;
    protected static final int FIELD_PRIORITY_Y = 77;
    protected static final int LABEL_GAP = 2;

    /** The port's name, in the black strip across the top middle. */
    protected static final int NAME_X = 70;
    protected static final int NAME_Y = 11;
    protected static final int NAME_WIDTH = 70;

    /** Capacity readout, to the right of the name. */
    protected static final int CAPTION_X = 146;
    protected static final int CAPTION_Y = 9;

    /** Secondary captions are drawn smaller so two of them fit under their headings. */
    protected static final float CAPTION_SCALE = 0.75F;

    /** The storage grid's scroll rail, in panel coordinates. */
    protected static final int RAIL_X = 140;
    protected static final int RAIL_Y = 28;
    protected static final int RAIL_WIDTH = 8;
    protected static final int RAIL_HEIGHT = 58;

    private static final GTScrollRail RAIL = GTScrollRail.bordered(
            RAIL_X, RAIL_Y, RAIL_WIDTH, RAIL_HEIGHT,
            0xFF8B8B8B, 0xFF373737, 0xFFC6C6C6, 10);

    /**
     * The horizontal energy trough.
     *
     * <p>Where it sits is per sheet; the cell arithmetic is shared with the machine window, since
     * the two troughs are the same shape.</p>
     */
    protected static final int TROUGH_HEIGHT = GTEnergyTrough.HEIGHT;
    protected static final int TROUGH_SPRITE_U = 86;
    protected static final int TROUGH_SPRITE_WIDTH = 61;

    // ---- the run switch ----

    protected static final int RUN_X = 178;
    protected static final int RUN_WIDTH = 20;
    protected static final int RUN_HEIGHT = 9;
    protected static final int RUN_SPRITE_U = 0;
    protected static final int RUN_SPRITE_PRESSED_U = 22;
    /** The base sprite is 22 by 11 with a transparent margin around the 20 by 9 cell. */
    protected static final int RUN_SPRITE_WIDTH = 22;
    protected static final int RUN_SPRITE_HEIGHT = 11;
    protected static final int RUN_SPRITE_DX = -1;
    protected static final int RUN_SPRITE_DY = -1;

    /**
     * The icon overlay, on the same 22 by 11 footprint as the base plate.
     *
     * <p>The icons are drawn at the plate's size with transparent padding around them, so they can
     * simply be layered over the plate at the same offset rather than positioned by hand. Green
     * triple arrow while the port takes part, yellow pause bar while it does not.</p>
     */
    protected static final int RUN_ICON_U_RUN = 0;
    protected static final int RUN_ICON_U_STOP = 22;
    protected static final int RUN_ICON_WIDTH = 22;
    protected static final int RUN_ICON_HEIGHT = 11;

    // ---- colours ----

    /** Text drawn directly on the light panel, which needs the darker ink to stay readable. */
    protected static final int INK_COLOUR = 0xFF404040;
    /** White, for text drawn inside the dark wells. */
    protected static final int TEXT_COLOUR = 0xFFFFFFFF;
    /** Resting state of a field: a dim grey, clearly apart from the white used while editing. */
    protected static final int INACTIVE_TEXT_COLOUR = 0xFF8A8A8A;
    protected static final int LABEL_COLOUR = INK_COLOUR;
    protected static final int CAPTION_COLOUR = INK_COLOUR;

    // ---- state ----

    /**
     * The four editable fields, each carrying the value it has sent but not yet seen come back.
     *
     * <p>Built in {@code init()}, because the boxes take this screen's font and the font is only
     * assigned when the screen is initialised. The echo bookkeeping lives with the box, so there is
     * no second map to keep in step.</p>
     */
    @Nullable
    private GTEchoField nameField;
    @Nullable
    private GTEchoField modeField;
    @Nullable
    private GTEchoField channelField;
    @Nullable
    private GTEchoField priorityField;
    private List<GTEchoField> fields = List.of();

    /** First visible row of the storage grid. */
    protected int scrollRow;

    /**
     * Cursor position from the last frame, in panel coordinates.
     *
     * <p>Recorded here because a key press callback carries no coordinates, and the hover code
     * needs to know which stack the player is pointing at when a recipe key is pressed.</p>
     */
    private double lastMouseX = -1;
    private double lastMouseY = -1;

    /**
     * Which button the cursor is holding down, or {@code -1} when none.
     *
     * <p>Tracked here because the screen has no other way to ask: every switch draws a pressed
     * plate while it is being clicked, and neither the render callback nor the widget classes carry
     * the mouse's button state. Hovering alone is not a press — showing the pressed plate on hover
     * made every switch look permanently held down.</p>
     */
    private int heldButton = -1;

    protected GTPortScreen(GTPortMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GTPortMenu.IMAGE_WIDTH;
        this.imageHeight = sheetHeight();
        // The text boxes are built in init(), not here: they take this screen's font, and the font
        // is only assigned when the screen is initialised. Building them now would pass null and
        // crash the first time one was rendered.
    }

    // ---- what a subclass answers ----

    /** The sheet this window draws itself from. */
    protected abstract ResourceLocation sheet();

    /** How tall that sheet's panel is. */
    protected abstract int sheetHeight();

    /** The middle of the window: the storage grid, or the energy trough. */
    protected abstract void drawMiddle(GuiGraphics graphics);

    /** The switches this kind of window carries, drawn over the panel. */
    protected abstract void drawSwitches(GuiGraphics graphics, int mouseX, int mouseY);

    /**
     * The switches this kind of window carries, for the tooltip walk.
     *
     * <p>Each button owns its own wording, so the window only has to name them; the run switch is
     * added by this class.</p>
     */
    protected abstract List<GTButton> switches();

    /**
     * Anything with a tooltip that is not a switch — a trough, a cell.
     *
     * <p>Default: nothing. A window with a plain grid has nothing else to explain.</p>
     */
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    /** Consumes a click that landed on one of this window's switches. */
    protected abstract boolean clickSwitch(double mouseX, double mouseY, int button);

    /** The capacity readout beside the name. */
    protected abstract void drawCapacity(GuiGraphics graphics);

    /** Whether this window pages its contents at all. */
    protected boolean scrolls() {
        return true;
    }

    /** The row the run switch's base plate is cut from on this sheet. */
    protected int runSpriteV() {
        return 178;
    }

    /** The row the run switch's icon is cut from on this sheet. */
    protected int runIconV() {
        return 189;
    }

    /** The row the run switch sits on. */
    protected int runY() {
        return 95;
    }

    // ---- setup ----

    private GTEchoField makeField(GTPortBlockEntity.Field field, String initial,
                                  GTEchoField.Kind kind) {
        // The field is labelled but drawn without a border: the panel already draws the well it
        // sits in, so a second box would double the outline.
        EditBox box = new EditBox(this.font, 0, 0, FIELD_WIDTH, FIELD_HEIGHT,
                Component.translatable("gui.goldentweaks.port.field." + field.name().toLowerCase()));
        box.setMaxLength(GTPortBlockEntity.TEXT_LIMIT);
        box.setBordered(false);
        // Two colours: white while the field is the one being edited, and a dimmer grey otherwise.
        // The uneditable colour is what a field that is not focused is drawn with, so that is the
        // one to set for the resting state.
        box.setTextColor(TEXT_COLOUR);
        box.setTextColorUneditable(INACTIVE_TEXT_COLOUR);
        box.setValue(initial);
        // Typing only updates the box; the server hears about it on commit.
        box.setResponder(value -> {
        });
        return new GTEchoField(box, kind);
    }

    @Override
    protected void init() {
        super.init();
        this.nameField = makeNameField();
        // Priority is the one numeric counter here; mode and channel only happen to contain digits
        // among separators, so grouping them would be wrong.
        this.modeField = makeField(GTPortBlockEntity.Field.MODE,
                this.menu.modeText(), GTEchoField.Kind.TEXT);
        this.channelField = makeField(GTPortBlockEntity.Field.CHANNEL,
                this.menu.channelText(), GTEchoField.Kind.TEXT);
        this.priorityField = makeField(GTPortBlockEntity.Field.PRIORITY,
                displayedPriority(this.menu.priorityText()), GTEchoField.Kind.NUMBER);
        this.fields = List.of(this.nameField, this.modeField, this.channelField, this.priorityField);

        placeNameField();
        place(this.modeField, FIELD_MODE_Y);
        place(this.channelField, FIELD_CHANNEL_Y);
        place(this.priorityField, FIELD_PRIORITY_Y);
        this.scrollRow = 0;
    }

    /**
     * Greys the channel field out while a craft is running.
     *
     * <p>The field still shows its value and still commits on close, but it refuses focus and
     * swallows typing — the server refuses the edit too, so a modified client gains nothing. The
     * other two fields stay live: they carry no meaning to the matcher yet.</p>
     */
    private void refreshChannelLock() {
        if (this.channelField == null) {
            return;
        }
        GTPortBlockEntity port = this.menu.port();
        boolean locked = port != null && port.isChannelLocked();
        this.channelField.box().setEditable(!locked);
        this.channelField.box().setTextColor(locked ? INACTIVE_TEXT_COLOUR : TEXT_COLOUR);
    }

    /**
     * The renaming box, laid over the name strip.
     *
     * <p>Same styling as the three counters but a different footprint: it spans the strip the title
     * used to be drawn in, so renaming replaces the label rather than sitting beside it.</p>
     */
    private GTEchoField makeNameField() {
        EditBox box = new EditBox(this.font, 0, 0, NAME_WIDTH, this.font.lineHeight,
                Component.translatable("gui.goldentweaks.port.name"));
        box.setMaxLength(GTPortBlockEntity.TEXT_LIMIT);
        box.setBordered(false);
        box.setTextColor(TEXT_COLOUR);
        box.setTextColorUneditable(INACTIVE_TEXT_COLOUR);
        box.setValue(this.menu.port() == null ? "" : this.menu.port().getDisplayName().getString());
        box.setResponder(value -> {
        });
        return new GTEchoField(box, GTEchoField.Kind.TEXT);
    }

    private void placeNameField() {
        if (this.nameField == null) {
            return;
        }
        EditBox box = this.nameField.box();
        box.setX(this.leftPos + NAME_X);
        box.setY(this.topPos + NAME_Y);
        this.addRenderableWidget(box);
    }

    private void place(@Nullable GTEchoField field, int y) {
        if (field == null) {
            return;
        }
        EditBox box = field.box();
        box.setX(this.leftPos + FIELD_X);
        box.setY(this.topPos + y);
        this.addRenderableWidget(box);
    }

    // ---- text field plumbing ----

    private void commit() {
        // Guarded because the boxes only exist after init(): a screen that is closed without ever
        // being initialised would otherwise reach for them here.
        if (this.modeField == null || this.channelField == null || this.priorityField == null) {
            return;
        }
        commitName();
        send(GTPortBlockEntity.Field.MODE, this.modeField.box().getValue());
        send(GTPortBlockEntity.Field.CHANNEL, this.channelField.box().getValue());
        send(GTPortBlockEntity.Field.PRIORITY,
                canonicalPriority(this.priorityField.box().getValue()));
    }

    /**
     * The grouped form of a stored priority, or the text itself when it is not a number.
     *
     * <p>The box shows a number the way the player reads it — {@code 1,600} — while the port keeps
     * the plain integer.</p>
     */
    private static String displayedPriority(String stored) {
        Integer value = GTNumberInput.parseInt(stored);
        return value == null ? stored : GTNumberInput.group(value);
    }

    /**
     * The plain integer a typed priority means.
     *
     * <p>Suffixes ({@code 1.6k}, {@code 16M}) and grouping separators are accepted, since the box
     * shows them back. Text that is not a number is sent unchanged, so the port's own fallback
     * still decides what a typo means.</p>
     */
    private static String canonicalPriority(String typed) {
        Integer value = GTNumberInput.parseInt(typed);
        return value == null ? typed : String.valueOf(value);
    }

    /**
     * Sends the name if it differs from what the port currently reports.
     *
     * <p>Compared against the entity's display name rather than a stored string: an empty box means
     * "no custom name", which reads back as the default wording, so the comparison has to be made
     * against that same wording.</p>
     */
    private void commitName() {
        GTPortBlockEntity port = this.menu.port();
        if (port == null || this.nameField == null) {
            return;
        }
        String typed = this.nameField.box().getValue();
        String shown = port.getDisplayName().getString();
        boolean clearing = typed.isBlank() && port.getCustomName() != null;
        boolean renaming = !typed.isBlank() && !typed.equals(shown);
        if (clearing || renaming) {
            // A cleared name reads back as the default wording, not as empty text.
            this.nameField.sent(clearing ? "" : typed);
            PacketDistributor.sendToServer(
                    new C2SPortTextPacket(port.getBlockPos(), GTPortBlockEntity.Field.NAME,
                            clearing ? "" : typed));
        }
    }

    private void send(GTPortBlockEntity.Field field, String value) {
        GTPortBlockEntity port = this.menu.port();
        GTEchoField held = fieldOf(field);
        if (port == null || held == null) {
            return;
        }
        String current = switch (field) {
            case NAME -> port.getDisplayName().getString();
            case MODE -> port.modeText();
            case CHANNEL -> port.channelText();
            case PRIORITY -> port.priorityText();
        };
        if (!current.equals(value)) {
            // Compared against the in-flight value as well: after a rejected entry the box and the
            // port disagree, and comparing only with the port would treat the correction as
            // "unchanged" and send nothing.
            if (held.matches(value)) {
                return;
            }
            // Stored in the form the box will show, not the form that goes on the wire: the echo
            // compares against the displayed text, and a bare integer would never match a group
            // like "1,600", leaving this field pinned to its pending value for good.
            held.sent(field == GTPortBlockEntity.Field.PRIORITY ? displayedPriority(value) : value);
            PacketDistributor.sendToServer(new C2SPortTextPacket(port.getBlockPos(), field, value));
        }
    }

    /** The field backing one wire field, or {@code null} before {@code init()}. */
    @Nullable
    private GTEchoField fieldOf(GTPortBlockEntity.Field field) {
        return switch (field) {
            case NAME -> this.nameField;
            case MODE -> this.modeField;
            case CHANNEL -> this.channelField;
            case PRIORITY -> this.priorityField;
        };
    }

    @Override
    public void onClose() {
        commit();
        super.onClose();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.heldButton = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Whether a switch is being pressed right now.
     *
     * <p>Held, not hovered: the plate is the button's own feedback for a click, and lighting it up
     * for a mere hover made every switch look stuck down. The cursor must also still be inside the
     * plate — dragging off it releases the press visually, which is what a button does.</p>
     */
    protected boolean isPressed(int x, int y, int width, int height, int mouseX, int mouseY) {
        return this.heldButton >= 0 && over(x, y, width, height, mouseX, mouseY);
    }

    /** Whether a point is inside a rectangle. */
    private static boolean over(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /** Whether the cursor is over a switch, for the click handler and for its tooltip. */
    protected boolean hits(int x, int y, int width, int height, double mouseX, double mouseY) {
        return over(x, y, width, height, (int) mouseX, (int) mouseY);
    }

    /** Sends a switch's flip to the server and lets the new state come back. */
    protected void toggle(C2SPortTogglePacket.Target target) {
        GTPortBlockEntity port = this.menu.port();
        if (port != null) {
            PacketDistributor.sendToServer(new C2SPortTogglePacket(port.getBlockPos(), target));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.heldButton = button;
        boolean insideField = false;
        for (GTEchoField field : this.fields) {
            if (field.box().isMouseOver(mouseX, mouseY)) {
                insideField = true;
                break;
            }
        }
        if (!insideField) {
            commit();
            // Through the screen, not the field: Screen keeps its own pointer to the focused
            // listener, and clearing the field alone leaves it stale, after which clicking that
            // same field again is ignored because the screen believes it is still focused.
            this.setFocused(null);
        }

        // A click on a stack belongs to EMI, not to the panel: left asks for recipes, right for uses.
        if (clickStack(mouseX, mouseY, button)) {
            return true;
        }

        if (clickSwitch(mouseX, mouseY, button)) {
            return true;
        }

        // Clicking the rail jumps to that position, the way a page scrollbar behaves.
        if (button == 0 && scrolls()
                && hits(this.leftPos + RAIL_X, this.topPos + RAIL_Y,
                        RAIL_WIDTH, RAIL_HEIGHT, mouseX, mouseY)) {
            scrollTo(mouseY);
            return true;
        }

        // The run switch flips participation; the server applies the change and reports it back.
        if (button == 0 && runButton().hits(mouseX, mouseY, this.leftPos, this.topPos)) {
            toggle(C2SPortTogglePacket.Target.ENABLED);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // R and U only mean anything while the cursor is over a stack, and only when no text box
        // has focus — otherwise typing an "r" into a field would open a recipe page.
        if (!this.fields.isEmpty() && !anyFieldFocused() && keyOverStack(keyCode, scanCode)) {
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            for (GTEchoField field : this.fields) {
                if (field.box().isFocused()) {
                    commit();
                    this.setFocused(null);
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ---- hover and recipe lookup ----

    /**
     * Which entry of the storage grid the cursor is over, or {@code -1}.
     *
     * <p>The grid is drawn as plain icons rather than EMI widgets, so hover has to be resolved by
     * hand here: only entries that are actually drawn can be hovered, which is why the index is
     * bounded by the contents size and not by the cell count.</p>
     */
    private int hoveredStack(double mouseX, double mouseY) {
        if (!scrolls()) {
            return -1;
        }
        List<GTReactionStack> rows = contents();
        int[] cells = cellOfEntry(rows);
        for (int cell = 0; cell < GTPortMenu.STORAGE_CELLS; cell++) {
            int column = cell % GTPortMenu.STORAGE_COLUMNS;
            int row = cell / GTPortMenu.STORAGE_COLUMNS;
            int x = this.leftPos + GTPortMenu.STORAGE_X + column * GTPortMenu.SLOT_STEP;
            int y = this.topPos + GTPortMenu.STORAGE_Y + row * GTPortMenu.SLOT_STEP;
            if (mouseX >= x && mouseX < x + GTPortMenu.SLOT_SIZE
                    && mouseY >= y && mouseY < y + GTPortMenu.SLOT_SIZE) {
                int absolute = this.scrollRow * GTPortMenu.STORAGE_COLUMNS + cell;
                for (int index = 0; index < cells.length; index++) {
                    if (cells[index] == absolute) {
                        return index;
                    }
                }
                return -1;
            }
        }
        return -1;
    }

    /**
     * The cell each entry starts at, in the same walk the grid uses.
     *
     * <p>Shared rather than recomputed: hover used to assume one entry per cell, so as soon as an
     * entry spanned several the two disagreed — the drawn grid and the tooltip pointed at different
     * entries, and the entries past the first few cells could not be hovered at all.</p>
     */
    private int[] cellOfEntry(List<GTReactionStack> rows) {
        int[] cells = new int[rows.size()];
        int perSlot = Math.max(1, this.menu.slotLimit());
        boolean single = this.menu.isSingleCellMode();
        int cursor = 0;
        for (int index = 0; index < rows.size(); index++) {
            cells[index] = cursor;
            cursor += spanOf(rows.get(index), perSlot, single);
        }
        return cells;
    }

    /** How many cells one entry claims. */
    protected static int spanOf(GTReactionStack stack, int perSlot, boolean single) {
        if (single) {
            return 1;
        }
        long span = (stack.amount() + perSlot - 1) / perSlot;
        return (int) Math.clamp(span, 1L, GTPortMenu.STORAGE_CELLS);
    }

    protected List<GTReactionStack> contents() {
        return this.menu.contents();
    }

    /** The EMI form of one entry, or {@code null} when EMI is absent or cannot build it. */
    @Nullable
    private EmiIngredient ingredientOf(GTReactionStack stack) {
        return GTStackIcon.ingredientOf(stack);
    }

    /**
     * Shows the stack's own tooltip, the same one EMI shows in a recipe page.
     *
     * <p>Built from the EMI stack rather than from the item, so a fluid or a chemical gets the
     * tooltip it would have there instead of a bare name.</p>
     */
    protected void renderStackTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int index = hoveredStack(mouseX, mouseY);
        if (index < 0) {
            return;
        }
        GTReactionStack stack = contents().get(index);
        EmiIngredient ingredient = ingredientOf(stack);
        if (ingredient != null) {
            // The first stack of the ingredient carries the text: an ingredient may hold several
            // alternatives, and the tooltip of the one the player sees is the useful one.
            var stacks = ingredient.getEmiStacks();
            if (!stacks.isEmpty()) {
                var lines = stacks.getFirst().getTooltipText();
                if (!lines.isEmpty()) {
                    graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
                    return;
                }
            }
        }
        // Without EMI only items can say anything at all.
        if (stack.kind() == GTReactionStack.Kind.ITEM) {
            var item = stack.toItemStack();
            if (!item.isEmpty()) {
                graphics.renderTooltip(this.font, item, mouseX, mouseY);
            }
        }
    }

    /**
     * The run switch.
     *
     * <p>The cell the cursor answers to is the visible 20 by 9 button; the sprites are one pixel
     * larger on every side because their art carries a transparent margin, so they are painted at
     * that same origin with the margin hanging outside. Keeping the hit box at the cell is what
     * lets the click, the tooltip and the plate all agree on where the button is.</p>
     */
    protected GTButton runButton() {
        return new GTButton(
                new GTButton.Sprite(RUN_SPRITE_U, runSpriteV(),
                        RUN_SPRITE_WIDTH, RUN_SPRITE_HEIGHT, RUN_SPRITE_PRESSED_U),
                new GTButton.Sprite(RUN_ICON_U_RUN, runIconV(),
                        RUN_ICON_WIDTH, RUN_ICON_HEIGHT, RUN_ICON_U_STOP),
                RUN_X, runY(),
                () -> List.of(Component.translatable(this.menu.port() != null
                        && this.menu.port().isEnabled()
                        ? "gui.goldentweaks.port.running.on"
                        : "gui.goldentweaks.port.running.off")));
    }

    /** The run switch's plate and icon, from this window's own sheet. */
    protected void drawRunButton(GuiGraphics graphics, int mouseX, int mouseY) {
        GTButton button = runButton();
        boolean held = isPressed(this.leftPos + button.x(), this.topPos + button.y(),
                RUN_WIDTH, RUN_HEIGHT, mouseX, mouseY);
        // Sprites are one pixel larger on every side; the margin hangs outside the hit box.
        int x = this.leftPos + button.x() + RUN_SPRITE_DX;
        int y = this.topPos + button.y() + RUN_SPRITE_DY;
        boolean enabled = this.menu.port() != null && this.menu.port().isEnabled();
        graphics.blit(sheet(), x, y,
                held ? RUN_SPRITE_PRESSED_U : RUN_SPRITE_U, runSpriteV(),
                RUN_SPRITE_WIDTH, RUN_SPRITE_HEIGHT);
        graphics.blit(sheet(), x, y,
                enabled ? RUN_ICON_U_RUN : RUN_ICON_U_STOP, runIconV(),
                RUN_ICON_WIDTH, RUN_ICON_HEIGHT);
    }

    /** Whether any of the text boxes currently has focus. */
    private boolean anyFieldFocused() {
        for (GTEchoField field : this.fields) {
            if (field.box().isFocused()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Opens recipes or uses for the stack under the cursor, if the key is the one EMI is bound to.
     *
     * <p>Bound through {@code EmiConfig} rather than by comparing against R and U: those are only
     * the defaults, and a player who rebound them would otherwise get nothing here. The mouse
     * position is read from the screen rather than passed in, because a key press carries no
     * coordinates.</p>
     *
     * @return whether the key was handled, so it does not also reach the panel behind
     */
    private boolean keyOverStack(int keyCode, int scanCode) {
        if (!ModList.get().isLoaded("emi")) {
            return false;
        }
        int index = hoveredStack(this.lastMouseX, this.lastMouseY);
        if (index < 0) {
            return false;
        }
        EmiIngredient ingredient = ingredientOf(contents().get(index));
        if (ingredient == null) {
            return false;
        }
        if (EmiConfig.viewRecipes.matchesKey(keyCode, scanCode)) {
            EmiApi.displayRecipes(ingredient);
            return true;
        }
        if (EmiConfig.viewUses.matchesKey(keyCode, scanCode)) {
            EmiApi.displayUses(ingredient);
            return true;
        }
        return false;
    }

    /**
     * Answers a click on a stack the way EMI does: left for recipes, right for uses.
     *
     * <p>Returns whether the click was consumed, so the window does not also treat it as a click on
     * the panel behind.</p>
     */
    private boolean clickStack(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) {
            return false;
        }
        int index = hoveredStack(mouseX, mouseY);
        if (index < 0) {
            return false;
        }
        EmiIngredient ingredient = ingredientOf(contents().get(index));
        if (ingredient == null) {
            return false;
        }
        if (button == 0) {
            EmiApi.displayRecipes(ingredient);
        } else {
            EmiApi.displayUses(ingredient);
        }
        return true;
    }

    // ---- scrolling ----

    /** Rows the grid needs for the current contents. */
    protected int totalRows() {
        return Math.max(1, Mth.ceil(contents().size() / (float) GTPortMenu.STORAGE_COLUMNS));
    }

    protected int maxScrollRow() {
        return Math.max(0, totalRows() - GTPortMenu.STORAGE_ROWS);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!scrolls() || scrollY == 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        // One notch moves one row: a short list needs only a nudge, and a long one still travels
        // quickly because the wheel repeats.
        this.scrollRow = Mth.clamp(this.scrollRow + (scrollY > 0 ? -1 : 1), 0, maxScrollRow());
        return true;
    }

    /** Jumps to the row under {@code mouseY}, for a click on the rail. */
    protected void scrollTo(double mouseY) {
        int max = maxScrollRow();
        if (max <= 0) {
            this.scrollRow = 0;
            return;
        }
        int trackTop = this.topPos + RAIL_Y + 1;
        int trackHeight = RAIL_HEIGHT - 2;
        double progress = (mouseY - trackTop) / (double) trackHeight;
        this.scrollRow = Mth.clamp((int) Math.round(progress * max), 0, max);
    }

    // ---- drawing ----

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        refreshChannelLock();
        syncChannelEcho();
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderStackTooltip(graphics, mouseX, mouseY);
        // Every switch explains itself; the run switch is the one this class owns, and the subclass
        // adds its own to the same walk.
        List<GTButton> buttons = new ArrayList<>(switches());
        buttons.add(runButton());
        GTButton.renderTooltips(graphics, this.font, buttons, mouseX, mouseY,
                this.leftPos, this.topPos);
        renderExtraTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(sheet(), this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight);
        drawLabels(graphics);
        drawMiddle(graphics);
        drawRunButton(graphics, mouseX, mouseY);
        drawSwitches(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // The pose is already translated to the panel origin here, so these use panel coordinates.
        drawCapacity(graphics);
    }

    /**
     * Shows the stored channel in the field once the server has normalised it.
     *
     * <p>A mistyped list is corrected to {@code 0} on save, and without this the box would keep
     * showing the mistake while the port actually behaves as a wildcard. The field is only
     * rewritten when it is not being edited, so a correction never fights the cursor.</p>
     */
    private void syncChannelEcho() {
        if (this.channelField == null || this.channelField.box().isFocused()) {
            return;
        }
        GTPortBlockEntity port = this.menu.port();
        if (port == null) {
            return;
        }
        echo(this.modeField, port.modeText());
        echo(this.channelField, port.channelText());
        echo(this.priorityField, displayedPriority(port.priorityText()));

        if (this.nameField != null && !this.nameField.box().isFocused()) {
            String shown = port.getDisplayName().getString();
            // A cleared name reads back as the block's default wording, so the round trip is
            // confirmed by the custom name being gone rather than by the text matching.
            this.nameField.echo(shown, pending -> pending.isEmpty()
                    ? port.getCustomName() == null
                    : shown.equals(pending));
        }
    }

    /**
     * Writes a field's stored value into its box, unless a value is still in flight.
     *
     * <p>The in-flight bookkeeping is the shared {@link GTEchoField}: the box keeps the text the
     * player typed until the port reports it back, and a different value arriving means the server
     * normalised it and lands as the correction.</p>
     */
    private void echo(@Nullable GTEchoField field, String stored) {
        if (field == null) {
            return;
        }
        field.echo(stored);
    }

    private void drawLabels(GuiGraphics graphics) {
        // Nudged one pixel lower than the field's own top edge: the sheet draws its well from y-1,
        // and sitting the caption flush with it read as touching.
        int nudge = 1;
        drawCentred(graphics, Component.translatable("gui.goldentweaks.port.field.mode"),
                FIELD_MODE_Y - LABEL_GAP - this.font.lineHeight + nudge);
        drawCentred(graphics, Component.translatable("gui.goldentweaks.port.field.channel"),
                FIELD_CHANNEL_Y - LABEL_GAP - this.font.lineHeight + nudge);
        drawCentred(graphics, Component.translatable("gui.goldentweaks.port.field.priority"),
                FIELD_PRIORITY_Y - LABEL_GAP - this.font.lineHeight + nudge);
    }

    private void drawCentred(GuiGraphics graphics, Component text, int y) {
        int textWidth = this.font.width(text);
        graphics.drawString(this.font, text,
                this.leftPos + FIELD_X + (FIELD_WIDTH - textWidth) / 2, this.topPos + y,
                LABEL_COLOUR, false);
    }

    // ---- shared drawing ----

    /**
     * The visible slice of the storage grid.
     *
     * <p>Only occupied cells are drawn: the frame drawn by the sheet already marks the grid, and
     * empty boxes would add nothing but noise.</p>
     *
     * <p>How many cells an entry takes is the stacking mode's whole effect. In single-cell mode each
     * entry is one cell wide, so the grid is a plain row-major list. In multi-cell mode an entry
     * occupies as many cells as it needs, measured as the ceiling of its amount over the slot
     * ceiling — a stack of 64 in a 64-per-slot bus takes one cell, a stack of 192 takes three.</p>
     */
    protected void drawStacks(GuiGraphics graphics) {
        List<GTReactionStack> rows = contents();
        if (rows.isEmpty()) {
            return;
        }

        int perSlot = Math.max(1, this.menu.slotLimit());
        boolean single = this.menu.isSingleCellMode();
        int columns = GTPortMenu.STORAGE_COLUMNS;
        int capacity = GTPortMenu.STORAGE_CELLS;

        // Walk entries, reserving however many cells each one claims, and stop when the visible
        // window is full. `cursor` is the first cell of the entry currently being laid out.
        int cursor = 0;
        int drawn = 0;
        for (GTReactionStack stack : rows) {
            if (drawn >= capacity) {
                break;
            }
            int span = spanOf(stack, perSlot, single);
            // Skip entries above the visible window, and clip one that starts inside it.
            if (cursor + span <= this.scrollRow * columns) {
                cursor += span;
                continue;
            }
            // The icon goes in the entry's first visible cell; the cells it spans beyond that are
            // reserved but left blank. Drawing it in each of them repeated one entry as several
            // identical icons, which read as separate stacks of the same thing.
            int firstVisible = Math.max(cursor, this.scrollRow * columns);
            int lastVisible = Math.min(cursor + span, this.scrollRow * columns + capacity);
            if (firstVisible < lastVisible) {
                int windowCell = firstVisible - this.scrollRow * columns;
                int column = windowCell % columns;
                int row = windowCell / columns;
                int x = this.leftPos + GTPortMenu.STORAGE_X + column * GTPortMenu.SLOT_STEP;
                int y = this.topPos + GTPortMenu.STORAGE_Y + row * GTPortMenu.SLOT_STEP;
                drawStack(graphics, stack, x, y);
                drawn += lastVisible - firstVisible;
            }
            cursor += span;
        }
    }

    /** One stack: the icon through EMI, then its amount on the corner, as EMI draws a slot. */
    protected void drawStack(GuiGraphics graphics, GTReactionStack stack, int x, int y) {
        GTStackIcon.draw(graphics, this.font, stack, x, y, true);
    }

    /** The rail beside the grid: a track plus a marker whose size shows how much fits. */
    protected void drawRail(GuiGraphics graphics) {
        RAIL.draw(graphics, totalRows(), GTPortMenu.STORAGE_ROWS, this.scrollRow);
    }

    /**
     * The horizontal energy trough, lit in the same steps the machine's own trough uses.
     *
     * <p>The cell arithmetic is shared with the machine window, so a port's bar and the machine's
     * fill in step rather than one filling smoothly while the other jumps.</p>
     */
    protected void drawTrough(GuiGraphics graphics, int x, int y, int spriteV) {
        GTEnergyTrough.draw(graphics, sheet(), x, y, TROUGH_SPRITE_U, spriteV,
                this.menu.energyStored(), this.menu.energyCapacity());
    }

    /**
     * Explains the trough: what is stored and what the part can hold.
     *
     * <p>An assembly needs this because its window has no room for the caption lines a dedicated
     * energy hatch prints under its trough — the assembly quotes its own byte and type ceilings
     * where they would go.</p>
     */
    protected void renderTroughTooltip(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        if (!hits(x, y, TROUGH_SPRITE_WIDTH, TROUGH_HEIGHT, mouseX, mouseY)) {
            return;
        }
        graphics.renderTooltip(this.font,
                Component.translatable("gui.goldentweaks.port.energy.stored_value",
                        GTEnergyFormat.formatWithUnit(this.menu.energyStored(), "FE"),
                        GTEnergyFormat.formatWithUnit(this.menu.energyCapacity(), "FE")),
                mouseX, mouseY);
    }

    /** Registers each window kind against the shared menu type. */
    @EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
    public static final class Registrar {

        private Registrar() {
        }

        @SubscribeEvent
        public static void onRegisterScreens(RegisterMenuScreensEvent event) {
            event.register(GTPortMenu.MENU.get(), GTPortScreens::create);
        }
    }
}
