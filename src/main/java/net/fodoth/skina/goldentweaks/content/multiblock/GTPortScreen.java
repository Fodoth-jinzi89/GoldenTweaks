package net.fodoth.skina.goldentweaks.content.multiblock;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTEnergyFormat;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStacks;
import net.fodoth.skina.goldentweaks.network.packet.C2SPortTextPacket;
import net.fodoth.skina.goldentweaks.network.packet.C2SPortTogglePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.HolderLookup;
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
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Window for one of this mod's ports.
 *
 * <p>Everything is positioned from the sheet's own pixels: both panels occupy
 * {@code (0,0)..(210,192)} and their sprites sit below that. Coordinates are constants rather than
 * computed, which is what keeps the three text fields, the storage area and the player's slots
 * aligned with the art.</p>
 *
 * <p>There are two sheets. Item and fluid ports use {@code gt_port_io}, which carries a 3 by 3
 * storage grid; energy ports use {@code gt_port_energy}, where that grid and its rail are replaced
 * by an upright trough. Everything else 鈥?the fields, the name, the buttons, the player area 鈥?is
 * identical, so only the middle of the drawing differs.</p>
 *
 * <p>Contents are drawn the way EMI draws a recipe's slots: an icon with its amount on the corner.
 * The grid is a fixed 3 by 3 viewport onto however many entries the port holds, so it scrolls on
 * the wheel and its rail jumps on a click, the same interaction the recipe pages offer.</p>
 */
public class GTPortScreen extends AbstractContainerScreen<GTPortMenu> {

    private static final ResourceLocation IO_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID,
                    "textures/gui/gt_port_io.png");
    private static final ResourceLocation ENERGY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID,
                    "textures/gui/gt_port_energy.png");

    // ---- panel measurements, all in sheet pixels ----

    /** The three free-form fields. */
    private static final int FIELD_X = 9;
    private static final int FIELD_WIDTH = 61;
    private static final int FIELD_HEIGHT = 9;
    private static final int FIELD_MODE_Y = 33;
    private static final int FIELD_CHANNEL_Y = 55;
    private static final int FIELD_PRIORITY_Y = 77;
    private static final int LABEL_GAP = 2;

    /** The port's name, in the black strip across the top middle. */
    private static final int NAME_X = 70;
    private static final int NAME_Y = 11;
    private static final int NAME_WIDTH = 70;

    /** Capacity readout, to the right of the name. */
    private static final int CAPTION_X = 146;
    private static final int CAPTION_Y = 9;

    /** Storage grid's scroll rail, on the IO sheet. */
    private static final int RAIL_X = 140;
    private static final int RAIL_Y = 28;
    private static final int RAIL_WIDTH = 8;
    private static final int RAIL_HEIGHT = 58;

    /**
     * The horizontal energy trough on the energy sheet, and the sprite it is filled from.
     *
     * <p>Laid out exactly like the machine's own trough: a 4 px first cell followed by 20 more at
     * 3 px each, so the fill is a run of lit cells rather than a smooth slice. The window is 61 px
     * wide, which is what those twenty cells add up to.</p>
     */
    private static final int TROUGH_X = 82;
    private static final int TROUGH_Y = 32;
    private static final int TROUGH_HEIGHT = 11;
    private static final int TROUGH_SPRITE_U = 86;
    private static final int TROUGH_SPRITE_V = 178;
    private static final int TROUGH_SPRITE_WIDTH = 61;
    private static final int TROUGH_FIRST_CELL = 4;
    private static final int TROUGH_CELL_STEP = 3;
    private static final int TROUGH_CELLS = 20;

    /** The four caption lines under the trough, left-aligned with it. */
    private static final int ENERGY_TEXT_X = 82;
    private static final int ENERGY_TEXT_TOP = 46;

    /** The run switch beside the player's grid, drawn from the same sprite pair the machine uses. */
    private static final int RUN_X = 178;
    private static final int RUN_Y = 95;
    private static final int RUN_WIDTH = 20;
    private static final int RUN_HEIGHT = 9;
    private static final int RUN_SPRITE_U = 0;
    private static final int RUN_SPRITE_PRESSED_U = 22;
    private static final int RUN_SPRITE_V = 178;
    /** The base sprite is 22 by 11 with a transparent margin around the 20 by 9 cell. */
    private static final int RUN_SPRITE_WIDTH = 22;
    private static final int RUN_SPRITE_HEIGHT = 11;
    private static final int RUN_SPRITE_DX = -1;
    private static final int RUN_SPRITE_DY = -1;

    /**
     * The icon overlay, on the same 22 by 11 footprint as the base plate.
     *
     * <p>The icons are drawn at the plate's size with transparent padding around them, so they can
     * simply be layered over the plate at the same offset rather than positioned by hand. Green
     * triple arrow while the port takes part, yellow pause bar while it does not.</p>
     */
    private static final int RUN_ICON_U_RUN = 0;
    private static final int RUN_ICON_U_STOP = 22;
    private static final int RUN_ICON_V = 189;
    private static final int RUN_ICON_WIDTH = 22;
    private static final int RUN_ICON_HEIGHT = 11;

    /**
     * The reach-through switch, on the first plate of the top-right block.
     *
     * <p>At {@code (154,27)}, the slot the stacking switch used to occupy. That switch is gone 鈥?an
     * energy port has no cells for an entry to span, so the setting controlled nothing there 鈥?and
     * shifting this one left keeps the buttons on plates the sheet actually has instead of leaving a
     * hole at the front of the row.</p>
     *
     * <p>One plate from the sheet, with the state shown by which of two icons is laid over it:
     * yellow is passive (the plain behaviour, where the port waits to be reached), red is active (it
     * also reaches through its own face).</p>
     */
    private static final int ACTIVE_X = 154;
    private static final int ACTIVE_Y = 27;
    private static final int ACTIVE_PLATE_U = 44;
    private static final int ACTIVE_PLATE_V = 178;
    private static final int ACTIVE_WIDTH = 21;
    private static final int ACTIVE_HEIGHT = 17;
    private static final int ACTIVE_ICON_U_PASSIVE = 44;
    private static final int ACTIVE_ICON_U_ACTIVE = 65;
    private static final int ACTIVE_ICON_V = 212;

    /** Text drawn directly on the light panel, which needs the darker ink to stay readable. */
    private static final int INK_COLOUR = 0xFF404040;

    /** White, for text drawn inside the dark wells. */
    private static final int TEXT_COLOUR = 0xFFFFFFFF;
    /** Resting state of a field: a dim grey, clearly apart from the white used while editing. */
    private static final int INACTIVE_TEXT_COLOUR = 0xFF8A8A8A;
    /** Captions sit on the light panel, so they keep the darker ink. */
    private static final int LABEL_COLOUR = INK_COLOUR;
    private static final int CAPTION_COLOUR = INK_COLOUR;

    /** Secondary captions are drawn smaller so two of them fit under their headings. */
    private static final float CAPTION_SCALE = 0.75F;

    private static final double MARQUEE_PIXELS_PER_SECOND = 28.0D;
    private static final long MARQUEE_PAUSE_MILLIS = 1200L;
    private static final int MARQUEE_GAP = 24;

    /** Scrollbar colours, matching the recipe page's plain rectangles. */
    private static final int RAIL_TRACK = 0xFF373737;
    private static final int RAIL_BORDER = 0xFF8B8B8B;
    private static final int RAIL_MARKER = 0xFFC6C6C6;
    private static final int MARKER_MIN_HEIGHT = 10;

    private EditBox nameField;
    private EditBox modeField;
    private EditBox channelField;
    private EditBox priorityField;
    private List<EditBox> fields = List.of();

    /** First visible row of the storage grid. */
    private int scrollRow;

    /**
     * Cursor position from the last frame, in panel coordinates.
     *
     * <p>Recorded here because a key press callback carries no coordinates, and the tooltip code
     * needs to know which stack the player is pointing at when R or U is pressed.</p>
     */
    private double lastMouseX = -1;
    private double lastMouseY = -1;

    /**
     * A name that has been sent but not yet echoed back by the server.
     *
     * <p>Without this the box flickered: the packet takes a round trip, and until it lands the local
     * entity still reports the old name, so the echo below would write that old value straight back
     * into the field and the player would see their edit vanish for a frame or two. Holding the sent
     * value here lets the box ignore the stale reading until the server catches up.</p>
     */
    @Nullable
    private String pendingName;

    /**
     * Values sent but not yet echoed back, keyed by field.
     *
     * <p>Same problem as the name: the port entity only reports a field once the packet lands, so
     * until then the echo read the old text and wrote it straight back into the box 鈥?the edit
     * appeared to bounce for a frame or two. Holding each sent value here keeps the box steady
     * until the server catches up, and it also covers a value the server normalises on save.</p>
     */
    private final Map<GTPortBlockEntity.Field, String> pendingFields =
            new EnumMap<>(GTPortBlockEntity.Field.class);

    public GTPortScreen(GTPortMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GTPortMenu.IMAGE_WIDTH;
        this.imageHeight = GTPortMenu.IMAGE_HEIGHT;
        // The text boxes are built in init(), not here: they take this screen's font, and the font
        // is only assigned when the screen is initialised. Building them now would pass null and
        // crash the first time one was rendered.
    }

    private EditBox makeField(GTPortBlockEntity.Field field, String initial) {
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
        return box;
    }

    @Override
    protected void init() {
        super.init();
        this.nameField = makeNameField();
        this.modeField = makeField(GTPortBlockEntity.Field.MODE, this.menu.modeText());
        this.channelField = makeField(GTPortBlockEntity.Field.CHANNEL, this.menu.channelText());
        this.priorityField = makeField(GTPortBlockEntity.Field.PRIORITY, this.menu.priorityText());
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
     * swallows typing 鈥?the server refuses the edit too, so a modified client gains nothing. The
     * other two fields stay live: they carry no meaning to the matcher yet.</p>
     */
    private void refreshChannelLock() {
        if (this.channelField == null) {
            return;
        }
        GTPortBlockEntity port = this.menu.port();
        boolean locked = port != null && port.isChannelLocked();
        this.channelField.setEditable(!locked);
        this.channelField.setTextColor(locked ? INACTIVE_TEXT_COLOUR : TEXT_COLOUR);
    }

    /**
     * The renaming box, laid over the name strip.
     *
     * <p>Same styling as the three counters but a different footprint: it spans the strip the title
     * used to be drawn in, so renaming replaces the label rather than sitting beside it.</p>
     */
    private EditBox makeNameField() {
        EditBox box = new EditBox(this.font, 0, 0, NAME_WIDTH, this.font.lineHeight,
                Component.translatable("gui.goldentweaks.port.name"));
        box.setMaxLength(GTPortBlockEntity.TEXT_LIMIT);
        box.setBordered(false);
        box.setTextColor(TEXT_COLOUR);
        box.setTextColorUneditable(INACTIVE_TEXT_COLOUR);
        box.setValue(this.menu.port() == null ? "" : this.menu.port().getDisplayName().getString());
        box.setResponder(value -> {
        });
        return box;
    }

    private void placeNameField() {
        this.nameField.setX(this.leftPos + NAME_X);
        this.nameField.setY(this.topPos + NAME_Y);
        addRenderableWidget(this.nameField);
    }

    private void place(EditBox box, int y) {
        box.setX(this.leftPos + FIELD_X);
        box.setY(this.topPos + y);
        addRenderableWidget(box);
    }

    // ---- text field plumbing ----

    private void commit() {
        // Guarded because the boxes only exist after init(): a screen that is closed without ever
        // being initialised would otherwise reach for them here.
        if (this.modeField == null) {
            return;
        }
        commitName();
        send(GTPortBlockEntity.Field.MODE, this.modeField.getValue());
        send(GTPortBlockEntity.Field.CHANNEL, this.channelField.getValue());
        send(GTPortBlockEntity.Field.PRIORITY, this.priorityField.getValue());
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
        if (port == null) {
            return;
        }
        String typed = this.nameField.getValue();
        String shown = port.getDisplayName().getString();
        boolean clearing = typed.isBlank() && port.getCustomName() != null;
        boolean renaming = !typed.isBlank() && !typed.equals(shown);
        if (clearing || renaming) {
            this.pendingName = clearing ? "" : typed;
            PacketDistributor.sendToServer(
                    new C2SPortTextPacket(port.getBlockPos(), GTPortBlockEntity.Field.NAME,
                            clearing ? "" : typed));
        }
    }

    private void send(GTPortBlockEntity.Field field, String value) {
        GTPortBlockEntity port = this.menu.port();
        if (port == null) {
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
            String baseline = this.pendingFields.containsKey(field)
                    ? this.pendingFields.get(field) : current;
            if (baseline.equals(value)) {
                return;
            }
            this.pendingFields.put(field, value);
            PacketDistributor.sendToServer(new C2SPortTextPacket(port.getBlockPos(), field, value));
        }
    }

    @Override
    public void onClose() {
        commit();
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean insideField = false;
        for (EditBox box : this.fields) {
            if (box.isMouseOver(mouseX, mouseY)) {
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

        // Clicking the rail jumps to that position, the way a page scrollbar behaves.
        if (button == 0 && this.menu.kind() != GTPortKind.ENERGY
                && mouseX >= this.leftPos + RAIL_X && mouseX < this.leftPos + RAIL_X + RAIL_WIDTH
                && mouseY >= this.topPos + RAIL_Y && mouseY < this.topPos + RAIL_Y + RAIL_HEIGHT) {
            scrollTo(mouseY);
            return true;
        }

        // The reach-through switch, handled exactly like the other two.
        if (button == 0
                && mouseX >= this.leftPos + ACTIVE_X && mouseX < this.leftPos + ACTIVE_X + ACTIVE_WIDTH
                && mouseY >= this.topPos + ACTIVE_Y && mouseY < this.topPos + ACTIVE_Y + ACTIVE_HEIGHT) {
            GTPortBlockEntity port = this.menu.port();
            if (port != null) {
                PacketDistributor.sendToServer(new C2SPortTogglePacket(
                        port.getBlockPos(), C2SPortTogglePacket.Target.ACTIVE));
            }
            return true;
        }

        // The run switch flips participation; the server applies the change and reports it back.
        if (button == 0
                && mouseX >= this.leftPos + RUN_X && mouseX < this.leftPos + RUN_X + RUN_WIDTH
                && mouseY >= this.topPos + RUN_Y && mouseY < this.topPos + RUN_Y + RUN_HEIGHT) {
            GTPortBlockEntity port = this.menu.port();
            if (port != null) {
                PacketDistributor.sendToServer(new C2SPortTogglePacket(
                        port.getBlockPos(), C2SPortTogglePacket.Target.ENABLED));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // R and U only mean anything while the cursor is over a stack, and only when no text box
        // has focus 鈥?otherwise typing an "r" into a field would open a recipe page.
        if (!this.fields.isEmpty() && !anyFieldFocused() && keyOverStack(keyCode, scanCode)) {
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            for (EditBox box : this.fields) {
                if (box.isFocused()) {
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
        if (this.menu.kind() == GTPortKind.ENERGY) {
            return -1;
        }
        for (int cell = 0; cell < GTPortMenu.STORAGE_CELLS; cell++) {
            int column = cell % GTPortMenu.STORAGE_COLUMNS;
            int row = cell / GTPortMenu.STORAGE_COLUMNS;
            int x = this.leftPos + GTPortMenu.STORAGE_X + column * GTPortMenu.SLOT_STEP;
            int y = this.topPos + GTPortMenu.STORAGE_Y + row * GTPortMenu.SLOT_STEP;
            if (mouseX >= x && mouseX < x + GTPortMenu.SLOT_SIZE
                    && mouseY >= y && mouseY < y + GTPortMenu.SLOT_SIZE) {
                int index = this.scrollRow * GTPortMenu.STORAGE_COLUMNS + cell;
                return index < contents().size() ? index : -1;
            }
        }
        return -1;
    }

    /** The EMI form of one entry, or {@code null} when EMI is absent or cannot build it. */
    @Nullable
    private EmiIngredient ingredientOf(GTReactionStack stack) {
        if (!ModList.get().isLoaded("emi")) {
            return null;
        }
        return GTReactionStacks.toEmiIngredient(stack, registries());
    }

    /**
     * Shows the stack's own tooltip, the same one EMI shows in a recipe page.
     *
     * <p>Built from the EMI stack rather than from the item, so a fluid or a chemical gets the
     * tooltip it would have there instead of a bare name.</p>
     */
    private void renderStackTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
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
                var lines = stacks.get(0).getTooltipText();
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
     * Explains the run switch while the cursor is over it.
     *
     * <p>The icon is a pair of arrows or a pause bar with no text of its own, so the label has to
     * say what the button does and which way it is currently set 鈥?the same treatment the machine's
     * own buttons get.</p>
     */
    private void renderRunTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + RUN_X;
        int y = this.topPos + RUN_Y;
        if (mouseX < x || mouseX >= x + RUN_WIDTH || mouseY < y || mouseY >= y + RUN_HEIGHT) {
            return;
        }
        boolean enabled = this.menu.port() != null && this.menu.port().isEnabled();
        graphics.renderTooltip(this.font, Component.translatable(enabled
                ? "gui.goldentweaks.port.running.on"
                : "gui.goldentweaks.port.running.off"), mouseX, mouseY);
    }

    /** Explains the reach-through switch, whose icon alone does not say which way round it is. */
    private void renderActiveTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + ACTIVE_X;
        int y = this.topPos + ACTIVE_Y;
        if (mouseX < x || mouseX >= x + ACTIVE_WIDTH || mouseY < y || mouseY >= y + ACTIVE_HEIGHT) {
            return;
        }
        graphics.renderTooltip(this.font, Component.translatable(this.menu.isActivePort()
                ? "gui.goldentweaks.port.active.on"
                : "gui.goldentweaks.port.active.off"), mouseX, mouseY);
    }

    /** Whether any of the text boxes currently has focus. */
    private boolean anyFieldFocused() {
        for (EditBox box : this.fields) {
            if (box.isFocused()) {
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
        if (dev.emi.emi.config.EmiConfig.viewRecipes.matchesKey(keyCode, scanCode)) {
            EmiApi.displayRecipes(ingredient);
            return true;
        }
        if (dev.emi.emi.config.EmiConfig.viewUses.matchesKey(keyCode, scanCode)) {
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
    private int totalRows() {
        return Math.max(1, Mth.ceil(contents().size() / (float) GTPortMenu.STORAGE_COLUMNS));
    }

    private int maxScrollRow() {
        return Math.max(0, totalRows() - GTPortMenu.STORAGE_ROWS);
    }

    private List<GTReactionStack> contents() {
        return this.menu.contents();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.menu.kind() == GTPortKind.ENERGY || scrollY == 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        // One notch moves one row: a short list needs only a nudge, and a long one still travels
        // quickly because the wheel repeats.
        this.scrollRow = Mth.clamp(this.scrollRow + (scrollY > 0 ? -1 : 1), 0, maxScrollRow());
        return true;
    }

    /** Jumps to the row under {@code mouseY}, for a click on the rail. */
    private void scrollTo(double mouseY) {
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        refreshChannelLock();
        syncChannelEcho();
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderStackTooltip(graphics, mouseX, mouseY);
        renderRunTooltip(graphics, mouseX, mouseY);
        renderActiveTooltip(graphics, mouseX, mouseY);
    }

    /**
     * Shows the stored channel in the field once the server has normalised it.
     *
     * <p>A mistyped list is corrected to {@code 0} on save, and without this the box would keep
     * showing the mistake while the port actually behaves as a wildcard. The field is only
     * rewritten when it is not being edited, so a correction never fights the cursor.</p>
     */
    private void syncChannelEcho() {
        if (this.channelField == null || this.channelField.isFocused()) {
            return;
        }
        GTPortBlockEntity port = this.menu.port();
        if (port == null) {
            return;
        }
        echo(this.modeField, GTPortBlockEntity.Field.MODE, port.modeText());
        echo(this.channelField, GTPortBlockEntity.Field.CHANNEL, port.channelText());
        echo(this.priorityField, GTPortBlockEntity.Field.PRIORITY, port.priorityText());

        if (this.nameField != null && !this.nameField.isFocused()) {
            String shown = port.getDisplayName().getString();
            if (this.pendingName != null) {
                // Ignore the entity until it reports what was sent. An empty pending value means the
                // name was cleared, which reads back as the block's default wording.
                boolean landed = this.pendingName.isEmpty()
                        ? port.getCustomName() == null
                        : shown.equals(this.pendingName);
                if (landed) {
                    this.pendingName = null;
                }
                return;
            }
            if (!shown.equals(this.nameField.getValue())) {
                this.nameField.setValue(shown);
            }
        }
    }

    /**
     * Writes a field's stored value into its box, unless a value is still in flight.
     *
     * <p>While a send is outstanding the box keeps the text the player typed: the port still
     * reports the old value, and writing that back is exactly what made it flash. Once the port
     * reports what was sent the pending record clears and ordinary echoing resumes 鈥?including a
     * normalised value, which simply differs from the sent one and so lands as the correction.</p>
     */
    private void echo(@Nullable EditBox box, GTPortBlockEntity.Field field, String stored) {
        if (box == null || box.isFocused()) {
            return;
        }
        if (this.pendingFields.containsKey(field)) {
            if (!stored.equals(this.pendingFields.get(field))) {
                return;
            }
            this.pendingFields.remove(field);
        }
        if (!stored.equals(box.getValue())) {
            box.setValue(stored);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        boolean energy = this.menu.kind() == GTPortKind.ENERGY;
        graphics.blit(energy ? ENERGY_TEXTURE : IO_TEXTURE, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight);

        drawLabels(graphics);

        if (energy) {
            drawEnergyTrough(graphics);
        } else {
            drawStacks(graphics);
            drawRail(graphics);
        }

        drawRunButton(graphics, mouseX, mouseY);
        // The stacking switch is deliberately absent: an energy port has no cells for an entry to
        // span, so the setting had nothing to control there.
        drawActiveButton(graphics, mouseX, mouseY);
    }

    /** The reach-through switch: plate, then the icon for the state it is in. */
    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + ACTIVE_X;
        int y = this.topPos + ACTIVE_Y;
        graphics.blit(IO_TEXTURE, x, y, ACTIVE_PLATE_U, ACTIVE_PLATE_V,
                ACTIVE_WIDTH, ACTIVE_HEIGHT);
        boolean active = this.menu.isActivePort();
        graphics.blit(IO_TEXTURE, x, y,
                active ? ACTIVE_ICON_U_ACTIVE : ACTIVE_ICON_U_PASSIVE, ACTIVE_ICON_V,
                ACTIVE_WIDTH, ACTIVE_HEIGHT);
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

    /**
     * The visible slice of the storage grid.
     *
     * <p>Only occupied cells are drawn: the frame drawn by the sheet already marks the grid, and
     * empty boxes would add nothing but noise.</p>
     *
     * <p>How many cells an entry takes is the stacking mode's whole effect. In single-cell mode each
     * entry is one cell wide, so the grid is a plain row-major list. In multi-cell mode an entry
     * occupies as many cells as it needs, measured as the ceiling of its amount over the slot
     * ceiling 鈥?a stack of 64 in a 64-per-slot bus takes one cell, a stack of 192 takes three.</p>
     */
    private void drawStacks(GuiGraphics graphics) {
        List<GTReactionStack> rows = contents();
        if (rows.isEmpty()) {
            return;
        }
        HolderLookup.Provider registries = registries();
        var context = dev.emi.emi.runtime.EmiDrawContext.wrap(graphics);

        int perSlot = Math.max(1, this.menu.slotLimit());
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
            // The amount is a long while the ceiling is an int, so the division is done wide and
            // then clamped: an entry can never need more cells than the grid has.
            int span = this.menu.isSingleCellMode() ? 1 : (int) Math.max(1L,
                    Math.min(GTPortMenu.STORAGE_CELLS, (stack.amount() + perSlot - 1) / perSlot));
            // Skip entries above the visible window, and clip one that starts inside it.
            if (cursor + span <= this.scrollRow * columns) {
                cursor += span;
                continue;
            }
            for (int cell = 0; cell < span && drawn < capacity; cell++) {
                int absolute = cursor + cell;
                int windowCell = absolute - this.scrollRow * columns;
                if (windowCell < 0) {
                    continue;
                }
                if (windowCell >= capacity) {
                    break;
                }
                int column = windowCell % columns;
                int row = windowCell / columns;
                int x = this.leftPos + GTPortMenu.STORAGE_X + column * GTPortMenu.SLOT_STEP;
                int y = this.topPos + GTPortMenu.STORAGE_Y + row * GTPortMenu.SLOT_STEP;
                drawStack(graphics, context, stack, x, y, registries);
                drawn++;
            }
            cursor += span;
        }
    }

    /** One stack: the icon through EMI, then its amount on the corner, as EMI draws a slot. */
    private void drawStack(GuiGraphics graphics, dev.emi.emi.runtime.EmiDrawContext context,
                           GTReactionStack stack, int x, int y,
                           @Nullable HolderLookup.Provider registries) {
        if (!ModList.get().isLoaded("emi")) {
            if (stack.kind() == GTReactionStack.Kind.ITEM) {
                var item = stack.toItemStack();
                if (!item.isEmpty()) {
                    graphics.renderItem(item, x, y);
                    graphics.renderItemDecorations(this.font, item, x, y);
                }
            }
            return;
        }

        var ingredient = GTReactionStacks.toEmiIngredient(stack, registries);
        if (ingredient == null) {
            return;
        }
        // Icon only: a stack's own render prints a count for the item flavour alone, so the amount
        // is added below through the very helper EMI uses for that.
        ingredient.render(graphics, x, y, 0.0F, dev.emi.emi.api.stack.EmiIngredient.RENDER_ICON);

        dev.emi.emi.EmiRenderHelper.renderAmount(context, x, y,
                Component.literal(GTPortAmounts.number(stack.amount(),
                        stack.kind() == GTReactionStack.Kind.ITEM)));
    }

    /** The rail beside the grid: a track plus a marker whose size shows how much fits. */
    private void drawRail(GuiGraphics graphics) {
        int x = this.leftPos + RAIL_X;
        int y = this.topPos + RAIL_Y;
        int w = RAIL_WIDTH;
        int h = RAIL_HEIGHT;

        graphics.fill(x, y, x + w, y + h, RAIL_BORDER);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, RAIL_TRACK);

        int inner = h - 2;
        int total = Math.max(totalRows(), GTPortMenu.STORAGE_ROWS);
        int marker = Math.max(MARKER_MIN_HEIGHT, Math.min(inner,
                Math.round(inner * (float) GTPortMenu.STORAGE_ROWS / (float) total)));
        int travel = inner - marker;
        int max = maxScrollRow();
        int markerY = y + 1 + (max <= 0 ? 0 : Math.round(travel * (float) this.scrollRow / (float) max));

        graphics.fill(x + 2, markerY, x + w - 2, markerY + marker, RAIL_MARKER);
    }

    /**
     * The horizontal energy trough, lit in the same steps the machine's own trough uses.
     *
     * <p>Each cell is five percent, so the count is the stored fraction times twenty, rounded down:
     * seven percent lights one cell and ninety-nine percent lights nineteen. Drawing the run as one
     * blit of the appropriate width is what the machine screen does, and it keeps the two bars in
     * step rather than letting one fill smoothly and the other in jumps.</p>
     */
    private void drawEnergyTrough(GuiGraphics graphics) {
        int capacity = this.menu.syncedCapacity();
        int stored = this.menu.syncedAmount();
        int x = this.leftPos + TROUGH_X;
        int y = this.topPos + TROUGH_Y;

        int cells = capacity <= 0 || stored <= 0
                ? 0 : (int) Math.min(TROUGH_CELLS, (long) stored * TROUGH_CELLS / capacity);
        if (cells > 0) {
            int width = TROUGH_FIRST_CELL + (cells - 1) * TROUGH_CELL_STEP;
            graphics.blit(ENERGY_TEXTURE, x, y,
                    TROUGH_SPRITE_U, TROUGH_SPRITE_V, width, TROUGH_HEIGHT);
        }

        drawEnergyCaptions(graphics);
    }

    /**
     * The run switch: base plate from the port sheet, icon from the machine's.
     *
     * <p>Two sheets are involved because the port sheets carry the button's base but not its
     * icons 鈥?the green triple arrow and the yellow pause bar live only on the machine sheet, in
     * the row the refresh hammer occupies. Blitting the base from ours and the icon from the
     * machine's is what makes the port's switch look like the machine's switch rather than an
     * empty plate.</p>
     */
    private void drawRunButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + RUN_X;
        int y = this.topPos + RUN_Y;
        boolean held = mouseX >= x && mouseX < x + RUN_WIDTH
                && mouseY >= y && mouseY < y + RUN_HEIGHT;

        // Base plate: the sprite is 22 by 11 with a transparent margin around a 20 by 9 cell.
        int baseU = held ? RUN_SPRITE_PRESSED_U : RUN_SPRITE_U;
        graphics.blit(IO_TEXTURE, x + RUN_SPRITE_DX, y + RUN_SPRITE_DY,
                baseU, RUN_SPRITE_V, RUN_SPRITE_WIDTH, RUN_SPRITE_HEIGHT);

        // Icon: green arrows while the port takes part, yellow pause while it does not. It shares
        // the plate's footprint, so it goes down at the same offset and layers over it.
        boolean enabled = this.menu.port() != null && this.menu.port().isEnabled();
        int iconU = enabled ? RUN_ICON_U_RUN : RUN_ICON_U_STOP;
        graphics.blit(IO_TEXTURE, x + RUN_SPRITE_DX, y + RUN_SPRITE_DY,
                iconU, RUN_ICON_V, RUN_ICON_WIDTH, RUN_ICON_HEIGHT);
    }

    /**
     * The four caption lines under the trough, left-aligned with it.
     *
     * <p>Two headings and two values: how much is stored and how fast the port accepts more. They
     * start at the trough's own left edge so the bar and the block read as one column.</p>
     */
    private void drawEnergyCaptions(GuiGraphics graphics) {
        // Both figures go through the translations, units included: the port window and the machine
        // tooltips have to spell "1.06 kFE/t" the same way, and only one place can own that wording.
        Component storedText = Component.translatable("gui.goldentweaks.port.energy.stored_value",
                GTEnergyFormat.formatWithUnit(this.menu.syncedAmount(), "FE"),
                GTEnergyFormat.formatWithUnit(this.menu.syncedCapacity(), "FE"));
        Component rateText = Component.translatable("gui.goldentweaks.port.energy.rate_value",
                GTEnergyFormat.formatWithUnit(this.menu.syncedRate(), "FE/t"));

        int x = this.leftPos + ENERGY_TEXT_X;
        int headingLine = this.font.lineHeight;
        int scaledLine = Math.round(headingLine * CAPTION_SCALE);
        int spacing = 1;
        int cursor = this.topPos + ENERGY_TEXT_TOP;

        graphics.drawString(this.font,
                Component.translatable("gui.goldentweaks.port.energy.stored"),
                x, cursor, CAPTION_COLOUR, false);
        cursor += headingLine + spacing;

        graphics.pose().pushPose();
        graphics.pose().translate(x, cursor, 0.0F);
        graphics.pose().scale(CAPTION_SCALE, CAPTION_SCALE, 1.0F);
        graphics.drawString(this.font, storedText, 0, 0, CAPTION_COLOUR, false);
        graphics.pose().popPose();
        cursor += scaledLine + spacing;

        // The rate reads differently depending on which way the port faces: an input hatch takes
        // energy in, a dynamo hatch pushes it out, and calling both "max input" would mislabel half
        // the ports.
        boolean output = this.menu.port() != null
                && this.menu.port().mode() == net.weibai.avaritia_more_items
                        .common.blocks.abstracts.multiblock.port.PortMode.OUTPUT;
        graphics.drawString(this.font,
                Component.translatable(output
                        ? "gui.goldentweaks.port.energy.output"
                        : "gui.goldentweaks.port.energy.input"),
                x, cursor, CAPTION_COLOUR, false);
        cursor += headingLine + spacing;

        graphics.pose().pushPose();
        graphics.pose().translate(x, cursor, 0.0F);
        graphics.pose().scale(CAPTION_SCALE, CAPTION_SCALE, 1.0F);
        graphics.drawString(this.font, rateText, 0, 0, CAPTION_COLOUR, false);
        graphics.pose().popPose();
    }

    @Nullable
    private static HolderLookup.Provider registries() {
        var level = Minecraft.getInstance().level;
        return level == null ? null : level.registryAccess();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // The pose is already translated to the panel origin here, so these use panel coordinates.
        if (this.menu.kind() != GTPortKind.ENERGY) {
            drawCapacity(graphics);
        }
    }

    /**
     * The name, scrolling when it does not fit.
     *
     * <p>The strip is only 70 px wide, and a port named by its tier and kind easily outgrows that,
     * so the text travels instead of being clipped.</p>
     */
    private void drawMarquee(GuiGraphics graphics, Component text, int x, int y, int width) {
        int textWidth = this.font.width(text);
        if (textWidth <= width) {
            graphics.drawString(this.font, text, x, y, TEXT_COLOUR, false);
            return;
        }
        int cycle = textWidth + MARQUEE_GAP;
        int offset = marqueeOffset(cycle);
        graphics.enableScissor(this.leftPos + x, this.topPos + y,
                this.leftPos + x + width, this.topPos + y + this.font.lineHeight);
        graphics.drawString(this.font, text, x - offset, y, TEXT_COLOUR, false);
        graphics.drawString(this.font, text, x - offset + cycle, y, TEXT_COLOUR, false);
        graphics.disableScissor();
    }

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
     * The capacity readout beside the name, as two left-aligned lines.
     *
     * <p>The heading is gone: at this width "瀹归噺" plus two figures did not fit, and the figures say
     * what they are well enough on their own. Dropping it lets both lines start where the heading
     * used to, which is the only place with room for a seven-digit fraction.</p>
     */
    private void drawCapacity(GuiGraphics graphics) {
        int x = CAPTION_X;

        // Compact rather than every digit: a high-tier hatch holds hundreds of thousands of mB, and
        // the exact figure is unreadable at this size. The same ladder the energy port uses, so a
        // number reads the same way across the machine's windows.
        String unit = this.menu.kind() == GTPortKind.FLUID ? " mB" : "";
        String amount = GTEnergyFormat.format(this.menu.syncedAmount())
                + " / " + GTEnergyFormat.format(this.menu.syncedCapacity()) + unit;
        String cells = this.menu.usedCells() + " / " + this.menu.totalCells() + " "
                + Component.translatable("gui.goldentweaks.port.cells").getString();

        graphics.pose().pushPose();
        graphics.pose().translate(x, CAPTION_Y, 0.0F);
        graphics.pose().scale(CAPTION_SCALE, CAPTION_SCALE, 1.0F);
        graphics.drawString(this.font, amount, 0, 0, CAPTION_COLOUR, false);
        graphics.drawString(this.font, cells, 0, this.font.lineHeight, CAPTION_COLOUR, false);
        graphics.pose().popPose();
    }

    /** Registers the screen against the menu type. */
    @EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
    public static final class Registrar {

        private Registrar() {
        }

        @SubscribeEvent
        public static void onRegisterScreens(RegisterMenuScreensEvent event) {
            event.register(GTPortMenu.MENU.get(), GTPortScreen::new);
        }
    }
}
