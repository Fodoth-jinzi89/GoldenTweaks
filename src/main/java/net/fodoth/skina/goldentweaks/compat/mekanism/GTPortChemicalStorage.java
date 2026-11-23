package net.fodoth.skina.goldentweaks.compat.mekanism;

import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * The tanks behind a chemical hopper, with the stacking mode enforced on the way in.
 *
 * <p>The mirror of {@code GTPortFluidStorage}: the two modes answer the same question — may a
 * single chemical take the whole hopper? — and the reasoning is written up there. It is a separate
 * class rather than a shared one because the two capabilities share nothing but that shape: a
 * {@code ChemicalStack} holds a {@code long} amount and is addressed by a registry holder, while a
 * {@code FluidStack} holds an {@code int} and is addressed by a {@code Fluid}.</p>
 *
 * <p>Kept out of the shared package on purpose: naming {@code ChemicalStack} in a class the port
 * entity loads unconditionally would make "AMI without Mekanism" fail at class load.</p>
 */
public class GTPortChemicalStorage implements IChemicalHandler {

    private static final String TAG_TANKS = "Tanks";
    private static final String TAG_CHEMICAL = "Chemical";
    private static final String TAG_AMOUNT = "Amount";

    private final ChemicalStack[] tanks;
    private final long tankCapacity;
    /** Read live from the port, so a toggle applies to the very next insertion. */
    private final BooleanSupplier singleCell;
    private Runnable changedHook = () -> {
    };

    public GTPortChemicalStorage(int tankCount, long tankCapacity, BooleanSupplier singleCell) {
        this.tanks = new ChemicalStack[Math.max(1, tankCount)];
        for (int index = 0; index < this.tanks.length; index++) {
            this.tanks[index] = ChemicalStack.EMPTY;
        }
        this.tankCapacity = tankCapacity;
        this.singleCell = singleCell;
    }

    public void setChangedHook(Runnable hook) {
        this.changedHook = hook;
    }

    public long totalAmount() {
        long total = 0L;
        for (ChemicalStack tank : this.tanks) {
            total += tank.getAmount();
        }
        return total;
    }

    public int tankCount() {
        return this.tanks.length;
    }

    /** The non-empty tanks, for the window's readout. */
    public List<ChemicalStack> contents() {
        List<ChemicalStack> out = new ArrayList<>();
        for (ChemicalStack tank : this.tanks) {
            if (!tank.isEmpty()) {
                out.add(tank);
            }
        }
        return out;
    }

    /**
     * This hopper's contents, as rows the port window can list.
     *
     * <p>Built here rather than by the window: a row needs a chemical's registry id, and that
     * conversion belongs with the class that already speaks Mekanism's types.</p>
     */
    public List<GTReactionStack> windowContents() {
        List<GTReactionStack> rows = new ArrayList<>();
        for (ChemicalStack tank : this.tanks) {
            if (tank.isEmpty()) {
                continue;
            }
            ResourceLocation id = tank.getChemicalHolder().unwrapKey()
                    .map(key -> key.location()).orElse(null);
            if (id == null) {
                continue;
            }
            rows.add(new GTReactionStack(GTReactionStack.Kind.CHEMICAL, id, tank.getAmount(),
                    Optional.empty()));
        }
        return rows;
    }

    /**
     * The tank this chemical is allowed to enter, or {@code null} when it must be turned away.
     *
     * <p>Same rule as the fluid hatch: a tank already holding this chemical always takes more; in
     * single-cell mode nothing else will, so a second chemical cannot be squeezed out by the first
     * filling the whole hopper.</p>
     */
    @Nullable
    private Integer admit(ChemicalStack resource) {
        for (int index = 0; index < this.tanks.length; index++) {
            ChemicalStack tank = this.tanks[index];
            if (!tank.isEmpty() && ChemicalStack.isSameChemical(tank, resource)) {
                return index;
            }
        }
        if (!this.singleCell.getAsBoolean()) {
            for (int index = 0; index < this.tanks.length; index++) {
                if (this.tanks[index].getAmount() < this.tankCapacity) {
                    return index;
                }
            }
            return null;
        }
        for (int index = 0; index < this.tanks.length; index++) {
            if (this.tanks[index].isEmpty()) {
                return index;
            }
        }
        return null;
    }

    @Override
    public int getChemicalTanks() {
        return this.tanks.length;
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        return this.tanks[tank];
    }

    @Override
    public void setChemicalInTank(int tank, ChemicalStack stack) {
        this.tanks[tank] = stack;
        this.changedHook.run();
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        return this.tankCapacity;
    }

    @Override
    public boolean isValid(int tank, ChemicalStack stack) {
        return !stack.isEmpty();
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        if (stack.isEmpty()) {
            return ChemicalStack.EMPTY;
        }
        // A different chemical in this tank means the insertion is refused outright, rather than
        // silently replacing what is there.
        ChemicalStack existing = this.tanks[tank];
        if (!existing.isEmpty() && !ChemicalStack.isSameChemical(existing, stack)) {
            return stack;
        }
        long room = this.tankCapacity - existing.getAmount();
        if (room <= 0) {
            return stack;
        }
        long moved = Math.min(room, stack.getAmount());
        long left = stack.getAmount() - moved;
        if (action.execute()) {
            this.tanks[tank] = stack.copyWithAmount(existing.getAmount() + moved);
            this.changedHook.run();
        }
        return left <= 0 ? ChemicalStack.EMPTY : stack.copyWithAmount(left);
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        ChemicalStack existing = this.tanks[tank];
        if (existing.isEmpty() || amount <= 0) {
            return ChemicalStack.EMPTY;
        }
        long taken = Math.min(amount, existing.getAmount());
        ChemicalStack out = existing.copyWithAmount(taken);
        if (action.execute()) {
            long left = existing.getAmount() - taken;
            this.tanks[tank] = left <= 0 ? ChemicalStack.EMPTY : existing.copyWithAmount(left);
            this.changedHook.run();
        }
        return out;
    }

    /**
     * How much of a chemical this port holds, by game id.
     *
     * <p>Machines address a chemical the same way a recipe file does, so the id is what they pass;
     * this turns it back into the stack the tanks hold.</p>
     */
    public long amountOf(ResourceLocation id) {
        ChemicalStack wanted = byId(id);
        if (wanted.isEmpty()) {
            return 0L;
        }
        long total = 0L;
        for (ChemicalStack tank : this.tanks) {
            if (!tank.isEmpty() && ChemicalStack.isSameChemical(tank, wanted)) {
                total += tank.getAmount();
            }
        }
        return total;
    }

    /**
     * The chemicals currently held, as game ids.
     *
     * <p>An active hopper walks this list to offer its contents to a neighbour, the same way the
     * fluid hatch walks its tanks.</p>
     */
    public List<ResourceLocation> chemicalIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        for (ChemicalStack tank : this.tanks) {
            if (tank.isEmpty()) {
                continue;
            }
            ResourceLocation id = tank.getChemicalHolder().unwrapKey()
                    .map(key -> key.location()).orElse(null);
            if (id != null && !ids.contains(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    /**
     * Removes up to {@code amount} of a chemical, by game id.
     *
     * @return how much was actually taken
     */
    public long extractById(ResourceLocation id, long amount) {
        ChemicalStack wanted = byId(id);
        if (wanted.isEmpty() || amount <= 0) {
            return 0L;
        }
        long taken = 0L;
        long remaining = amount;
        for (int index = 0; index < this.tanks.length && remaining > 0; index++) {
            if (this.tanks[index].isEmpty()
                    || !ChemicalStack.isSameChemical(this.tanks[index], wanted)) {
                continue;
            }
            ChemicalStack out = extractChemical(index, remaining, Action.EXECUTE);
            taken += out.getAmount();
            remaining -= out.getAmount();
        }
        return taken;
    }

    /**
     * Adds up to {@code amount} of a chemical, by game id.
     *
     * @return how much was actually accepted
     */
    public long insertById(ResourceLocation id, long amount) {
        ChemicalStack wanted = byId(id);
        if (wanted.isEmpty() || amount <= 0) {
            return 0L;
        }
        ChemicalStack remaining = insertChemical(wanted.copyWithAmount(amount), Action.EXECUTE);
        return amount - remaining.getAmount();
    }

    /**
     * How much of a chemical this port would still accept, capped at {@code wanted}.
     *
     * <p>Follows the same tank order {@link #insertChemical(ChemicalStack, Action)} follows, so the
     * forecast cannot promise room the insertion would then refuse.</p>
     */
    public long roomFor(ResourceLocation id, long wanted) {
        ChemicalStack wantedStack = byId(id);
        if (wantedStack.isEmpty() || wanted <= 0) {
            return 0L;
        }
        long room = 0L;
        boolean started = false;
        for (int index = 0; index < this.tanks.length && room < wanted; index++) {
            ChemicalStack tank = this.tanks[index];
            boolean same = !tank.isEmpty() && ChemicalStack.isSameChemical(tank, wantedStack);
            boolean free = tank.isEmpty();
            if (same) {
                room += this.tankCapacity - tank.getAmount();
                started = true;
            } else if (!started && free && !this.singleCell.getAsBoolean()) {
                room += this.tankCapacity;
                started = true;
            }
        }
        return Math.min(room, wanted);
    }

    /** A one-unit stack of the chemical an id names, or {@code EMPTY} when it names none. */
    private static ChemicalStack byId(ResourceLocation id) {
        // Chemicals live in Mekanism's own registry rather than a vanilla one, so the id is resolved
        // there. An empty holder means this world has no such chemical.
        return MekanismAPI.CHEMICAL_REGISTRY.getHolder(id)
                .map(holder -> new ChemicalStack(holder, 1L))
                .orElse(ChemicalStack.EMPTY);
    }

    /**
     * The handler's whole-hopper insert, which is where the stacking mode is applied.
     *
     * <p>Mekanism's default would walk the tanks in order regardless of the mode; this one asks
     * {@link #admit} first so single-cell behaviour holds for pipes as well as for the machine.</p>
     */
    @Override
    public ChemicalStack insertChemical(ChemicalStack stack, Action action) {
        if (stack.isEmpty()) {
            return ChemicalStack.EMPTY;
        }
        if (!this.singleCell.getAsBoolean()) {
            ChemicalStack remaining = stack.copy();
            for (int index = 0; index < this.tanks.length && !remaining.isEmpty(); index++) {
                remaining = insertChemical(index, remaining, action);
            }
            return remaining;
        }
        Integer tank = admit(stack);
        if (tank == null) {
            return stack;
        }
        return insertChemical(tank, stack, action);
    }

    // ---- persistence ----

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ChemicalStack tank : this.tanks) {
            if (tank.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.put(TAG_CHEMICAL, tank.save(registries));
            entry.putLong(TAG_AMOUNT, tank.getAmount());
            list.add(entry);
        }
        tag.put(TAG_TANKS, list);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        for (int index = 0; index < this.tanks.length; index++) {
            this.tanks[index] = ChemicalStack.EMPTY;
        }
        ListTag list = tag.getList(TAG_TANKS, Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size() && index < this.tanks.length; index++) {
            CompoundTag entry = list.getCompound(index);
            ChemicalStack stack = ChemicalStack.parseOptional(registries, entry.getCompound(TAG_CHEMICAL));
            if (stack.isEmpty()) {
                continue;
            }
            // Clamped rather than trusted, the same way the fluid hatch clamps a shrinking tank.
            long amount = Math.min(entry.getLong(TAG_AMOUNT), this.tankCapacity);
            this.tanks[index] = stack.copyWithAmount(amount);
        }
    }
}
