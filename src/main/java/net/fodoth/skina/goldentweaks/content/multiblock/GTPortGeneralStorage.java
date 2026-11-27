package net.fodoth.skina.goldentweaks.content.multiblock;

import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The storage behind a general assembly: every kind at once, in one part.
 *
 * <p>The other ports hold one kind each and are asked about that one kind. This holds a slice of
 * all five, the way an all-type storage cell does, so one part can feed a machine that wants items,
 * fluids and energy at the same time.</p>
 *
 * <h2>The two figures</h2>
 *
 * <pre>
 * bytes = 1024 * 4^(level-1)
 * types =    4 * level
 * </pre>
 *
 * <p>Bytes grow geometrically and types linearly, which is how AE sizes a cell too: capacity is
 * what a tier buys, type count is a property of being a cell at all. The consequence is that the
 * bytes one type is worth <em>grows</em> with the tier — 256 at ULV, 45.8 G at MAX — rather than
 * staying fixed.</p>
 *
 * <h2>Why every kind gets cells of its own</h2>
 *
 * <p>The cell budget is split four ways — energy has its own store and its own figures, so it
 * takes no cells — and <b>every kind gets at least one</b>.
 * AE's shared pool is the other option and it is what a cell does, but a machine asks about one
 * kind at a time: a craft that happened to arrive after some item stock would find its fluid share
 * eaten, and the window would have no way to say why. A fixed split keeps every kind reachable,
 * which is the point of putting them in one part.</p>
 *
 * <p>One byte is one unit of any content — one item, one mB of fluid, one Mekanism unit, one point
 * of essentia — except energy, which is counted a thousand to the byte so its figures stay beside
 * the others rather than dwarfing them.</p>
 */
public final class GTPortGeneralStorage {

    private final int typeBudget;
    /** Capacity of one cell, in bytes: the tier's budget split over the cells. */
    private final int cellBytes;
    /** Cells in all, summed over the four byte kinds. */
    private final int totalCells;

    /** The tier, kept for the per-kind quotas the switch reads. */
    private final GTPortTier tier;
    /** The player's layout switch, asked on every write rather than baked in at construction. */
    private final java.util.function.BooleanSupplier split;
    private Runnable changedHook;

    private final ItemStackHandler items;
    private final GTPortFluidStorage fluids;
    private final GTPortEnergyStorage energy;
    private final Object chemicals;
    private final Object essentia;

    public GTPortGeneralStorage(GTPortTier tier, java.util.function.BooleanSupplier singleCell,
                                java.util.function.BooleanSupplier active,
                                java.util.function.BooleanSupplier output,
                                Runnable changedHook) {
        this.typeBudget = tier.generalTypes();
        this.tier = tier;
        this.split = singleCell;
        this.changedHook = changedHook;

        // Every store is built at the whole layout's size and never rebuilt: the switch only decides
        // how much of each kind may be used, and the stores ask it on every write.
        //
        // That is not just tidier — a rebuild hands out new store objects, and everything outside
        // this block holds the old ones. A pattern provider that had cached a handler kept writing
        // into an orphaned buffer, which is why flipping the switch looked like it swallowed the
        // contents: they had gone somewhere nothing reads.
        int cells = tier.generalTotalCells();
        this.totalCells = cells;
        this.cellBytes = tier.generalCellBytes();

        int limit = this.cellBytes;
        this.items = new ItemStackHandler(cells) {
            @Override
            public int getSlotLimit(int slot) {
                return limit;
            }

            @Override
            protected int getStackLimit(int slot, @NotNull ItemStack stack) {
                // The cell ceiling, not the item's own stack size: a cell is a buffer, and the
                // vanilla unstackable rule is an inventory rule that does not apply to one.
                return limit;
            }

            /**
             * Enforces the split on the way in.
             *
             * <p>Split mode confines one item to the cells this kind was allotted, so four kinds
             * arriving at once cannot squeeze each other out. Whole mode lets one item use every
             * cell, which is what a line fed by a single bulk source wants.</p>
             */
            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (!stack.isEmpty()) {
                    int quota = quota(0);
                    if (slot >= quota) {
                        return stack;
                    }
                    if (splitMode() && holdsElsewhereBeneath(slot, stack, quota)) {
                        return stack;
                    }
                    // Clipped last, after the layout has decided this cell may hold the kind: the
                    // byte budget is the part's own ceiling, and whole mode lets one kind reach it.
                    long room = roomFor(Kind.ITEM, stack.getCount());
                    if (room <= 0) {
                        return stack;
                    }
                    if (room < stack.getCount()) {
                        return super.insertItem(slot, stack.copyWithCount((int) room), simulate);
                    }
                }
                return super.insertItem(slot, stack, simulate);
            }

            /**
             * Whether a cell below the quota already holds this item.
             *
             * <p>Only the cells this kind may use count: a stack left above the quota by an earlier
             * whole-mode session is not competing for the same room.</p>
             */
            private boolean holdsElsewhereBeneath(int slot, ItemStack stack, int quota) {
                for (int index = 0; index < Math.min(quota, getSlots()); index++) {
                    if (index == slot) {
                        continue;
                    }
                    ItemStack other = getStackInSlot(index);
                    if (!other.isEmpty() && ItemStack.isSameItemSameComponents(other, stack)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected void onContentsChanged(int slot) {
                GTPortGeneralStorage.this.changedHook.run();
            }
        };

        this.fluids = new GTPortFluidStorage(cells, this.cellBytes, this::spreadEverywhere);
        this.fluids.setChangedHook(changedHook);
        this.fluids.setQuota(() -> quota(1));
        this.fluids.setRoom(wanted -> (int) this.roomFor(Kind.FLUID, wanted));

        // Energy is not sized from the byte budget: it has to answer with the same capacity and
        // rate a dedicated energy port of this tier does, or a machine's dual-hatch census would
        // read a different figure from the assembly than from the hatch beside it. The mode picks
        // which half of the table applies, exactly as it does for a hatch or a dynamo.
        boolean isOutput = output.getAsBoolean();
        this.energy = new GTPortEnergyStorage(
                isOutput ? GTPortEnergyTiers.outputCapacityExact(tier.level())
                        : GTPortEnergyTiers.capacityExact(tier.level()),
                isOutput ? GTPortEnergyTiers.outputTransferExact(tier.level())
                        : GTPortEnergyTiers.transferExact(tier.level()));
        this.energy.setChangedHook(changedHook);

        this.chemicals = GTPortStorages.createChemical(cells, this.cellBytes,
                this::spreadEverywhere, changedHook);
        GTPortStorages.setChemicalQuota(this.chemicals, () -> quota(2));
        GTPortStorages.setChemicalRoom(this.chemicals, wanted -> this.roomFor(Kind.CHEMICAL, wanted));

        this.essentia = GTPortStorages.createEssentia(cells, this.cellBytes,
                this::spreadEverywhere, active, output, changedHook);
        GTPortStorages.setEssentiaQuota(this.essentia, () -> quota(3));
        GTPortStorages.setEssentiaRoom(this.essentia, wanted -> (int) this.roomFor(Kind.ESSENTIA, wanted));
    }

    /**
     * The inner stores' own stacking switch, always off.
     *
     * <p>Their switch asks "may one entry take several cells", which is a different question from the
     * assembly's "do the four kinds share the cells". The sharing is decided by the quota below, so
     * these stores are told to spread freely and then kept inside their share by that.</p>
     */
    private boolean spreadEverywhere() {
        return false;
    }

    // ---- the write quota ----
    //
    // The stores are all the whole layout's size. What the switch decides is how many of their cells
    // each kind may fill, and it is asked at write time rather than fixed at construction: a fixed
    // size would have to be rebuilt to change, and rebuilding is what lost the contents.

    /** Whether the kinds share the cells rather than each being allowed all of them. */
    private boolean splitMode() {
        return this.split.getAsBoolean();
    }

    /** Cells one kind may fill under the current layout. */
    private int quota(int kindIndex) {
        return splitMode() ? this.tier.generalCellsForKind(kindIndex) : this.totalCells;
    }

    /** The four kinds that are counted in bytes, named so the budget can be asked per kind. */
    private enum Kind {
        ITEM, FLUID, CHEMICAL, ESSENTIA
    }

    /**
     * How much of {@code wanted} this kind may still take, in bytes.
     *
     * <p>The part keeps one budget and the switch decides how it is shared. Split mode hands each
     * kind its own share and stops it there. Whole mode lets a kind borrow the others' shares — but
     * still only what the part has left, so no kind can push the total past the budget. That is the
     * difference the switch is for: not a bigger part, just a different way of filling it.</p>
     */
    private long roomFor(Kind kind, long wanted) {
        if (wanted <= 0) {
            return 0L;
        }
        if (splitMode()) {
            long share = (long) this.cellBytes * this.tier.generalCellsForKind(kind.ordinal());
            return Math.max(0L, Math.min(wanted, share - used(kind)));
        }
        return Math.max(0L, Math.min(wanted, byteBudget() - used()));
    }

    /** Bytes one kind is holding. */
    private long used(Kind kind) {
        return switch (kind) {
            case ITEM -> itemBytes();
            case FLUID -> this.fluids.totalAmount();
            case CHEMICAL -> sum(GTPortStorages.chemicalContents(this.chemicals));
            case ESSENTIA -> sum(GTPortStorages.essentiaContents(this.essentia));
        };
    }

    /** Bytes all four kinds are holding together. */
    private long used() {
        return itemBytes() + this.fluids.totalAmount()
                + sum(GTPortStorages.chemicalContents(this.chemicals))
                + sum(GTPortStorages.essentiaContents(this.essentia));
    }

    private long itemBytes() {
        long total = 0L;
        for (int slot = 0; slot < this.items.getSlots(); slot++) {
            ItemStack stack = this.items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static long sum(List<GTReactionStack> stacks) {
        long total = 0L;
        for (GTReactionStack stack : stacks) {
            total += stack.amount();
        }
        return total;
    }

    // ---- the five stores, for the entity's own fields ----

    public ItemStackHandler items() {
        return this.items;
    }

    public GTPortFluidStorage fluids() {
        return this.fluids;
    }

    public GTPortEnergyStorage energy() {
        return this.energy;
    }

    public Object chemicals() {
        return this.chemicals;
    }

    public Object essentia() {
        return this.essentia;
    }

    /** The chemicals this assembly holds, for the window and the Jade panel. */
    public List<GTReactionStack> chemicalContents() {
        return GTPortStorages.chemicalContents(this.chemicals);
    }

    /** The aspect this assembly holds, for the window and the Jade panel. */
    public List<GTReactionStack> essentiaContents() {
        return GTPortStorages.essentiaContents(this.essentia);
    }

    // ---- the two figures the window shows ----

    /**
     * Bytes the assembly can hold, which the layout does not change.
     *
     * <p>The switch decides how the budget is <em>shared</em>, not how large it is: split mode
     * divides it between the four kinds, whole mode lets one kind spend it all. A part does not
     * become four times roomier because a player stopped sharing.</p>
     */
    public long byteBudget() {
        return (long) this.cellBytes * this.totalCells;
    }

    /** Types the assembly can hold in all. */
    public int typeBudget() {
        return this.typeBudget;
    }

    /** Cells in all, summed over the four byte kinds. */
    public int totalCells() {
        return this.totalCells;
    }

    /** What one cell holds, in bytes: the figure an entry's cell span is measured against. */
    public int cellBytes() {
        return this.cellBytes;
    }

    /**
     * Bytes currently in use, summed over the four byte kinds.
     *
     * <p>Recomputed rather than tracked: an incremental counter has to be adjusted on every path
     * that moves content, and one missed adjustment leaves it permanently wrong with no way to
     * notice. Summing what is actually there cannot drift.</p>
     *
     * <p>Energy is not counted. Its store is sized from the energy table rather than from this
     * budget, so charging it against the bytes would report a part as fuller than it is — and a
     * charge sitting in a hatch is not something a craft competes with the items for.</p>
     */
    public long bytesUsed() {
        return used();
    }

    /**
     * Types currently in use: one per distinct thing held, energy excluded.
     *
     * <p>Two slots of the same item count once — a type is a kind of thing, not a cell.</p>
     */
    public int typesUsed() {
        Set<String> seen = new HashSet<>();

        for (int slot = 0; slot < this.items.getSlots(); slot++) {
            ItemStack stack = this.items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                seen.add("item:" + BuiltInRegistries.ITEM.getKey(stack.getItem()));
            }
        }
        for (int tank = 0; tank < this.fluids.getTanks(); tank++) {
            var stack = this.fluids.getFluidInTank(tank);
            if (!stack.isEmpty()) {
                seen.add("fluid:" + BuiltInRegistries.FLUID.getKey(stack.getFluid()));
            }
        }
        for (GTReactionStack stack : GTPortStorages.chemicalContents(this.chemicals)) {
            seen.add("chemical:" + stack.id());
        }
        for (GTReactionStack stack : GTPortStorages.essentiaContents(this.essentia)) {
            seen.add("essentia:" + stack.id());
        }

        return seen.size();
    }

    // ---- persistence ----
    //
    // One tag holding all five stores, keyed by kind. The entity's own save path picks a single
    // store by which kind the port is; the assembly is five kinds at once, so it writes its own.

    private static final String TAG_ITEMS = "Items";
    private static final String TAG_FLUIDS = "Fluids";
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_CHEMICALS = "Chemicals";
    private static final String TAG_ESSENTIA = "Essentia";

    public net.minecraft.nbt.CompoundTag save(net.minecraft.core.HolderLookup.Provider registries) {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.put(TAG_ITEMS, this.items.serializeNBT(registries));
        tag.put(TAG_FLUIDS, this.fluids.save(registries));
        net.minecraft.nbt.CompoundTag energyTag = new net.minecraft.nbt.CompoundTag();
        this.energy.save(energyTag);
        tag.put(TAG_ENERGY, energyTag);
        tag.put(TAG_CHEMICALS, GTPortStorages.saveChemical(this.chemicals, registries));
        tag.put(TAG_ESSENTIA, GTPortStorages.saveEssentia(this.essentia, registries));
        return tag;
    }

    public void load(net.minecraft.nbt.CompoundTag tag,
                     net.minecraft.core.HolderLookup.Provider registries) {
        if (tag.contains(TAG_ITEMS)) {
            this.items.deserializeNBT(registries, tag.getCompound(TAG_ITEMS));
        }
        if (tag.contains(TAG_FLUIDS)) {
            this.fluids.load(tag.getCompound(TAG_FLUIDS), registries);
        }
        if (tag.contains(TAG_ENERGY)) {
            this.energy.load(tag.getCompound(TAG_ENERGY));
        }
        if (tag.contains(TAG_CHEMICALS)) {
            GTPortStorages.loadChemical(this.chemicals, tag.getCompound(TAG_CHEMICALS), registries);
        }
        if (tag.contains(TAG_ESSENTIA)) {
            GTPortStorages.loadEssentia(this.essentia, tag.getCompound(TAG_ESSENTIA), registries);
        }
    }
}
