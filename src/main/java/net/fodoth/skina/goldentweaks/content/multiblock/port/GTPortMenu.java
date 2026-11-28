package net.fodoth.skina.goldentweaks.content.multiblock.port;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.PortMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionStack;

/**
 * Container for one of this mod's ports.
 *
 * <p>The window is laid out to match {@code gt_port_io.png}, so every coordinate here is taken from
 * that sheet rather than computed. That fixes the window at one size for every port kind: the
 * storage band is a 3 by 3 grid and the lower half is the player's, identical for items and fluids,
 * and an energy port reuses the same frame with a bar where the grid would be.</p>
 *
 * <p>Only the player's slots and the two single-item slots are real container slots. A port's own
 * contents are not slotted: the window lists them and reads them from the block entity on both
 * sides, so nothing about them travels through the slot sync.</p>
 */
public class GTPortMenu extends AbstractContainerMenu {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, GoldenTweaks.MODID);

    /** Position, kind and slot count: everything the factory needs, written by the opening block. */
    public static final DeferredHolder<MenuType<?>, MenuType<GTPortMenu>> MENU =
            MENU_TYPES.register("gt_port", () -> IMenuTypeExtension.create(
                    (id, inventory, data) -> new GTPortMenu(id, inventory,
                            data.readBlockPos(), data.readEnum(GTPortKind.class),
                            data.readVarInt())));

    /** Synced values: how much the port holds, how much it can hold, and its input rate. */
    public static final int DATA_AMOUNT = 0;
    public static final int DATA_CAPACITY = 1;
    public static final int DATA_RATE = 2;
    /** One when entries are drawn one cell each, zero when they may span several. */
    public static final int DATA_SINGLE_CELL = 3;
    /** One when the port also reaches through its face. */
    public static final int DATA_ACTIVE = 4;
    /** One when an assembly counts as an energy hatch in its channel. */
    public static final int DATA_ENERGY_CHANNEL = 5;
    private static final int DATA_COUNT = 6;

    /** Panel size, from the sheet. The panel ends where the sprite band begins. */
    public static final int IMAGE_WIDTH = 210;
    public static final int IMAGE_HEIGHT = 178;

    /**
     * The same for a general assembly, whose sheet grew a trough row along the top of the lower
     * band.
     *
     * <p>A second pair of constants rather than one shared pair: the two sheets are separate files
     * and only the assembly's moved, so shifting the shared values by eleven would have moved the
     * single-kind windows off their own art.</p>
     */
    public static final int GENERAL_IMAGE_HEIGHT = 189;

    /** Slot pitch, matching the sheet's cells. */
    public static final int SLOT_SIZE = 16;
    public static final int SLOT_STEP = 18;
    public static final int MAX_COLUMNS = 9;
    public static final int INVENTORY_ROWS = 3;

    /** Storage band: the 3 by 3 grid at the sheet's coordinates. */
    public static final int STORAGE_X = 78;
    public static final int STORAGE_Y = 30;
    public static final int STORAGE_COLUMNS = 3;
    public static final int STORAGE_ROWS = 3;
    public static final int STORAGE_CELLS = STORAGE_COLUMNS * STORAGE_ROWS;

    /**
     * Player inventory origin, from the sheet.
     *
     * <p>Asked of the menu rather than read as constants: an assembly's sheet grew a trough row at
     * the top of the lower band, and everything in that band moved down eleven pixels with it. The
     * three switch plates above are on the panel proper and did not move.</p>
     */
    public static final int INVENTORY_X = 9;
    public static final int INVENTORY_Y = 95;
    public static final int HOTBAR_Y = 153;

    /** The same three for an assembly, eleven pixels lower. */
    private static final int GENERAL_SHIFT = 11;

    /** How far this window's lower band sits below the single-kind sheet's. */
    private int bandShift() {
        return this.kind == GTPortKind.GENERAL ? GENERAL_SHIFT : 0;
    }

    /** The two single-item slots on the right: tool above filter. */
    public static final int TOOL_X = 180;
    public static final int TOOL_Y = 115;
    public static final int FILTER_X = 180;
    public static final int FILTER_Y = 146;

    /** Index of the tool slot, then the filter slot, at the head of this menu's slot list. */
    public static final int TOOL_INDEX = 0;
    public static final int FILTER_INDEX = 1;
    private static final int OWN_SLOTS = 2;

    private final GTPortBlockEntity port;
    private final boolean clientSide;
    private final GTPortKind kind;
    private final ContainerData data;

    public GTPortMenu(int id, Inventory inventory, BlockPos pos,
                      GTPortKind kind, int slots) {
        super(MENU.get(), id);
        this.port = resolve(inventory, pos);
        this.clientSide = inventory.player.level().isClientSide;
        this.kind = this.port != null ? this.port.kind() : kind;

        this.data = new SyncedData();
        addDataSlots(this.data);

        // Tool and filter first, so their indices stay fixed whatever the player's slot count is.
        int shift = bandShift();
        addSlot(new SlotItemHandler(ownHandler(this.port, true), 0, TOOL_X, TOOL_Y + shift));
        addSlot(new SlotItemHandler(ownHandler(this.port, false), 0, FILTER_X, FILTER_Y + shift));

        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int column = 0; column < MAX_COLUMNS; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        INVENTORY_X + column * SLOT_STEP, INVENTORY_Y + shift + row * SLOT_STEP));
            }
        }
        for (int column = 0; column < MAX_COLUMNS; column++) {
            addSlot(new Slot(inventory, column,
                    INVENTORY_X + column * SLOT_STEP, HOTBAR_Y + shift));
        }
    }

    /**
     * The handler behind the tool or filter slot.
     *
     * <p>A client-side menu has no block entity to read, but {@code SlotItemHandler} still needs
     * somewhere to put the synced stack. A one-slot handler is the stand-in there; its contents are
     * replaced as the server's values arrive and are never written anywhere.</p>
     */
    private static IItemHandler ownHandler(GTPortBlockEntity port, boolean tool) {
        if (port != null) {
            return tool ? port.toolSlot() : port.filterSlot();
        }
        return tool ? CLIENT_TOOL : CLIENT_FILTER;
    }

    /** Client-side stand-ins, one per slot so the two cannot overwrite each other. */
    private static final ItemStackHandler CLIENT_TOOL = new ItemStackHandler(1);
    private static final ItemStackHandler CLIENT_FILTER = new ItemStackHandler(1);

    /**
     * The menu's tracked values.
     *
     * <p>Only the server answers from the entity. The client's copy of the entity is not usable for
     * this — it is told about item stacks and field text, never about fluid or energy amounts — so
     * the server's figures are written into the cache by {@link #set} and read back by
     * {@link #get}.</p>
     */
    private final class SyncedData implements ContainerData {

        private final int[] cache = new int[DATA_COUNT];

        @Override
        public int get(int index) {
            if (!GTPortMenu.this.clientSide) {
                return switch (index) {
                    case DATA_AMOUNT -> GTPortMenu.this.filled();
                    case DATA_CAPACITY -> GTPortMenu.this.capacity();
                    case DATA_RATE -> GTPortMenu.this.inputRate();
                case DATA_SINGLE_CELL -> GTPortMenu.this.isSingleCellMode() ? 1 : 0;
                case DATA_ACTIVE -> GTPortMenu.this.isActivePort() ? 1 : 0;
                case DATA_ENERGY_CHANNEL -> GTPortMenu.this.portJoinsEnergyChannel() ? 1 : 0;
                    default -> 0;
                };
            }
            return this.cache[index];
        }

        @Override
        public void set(int index, int value) {
            this.cache[index] = value;
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    }

    /** Resolves the block entity on the server; {@code null} on the client. */
    private static GTPortBlockEntity resolve(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof GTPortBlockEntity found) {
            return found;
        }
        return null;
    }

    public GTPortKind kind() {
        return this.kind;
    }

    /**
     * Which way this port faces.
     *
     * <p>Read from the entity on both sides: unlike the amounts, the direction is fixed by the block
     * that was placed, so the client's copy carries it as reliably as the server's.</p>
     */
    public PortMode mode() {
        return this.port == null ? PortMode.INPUT : this.port.mode();
    }

    /** The block entity behind this window, or {@code null} on the client. */
    public GTPortBlockEntity port() {
        return this.port;
    }

    /** The {@code mode} field's current text, defaulting when there is no entity. */
    public String modeText() {
        return this.port == null ? GTPortBlockEntity.DEFAULT_TEXT : this.port.modeText();
    }

    /** The {@code channel} field's current text. */
    public String channelText() {
        return this.port == null ? GTPortBlockEntity.DEFAULT_TEXT : this.port.channelText();
    }

    /** The {@code priority} field's current text. */
    public String priorityText() {
        return this.port == null ? GTPortBlockEntity.DEFAULT_TEXT : this.port.priorityText();
    }

    /** What the port currently holds, as reaction stacks the window lists. */
    public List<GTReactionStack> contents() {
        return this.port == null ? List.of() : this.port.contents();
    }

    /**
     * How much is stored.
     *
     * <p>An energy port answers with its charge in FE and a fluid port with its tank in mB; both are
     * single quantities the entity already tracks. Items are summed over their entries, since an
     * item port's contents are a set of stacks rather than one amount.</p>
     */
    public int filled() {
        if (this.port == null) {
            return 0;
        }
        if (this.kind == GTPortKind.ENERGY) {
            var storage = this.port.energyStorage();
            return storage == null ? 0 : storage.getEnergyStored();
        }
        long total = 0L;
        for (GTReactionStack stack : this.port.contents()) {
            total += stack.amount();
        }
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    /** How much the port can hold, in the same unit as {@link #filled()}. */
    public int capacity() {
        return this.port == null ? 0 : this.port.capacity();
    }

    /** Synced stored amount. */
    public int syncedAmount() {
        return this.data.get(DATA_AMOUNT);
    }

    /** Synced capacity. */
    public int syncedCapacity() {
        return this.data.get(DATA_CAPACITY);
    }

    /** How much one slot holds, which is what an entry's cell span is measured in. */
    public int slotLimit() {
        return this.port == null ? 64 : this.port.cellSize();
    }

    /** Whether each entry occupies a single cell. */
    public boolean isSingleCellMode() {
        return this.port == null || this.port.isSingleCellMode();
    }

    /** Whether the port also reaches through its face, rather than only waiting to be reached. */
    public boolean isActivePort() {
        return this.port == null || this.port.isActive();
    }

    /**
     * Whether an assembly counts as an energy hatch in its channel.
     *
     * <p>Synced rather than read off the entity: the button has to show the server's answer on both
     * sides, and the client's copy of the entity is not what the machine consulted.</p>
     */
    public boolean joinsEnergyChannel() {
        return this.data.get(DATA_ENERGY_CHANNEL) != 0;
    }

    /**
     * The same question, asked of the server's entity.
     *
     * <p>Separate from {@link #joinsEnergyChannel()} because that one reads the synced slot, which is
     * filled by asking this. Using it inside {@link SyncedData#get} called back into the slot it was
     * computing and recursed until the stack ran out — which is why pressing the button crashed
     * rather than merely showing the wrong state.</p>
     */
    private boolean portJoinsEnergyChannel() {
        return this.port == null || this.port.joinsEnergyChannel();
    }

    /** Synced input rate, in FE per tick. */
    public int syncedRate() {
        return this.data.get(DATA_RATE);
    }

    /**
     * Charge and ceiling in FE, for the trough.
     *
     * <p>Its own accessors rather than {@link #syncedAmount()} and {@link #syncedCapacity()}: on an
     * assembly those two answer in bytes, since that is what its storage is measured in. The trough
     * is the energy half of the same window and has to be told in FE, so it asks separately instead
     * of being handed a figure in the wrong unit.</p>
     */
    public int energyStored() {
        var storage = this.port == null ? null : this.port.energyStorage();
        return storage == null ? 0 : storage.getEnergyStored();
    }

    /** The same ceiling, in FE. */
    public int energyCapacity() {
        var storage = this.port == null ? null : this.port.energyStorage();
        if (storage != null) {
            return storage.getMaxEnergyStored();
        }
        // Follows the tier and the direction, both of which are fixed by the block that was placed.
        // A window whose entity has not synced yet therefore still draws a correctly sized trough
        // instead of an empty one.
        int level = this.port == null ? 1 : this.port.portTier().level();
        return this.mode() == PortMode.OUTPUT
                ? GTPortEnergyTiers.outputCapacity(level)
                : GTPortEnergyTiers.capacity(level);
    }

    /** How fast the port accepts energy, in FE per tick. */
    public int inputRate() {
        return this.port == null ? 0 : this.port.inputRate();
    }

    /** How many stack cells the storage band shows. An energy port shows none. */
    public int visibleCells() {
        return this.kind == GTPortKind.ENERGY ? 0 : STORAGE_CELLS;
    }

    /** How many kinds the port currently holds. */
    public int usedCells() {
        return contents().size();
    }

    /**
     * How many bytes the general assembly holds, exactly.
     *
     * <p>Not synced: the ceiling follows the tier, and the tier follows the block, so both sides
     * read it from the same place. Only the stored figure travels, and that one is small enough to
     * be a synced int until the assembly is nearly full.</p>
     */
    public long generalBytes() {
        return this.port == null ? 0L : this.port.generalBytes();
    }

    /** How many distinct types the general assembly holds, exactly. */
    public int generalTypes() {
        return this.port == null ? 0 : this.port.generalTypes();
    }

    /** Bytes the assembly currently holds, summed over the five kinds. */
    public long generalBytesUsed() {
        return this.port == null ? 0L : this.port.generalBytesUsed();
    }

    /** Types the assembly currently holds. */
    public int generalTypesUsed() {
        return this.port == null ? 0 : this.port.generalTypesUsed();
    }

    /**
     * How many cells the port has in all, which is its tier's slot count.
     *
     * <p>Not the size of the visible band: the band is a fixed 3 by 3 window that pages through
     * whatever the port holds, so reporting its nine would say "9" for every tier and hide the very
     * thing the readout is for.</p>
     */
    public int totalCells() {
        return switch (this.kind) {
            case ITEM -> this.port == null ? 0 : this.port.itemHandler() == null
                    ? 0 : Objects.requireNonNull(this.port.itemHandler()).getSlots();
            case FLUID -> this.port == null ? 0 : this.port.fluidHandler() == null
                    ? 0 : Objects.requireNonNull(this.port.fluidHandler()).getTanks();
            case ENERGY -> 0;
            case CHEMICAL -> this.port == null ? 0 : this.port.chemicalTankCount();
            case ESSENTIA -> this.port == null ? 0 : this.port.essentiaTankCount();
            // The assembly reports bytes and types instead of a cell count; see #generalBytes.
            case GENERAL -> 0;
        };
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.port != null && this.port.getLevel() != null
                && player.distanceToSqr(
                        this.port.getBlockPos().getX() + 0.5D,
                        this.port.getBlockPos().getY() + 0.5D,
                        this.port.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerStart = OWN_SLOTS;
        int playerEnd = this.slots.size();
        int gridEnd = playerStart + INVENTORY_ROWS * MAX_COLUMNS;

        if (index < OWN_SLOTS) {
            // Out of the tool or filter slot, into the player's inventory only.
            if (!moveItemStackTo(stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < gridEnd) {
            if (!moveItemStackTo(stack, gridEnd, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, playerStart, gridEnd, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }
}
