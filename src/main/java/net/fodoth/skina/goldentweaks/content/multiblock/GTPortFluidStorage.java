package net.fodoth.skina.goldentweaks.content.multiblock;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The tanks behind a fluid port, with the stacking mode enforced on the way in.
 *
 * <p>The two modes answer one question: may a single fluid take the whole hatch?</p>
 *
 * <p>Single-cell mode says no. A fluid is admitted only into the one tank that already holds it, or
 * else the first empty tank — and only up to that tank's own capacity. Two fluids arriving at
 * unequal rates therefore both get in: the faster one fills its tank and stops there instead of
 * spilling over the one the slower fluid needs. That is what makes a two-tank hatch fed by a single
 * mixed pipe work at all.</p>
 *
 * <p>Multi-cell mode says yes. A fluid takes whatever room it finds, tank after tank, so a hatch can
 * be completely occupied by one fluid and the next one is turned away. Deliberate: it is how a
 * player asks for a bigger single-fluid buffer.</p>
 */
public class GTPortFluidStorage implements IFluidHandler {

    private static final String TAG_TANKS = "Tanks";
    private static final String TAG_FLUID = "Fluid";
    private static final String TAG_AMOUNT = "Amount";

    private final FluidTank[] tanks;
    /** Read live from the port, so a toggle applies to the very next insertion. */
    private final java.util.function.BooleanSupplier singleCell;
    /**
     * How many tanks are in use, read live like the mode.
     *
     * <p>A general assembly builds this store at the whole layout's size and lets the player's
     * switch decide how much of it belongs to this kind. Refusing the tanks past the quota keeps the
     * store object itself — and everyone's reference to it — intact across a switch, which rebuilding
     * could not do.</p>
     */
    private java.util.function.IntSupplier quota = () -> Integer.MAX_VALUE;
    /**
     * How much of a request may actually be taken, in mB.
     *
     * <p>Asked on the way in, after the quota has picked the tank. An assembly shares one byte
     * budget between four kinds, so a kind that is allowed every tank still cannot take more than
     * the part has left — which is what lets one kind borrow the others' shares without any of them
     * passing the total.</p>
     */
    private java.util.function.IntUnaryOperator room = wanted -> wanted;
    private Runnable changedHook = () -> {
    };

    public GTPortFluidStorage(int tankCount, int tankCapacity, java.util.function.BooleanSupplier singleCell) {
        this.tanks = new FluidTank[Math.max(1, tankCount)];
        for (int index = 0; index < this.tanks.length; index++) {
            this.tanks[index] = new FluidTank(tankCapacity) {
                @Override
                protected void onContentsChanged() {
                    GTPortFluidStorage.this.changedHook.run();
                }
            };
        }
        this.singleCell = singleCell;
    }

    /**
     * Restricts how many tanks this store may use.
     *
     * <p>Asked on every write rather than taken at construction, so a player's switch applies on the
     * next insertion without the store object being replaced.</p>
     */
    public void setQuota(java.util.function.IntSupplier quota) {
        this.quota = quota;
    }

    /** Caps how much of one request this store may take, for a port sharing a budget. */
    public void setRoom(java.util.function.IntUnaryOperator room) {
        this.room = room;
    }

    /** Whether a tank index is inside the quota. */
    private boolean inQuota(int tank) {
        return tank < this.quota.getAsInt();
    }

    public void setChangedHook(Runnable hook) {
        this.changedHook = hook;
    }

    /** Total mB across every tank. */
    public int totalAmount() {
        int total = 0;
        for (FluidTank tank : this.tanks) {
            total += tank.getFluidAmount();
        }
        return total;
    }

    public int tankCount() {
        return this.tanks.length;
    }

    /** The non-empty tanks, for the window's readout. */
    public java.util.List<FluidStack> contents() {
        java.util.List<FluidStack> out = new java.util.ArrayList<>();
        for (FluidTank tank : this.tanks) {
            if (!tank.isEmpty()) {
                out.add(tank.getFluid());
            }
        }
        return out;
    }

    // ---- the mode decides where a fluid may go ----

    /**
     * The tank this fluid is allowed to enter, or {@code null} when it must be turned away.
     *
     * <p>In multi-cell mode that is simply the first tank with room. In single-cell mode it is the
     * tank already holding this fluid, or else the first empty one — never a tank belonging to
     * something else, which is the whole protection the mode offers.</p>
     */
    @Nullable
    private FluidTank admit(FluidStack resource) {
        // A tank already holding this fluid always accepts more, in either mode.
        for (int index = 0; index < this.tanks.length; index++) {
            FluidTank tank = this.tanks[index];
            if (inQuota(index) && !tank.isEmpty() && tank.getFluid().is(resource.getFluid())) {
                return tank;
            }
        }
        if (!this.singleCell.getAsBoolean()) {
            for (int index = 0; index < this.tanks.length; index++) {
                FluidTank tank = this.tanks[index];
                if (inQuota(index) && tank.getFluidAmount() < tank.getCapacity()) {
                    return tank;
                }
            }
            return null;
        }
        // Single-cell: only a tank with nothing in it may start holding something new.
        for (int index = 0; index < this.tanks.length; index++) {
            FluidTank tank = this.tanks[index];
            if (inQuota(index) && tank.isEmpty()) {
                return tank;
            }
        }
        return null;
    }

    @Override
    public int getTanks() {
        return this.tanks.length;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return this.tanks[tank].getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.tanks[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.tanks[tank].isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, @NotNull FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        // Clipped first, so the shared-budget cap applies to the request rather than to each tank:
        // a kind borrowing another's share must not pass the part's total.
        int allowed = this.room.applyAsInt(resource.getAmount());
        if (allowed <= 0) {
            return 0;
        }
        FluidStack request = allowed >= resource.getAmount()
                ? resource : resource.copyWithAmount(allowed);
        if (!this.singleCell.getAsBoolean()) {
            // Multi-cell: take whatever room exists, tank after tank — but only inside the quota.
            int accepted = 0;
            FluidStack remaining = request.copy();
            for (int index = 0; index < this.tanks.length && inQuota(index); index++) {
                if (remaining.isEmpty()) {
                    break;
                }
                int moved = this.tanks[index].fill(remaining, action);
                accepted += moved;
                remaining = remaining.copyWithAmount(remaining.getAmount() - moved);
            }
            return accepted;
        }
        FluidTank tank = admit(request);
        if (tank == null) {
            return 0;
        }
        return tank.fill(request, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, @NotNull FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        for (FluidTank tank : this.tanks) {
            if (!tank.isEmpty() && tank.getFluid().is(resource.getFluid())) {
                return tank.drain(resource, action);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
        for (FluidTank tank : this.tanks) {
            if (!tank.isEmpty()) {
                return tank.drain(maxDrain, action);
            }
        }
        return FluidStack.EMPTY;
    }

    // ---- persistence ----

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (FluidTank tank : this.tanks) {
            if (tank.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.put(TAG_FLUID, tank.getFluid().save(registries));
            entry.putInt(TAG_AMOUNT, tank.getFluidAmount());
            list.add(entry);
        }
        tag.put(TAG_TANKS, list);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        for (FluidTank tank : this.tanks) {
            tank.setFluid(FluidStack.EMPTY);
        }
        ListTag list = tag.getList(TAG_TANKS, Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size() && index < this.tanks.length; index++) {
            CompoundTag entry = list.getCompound(index);
            FluidStack stack = FluidStack.parseOptional(registries, entry.getCompound(TAG_FLUID));
            if (stack.isEmpty()) {
                continue;
            }
            // Clamped rather than trusted: the tier can shrink between sessions, and a stale amount
            // larger than the new tank would show more than it can hold.
            stack.setAmount(Math.min(stack.getAmount(), this.tanks[index].getCapacity()));
            this.tanks[index].setFluid(stack);
        }
    }
}
