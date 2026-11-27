package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Container menu of the basic chemical reaction kettle.
 *
 * <p>Layout follows the vanilla chest, only the panel is wider: the player inventory keeps the
 * usual eighteen pixel pitch starting at {@code (9, 95)}, the hotbar sits at {@code (9, 153)} and
 * the kettle adds a single controller slot at {@code (180, 146)}. Slot coordinates are item
 * positions, so each one is the top left pixel of its eighteen pixel slot frame plus one.</p>
 *
 * <p>The kettle is deliberately a one slot container. It holds the machine's own controller item
 * so that breaking the block can return what was stored in it; {@code Containers
 * .dropContentsOnDestroy} reads exactly this view. The slot stacks like ordinary storage, up to
 * {@link GTChemicalReactorMachineBlockEntity#CONTROLLER_SLOT_LIMIT}.</p>
 *
 * <p>Energy is not buffered inside the kettle: {@link
 * GTChemicalReactorMachineBlockEntity#getEnergyCapacity()} is zero and the totals reported here
 * come from the energy input ports of the assembled structure. The menu only mirrors them to the
 * client, split into two shorts because {@code ClientboundContainerSetDataPacket} truncates its
 * values to sixteen bits.</p>
 */
public class GTChemicalReactorMenu extends AbstractContainerMenu {

    /** Widget identifiers shared with {@code GTChemicalReactorScreen}. */
    public static final int BUTTON_TOGGLE_LOCK = 0;
    public static final int BUTTON_TOGGLE_VOID = 1;
    public static final int BUTTON_TOGGLE_BATCH = 2;
    public static final int BUTTON_REFRESH = 3;
    public static final int BUTTON_TOGGLE_RUNNING = 4;
    public static final int BUTTON_DISMANTLE = 5;
    /** Fourth add-on toggle, drawn beside batch mode. */
    public static final int BUTTON_TOGGLE_ISOLATE = 6;

    public static final int DATA_STORED_LO = 0;
    public static final int DATA_STORED_HI = 1;
    public static final int DATA_CAPACITY_LO = 2;
    public static final int DATA_CAPACITY_HI = 3;
    public static final int DATA_FLAGS = 4;
    public static final int DATA_RATE_LO = 5;
    public static final int DATA_RATE_HI = 6;
    /** Ordinal of {@link GTReactorStatus}, so the screen can phrase the status line. */
    public static final int DATA_STATUS = 7;
    public static final int DATA_PROGRESS = 8;
    public static final int DATA_LENGTH = 9;
    /** The group the running craft belongs to, or {@link #NO_CHANNEL} when there is none. */
    public static final int DATA_CHANNEL = 10;
    /** How far the machine may overclock; {@code -1} for no ceiling. */
    public static final int DATA_OVERCLOCK = 11;
    /** Parallel ceiling; {@code 0} means "always at maximum". */
    public static final int DATA_PARALLEL = 12;
    /** Cost of the cheapest rejected recipe, split into two halves like the energy counters. */
    public static final int DATA_REFUSED_LO = 13;
    public static final int DATA_REFUSED_HI = 14;
    public static final int DATA_COUNT = 15;

    /** Sentinel for "no channel": a real channel is any int, so it needs an out-of-band value. */
    public static final int NO_CHANNEL = Integer.MIN_VALUE;

    /** Machine state is mirrored to the client once per second. */
    public static final int DATA_SYNC_INTERVAL_TICKS = 20;

    /** Ticks until the next periodic sync; starts at zero so the first broadcast fills the data. */
    private int dataSyncCountdown;

    public static final int FLAG_LOCK_RECIPE = 1;
    public static final int FLAG_VOID_OUTPUT = 1 << 1;
    public static final int FLAG_BATCH_MODE = 1 << 2;
    public static final int FLAG_RUNNING = 1 << 3;
    public static final int FLAG_ISOLATE_INPUT = 1 << 4;

    private static final int MACHINE_SLOT = 0;
    private static final int PLAYER_START = 1;
    private static final int HOTBAR_START = PLAYER_START + 27;
    private static final int PLAYER_END = PLAYER_START + 36;

    private static final int CONTROLLER_SLOT_X = 180;
    private static final int CONTROLLER_SLOT_Y = 146;
    private static final int INVENTORY_X = 9;
    private static final int INVENTORY_Y = 95;
    private static final int HOTBAR_Y = 153;
    private static final int SLOT_PITCH = 18;

    private final Container container;

    @Nullable
    private final GTChemicalReactorMachineBlockEntity tile;

    private final ContainerData data = new SimpleContainerData(DATA_COUNT);

    /** Client side constructor: the block entity is resolved from the position sent by the server. */
    public GTChemicalReactorMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, resolveContainer(inventory, pos), resolveTile(inventory, pos));
    }

    /**
     * Opens the window for a machine that is not the basic kettle.
     *
     * <p>The menu type is what the client uses to pick a screen class, so each machine's factory
     * has to name its own. Resolving the tile happens the same way it does for the basic one.</p>
     */
    public static GTChemicalReactorMenu create(MenuType<?> type, int containerId,
                                               Inventory inventory, BlockPos pos) {
        return new GTChemicalReactorMenu(type, containerId, inventory,
                resolveContainer(inventory, pos), resolveTile(inventory, pos));
    }

    /**
     * The same, for a caller that already holds the tile.
     *
     * <p>{@code createMenu} is handed the entity, so looking it up by position again would be a
     * redundant walk of the chunk.
     */
    public static GTChemicalReactorMenu create(MenuType<?> type, int containerId,
                                               Inventory inventory,
                                               GTChemicalReactorMachineBlockEntity tile) {
        return new GTChemicalReactorMenu(type, containerId, inventory, tile, tile);
    }

    /** Server side constructor, used by {@link GTChemicalReactorMachineBlockEntity#createMenu}. */
    public GTChemicalReactorMenu(int containerId, Inventory inventory, GTChemicalReactorMachineBlockEntity tile) {
        this(containerId, inventory, tile, tile);
    }

    private GTChemicalReactorMenu(int containerId, Inventory inventory, Container container,
                                  @Nullable GTChemicalReactorMachineBlockEntity tile) {
        this(GTChemicalReactorMultiblock.MENU.get(), containerId, inventory, container, tile);
    }

    /**
     * The constructor a second machine uses.
     *
     * <p>The menu type is what tells the client which window class to open, so the advanced kettle
     * has to pass its own; everything below is shared.</p>
     */
    protected GTChemicalReactorMenu(MenuType<?> type, int containerId, Inventory inventory,
                                    Container container,
                                    @Nullable GTChemicalReactorMachineBlockEntity tile) {
        super(type, containerId);
        this.container = container;
        this.tile = tile;

        this.addSlot(new ControllerSlot(container, MACHINE_SLOT, CONTROLLER_SLOT_X, CONTROLLER_SLOT_Y));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9,
                        INVENTORY_X + column * SLOT_PITCH, INVENTORY_Y + row * SLOT_PITCH));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column,
                    INVENTORY_X + column * SLOT_PITCH, HOTBAR_Y));
        }

        this.addDataSlots(this.data);
    }

    private static Container resolveContainer(Inventory inventory, BlockPos pos) {
        GTChemicalReactorMachineBlockEntity tile = resolveTile(inventory, pos);
        return tile != null ? tile : new SimpleContainer(1);
    }

    @Nullable
    private static GTChemicalReactorMachineBlockEntity resolveTile(Inventory inventory, BlockPos pos) {
        return inventory.player.level().getBlockEntity(pos)
                instanceof GTChemicalReactorMachineBlockEntity tile ? tile : null;
    }

    public Component getDisplayName() {
        return this.tile != null
                ? this.tile.getDisplayName()
                : Component.translatable("block.goldentweaks.chemical_plant_controller");
    }

    public int getEnergyStored() {
        return combine(DATA_STORED_LO, DATA_STORED_HI);
    }

    public int getEnergyCapacity() {
        return combine(DATA_CAPACITY_LO, DATA_CAPACITY_HI);
    }

    /** Total energy the machine can accept per tick, summed over its energy input ports. */
    public int getInputEnergyRate() {
        return combine(DATA_RATE_LO, DATA_RATE_HI);
    }

    /** Current status of the machine, for the line under the title. */
    public GTReactorStatus getStatus() {
        int ordinal = this.data.get(DATA_STATUS);
        GTReactorStatus[] values = GTReactorStatus.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : GTReactorStatus.IDLE;
    }

    /** Position of the machine, or {@code null} when the tile is not available on this side. */
    @Nullable
    public BlockPos getMachinePos() {
        return this.tile == null ? null : this.tile.getBlockPos();
    }

    /** Elapsed ticks of the active recipe. */
    public int getRecipeProgress() {
        return this.data.get(DATA_PROGRESS);
    }

    /** Length of the active recipe in ticks. */
    public int getRecipeLength() {
        return this.data.get(DATA_LENGTH);
    }

    /** The group the running craft belongs to, or {@code null} when the machine is idle. */
    @Nullable
    public Integer getActiveChannel() {
        int value = this.data.get(DATA_CHANNEL);
        return value == NO_CHANNEL ? null : value;
    }

    /**
     * Filled cells of the energy trough, out of the twenty the fill sprite provides.
     *
     * <p>Each cell is five percent, so the count is the percentage divided by five rounded down:
     * seven percent lights one cell and ninety nine percent lights nineteen.</p>
     */
    public int getEnergyCells() {
        int capacity = getEnergyCapacity();
        int stored = getEnergyStored();
        if (capacity <= 0 || stored <= 0) {
            return 0;
        }
        long cells = (long) stored * 20L / capacity;
        return (int) Math.min(20L, cells);
    }

    public boolean isLockRecipe() {
        return hasFlag(FLAG_LOCK_RECIPE);
    }

    public boolean isVoidOutput() {
        return hasFlag(FLAG_VOID_OUTPUT);
    }

    public boolean isBatchMode() {
        return hasFlag(FLAG_BATCH_MODE);
    }

    public boolean isRunning() {
        return hasFlag(FLAG_RUNNING);
    }

    /** Whether the machine is in the "input isolated" state; the button on the panel toggles it. */
    public boolean isIsolateInput() {
        return hasFlag(FLAG_ISOLATE_INPUT);
    }

    private boolean hasFlag(int flag) {
        return (this.data.get(DATA_FLAGS) & flag) != 0;
    }

    private int combine(int lowIndex, int highIndex) {
        int low = this.data.get(lowIndex) & 0xFFFF;
        int high = this.data.get(highIndex) & 0xFFFF;
        return (high << 16) | low;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        if (this.tile == null) {
            return false;
        }
        boolean handled = this.tile.handleMenuButton(player, id);
        // The switch, the flags and the energy totals can all change without touching a slot, so
        // push a fresh snapshot right away instead of waiting for the next periodic sync.
        forceSync();
        return handled;
    }

    /**
     * Periodically mirrors the machine state to the client, once per
     * {@value #DATA_SYNC_INTERVAL_TICKS} ticks.
     *
     * <p>Vanilla calls this every tick, and {@code SimpleContainerData.set} marks its slot dirty
     * unconditionally, so writing the values here would send five packets per tick even when
     * nothing changed. The values only move at the speed of the machine's own ten second structure
     * scan and of port energy, so a one second cadence is plenty and keeps the traffic flat.</p>
     */
    @Override
    public void broadcastChanges() {
        if (this.tile != null) {
            // The status line counts up while a recipe runs, so it is refreshed every tick. Those
            // slots are two bytes and one ordinal, which is far cheaper than the energy totals.
            syncStatus();
            if (--this.dataSyncCountdown <= 0) {
                syncData();
                this.dataSyncCountdown = DATA_SYNC_INTERVAL_TICKS;
            }
        }
        super.broadcastChanges();
    }

    /** Mirrors the status line only; cheap enough to run every tick. */
    private void syncStatus() {
        assert this.tile != null;
        this.data.set(DATA_STATUS, this.tile.getStatus().ordinal());
        this.data.set(DATA_PROGRESS, this.tile.getRecipeProgress());
        this.data.set(DATA_LENGTH, this.tile.getRecipeLength());
        Integer channel = this.tile.activeChannel();
        this.data.set(DATA_CHANNEL, channel == null ? NO_CHANNEL : channel);
        // Synced here, not only in the one-second pass: the status can flip to "not enough power"
        // on any tick, and the line quotes this figure — a stale zero would be printed for up to a
        // second before the real number arrived.
        int refused = this.tile.refusedCost();
        this.data.set(DATA_REFUSED_LO, refused & 0xFFFF);
        this.data.set(DATA_REFUSED_HI, (refused >>> 16) & 0xFFFF);
    }

    /** The overclock setting as the side pane shows it; {@code -1} means automatic. */
    public int getOverclockLimit() {
        return this.data.get(DATA_OVERCLOCK);
    }

    /**
     * The overclock levels actually in force.
     *
     * <p>Not the same as {@link #getOverclockLimit}: the setting may say automatic, and either way
     * the energy ports cap it. The side pane shows this one, because it is what the machine runs
     * at and what the cost is billed against.</p>
     */
    @SuppressWarnings("unused")
    public int getOverclockSteps() {
        return this.tile == null ? 0 : this.tile.effectiveOverclock();
    }

    /** The highest overclock this machine's energy hatches can support at all. */
    public int getReachableOverclock() {
        return this.tile == null ? 0 : this.tile.reachableOverclock();
    }

    /** The cap on what the overclock box may hold, independent of the setting it edits. */
    public int getOverclockCeiling() {
        return this.tile == null ? 0 : this.tile.overclockCeiling();
    }

    /**
     * The parallel copies this machine's hardware grants, or zero when it imposes no ceiling.
     *
     * <p>Read straight from the tile rather than recomputed from a voltage tier here: the arithmetic
     * belongs to the machine that applies it, and duplicating it in the window is how the figure the
     * player sees drifts away from the figure the scheduler uses.</p>
     */
    public int getMachineParallelCeiling() {
        return this.tile == null ? 0 : this.tile.machineParallelCeiling();
    }

    /** Cheapst recipe the last search turned down for cost, in FE per tick; zero when none. */
    public int getRefusedCost() {
        return combine(DATA_REFUSED_LO, DATA_REFUSED_HI);
    }

    /** The parallel ceiling as the side pane shows it; {@code 0} means "always at maximum". */
    public int getParallelLimit() {
        return this.data.get(DATA_PARALLEL);
    }

    /** Writes the current machine state into the synced data slots. */
    private void syncData() {
        if (this.tile == null) {
            return;
        }
        int stored = this.tile.getStoredInputEnergy();
        int capacity = this.tile.getInputEnergyCapacity();
        int rate = this.tile.getInputEnergyRate();
        this.data.set(DATA_STORED_LO, stored & 0xFFFF);
        this.data.set(DATA_STORED_HI, (stored >>> 16) & 0xFFFF);
        this.data.set(DATA_CAPACITY_LO, capacity & 0xFFFF);
        this.data.set(DATA_CAPACITY_HI, (capacity >>> 16) & 0xFFFF);
        this.data.set(DATA_RATE_LO, rate & 0xFFFF);
        this.data.set(DATA_RATE_HI, (rate >>> 16) & 0xFFFF);
        this.data.set(DATA_FLAGS, this.tile.getStateFlags());
        this.data.set(DATA_OVERCLOCK, this.tile.getOverclockLimit());
        this.data.set(DATA_PARALLEL, this.tile.getParallelLimit());
        syncStatus();
    }

    /** Syncs immediately, then restarts the one second cadence. */
    public void forceSync() {
        if (this.tile == null) {
            return;
        }
        syncData();
        this.dataSyncCountdown = DATA_SYNC_INTERVAL_TICKS;
        super.broadcastChanges();
    }

    /**
     * Shift-click routing: the machine slot is the first destination for everything.
     *
     * <p>Shifting out of the machine slot returns the item to the player. Shifting from the
     * player inventory first tries the machine slot, then falls back to moving between the
     * inventory and the hotbar.</p>
     */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;

        if (index == MACHINE_SLOT) {
            moved = this.moveItemStackTo(stack, PLAYER_START, PLAYER_END, true);
        } else {
            moved = this.moveItemStackTo(stack, MACHINE_SLOT, MACHINE_SLOT + 1, false);
            if (!moved) {
                moved = index < HOTBAR_START
                        ? this.moveItemStackTo(stack, HOTBAR_START, PLAYER_END, false)
                        : this.moveItemStackTo(stack, PLAYER_START, HOTBAR_START, false);
            }
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.tile != null) {
            // The dismantle button removes the host, and a removed block entity keeps its old
            // level and position, so these two checks are what close the screen over the air.
            if (this.tile.isRemoved()) {
                return false;
            }
            // The host block class, not one machine's id: both kettles share the class, so naming
            // the basic id invalidated the advanced window on the tick it opened and vanilla shut
            // it again. A dismantled host becomes air or casing, neither of that class, so the
            // teardown case still closes the window.
            if (this.tile.getLevel() != null
                    && !(this.tile.getLevel().getBlockState(this.tile.getBlockPos())
                            .getBlock() instanceof GTChemicalReactorMachineBlock)) {
                return false;
            }
        }
        return this.container.stillValid(player);
    }

    private static final class ControllerSlot extends Slot {

        private ControllerSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return this.container.canPlaceItem(this.getSlotIndex(), stack);
        }

        @Override
        public int getMaxStackSize() {
            return GTChemicalReactorMachineBlockEntity.CONTROLLER_SLOT_LIMIT;
        }
    }
}
