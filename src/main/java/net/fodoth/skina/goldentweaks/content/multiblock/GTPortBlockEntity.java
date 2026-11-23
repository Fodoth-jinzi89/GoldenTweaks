package net.fodoth.skina.goldentweaks.content.multiblock;

import net.fodoth.skina.goldentweaks.GoldenTweaks;

import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.world.Container;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.weibai.avaritia_more_items.common.items.matter_cluster.BetterMatterClusterItem;
import net.weibai.avaritia_more_items.common.items.matter_cluster.FluidMatterClusterItem;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.PortMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * Storage behind one of this mod's own ports.
 *
 * <p>One block entity type serves every port; the kind comes from the block, and the tier from the
 * block state. Capacities are taken from AMI's per-tier tables so a port holds exactly what an AMI
 * port of the same tier would — the multiblock matcher compares against those numbers when it counts
 * what the ports can supply.</p>
 *
 * <p>The handler is rebuilt whenever the tier changes. Rebuilding rather than keeping a fixed-size
 * buffer means a port never has to migrate contents between differently sized stores mid-tick; the
 * contents are re-inserted into the new handler and any surplus is dropped, which can only happen if
 * a player shrinks a port that was full.</p>
 */
public class GTPortBlockEntity extends BlockEntity
        implements MenuProvider, Nameable, Container {

    private static final String TAG_CONTENT = "Content";
    private static final String TAG_MODE = "Mode";
    private static final String TAG_CHANNEL = "Channel";

    private static final String TAG_PRIORITY = "Priority";
    private static final String TAG_ENABLED = "Enabled";
    /** Stacking mode: one cell per entry, or as many as it needs. */
    private static final String TAG_SINGLE_CELL = "SingleCell";
    /** Whether the port also pulls from / pushes to the block it faces. */
    private static final String TAG_ACTIVE = "Active";
    private static final String TAG_CUSTOM_NAME = "CustomName";
    private static final String TAG_TOOL = "Tool";
    private static final String TAG_FILTER = "Filter";

    /** Longest accepted text in the three free-form fields, counted in characters. */
    public static final int TEXT_LIMIT = 64;

    @Nullable
    private IItemHandler items;
    @Nullable
    private IFluidHandler fluids;
    @Nullable
    private IEnergyStorage energy;

    /**
     * The three free-form fields shown at the top left.
     *
     * <p>Stored as plain strings, defaulting to {@code "0"} as the panel shows them. They are kept
     * per block and dropped with it: a broken port returns an item carrying no data, so a field
     * value never outlives the block that held it.</p>
     */
    /**
     * The name the player gave this port, or {@code null} for the default one.
     *
     * <p>Held as a component rather than a plain string so it keeps its styling, and so the same
     * value can be written straight onto the dropped item's {@code CUSTOM_NAME}.</p>
     */
    @Nullable
    private Component customName;

    private String mode = DEFAULT_TEXT;
    private String channel = DEFAULT_TEXT;
    private String priority = DEFAULT_TEXT;

    /**
     * Whether this port takes part in the machine.
     *
     * <p>A disabled port still exists and still shows its contents, but reports no capability, so
     * the machine neither draws from nor delivers to it. That is what the run button on the window
     * toggles.</p>
     */
    private boolean enabled = true;

    /**
     * Whether each kind of content is confined to its own share of the port.
     *
     * <p>The two directions want opposite defaults, because they solve opposite problems.</p>
     *
     * <p>An <b>input</b> port is fed by pipes that may deliver several things at unequal rates. It
     * has to keep room for each of them, or the faster one fills the whole port and the slower one
     * never gets in — so inputs start in single-cell mode, one share per kind.</p>
     *
     * <p>An <b>output</b> port receives whatever a finished craft produces, usually one kind at a
     * time, and wants the largest possible buffer for it — so outputs start in multi-cell mode.</p>
     */
    private boolean singleCell;

    /**
     * Whether this port reaches through its own face instead of waiting to be reached.
     *
     * <p>Passive is the plain behaviour: the port offers a capability and the machine, or a pipe,
     * comes to it. Active adds one extra step each tick — an input pulls from whatever block it
     * faces, an output pushes into it. That is what lets a hatch sit against a chest or a tank
     * without a pipe in between.</p>
     *
     * <p>Defaults follow the direction, because the two want opposite things: an input is normally
     * pushed into by a belt or a pipe, while an output is expected to hand its products on. Only the
     * first build applies the default; a later tier change keeps the player's choice.</p>
     */
    private boolean active;

    /**
     * Ticks left before the next reach-through step.
     *
     * <p>Per tier, not per tick: a cheap port moves one item a second at best. Kept as a field so a
     * slow port still fires on a steady cadence rather than whenever it happens to be ticked.</p>
     */
    private int reachCountdown = 1;

    /**
     * Whether the channel field is frozen because the machine is running a craft.
     *
     * <p>Set by the machine when it starts a reaction and cleared when it ends. Channels decide
     * which ports a craft draws from and delivers to, so letting a player re-group the ports mid-run
     * would move an in-flight recipe onto a different production line.</p>
     */
    private boolean channelLocked;

    /**
     * The chemical storage for a hopper, or {@code null} for any other kind.
     *
     * <p>Typed as {@code Object} on purpose: the concrete class names {@code ChemicalStack}, so a
     * field of that type would have to be resolved whenever this entity loads — and it loads on
     * every world, including ones without Mekanism. {@link GTPortStorages} narrows it back when the
     * mod is known to be there.</p>
     */
    @Nullable
    private Object chemicals;

    /** The essentia storage for a tank, or {@code null} for any other kind. Same reasoning. */
    @Nullable
    private Object essentia;

    /** The tool and filter slots in the lower right, one item each. */
    private final ItemStackHandler toolSlot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            GTPortBlockEntity.this.onContentChanged();
        }
    };
    private final ItemStackHandler filterSlot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            GTPortBlockEntity.this.onContentChanged();
        }
    };

    /** Default text for the three fields, which the panel shows until something is typed. */
    public static final String DEFAULT_TEXT = "0";

    /** Tier the current handler was built for, so a state change can be detected. */
    private int builtTier = -1;

    public GTPortBlockEntity(BlockPos pos, BlockState state) {
        this(Objects.requireNonNull(GTMultiblockBlocks.PORT_ENTITY).get(), pos, state);
    }

    public GTPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** The kind this port carries, read from its block. */
    public GTPortKind kind() {
        return getBlockState().getBlock() instanceof GTPortBlock port ? port.kind() : GTPortKind.ITEM;
    }

    /**
     * The face this port points at.
     *
     * <p>Not a restriction: the port is reachable from every side. The setting exists only to give
     * the active mode somewhere to reach — an active port pulls from, or pushes into, whatever block
     * this face touches.</p>
     */
    public Direction front() {
        return getBlockState().getValue(GTPortBlock.FACING);
    }


    /**
     * Marks the port dirty and pushes its contents to nearby clients.
     *
     * <p>The window lists what the port holds, and that list is built from this entity on both
     * sides. Item and fluid ports have no container slots any more, so nothing carries their
     * contents to the client on its own: without this the client's copy stays at whatever it was
     * when the chunk was loaded, and the list would show the port as empty forever.</p>
     *
     * <p>Energy is excluded on purpose — it is read every tick from the menu's synced values, and
     * broadcasting a packet per tick would be pure noise.</p>
     */
    private void onContentChanged() {
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state,
                    Block.UPDATE_CLIENTS);
        }
    }

    /** This port's tier, read from its block, which was registered at that tier. */
    public GTPortTier portTier() {
        return getBlockState().getBlock() instanceof GTPortBlock port
                ? port.portTier() : GTPortTier.byLevel(1);
    }

    /** The AMI tier of the same rank, which the structure scan compares against. */
    public MultiblockPortTier tier() {
        return portTier().ami();
    }

    /** Rebuilds the handler if the tier no longer matches the one it was built for. */
    private void ensureHandler() {
        int tierIndex = portTier().level();
        if (tierIndex == this.builtTier) {
            return;
        }

        boolean firstBuild = this.builtTier == -1;
        this.builtTier = tierIndex;
        if (firstBuild) {
            // The direction decides both resting switches. Only on the first build: a later rebuild
            // is a tier change, and the player's own choices must survive it.
            this.singleCell = mode() == PortMode.INPUT;
            this.active = mode() == PortMode.OUTPUT;
        }

        switch (kind()) {
            case ITEM -> {
                // Slots scale with the level squared, and each slot's ceiling with the level, so a
                // tier IX bus holds 81 stacks of up to 576 rather than 9 of 64.
                int slots = portTier().itemSlots();
                int limit = portTier().slotLimit();
                this.items = new ItemStackHandler(slots) {
                    /** One cell's worth. The mode changes how many cells a type may spread across,
                     * never how much a single cell holds. */
                    @Override
                    public int getSlotLimit(int slot) {
                        return limit;
                    }

                    /**
                     * The cell ceiling, ignoring what the item itself can stack to.
                     *
                     * <p>{@link ItemStackHandler} otherwise takes the smaller of this and the item's
                     * own {@code getMaxStackSize()}, which caps a slot at one for anything vanilla
                     * keeps unstackable — a single pickaxe or bucket per cell, however large the cell
                     * is. That is a vanilla inventory rule, not a storage one: AE's cells count items
                     * and pay no attention to it, and this is the same kind of buffer.</p>
                     */
                    @Override
                    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
                        return limit;
                    }

                    /**
                     * Takes as much as was asked for, not one vanilla stack of it.
                     *
                     * <p>The base implementation clips the request to the item's own stack size, so a
                     * slot holding 400 pickaxes would hand them out one at a time however large the
                     * amount asked for — a hopper pulling from the port would appear to swallow far
                     * less than it was given. The cell's own ceiling is the only limit here.</p>
                     */
                    @Override
                    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                        if (amount <= 0) {
                            return ItemStack.EMPTY;
                        }
                        ItemStack existing = getStackInSlot(slot);
                        if (existing.isEmpty()) {
                            return ItemStack.EMPTY;
                        }
                        int taken = Math.min(amount, existing.getCount());
                        if (!simulate) {
                            ItemStack left = existing.copyWithCount(existing.getCount() - taken);
                            setStackInSlot(slot, left);
                        }
                        return existing.copyWithCount(taken);
                    }

                    /**
                     * Saves counts as plain ints rather than through {@link ItemStack#save}.
                     *
                     * <p>That path runs the stack through {@code ItemStack.CODEC}, whose count field is
                     * {@code intRange(1, 99)} — a cell holding 576 would be written back as 99 and the
                     * rest silently lost on every save. Keeping the count beside the stack is what lets
                     * a cell hold more than a vanilla inventory ever could, and survive the trip.</p>
                     */
                    @Override
                    public @NotNull CompoundTag serializeNBT(HolderLookup.@NotNull Provider registries) {
                        CompoundTag tag = new CompoundTag();
                        ListTag list = new ListTag();
                        for (int slot = 0; slot < getSlots(); slot++) {
                            ItemStack stack = getStackInSlot(slot);
                            if (stack.isEmpty()) {
                                continue;
                            }
                            CompoundTag entry = new CompoundTag();
                            entry.putInt("Slot", slot);
                            entry.putInt("Count", stack.getCount());
                            entry.put("Stack", stack.copyWithCount(1).save(registries));
                            list.add(entry);
                        }
                        tag.put("Items", list);
                        return tag;
                    }

                    @Override
                    public void deserializeNBT(HolderLookup.@NotNull Provider registries, @NotNull CompoundTag tag) {
                        for (int slot = 0; slot < getSlots(); slot++) {
                            setStackInSlot(slot, ItemStack.EMPTY);
                        }
                        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
                        for (int index = 0; index < list.size(); index++) {
                            CompoundTag entry = list.getCompound(index);
                            int slot = entry.getInt("Slot");
                            if (slot < 0 || slot >= getSlots()) {
                                continue;
                            }
                            ItemStack stack = ItemStack.parseOptional(registries, entry.getCompound("Stack"));
                            if (stack.isEmpty()) {
                                continue;
                            }
                            // Clamped to the cell, not to the item: an older save may predate a tier
                            // change, and the amount still has to fit what this port can hold.
                            int count = Math.min(entry.getInt("Count"), limit);
                            setStackInSlot(slot, stack.copyWithCount(Math.max(1, count)));
                        }
                    }

                    /**
                     * Enforces the stacking mode on the way in.
                     *
                     * <p>Not a drawing preference. Multi-cell mode is simply a bigger chest: one type
                     * may fill several slots, and still only one slot's worth each. Single-cell mode
                     * confines a type to the slot it landed in, so it holds one slot's worth and no
                     * more.</p>
                     */
                    @Override
                    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                        if (singleCell && !stack.isEmpty() && holdsTypeElsewhere(slot, stack)) {
                            return stack;
                        }
                        return super.insertItem(slot, stack, simulate);
                    }

                    @Override
                    protected void onContentsChanged(int slot) {
                        GTPortBlockEntity.this.onContentChanged();
                    }
                };
                this.fluids = null;
                this.energy = null;
                this.chemicals = null;
                this.essentia = null;
            }
            case FLUID -> {
                // One tank per level, each holding 8000 mB per level. Ours rather than AMI's,
                // because the stacking mode has to decide which tank a fluid may enter — AMI's
                // buffer spreads a fluid across every tank with room, which is exactly the
                // behaviour multi-cell mode asks for and single-cell mode must prevent.
                int tanks = portTier().fluidTanks();
                int capacity = portTier().tankCapacity();
                GTPortFluidStorage replacement = new GTPortFluidStorage(
                        tanks, capacity, this::isSingleCellMode);
                replacement.setChangedHook(this::onContentChanged);
                this.fluids = replacement;
                this.items = null;
                this.energy = null;
                this.chemicals = null;
                this.essentia = null;
            }
            case CHEMICAL -> {
                this.items = null;
                this.fluids = null;
                this.energy = null;
                this.chemicals = GTPortStorages.createChemical(
                        portTier().chemicalTanks(), portTier().chemicalTankCapacity(),
                        this::isSingleCellMode, this::onContentChanged);
                this.essentia = null;
            }
            case ESSENTIA -> {
                this.items = null;
                this.fluids = null;
                this.energy = null;
                this.chemicals = null;
                this.essentia = GTPortStorages.createEssentia(
                        portTier().essentiaTanks(), portTier().essentiaTankCapacity(),
                        this::isSingleCellMode, this::isActive,
                        () -> mode() == PortMode.OUTPUT, this::onContentChanged);
            }
            case ENERGY -> {
                // A dynamo hatch supplies and stores half what an energy hatch of the same tier
                // does; the block's own mode is what decides which of the two this port is.
                boolean output = mode() == PortMode.OUTPUT;
                // Long, not int: the top tiers hold more than an int can express, and clamping on
                // the way in is the ceiling this was meant to lift.
                BigInteger capacity = output
                        ? GTPortEnergyTiers.outputCapacityExact(portTier().level())
                        : GTPortEnergyTiers.capacityExact(portTier().level());
                BigInteger transfer = output
                        ? GTPortEnergyTiers.outputTransferExact(portTier().level())
                        : GTPortEnergyTiers.transferExact(portTier().level());
                // Insertion is capped at the transfer rate, extraction is not. The machine draws a
                // whole tick's cost in one call and reads a shortfall as "no power", so a symmetric
                // cap would switch it off whenever a recipe cost more than the port's transfer rate.
                GTPortEnergyStorage replacement = new GTPortEnergyStorage(capacity, transfer);
                replacement.setChangedHook(this::onContentChanged);
                this.energy = replacement;
                this.items = null;
                this.fluids = null;
                this.chemicals = null;
                this.essentia = null;
            }
        }
    }

    /**
     * Whether this handler already holds {@code stack}'s type in a slot other than {@code slot}.
     *
     * <p>Only meaningful in single-cell mode, where one type is confined to one cell. Counts as a
     * match only when the other slot holds the same item — a partially filled slot still counts, so
     * the type cannot creep into a second cell as the first one fills.</p>
     */
    private boolean holdsTypeElsewhere(int slot, ItemStack stack) {
        if (!(this.items instanceof ItemStackHandler handler)) {
            return false;
        }
        for (int index = 0; index < handler.getSlots(); index++) {
            if (index == slot) {
                continue;
            }
            ItemStack other = handler.getStackInSlot(index);
            if (!other.isEmpty() && ItemStack.isSameItemSameComponents(other, stack)) {
                return true;
            }
        }
        return false;
    }

    // ---- Container view, so Containers.dropContentsOnDestroy can return the contents ----
    //
    // The machine does the same thing for its controller slot, and for the same reason: that helper
    // is the one that reads a tile's container and drops it, guarding on the block actually
    // changing. Exposing it here means a broken port spills what it held, and the packing into
    // matter clusters happens in the drop path itself rather than being reinvented per block.

    @Override
    public int getContainerSize() {
        return storageSlots();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        if (this.items != null) {
            return slot < this.items.getSlots() ? this.items.getStackInSlot(slot) : ItemStack.EMPTY;
        }
        // Fluids have no item form; the drop path reads them through the fluid view below.
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (this.items != null && slot < this.items.getSlots()) {
            this.items.insertItem(slot, stack, false);
        }
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if (this.items == null || slot >= this.items.getSlots()) {
            return ItemStack.EMPTY;
        }
        return this.items.extractItem(slot, amount, false);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot, Integer.MAX_VALUE);
    }

    @Override
    public void clearContent() {
        if (this.items != null) {
            for (int slot = 0; slot < this.items.getSlots(); slot++) {
                this.items.extractItem(slot, Integer.MAX_VALUE, false);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        if (this.items != null) {
            for (int slot = 0; slot < this.items.getSlots(); slot++) {
                if (!this.items.getStackInSlot(slot).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    /**
     * Empties the port onto the ground, packed into matter clusters.
     *
     * <p>One cluster for the items it held and one for the fluids, matching what a dismantled
     * machine gives back. Called from the block's removal, which is the only point where the
     * contents still exist.</p>
     */
    public void dropContentsPacked() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        if (this.items != null) {
            for (int slot = 0; slot < this.items.getSlots(); slot++) {
                ItemStack stack = this.items.extractItem(slot, Integer.MAX_VALUE, false);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }
        if (!items.isEmpty()) {
            List<ItemStack> packed = BetterMatterClusterItem.makeClusters(items);
            for (ItemStack cluster : packed.isEmpty() ? items : packed) {
                Block.popResource(this.level, this.worldPosition, cluster);
            }
        }
        spillFluids();
    }

    /** Drops whatever the tanks hold, packed into fluid clusters. */
    private void spillFluids() {
        if (this.level == null || this.level.isClientSide || this.fluids == null) {
            return;
        }
        java.util.List<FluidStack> held = new java.util.ArrayList<>();
        for (int tank = 0; tank < this.fluids.getTanks(); tank++) {
            FluidStack stack = this.fluids.getFluidInTank(tank);
            if (stack.isEmpty()) {
                continue;
            }
            FluidStack drained = this.fluids.drain(stack, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                held.add(drained);
            }
        }
        if (held.isEmpty()) {
            return;
        }
        List<ItemStack> clusters = FluidMatterClusterItem.makeClusters(held);
        for (ItemStack cluster : clusters) {
            Block.popResource(this.level, this.worldPosition, cluster);
        }
    }

    /** How many slots the container view exposes: item slots, or none for the other kinds. */
    private int storageSlots() {
        return this.items == null ? 0 : this.items.getSlots();
    }

    /** The item handler for an item port, or {@code null} for any other kind. */
    @Nullable
    public IItemHandler itemHandler() {
        ensureHandler();
        return this.items;
    }

    /** The fluid handler for a fluid port, or {@code null} for any other kind. */
    @Nullable
    public IFluidHandler fluidHandler() {
        ensureHandler();
        return this.fluids;
    }

    /** The energy storage for an energy port, or {@code null} for any other kind. */
    @Nullable
    public IEnergyStorage energyStorage() {
        ensureHandler();
        return this.energy;
    }

    /** How much this port can hold: items across all slots, or mB across all tanks. */
    public int capacity() {
        return switch (kind()) {
            case ITEM -> portTier().itemSlots() * portTier().slotLimit();
            case FLUID -> portTier().fluidTanks() * portTier().tankCapacity();
            // Clamped here only because the menu syncs an int; the port itself holds the exact
            // figure.
            case ENERGY -> (mode() == PortMode.OUTPUT
                    ? GTPortEnergyTiers.outputCapacityExact(portTier().level())
                    : GTPortEnergyTiers.capacityExact(portTier().level()))
                    .min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
            case CHEMICAL -> portTier().chemicalTanks() * portTier().chemicalTankCapacity();
            case ESSENTIA -> portTier().essentiaTanks() * portTier().essentiaTankCapacity();
        };
    }

    /**
     * How fast this port accepts energy, in FE per tick.
     *
     * <p>Zero for anything that is not an energy port, so the window can ask unconditionally and
     * show nothing.</p>
     */
    public int inputRate() {
        if (kind() != GTPortKind.ENERGY) {
            return 0;
        }
        return mode() == PortMode.OUTPUT
                ? GTPortEnergyTiers.outputTransfer(portTier().level())
                : GTPortEnergyTiers.transfer(portTier().level());
    }

    /** Tanks a chemical hopper reports, or zero for the other kinds. */
    public int chemicalTankCount() {
        return kind() == GTPortKind.CHEMICAL ? portTier().chemicalTanks() : 0;
    }

    /** Tanks an essentia port reports, or zero for the other kinds. */
    public int essentiaTankCount() {
        return kind() == GTPortKind.ESSENTIA ? portTier().essentiaTanks() : 0;
    }

    /** The chemical handler, or {@code null}. Narrowed by the caller through GTPortStorages. */
    @Nullable
    public Object chemicalStorage() {
        ensureHandler();
        return this.chemicals;
    }

    /** The essentia container, or {@code null}. Narrowed by the caller through GTPortStorages. */
    @Nullable
    public Object essentiaStorage() {
        ensureHandler();
        return this.essentia;
    }

    /**
     * The essentia transport the pipe network sees, or {@code null}.
     *
     * <p>Declared with its Thaumcraft type rather than as {@code Object}: this accessor is only
     * called from the capability lambda, which itself only exists inside the Thaumcraft branch of
     * {@code GTPortCapabilities}. Naming the interface here costs nothing on a world without the
     * mod — this class is only linked when the port entity is, and the field it reads stays
     * {@code Object}.</p>
     */
    @Nullable
    public Object essentiaTransportObject() {
        ensureHandler();
        return this.essentia;
    }

    /** How much one slot of this port holds, which is what an entry's cell span is measured in. */
    public int slotLimit() {
        return portTier().slotLimit();
    }

    /**
     * How much one cell of this port holds, in whatever unit the port deals in.
     *
     * <p>Items and fluids are measured differently — a slot is a stack ceiling, a cell is a tank —
     * so the same number cannot describe both. Reading it from the port keeps the grid's arithmetic
     * in one place instead of assuming the item figure everywhere.</p>
     */
    public int cellSize() {
        return switch (kind()) {
            case ITEM -> portTier().slotLimit();
            case FLUID -> portTier().tankCapacity();
            case ENERGY -> Integer.MAX_VALUE;
            case CHEMICAL -> portTier().chemicalTankCapacity();
            case ESSENTIA -> portTier().essentiaTankCapacity();
        };
    }

    /**
     * One server tick: the reach-through step, when this port is active.
     *
     * <p>Passive ports do nothing here — that is the whole difference between the two settings. An
     * active one looks at the block in front of its face and moves contents across one slot per
     * boundary per tick, which is the rate a pipe would have managed and keeps a full chest from
     * being drained in a single tick.</p>
     *
     * <p>{@code null} now means something different from what it means to the capability: the
     * capability treats {@code null} as "the machine asking", while this asks the world, so the
     * front face is always resolved explicitly.</p>
     */
    public void serverTick() {
        // A switched-off port does nothing at all, including reaching through its face: the run
        // switch is the machine's own say over whether this part takes part, and honouring it here
        // but not in the reach-through would let a "disabled" port keep moving items.
        if (!this.active || !this.enabled || this.level == null || this.level.isClientSide) {
            return;
        }
        // Items and fluids move in tier-sized steps every so many ticks: the cheap ports are slow on
        // purpose, so a ULV hatch cannot empty a chest as fast as a UV one. Energy is exempt — its
        // rate is already a per-tick figure — so the countdown only gates those two.
        boolean stepDue = --this.reachCountdown <= 0;
        if (stepDue) {
            this.reachCountdown = portTier().reachInterval();
        }

        Direction front = front();
        BlockPos target = this.worldPosition.relative(front);
        if (!this.level.isLoaded(target)) {
            return;
        }

        switch (kind()) {
            case ITEM -> {
                if (stepDue) {
                    reachItems(target, front);
                }
            }
            case FLUID -> {
                if (stepDue) {
                    reachFluids(target, front);
                }
            }
            case ENERGY -> reachEnergy(target, front);
            case ESSENTIA -> {
                if (stepDue) {
                    reachEssentia(target, front);
                }
            }
            case CHEMICAL -> {
                if (stepDue) {
                    reachChemicals(target, front);
                }
            }
        }
    }

    /**
     * Moves one slot of items across the front face.
     *
     * <p>An input pulls, an output pushes. Both sides are resolved from the world rather than from
     * this port's own handlers, so the far side is whatever block happens to be there.</p>
     */
    private void reachItems(BlockPos target, Direction front) {
        IItemHandler ours = itemHandler();
        if (ours == null) {
            return;
        }
        IItemHandler theirs = null;
        if (this.level != null) {
            theirs = this.level.getCapability(
                    Capabilities.ItemHandler.BLOCK, target, front.getOpposite());
        }
        if (theirs == null) {
            return;
        }

        int budget = portTier().reachItems();
        boolean input = mode() == PortMode.INPUT;
        IItemHandler from = input ? theirs : ours;
        IItemHandler to = input ? ours : theirs;
        for (int slot = 0; slot < from.getSlots() && budget > 0; slot++) {
            ItemStack stack = from.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack offer = stack.copyWithCount(Math.min(stack.getCount(), budget));
            ItemStack leftover = insertInto(to, offer);
            int moved = offer.getCount() - leftover.getCount();
            if (moved <= 0) {
                continue;
            }
            from.extractItem(slot, moved, false);
            budget -= moved;
        }
    }

    /**
     * Offers {@code stack} to a handler and returns what could not be taken.
     *
     * <p>Inserted one slot at a time so a partly filled destination keeps the remainder, which is
     * the amount that has to stay behind in the source.</p>
     */
    private static ItemStack insertInto(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    /** Fluid counterpart of {@link #reachItems}. */
    private void reachFluids(BlockPos target, Direction front) {
        IFluidHandler ours = this.fluids;
        if (ours == null) {
            return;
        }
        IFluidHandler theirs = null;
        if (this.level != null) {
            theirs = this.level.getCapability(
                    Capabilities.FluidHandler.BLOCK, target,
                    front.getOpposite());
        }
        if (theirs == null) {
            return;
        }

        IFluidHandler from = mode() == PortMode.INPUT ? theirs : ours;
        IFluidHandler to = mode() == PortMode.INPUT ? ours : theirs;

        // A budget shared across every tank, so one step can carry several fluids — a hatch fed by a
        // mixed pipe must not spend its whole step on whichever fluid happens to sit in tank 0.
        int budget = portTier().reachFluid();
        for (int tank = 0; tank < from.getTanks() && budget > 0; tank++) {
            FluidStack held = from.getFluidInTank(tank);
            if (held.isEmpty()) {
                continue;
            }
            FluidStack offer = held.copyWithAmount(Math.min(held.getAmount(), budget));
            int taken = to.fill(offer, IFluidHandler.FluidAction.EXECUTE);
            if (taken <= 0) {
                continue;
            }
            from.drain(offer.copyWithAmount(taken), IFluidHandler.FluidAction.EXECUTE);
            budget -= taken;
        }
    }

    /**
     * Essentia counterpart of {@link #reachItems}.
     *
     * <p>Essentia is addressed per aspect rather than by slot, so the walk is over ids: an output
     * offers what it holds, an input asks the neighbour what it has. Both sides go through the
     * Thaumcraft capability, which is also what the ME interface's access card reads.</p>
     */
    private void reachEssentia(BlockPos target, Direction front) {
        // The storage is built lazily, so ask the accessor rather than reading the field: on the
        // first tick after a load it is still null here.
        ensureHandler();
        Object storage = this.essentia;
        if (storage == null || this.level == null) {
            return;
        }
        Direction side = front.getOpposite();
        boolean input = mode() == PortMode.INPUT;
        int budget = portTier().reachItems();
        List<ResourceLocation> aspects = input
                ? GTPortStorages.neighbourEssentiaAspects(this.level, target, side)
                : GTPortStorages.essentiaAspects(storage);
        for (ResourceLocation aspect : aspects) {
            if (budget <= 0) {
                break;
            }
            long moved = GTPortStorages.reachEssentia(
                    storage, aspect, budget, this.level, target, side, input);
            if (moved > 0) {
                budget -= (int) Math.min(moved, budget);
            }
        }
    }

    /**
     * Chemical counterpart of {@link #reachItems}.
     *
     * <p>Chemicals are addressed per chemical rather than by slot, so an output walks what it holds
     * and an input asks the neighbour what it has — the same shape as the essentia path.</p>
     */
    private void reachChemicals(BlockPos target, Direction front) {
        ensureHandler();
        Object storage = this.chemicals;
        if (storage == null || this.level == null) {
            return;
        }
        Direction side = front.getOpposite();
        boolean input = mode() == PortMode.INPUT;
        long budget = portTier().reachFluid();
        List<ResourceLocation> chemicals = input
                ? GTPortStorages.neighbourChemicalIds(this.level, target, side)
                : GTPortStorages.chemicalIds(storage);
        for (ResourceLocation chemical : chemicals) {
            if (budget <= 0) {
                break;
            }
            long moved = GTPortStorages.reachChemical(
                    storage, chemical, budget, this.level, target, side, input);
            if (moved > 0) {
                budget -= Math.min(moved, budget);
            }
        }
    }

    /** Energy counterpart of {@link #reachItems}: a fixed slice per tick, in either direction. */
    private void reachEnergy(BlockPos target, Direction front) {
        if (this.energy == null) {
            return;
        }
        var capability = Capabilities.EnergyStorage.BLOCK;
        IEnergyStorage theirs =
                null;
        if (this.level != null) {
            theirs = this.level.getCapability(capability, target, front.getOpposite());
        }
        if (theirs == null) {
            return;
        }
        // Energy has no slow cadence of its own: the tier's transfer rate *is* the per-tick figure,
        // set when the tiers were balanced, so it is moved every tick rather than in occasional
        // larger steps like items and fluids.
        int rate = inputRate();
        if (rate <= 0) {
            return;
        }

        if (mode() == PortMode.INPUT) {
            // Pull: ask the neighbour for a slice, offer it here.
            int wanted = theirs.extractEnergy(rate, true);
            if (wanted <= 0) {
                return;
            }
            int accepted = this.energy.receiveEnergy(wanted, false);
            if (accepted > 0) {
                theirs.extractEnergy(accepted, false);
            }
        } else {
            int wanted = this.energy.extractEnergy(rate, true);
            if (wanted <= 0) {
                return;
            }
            int accepted = theirs.receiveEnergy(wanted, false);
            if (accepted > 0) {
                this.energy.extractEnergy(accepted, false);
            }
        }
    }

    /** Whether this port reaches through its face rather than waiting to be reached. */
    public boolean isActive() {
        return this.active;
    }

    /** Flips the reach-through behaviour and tells nearby clients. */
    public void toggleActive() {
        this.active = !this.active;
        onContentChanged();
    }

    /** Whether entries are drawn one cell each, rather than spanning as many as they need. */
    public boolean isSingleCellMode() {
        return this.singleCell;
    }

    /** Flips the stacking mode and tells nearby clients. */
    public void toggleSingleCell() {
        this.singleCell = !this.singleCell;
        onContentChanged();
    }

    /** Whether this port currently takes part in the machine. */
    public boolean isEnabled() {
        return this.enabled;
    }

    /** Turns participation on or off, and tells nearby clients so the button follows. */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        onContentChanged();
    }

    /** The tool slot, one item. */
    public IItemHandler toolSlot() {
        return this.toolSlot;
    }

    /** The filter slot, one item. */
    public IItemHandler filterSlot() {
        return this.filterSlot;
    }

    /**
     * The name to show, either the player's or the block's own.
     *
     * <p>Every tier has its own key: each tier is a separate block, so the name states which one it
     * is — a player looking at two ports has no other way to tell a ULV one from a UV one. The
     * tier-agnostic key remains as a fallback so a partially translated build still names the block
     * rather than printing the raw key.</p>
     */
    @Override
    public @NotNull Component getDisplayName() {
        if (this.customName != null) {
            return this.customName;
        }
        String base = "block.goldentweaks.gt_port_"
                + kind().id() + "_" + mode().name().toLowerCase(java.util.Locale.ROOT);
        String key = base + "_" + portTier().id();
        return Language.getInstance().has(key)
                ? Component.translatable(key) : Component.translatable(base);
    }

    /** The player-given name, or {@code null} when the port still uses its default. */
    @Nullable
    @Override
    public Component getCustomName() {
        return this.customName;
    }

    /** Whether the player has renamed this port. Read by the tooltip mods to title the panel. */
    @Override
    public boolean hasCustomName() {
        return this.customName != null;
    }

    /**
     * The name used when this port is referred to rather than displayed.
     *
     * <p>The same value as {@link #getDisplayName()}; {@code Nameable} asks for both, and the
     * machine answers them the same way, so the two agree whichever one a mod reaches for.</p>
     */
    @Override
    public @NotNull Component getName() {
        return getDisplayName();
    }

    /**
     * Renames the port, or restores the default wording when given {@code null}.
     *
     * <p>An empty name clears the override rather than storing a blank line, which is what a player
     * deleting the text expects.</p>
     */
    public void setCustomName(@Nullable Component name) {
        this.customName = name;
        onContentChanged();
    }

    /** The {@code mode} field's text. */
    public String modeText() {
        return this.mode;
    }

    /** The {@code channel} field's text. */
    public String channelText() {
        return this.channel;
    }

    /** The {@code priority} field's text. */
    public String priorityText() {
        return this.priority;
    }

    /**
     * The parsed priority: higher is used first.
     *
     * <p>Anything that is not an integer reads as zero, the same default the field starts with. A
     * port is never excluded for a bad value; it simply sorts with the neutral group, which is the
     * least surprising outcome for a typo.</p>
     */
    public int priority() {
        try {
            return Integer.parseInt(this.priority.trim());
        } catch (NumberFormatException unparseable) {
            return 0;
        }
    }

    /**
     * Stores one of the free-form fields.
     *
     * <p>Clip and sanitise rather than trust the client: the value arrives from a text box, and a
     * length limit enforced only there would still let a modified client store anything.</p>
     *
     * <p>The channel field is normalised on save. Unparseable text becomes {@code "0"}, a wildcard,
     * and a list containing the wildcard collapses to just that — the stored value is what the
     * window then echoes back, so a mistyped field shows its correction instead of silently acting
     * as a wildcard while still reading as something else.</p>
     */
    public void setText(Field field, String value) {
        // A channel edit during a run is refused rather than applied: the craft was matched against
        // the ports as they were, and re-grouping them now would redirect its outputs.
        if (field == Field.CHANNEL && this.channelLocked) {
            return;
        }
        String text = value.length() > TEXT_LIMIT ? value.substring(0, TEXT_LIMIT) : value;
        if (field == Field.NAME) {
            // The name is not a free-form counter: it is the port's identity, so an empty box puts
            // the default wording back instead of leaving the port unnamed.
            this.customName = text.isBlank() ? null : Component.literal(text);
            onContentChanged();
            return;
        }
        if (field == Field.CHANNEL) {
            text = GTPortChannels.normalise(text);
        }
        switch (field) {
            case MODE -> this.mode = text;
            case CHANNEL -> this.channel = text;
            case PRIORITY -> this.priority = text;
        }
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    /** Whether the channel field is currently frozen by a running craft. */
    public boolean isChannelLocked() {
        return this.channelLocked;
    }

    /**
     * Freezes or unfreezes the channel field.
     *
     * <p>Called by the machine when a craft starts and ends. The window reads it to grey the field
     * out, and {@link #setText} refuses the edit outright so a stale packet cannot slip through.</p>
     */
    public void setChannelLocked(boolean locked) {
        if (this.channelLocked == locked) {
            return;
        }
        this.channelLocked = locked;
        onContentChanged();
    }

    /** The channels this port takes part in, as parsed from its field. */
    public List<Integer> channels() {
        return GTPortChannels.effective(this.channel);
    }

    /**
     * The modes this port serves, as parsed from its field.
     *
     * <p>Same syntax as the channel field, and the same wildcard rule: {@code 0} means "every
     * mode". Ports are grouped by mode when the machine looks for work, so a set of ports sharing a
     * mode is a set of ports that belong together.</p>
     */
    public List<Integer> modes() {
        return GTPortChannels.effective(this.mode);
    }

    /** Whether this port joins the group for {@code channel}. */
    public boolean joinsChannel(int channel) {
        return GTPortChannels.joins(this.channel, channel);
    }

    /** Which of the three free-form fields a value belongs to. */
    public enum Field {
        NAME,
        MODE,
        CHANNEL,
        PRIORITY
    }

    /**
     * What this port currently holds, as reaction stacks the window can list.
     *
     * <p>Item ports contribute one entry per occupied slot and fluid ports one entry for their
     * single tank. Both sides answer: the client reads its own entity, which the change hook keeps
     * up to date, so the list needs no separate sync channel of its own.</p>
     */
    public List<GTReactionStack> contents() {
        ensureHandler();
        List<GTReactionStack> rows = new ArrayList<>();
        switch (kind()) {
            case ITEM -> {
                if (this.items == null) {
                    break;
                }
                for (int slot = 0; slot < this.items.getSlots(); slot++) {
                    ItemStack stack = this.items.getStackInSlot(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    rows.add(new GTReactionStack(GTReactionStack.Kind.ITEM,
                            BuiltInRegistries.ITEM.getKey(stack.getItem()),
                            stack.getCount(), Optional.empty()));
                }
            }
            case FLUID -> {
                if (this.fluids == null) {
                    break;
                }
                // Every tank, not just the first: a hatch holds one tank per tier level, and a mixed
                // feed — water in one, lava in another — is exactly what those extra tanks are for.
                for (int tank = 0; tank < this.fluids.getTanks(); tank++) {
                    FluidStack stack = this.fluids.getFluidInTank(tank);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    rows.add(new GTReactionStack(GTReactionStack.Kind.FLUID,
                            BuiltInRegistries.FLUID.getKey(stack.getFluid()),
                            stack.getAmount(), Optional.empty()));
                }
            }
            case CHEMICAL -> rows.addAll(GTPortStorages.chemicalContents(this.chemicals));
            case ESSENTIA -> rows.addAll(GTPortStorages.essentiaContents(this.essentia));
            case ENERGY -> {
            }
        }
        return rows;
    }

    /** The port's own mode, read from its block. */
    public PortMode mode() {
        return getBlockState().getBlock() instanceof GTPortBlock port
                ? port.mode() : PortMode.INPUT;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        // Reached when something opens this provider without the extra packet data; the packet path
        // used by the block's click handler is the one that normally builds the menu.
        return new GTPortMenu(id, inventory, this.worldPosition, kind(),
                itemHandler() == null ? 0 : Objects.requireNonNull(itemHandler()).getSlots());
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        // Contents travel with the block entity update. Without this the client's copy stays empty,
        // so an item port would always look hollow however full it really was.
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        ensureHandler();
        tag.putString(TAG_MODE, this.mode);
        tag.putString(TAG_CHANNEL, this.channel);
        tag.putString(TAG_PRIORITY, this.priority);
        tag.putBoolean(TAG_ENABLED, this.enabled);
        tag.putBoolean(TAG_SINGLE_CELL, this.singleCell);
        tag.putBoolean(TAG_ACTIVE, this.active);
        if (this.customName != null) {
            tag.putString(TAG_CUSTOM_NAME,
                    Component.Serializer.toJson(this.customName, registries));
        }
        tag.put(TAG_TOOL, this.toolSlot.serializeNBT(registries));
        tag.put(TAG_FILTER, this.filterSlot.serializeNBT(registries));
        if (this.items instanceof ItemStackHandler handler) {
            tag.put(TAG_CONTENT, handler.serializeNBT(registries));
        } else if (this.fluids instanceof GTPortFluidStorage buffer) {
            tag.put(TAG_CONTENT, buffer.save(registries));
        } else if (this.energy instanceof GTPortEnergyStorage storage) {
            CompoundTag content = new CompoundTag();
            storage.save(content);
            tag.put(TAG_CONTENT, content);
        } else if (this.chemicals != null) {
            tag.put(TAG_CONTENT, GTPortStorages.saveChemical(this.chemicals, registries));
        } else if (this.essentia != null) {
            tag.put(TAG_CONTENT, GTPortStorages.saveEssentia(this.essentia, registries));
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        // Build first so the load target already has the right capacity for this state's tier.
        ensureHandler();

        // Fields default rather than stay empty when a tag is missing: a port placed before this
        // state existed, or handed over with no data, still has to show the panel's defaults.
        this.mode = tag.contains(TAG_MODE) ? tag.getString(TAG_MODE) : DEFAULT_TEXT;
        this.channel = tag.contains(TAG_CHANNEL) ? tag.getString(TAG_CHANNEL) : DEFAULT_TEXT;
        this.priority = tag.contains(TAG_PRIORITY) ? tag.getString(TAG_PRIORITY) : DEFAULT_TEXT;
        this.enabled = !tag.contains(TAG_ENABLED) || tag.getBoolean(TAG_ENABLED);
        // A saved value always wins over the direction default: the port was already configured.
        this.singleCell = tag.contains(TAG_SINGLE_CELL)
                ? tag.getBoolean(TAG_SINGLE_CELL)
                : mode() == PortMode.INPUT;
        this.active = tag.contains(TAG_ACTIVE)
                ? tag.getBoolean(TAG_ACTIVE)
                : mode() == PortMode.OUTPUT;
        try {
            this.customName = tag.contains(TAG_CUSTOM_NAME)
                    ? Component.Serializer.fromJson(tag.getString(TAG_CUSTOM_NAME), registries)
                    : null;
        } catch (RuntimeException malformed) {
            // A hand-edited or corrupt name must not take the whole block entity down with it.
            this.customName = null;
        }
        if (tag.contains(TAG_TOOL)) {
            this.toolSlot.deserializeNBT(registries, tag.getCompound(TAG_TOOL));
        }
        if (tag.contains(TAG_FILTER)) {
            this.filterSlot.deserializeNBT(registries, tag.getCompound(TAG_FILTER));
        }

        if (!tag.contains(TAG_CONTENT)) {
            return;
        }
        CompoundTag content = tag.getCompound(TAG_CONTENT);
        if (this.items instanceof ItemStackHandler handler) {
            handler.deserializeNBT(registries, content);
        } else if (this.fluids instanceof GTPortFluidStorage buffer) {
            buffer.load(content, registries);
        } else if (this.energy instanceof GTPortEnergyStorage storage) {
            storage.load(content);
        } else if (this.chemicals != null) {
            GTPortStorages.loadChemical(this.chemicals, content, registries);
        } else if (this.essentia != null) {
            GTPortStorages.loadEssentia(this.essentia, content, registries);
        }
    }
}
