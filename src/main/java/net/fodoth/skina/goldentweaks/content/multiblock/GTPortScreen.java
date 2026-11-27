package net.fodoth.skina.goldentweaks.content.multiblock;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTEnergyFormat;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStacks;
import net.fodoth.skina.goldentweaks.network.packet.C2SPortTextPacket;
import net.fodoth.skina.goldentweaks.network.packet.C2SPortTogglePacket;
import net.fodoth.skina.goldentweaks.util.GTNumberInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
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
import dev.emi.emi.api.stack.EmiStack;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client.GTAspectEmiStacks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.EmiRenderHelper;
import mekanism.api.MekanismAPI;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftAdditionalItems;
import net.minecraft.Util;
import net.minecraft.world.level.material.Fluid;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.PortMode;
import thaumcraft.client.gui.AspectGuiRenderer;
import thaumcraft.api.aspects.Aspect;

/**
 * Window for one of this mod's ports.
 *
 * <p>Everything is positioned from the sheet's own pixels: the panels occupy
 * {@code (0,0)..(210,178)} — eleven pixels taller for a general assembly — and their sprites sit
 * below that. Coordinates are constants rather than computed, which is what keeps the three text
 * fields, the storage area and the player's slots aligned with the art.</p>
 *
 * <p>There are three sheets. Item and fluid ports use {@code gt_port_io}, which carries a 3 by 3
 * storage grid; energy ports use {@code gt_port_energy}, where that grid and its rail are replaced
 * by an upright trough; a general assembly uses
 * {@link #GENERAL_TEXTURE} and draws <em>both</em> halves, since it holds contents and energy at
 * once, plus a third switch plate for its channel.</p>
 *
 * <p>The sheets are separate drawings rather than one stretched copy, so every plate, icon and
 * trough has its own coordinates per sheet. They are read through the small accessors below
 * ({@link #modeY()}, {@link #plateV()}, …) which the drawing, the click handler and the tooltip all
 * call — that is what stops a plate from being painted in one place and answering the cursor in
 * another.</p>
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
    /**
     * The general assembly's sheet: the same panel plus a trough row and a third switch plate.
     *
     * <p>Its own file rather than a stretched version of the other two. An assembly holds energy as
     * well as contents, so it needs both the storage grid and the trough — and a third plate, since
     * the reach-through switch has nothing to reach with on a part that is not a hatch of one kind.
     * Drawing all that from the item sheet would mean three conditional offsets on every constant;
     * a sheet of its own keeps the numbers literal.</p>
     */
    private static final ResourceLocation GENERAL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID,
                    "textures/gui/gt_port_io_general.png");

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
     * The horizontal energy trough, and the sprite it is filled from.
     *
     * <p>Laid out exactly like the machine's own trough: a 4 px first cell followed by 20 more at
     * 3 px each, so the fill is a run of lit cells rather than a smooth slice. The window is 61 px
     * wide, which is what those twenty cells add up to.</p>
     *
     * <p>These are the <em>single-kind</em> rows; an assembly's trough sits elsewhere and is cut from
     * its own sheet — see {@link #GENERAL_TROUGH_X}. The cell arithmetic is shared, because the two
     * troughs are the same shape.</p>
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

    /**
     * The same trough on an assembly's sheet.
     *
     * <p>Its own coordinates, not the band offset: the trough is the row the rest of the band was
     * moved down for, so it sits where the single-kind panel used to end rather than eleven pixels
     * from anything.</p>
     */
    private static final int GENERAL_TROUGH_X = 139;
    private static final int GENERAL_TROUGH_Y = 90;
    private static final int GENERAL_TROUGH_SPRITE_V = 189;

    /**
     * The four caption lines under the trough, left-aligned with it.
     *
     * <p>Drawn only on the energy hatches; an assembly's window quotes its own ceilings above the
     * grid, so it carries the trough alone.</p>
     */
    private static final int ENERGY_TEXT_X = 82;
    private static final int ENERGY_TEXT_TOP = 46;

    /** The run switch beside the player's grid, drawn from the same sprite pair the machine uses. */
    private static final int RUN_X = 178;
    private static final int RUN_Y = 95;
    /** The same switch on an assembly's sheet, whose band sits lower. */
    private static final int GENERAL_RUN_Y = 106;
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
    /** The same overlay on an assembly's sheet. */
    private static final int GENERAL_RUN_ICON_V = 200;
    private static final int RUN_ICON_WIDTH = 22;
    private static final int RUN_ICON_HEIGHT = 11;

    /**
     * The stacking switch, on the first plate of the top-right block.
     *
     * <p>Only shown by item and fluid ports: an energy port has no cells for an entry to span, so
     * the setting controls nothing there and its plate is given to the reach-through switch
     * instead.</p>
     */
    private static final int MODE_X = 154;
    private static final int MODE_Y = 27;
    private static final int MODE_PLATE_U = 44;
    private static final int MODE_PLATE_PRESSED_U = 65;
    private static final int MODE_PLATE_V = 178;
    private static final int MODE_WIDTH = 21;
    private static final int MODE_HEIGHT = 17;
    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 65;
    private static final int MODE_ICON_V = 195;

    /**
     * The same switch on an assembly's sheet, as its own set of numbers.
     *
     * <p>A set rather than an offset: the sheet is drawn art, not a shifted copy, so its plate and
     * icon rows were re-cut at their own coordinates. One offset applied to everything assumed they
     * moved together, and putting the drawn plate somewhere the cursor did not follow is what made
     * the buttons look misplaced.</p>
     */
    private static final int GENERAL_MODE_X = 154;
    private static final int GENERAL_MODE_Y = 27;
    private static final int GENERAL_PLATE_V = 189;
    private static final int GENERAL_MODE_ICON_V = 206;

    /**
     * The reach-through switch: second plate for item and fluid ports, first for an energy port.
     *
     * <p>Sitting it after the stacking switch keeps the row contiguous; an energy port has no
     * stacking switch, so it takes the first plate rather than leaving a gap.</p>
     */
    private static final int ACTIVE_X = 179;
    /** Where the reach-through switch goes when there is no stacking switch in front of it. */
    private static final int ACTIVE_X_NO_MODE = 154;
    private static final int ACTIVE_Y = 27;
    private static final int ACTIVE_PLATE_U = 44;
    private static final int ACTIVE_PLATE_PRESSED_U = 65;
    private static final int ACTIVE_PLATE_V = 178;
    private static final int ACTIVE_WIDTH = 21;
    private static final int ACTIVE_HEIGHT = 17;
    private static final int ACTIVE_ICON_U_PASSIVE = 44;
    private static final int ACTIVE_ICON_U_ACTIVE = 65;
    private static final int ACTIVE_ICON_V = 212;

    /**
     * The same switch on an assembly's sheet.
     *
     * <p>It keeps the reach-through switch, since the part still has an inventory and a trough a
     * pipe can reach; the channel plate sits below it rather than replacing it.</p>
     */
    private static final int GENERAL_ACTIVE_X = 179;
    private static final int GENERAL_ACTIVE_Y = 27;
    private static final int GENERAL_ACTIVE_ICON_V = 223;

    /**
     * The channel-participation switch, on the second plate of the same block.
     *
     * <p>An assembly only. It answers whether the part counts as an energy hatch or dynamo in its
     * channel, which is the difference between joining a same-tier pair (and the doubling that
     * brings) and being a part the machine draws no power from.</p>
     *
     * <p>The reach-through switch keeps its plate: an assembly still has an inventory and a trough
     * that a pipe can reach, so this one sits under it rather than taking its place.</p>
     */
    private static final int CHANNEL_X = 154;
    private static final int CHANNEL_Y = 48;
    private static final int CHANNEL_PLATE_U = 44;
    private static final int CHANNEL_PLATE_PRESSED_U = 65;
    private static final int CHANNEL_PLATE_V = 189;
    private static final int CHANNEL_WIDTH = 21;
    private static final int CHANNEL_HEIGHT = 17;
    private static final int CHANNEL_ICON_U_JOINS = 86;
    private static final int CHANNEL_ICON_U_ISOLATED = 107;
    private static final int CHANNEL_ICON_V = 206;

    /** The same, on an assembly's sheet: the second row of that block. */
    private static final int GENERAL_CHANNEL_X = 154;
    private static final int GENERAL_CHANNEL_Y = 48;

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
     * Which button the cursor is holding down, or {@code -1} when none.
     *
     * <p>Tracked here because the screen has no other way to ask: every switch draws a pressed plate
     * while it is being clicked, and neither the render callback nor the widget classes carry the
     * mouse's button state. Hovering alone is not a press — showing the pressed plate on hover made
     * all three switches look permanently held down.</p>
     */
    private int heldButton = -1;

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
     * until then the echo read the old text and wrote it straight back into the box — the edit
     * appeared to bounce for a frame or two. Holding each sent value here keeps the box steady
     * until the server catches up, and it also covers a value the server normalises on save.</p>
     */
    private final Map<GTPortBlockEntity.Field, String> pendingFields =
            new EnumMap<>(GTPortBlockEntity.Field.class);

    public GTPortScreen(GTPortMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GTPortMenu.IMAGE_WIDTH;
        // An assembly's sheet is eleven pixels taller: its lower band grew a trough row along the
        // top. One height per sheet, since the window is blitted at this size.
        this.imageHeight = menu.kind() == GTPortKind.GENERAL
                ? GTPortMenu.GENERAL_IMAGE_HEIGHT : GTPortMenu.IMAGE_HEIGHT;
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
        this.priorityField = makeField(GTPortBlockEntity.Field.PRIORITY,
                displayedPriority(this.menu.priorityText()));
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
        send(GTPortBlockEntity.Field.PRIORITY, canonicalPriority(this.priorityField.getValue()));
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
            String baseline = this.pendingFields.getOrDefault(field, current);
            if (baseline.equals(value)) {
                return;
            }
            // Stored in the form the box will show, not the form that goes on the wire: `echo`
            // compares against the displayed text, and a bare integer would never match a group
            // like "1,600", leaving this field pinned to its pending value for good.
            this.pendingFields.put(field,
                    field == GTPortBlockEntity.Field.PRIORITY ? displayedPriority(value) : value);
            PacketDistributor.sendToServer(new C2SPortTextPacket(port.getBlockPos(), field, value));
        }
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
    private boolean isPressed(int x, int y, int width, int height, int mouseX, int mouseY) {
        return this.heldButton >= 0 && over(x, y, width, height, mouseX, mouseY);
    }

    /** Whether a point is inside a rectangle. */
    private static boolean over(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /** Whether the cursor is over a switch, for the click handler and for its tooltip. */
    private boolean hits(int x, int y, int width, int height, double mouseX, double mouseY) {
        return over(x, y, width, height, (int) mouseX, (int) mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.heldButton = button;
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

        // The stacking switch only exists on item and fluid ports, so its plate is checked only
        // there — otherwise clicking an energy port's first plate would both change the mode and
        // reach through, since both controls claim the same spot.
        if (button == 0 && this.menu.kind() != GTPortKind.ENERGY
                && hits(this.leftPos + modeX(), this.topPos + modeY(),
                        MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY)) {
            GTPortBlockEntity port = this.menu.port();
            if (port != null) {
                PacketDistributor.sendToServer(new C2SPortTogglePacket(
                        port.getBlockPos(), C2SPortTogglePacket.Target.STACK_MODE));
            }
            return true;
        }

        // The channel switch, on an assembly only: a dedicated hatch is a hatch by construction.
        if (button == 0 && this.menu.kind() == GTPortKind.GENERAL
                && hits(this.leftPos + channelX(), this.topPos + channelY(),
                        CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY)) {
            GTPortBlockEntity port = this.menu.port();
            if (port != null) {
                PacketDistributor.sendToServer(new C2SPortTogglePacket(
                        port.getBlockPos(), C2SPortTogglePacket.Target.ENERGY_CHANNEL));
            }
            return true;
        }

        // The reach-through switch, handled exactly like the other two.
        if (button == 0
                && hits(this.leftPos + activeX(), this.topPos + activeY(),
                        ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY)) {
            GTPortBlockEntity port = this.menu.port();
            if (port != null) {
                PacketDistributor.sendToServer(new C2SPortTogglePacket(
                        port.getBlockPos(), C2SPortTogglePacket.Target.ACTIVE));
            }
            return true;
        }

        // The run switch flips participation; the server applies the change and reports it back.
        if (button == 0
                && hits(this.leftPos + RUN_X, this.topPos + runY(),
                        RUN_WIDTH, RUN_HEIGHT, mouseX, mouseY)) {
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
        // has focus — otherwise typing an "r" into a field would open a recipe page.
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
     * The cell each entry starts at, in the same walk {@link #drawStacks} uses.
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
    private static int spanOf(GTReactionStack stack, int perSlot, boolean single) {
        if (single) {
            return 1;
        }
        long span = (stack.amount() + perSlot - 1) / perSlot;
        return (int) Math.clamp(span, 1L, GTPortMenu.STORAGE_CELLS);
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
     * Explains the run switch while the cursor is over it.
     *
     * <p>The icon is a pair of arrows or a pause bar with no text of its own, so the label has to
     * say what the button does and which way it is currently set — the same treatment the machine's
     * own buttons get.</p>
     */
    private void renderRunTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + RUN_X;
        int y = this.topPos + runY();
        if (mouseX < x || mouseX >= x + RUN_WIDTH || mouseY < y || mouseY >= y + RUN_HEIGHT) {
            return;
        }
        boolean enabled = this.menu.port() != null && this.menu.port().isEnabled();
        graphics.renderTooltip(this.font, Component.translatable(enabled
                ? "gui.goldentweaks.port.running.on"
                : "gui.goldentweaks.port.running.off"), mouseX, mouseY);
    }

    /** Explains the stacking switch: the icon alone does not say which way round it is. */
    private void renderModeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.menu.kind() == GTPortKind.ENERGY) {
            return;
        }
        int x = this.leftPos + modeX();
        int y = this.topPos + modeY();
        if (mouseX < x || mouseX >= x + MODE_WIDTH || mouseY < y || mouseY >= y + MODE_HEIGHT) {
            return;
        }
        // The assembly's switch answers a different question from a single-kind port's, so it
        // names a different pair of modes rather than borrowing wording that would read as a
        // per-entry drawing preference.
        String tooltip;
        if (this.menu.kind() == GTPortKind.GENERAL) {
            tooltip = this.menu.isSingleCellMode()
                    ? "gui.goldentweaks.port.split.on"
                    : "gui.goldentweaks.port.split.off";
        } else {
            tooltip = this.menu.isSingleCellMode()
                    ? "gui.goldentweaks.port.stack.single"
                    : "gui.goldentweaks.port.stack.multi";
        }
        graphics.renderTooltip(this.font, Component.translatable(tooltip), mouseX, mouseY);
    }

    /**
     * Explains the channel switch.
     *
     * <p>Four strings rather than two: the answer depends on the direction as well, since the part
     * joins as an energy hatch when it feeds the machine and as a dynamo when it receives from it.</p>
     */
    private void renderChannelTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.menu.kind() != GTPortKind.GENERAL) {
            return;
        }
        int x = this.leftPos + channelX();
        int y = this.topPos + channelY();
        if (mouseX < x || mouseX >= x + CHANNEL_WIDTH || mouseY < y || mouseY >= y + CHANNEL_HEIGHT) {
            return;
        }
        boolean output = this.menu.mode() == PortMode.OUTPUT;
        boolean joins = this.menu.joinsEnergyChannel();
        String key;
        if (output) {
            key = joins ? "gui.goldentweaks.port.channel.joins.dynamo"
                    : "gui.goldentweaks.port.channel.isolated.dynamo";
        } else {
            key = joins ? "gui.goldentweaks.port.channel.joins.hatch"
                    : "gui.goldentweaks.port.channel.isolated.hatch";
        }
        graphics.renderTooltip(this.font, Component.translatable(key), mouseX, mouseY);
    }

    /** Explains the reach-through switch, whose icon alone does not say which way round it is. */
    private void renderActiveTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();
        int y = this.topPos + activeY();
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
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        refreshChannelLock();
        syncChannelEcho();
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderStackTooltip(graphics, mouseX, mouseY);
        renderRunTooltip(graphics, mouseX, mouseY);
        renderModeTooltip(graphics, mouseX, mouseY);
        renderChannelTooltip(graphics, mouseX, mouseY);
        renderTroughTooltip(graphics, mouseX, mouseY);
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
        echo(this.priorityField, GTPortBlockEntity.Field.PRIORITY,
                displayedPriority(port.priorityText()));

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
     * reports what was sent the pending record clears and ordinary echoing resumes — including a
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
        boolean general = this.menu.kind() == GTPortKind.GENERAL;
        graphics.blit(sheet(), this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight);

        drawLabels(graphics);

        if (energy) {
            drawEnergyTrough(graphics);
            // Only a hatch gets the four captions below the trough. An assembly's window already
            // quotes its own ceilings above, and repeating the energy figures there would be the
            // only place in the window where two units shared one column.
            drawEnergyCaptions(graphics);
        } else {
            drawStacks(graphics);
            drawRail(graphics);
        }
        // An assembly holds energy as well as contents, so it draws both — the trough alone, with no
        // captions under it.
        if (general) {
            drawEnergyTrough(graphics);
        }

        drawRunButton(graphics, mouseX, mouseY);
        // An energy port has no cells for an entry to span, so it gets no stacking switch.
        if (!energy) {
            drawModeButton(graphics, mouseX, mouseY);
        }
        // An assembly gets one more plate than the others: split mode, channel participation, and
        // reach-through all apply to it. The channel one is not a replacement — reaching through is
        // still useful, since the part has an inventory and a trough a pipe can reach.
        if (general) {
            drawChannelButton(graphics, mouseX, mouseY);
        }
        drawActiveButton(graphics, mouseX, mouseY);
    }

    /** The sheet this port draws itself from. */
    private ResourceLocation sheet() {
        return switch (this.menu.kind()) {
            case ENERGY -> ENERGY_TEXTURE;
            case GENERAL -> GENERAL_TEXTURE;
            default -> IO_TEXTURE;
        };
    }

    // ---- where this window's sheet puts its plates ----
    //
    // One answer per question, used by the drawing, the click handler and the tooltip alike. Each
    // sheet has its own set of numbers rather than a shared one plus an offset: they are separate
    // drawings, and the plates were re-cut at their own rows.

    private boolean general() {
        return this.menu.kind() == GTPortKind.GENERAL;
    }

    private int modeX() {
        return general() ? GENERAL_MODE_X : MODE_X;
    }

    private int modeY() {
        return general() ? GENERAL_MODE_Y : MODE_Y;
    }

    private int plateV() {
        return general() ? GENERAL_PLATE_V : MODE_PLATE_V;
    }

    private int modeIconV() {
        return general() ? GENERAL_MODE_ICON_V : MODE_ICON_V;
    }

    private int activeX() {
        return general() ? GENERAL_ACTIVE_X
                : this.menu.kind() == GTPortKind.ENERGY ? ACTIVE_X_NO_MODE : ACTIVE_X;
    }

    private int activeY() {
        return general() ? GENERAL_ACTIVE_Y : ACTIVE_Y;
    }

    private int activeIconV() {
        return general() ? GENERAL_ACTIVE_ICON_V : ACTIVE_ICON_V;
    }

    private int channelX() {
        return general() ? GENERAL_CHANNEL_X : CHANNEL_X;
    }

    private int channelY() {
        return general() ? GENERAL_CHANNEL_Y : CHANNEL_Y;
    }

    private int runY() {
        return general() ? GENERAL_RUN_Y : RUN_Y;
    }

    private int runSpriteV() {
        return general() ? GENERAL_PLATE_V : RUN_SPRITE_V;
    }

    private int runIconV() {
        return general() ? GENERAL_RUN_ICON_V : RUN_ICON_V;
    }

    private int troughSpriteV() {
        return general() ? GENERAL_TROUGH_SPRITE_V : TROUGH_SPRITE_V;
    }

    private int troughX() {
        return general() ? GENERAL_TROUGH_X : TROUGH_X;
    }

    private int troughY() {
        return general() ? GENERAL_TROUGH_Y : TROUGH_Y;
    }

    /** The stacking switch: base plate, then the icon for the mode it is in. */
    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + modeX();
        int y = this.topPos + modeY();
        boolean held = isPressed(x, y, MODE_WIDTH, MODE_HEIGHT, mouseX, mouseY);
        graphics.blit(sheet(), x, y,
                held ? MODE_PLATE_PRESSED_U : MODE_PLATE_U, plateV(),
                MODE_WIDTH, MODE_HEIGHT);
        boolean single = this.menu.isSingleCellMode();
        graphics.blit(sheet(), x, y,
                single ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI, modeIconV(),
                MODE_WIDTH, MODE_HEIGHT);
    }

    /** The reach-through switch: plate, then the icon for the state it is in. */
    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + activeX();
        int y = this.topPos + activeY();
        boolean held = isPressed(x, y, ACTIVE_WIDTH, ACTIVE_HEIGHT, mouseX, mouseY);
        graphics.blit(sheet(), x, y,
                held ? ACTIVE_PLATE_PRESSED_U : ACTIVE_PLATE_U, plateV(),
                ACTIVE_WIDTH, ACTIVE_HEIGHT);
        boolean active = this.menu.isActivePort();
        graphics.blit(sheet(), x, y,
                active ? ACTIVE_ICON_U_ACTIVE : ACTIVE_ICON_U_PASSIVE, activeIconV(),
                ACTIVE_WIDTH, ACTIVE_HEIGHT);
    }

    /**
     * The channel-participation switch: plate, then the icon for the state it is in.
     *
     * <p>The plate shows the press, not the setting — the same rule the run switch follows. Both
     * states share one icon position, so the picture says what the switch <em>is</em> and the plate
     * says whether the cursor is on it; reading the setting from the plate made the button look
     * permanently held down.</p>
     */
    private void drawChannelButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + channelX();
        int y = this.topPos + channelY();
        boolean held = isPressed(x, y, CHANNEL_WIDTH, CHANNEL_HEIGHT, mouseX, mouseY);
        graphics.blit(GENERAL_TEXTURE, x, y,
                held ? CHANNEL_PLATE_PRESSED_U : CHANNEL_PLATE_U, CHANNEL_PLATE_V,
                CHANNEL_WIDTH, CHANNEL_HEIGHT);
        boolean joins = this.menu.joinsEnergyChannel();
        graphics.blit(GENERAL_TEXTURE, x, y,
                joins ? CHANNEL_ICON_U_JOINS : CHANNEL_ICON_U_ISOLATED, CHANNEL_ICON_V,
                CHANNEL_WIDTH, CHANNEL_HEIGHT);
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
     * ceiling — a stack of 64 in a 64-per-slot bus takes one cell, a stack of 192 takes three.</p>
     */
    private void drawStacks(GuiGraphics graphics) {
        List<GTReactionStack> rows = contents();
        if (rows.isEmpty()) {
            return;
        }
        HolderLookup.Provider registries = registries();
        var context = EmiDrawContext.wrap(graphics);

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
                drawStack(graphics, context, stack, x, y, registries);
                drawn += lastVisible - firstVisible;
            }
            cursor += span;
        }
    }

    /**
     * Draws one aspect, deferring to Thaumcraft's own renderer.
     *
     * <p>Not a plain blit: the sheet comes from {@code Aspect.image()}, the tint from
     * {@code Aspect.color()}, and the cosmic aspects are not sheet-based at all — they render as a
     * dedicated item model. All three of those live in {@code AspectGuiRenderer}, and the mod
     * already wraps the cosmic case in {@code GTAspectEntry.isCosmic}.</p>
     *
     * <p>Guarded because every class named here belongs to Thaumcraft: this screen is loaded on
     * packs that do not have it.</p>
     */
    private static void drawEssentia(GuiGraphics graphics, GTReactionStack stack, int x, int y) {
        String tag = stack.essentiaTag();
        if (tag == null) {
            return;
        }
        try {
            Aspect aspect = Aspect.getAspect(tag);
            if (aspect == null) {
                return;
            }
            if (GTAspectEntry.isCosmic(tag)) {
                graphics.renderItem(GTThaumcraftAdditionalItems.cosmicIconStack(), x, y);
            } else {
                AspectGuiRenderer.draw(graphics, aspect, x, y, 16, 1.0F);
            }
        } catch (NoClassDefFoundError absent) {
            // Nothing sensible to draw without the mod's renderer.
        }
    }

    /**
     * The still frame of a fluid, at the path vanilla lays them out on.
     *
     * <p>Fluids are not items, so there is no sprite to ask for; the texture name is derived from
     * the fluid's id the same way the vanilla block models do it.</p>
     */
    @Nullable
    private static ResourceLocation fluidSprite(GTReactionStack stack) {
        Fluid fluid = stack.fluid();
        if (fluid == null) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        // Vanilla's flowing and still frames live under the namespace's own block textures.
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                "textures/block/" + id.getPath() + "_still.png");
    }

    /**
     * The icon Mekanism keeps for a chemical, or {@code null} when Mekanism is absent.
     *
     * <p>Read through the chemical rather than derived from the id: Mekanism lets a chemical name
     * its own icon, so the registry entry is the only thing that knows the right path.</p>
     */
    @Nullable
    private static ResourceLocation chemicalSprite(GTReactionStack stack) {
        try {
            var holder = MekanismAPI.CHEMICAL_REGISTRY.getHolder(stack.id()).orElse(null);
            if (holder == null) {
                return null;
            }
            ResourceLocation icon = holder.value().getIcon();
            // getIcon gives the texture path without an extension or the textures/ prefix.
            return ResourceLocation.fromNamespaceAndPath(icon.getNamespace(),
                    "textures/" + icon.getPath() + ".png");
        } catch (NoClassDefFoundError absent) {
            return null;
        }
    }

    /** One stack: the icon through EMI, then its amount on the corner, as EMI draws a slot. */
    private void drawStack(GuiGraphics graphics, EmiDrawContext context,
                           GTReactionStack stack, int x, int y,
                           @Nullable HolderLookup.Provider registries) {
        // Essentia has no EMI stack at all, so its icon is drawn directly. The count label goes
        // through EMI's helper only when EMI is installed — that class is not optional otherwise.
        if (stack.kind() == GTReactionStack.Kind.ESSENTIA) {
            // scex registers the aspect as a viewer ingredient; drawing that entry instead of the
            // bare icon is what lets the player look a recipe up by hovering this slot.
            EmiStack registered = GTAspectEmiStacks.of(stack.essentiaTag());
            if (registered != null) {
                registered.setAmount(stack.amount())
                        .render(graphics, x, y, 0.0F, EmiIngredient.RENDER_ICON);
                if (ModList.get().isLoaded("emi")) {
                    EmiRenderHelper.renderAmount(context, x, y,
                            Component.literal(GTPortAmounts.number(stack.amount(), true)));
                } else {
                    drawAmount(graphics, stack.amount(), true, x, y);
                }
                return;
            }
            drawEssentia(graphics, stack, x, y);
            // Essentia counts in whole units like an item, so it gets the item-shaped badge
            // instead of the fluid one that would append "mB".
            if (ModList.get().isLoaded("emi")) {
                EmiRenderHelper.renderAmount(context, x, y,
                        Component.literal(GTPortAmounts.number(stack.amount(), true)));
            } else {
                drawAmount(graphics, stack.amount(), true, x, y);
            }
            return;
        }

        if (!ModList.get().isLoaded("emi")) {
            // Without EMI the sprite is resolved here from whatever the owning mod ships: an item
            // stack, a fluid's still frame, or Mekanism's own chemical icon.
            switch (stack.kind()) {
                case ITEM -> {
                    var item = stack.toItemStack();
                    if (!item.isEmpty()) {
                        graphics.renderItem(item, x, y);
                        graphics.renderItemDecorations(this.font, item, x, y);
                        return;
                    }
                }
                case FLUID -> {
                    ResourceLocation sprite = fluidSprite(stack);
                    if (sprite != null) {
                        graphics.blit(sprite, x, y, 0, 0, 16, 16, 16, 16);
                    }
                }
                case CHEMICAL -> {
                    ResourceLocation sprite = chemicalSprite(stack);
                    if (sprite != null) {
                        graphics.blit(sprite, x, y, 0, 0, 16, 16, 16, 16);
                    }
                }
                default -> {
                }
            }
            drawAmount(graphics, stack.amount(),
                    stack.kind() == GTReactionStack.Kind.ITEM, x, y);
            return;
        }

        var ingredient = GTReactionStacks.toEmiIngredient(stack, registries);
        if (ingredient == null) {
            return;
        }
        // Icon only: a stack's own render prints a count for the item flavour alone, so the amount
        // is added below through the very helper EMI uses for that.
        ingredient.render(graphics, x, y, 0.0F, EmiIngredient.RENDER_ICON);

        EmiRenderHelper.renderAmount(context, x, y,
                Component.literal(GTPortAmounts.number(stack.amount(),
                        stack.kind() == GTReactionStack.Kind.ITEM)));
    }

    /**
     * The amount, bottom-right of the cell, in the shape a player expects from an inventory slot.
     *
     * <p>Only used when EMI is absent: with EMI installed its own helper draws the same label and
     * matching its spacing keeps the two windows looking identical.</p>
     */
    private void drawAmount(GuiGraphics graphics, long amount, boolean item, int x, int y) {
        Component text = Component.literal(GTPortAmounts.number(amount, item));
        int width = this.font.width(text);
        graphics.drawString(this.font, text, x + 17 - width, y + 9, 0xFFFFFFFF, true);
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
        int marker = Math.clamp(
                Math.round(inner * (float) GTPortMenu.STORAGE_ROWS / (float) total), MARKER_MIN_HEIGHT, inner);
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
        int capacity = this.menu.energyCapacity();
        int stored = this.menu.energyStored();
        int x = this.leftPos + troughX();
        int y = this.topPos + troughY();

        int cells = capacity <= 0 || stored <= 0
                ? 0 : (int) Math.min(TROUGH_CELLS, (long) stored * TROUGH_CELLS / capacity);
        if (cells > 0) {
            int width = TROUGH_FIRST_CELL + (cells - 1) * TROUGH_CELL_STEP;
            // A dedicated hatch fills from the energy sheet; an assembly's trough is cut into its
            // own sheet, so the sprite comes from there instead.
            boolean general = this.menu.kind() == GTPortKind.GENERAL;
            graphics.blit(general ? GENERAL_TEXTURE : ENERGY_TEXTURE, x, y,
                    TROUGH_SPRITE_U, troughSpriteV(), width, TROUGH_HEIGHT);
        }
    }

    /**
     * Explains the trough: what is stored and what the part can hold.
     *
     * <p>Only an assembly needs this. A dedicated energy hatch prints the same figures on the four
     * caption lines under its trough; an assembly has no room for those, because its window quotes
     * its own byte and type ceilings where they would go.</p>
     */
    private void renderTroughTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + troughX();
        int y = this.topPos + troughY();
        if (mouseX < x || mouseX >= x + TROUGH_SPRITE_WIDTH
                || mouseY < y || mouseY >= y + TROUGH_HEIGHT) {
            return;
        }
        graphics.renderTooltip(this.font,
                Component.translatable("gui.goldentweaks.port.energy.stored_value",
                        GTEnergyFormat.formatWithUnit(this.menu.energyStored(), "FE"),
                        GTEnergyFormat.formatWithUnit(this.menu.energyCapacity(), "FE")),
                mouseX, mouseY);
    }

    /**
     * The run switch: base plate from the port sheet, icon from the machine's.
     *
     * <p>Two sheets are involved because the port sheets carry the button's base but not its
     * icons — the green triple arrow and the yellow pause bar live only on the machine sheet, in
     * the row the refresh hammer occupies. Blitting the base from ours and the icon from the
     * machine's is what makes the port's switch look like the machine's switch rather than an
     * empty plate.</p>
     */
    private void drawRunButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + RUN_X;
        int y = this.topPos + runY();
        boolean held = isPressed(x, y, RUN_WIDTH, RUN_HEIGHT, mouseX, mouseY);

        // Base plate: the sprite is 22 by 11 with a transparent margin around a 20 by 9 cell.
        int baseU = held ? RUN_SPRITE_PRESSED_U : RUN_SPRITE_U;
        graphics.blit(sheet(), x + RUN_SPRITE_DX, y + RUN_SPRITE_DY,
                baseU, runSpriteV(), RUN_SPRITE_WIDTH, RUN_SPRITE_HEIGHT);

        // Icon: green arrows while the port takes part, yellow pause while it does not. It shares
        // the plate's footprint, so it goes down at the same offset and layers over it.
        boolean enabled = this.menu.port() != null && this.menu.port().isEnabled();
        int iconU = enabled ? RUN_ICON_U_RUN : RUN_ICON_U_STOP;
        graphics.blit(sheet(), x + RUN_SPRITE_DX, y + RUN_SPRITE_DY,
                iconU, runIconV(), RUN_ICON_WIDTH, RUN_ICON_HEIGHT);
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
                && this.menu.port().mode() == PortMode.OUTPUT;
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
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
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
        long phase = Util.getMillis() % (MARQUEE_PAUSE_MILLIS + lapMillis);
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
        String amount;
        String cells;
        if (this.menu.kind() == GTPortKind.GENERAL) {
            // Bytes and types, not a cells count: the assembly's two ceilings are what the player
            // compares against, and both are formatted on the compact ladder so a top-tier figure
            // stays readable. Read from the entity on both sides — the ceilings are fixed by the
            // tier, and the used figures are small enough to arrive with the block update.
            amount = GTEnergyFormat.format(this.menu.generalBytesUsed())
                    + " / " + GTEnergyFormat.format(this.menu.generalBytes())
                    + " " + Component.translatable("gui.goldentweaks.port.bytes").getString();
            cells = this.menu.generalTypesUsed()
                    + " / " + GTEnergyFormat.format(this.menu.generalTypes())
                    + " " + Component.translatable("gui.goldentweaks.port.types").getString();
        } else {
            // Compact rather than every digit: a high-tier hatch holds hundreds of thousands of mB,
            // and the exact figure is unreadable at this size. The same ladder the energy port uses,
            // so a number reads the same way across the machine's windows.
            String unit = this.menu.kind() == GTPortKind.FLUID ? " mB" : "";
            amount = GTEnergyFormat.format(this.menu.syncedAmount())
                    + " / " + GTEnergyFormat.format(this.menu.syncedCapacity()) + unit;
            cells = this.menu.usedCells() + " / " + this.menu.totalCells() + " "
                    + Component.translatable("gui.goldentweaks.port.cells").getString();
        }

        graphics.pose().pushPose();
        graphics.pose().translate(CAPTION_X, CAPTION_Y, 0.0F);
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
