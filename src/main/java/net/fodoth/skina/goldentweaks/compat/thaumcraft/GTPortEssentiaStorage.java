package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import net.minecraft.core.Direction;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * The tanks behind an essentia port, with the stacking mode enforced on the way in.
 *
 * <p>Essentia has no stack type of its own: Thaumcraft addresses it as {@code Aspect} plus a count,
 * and a container exposes an {@link AspectList}. This class keeps one {@code Aspect} per tank so
 * the stacking mode can be applied the same way it is for fluids and chemicals — one tank holds one
 * aspect, and the mode decides whether a single aspect may take the whole port.</p>
 *
 * <p>Kept in the Thaumcraft compat package: naming {@code Aspect} from a class the port entity
 * loads unconditionally would break the game without Thaumcraft installed.</p>
 */
public class GTPortEssentiaStorage implements IAspectContainer, IEssentiaTransport {

    private static final String TAG_TANKS = "Tanks";
    private static final String TAG_ASPECT = "Aspect";
    private static final String TAG_AMOUNT = "Amount";

    /**
     * The suction this port advertises while active.
     *
     * <p>Twice a Thaumcraft reservoir's 24. A port is one face of a large machine rather than a
     * standalone jar, so it has to out-pull the tubes around it or the essentia never arrives; the
     * doubling is what gives it that priority.</p>
     */
    private static final int RESERVOIR_SUCTION = 48;

    private final Aspect[] aspects;
    private final int[] amounts;
    private final int tankCapacity;
    private final BooleanSupplier singleCell;
    /** Whether the port is in active mode; only then does it advertise suction. */
    private final BooleanSupplier active;
    /** Whether this port outputs; an output advertises the negative of the same figure. */
    private final BooleanSupplier output;
    /** How many tanks are in use. Read live, so a switch never has to rebuild this store. */
    private java.util.function.IntSupplier quota = () -> Integer.MAX_VALUE;
    /** How much of a request may actually be taken. See the fluid store's {@code room}. */
    private java.util.function.IntUnaryOperator room = wanted -> wanted;
    private Runnable changedHook = () -> {
    };

    public GTPortEssentiaStorage(int tankCount, int tankCapacity, BooleanSupplier singleCell,
                                 BooleanSupplier active, BooleanSupplier output) {
        int count = Math.max(1, tankCount);
        this.aspects = new Aspect[count];
        this.amounts = new int[count];
        this.tankCapacity = tankCapacity;
        this.singleCell = singleCell;
        this.active = active;
        this.output = output;
    }

    /**
     * Restricts how many tanks this store may use.
     *
     * <p>Asked on every write rather than taken at construction, so a player's switch applies
     * without the store object being replaced — which is what keeps outside references valid.</p>
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

    public int tankCount() {
        return this.aspects.length;
    }

    public int totalAmount() {
        int total = 0;
        for (int amount : this.amounts) {
            total += amount;
        }
        return total;
    }

    /** The non-empty tanks, for the window's readout. */
    public List<AspectList> contents() {
        List<AspectList> out = new ArrayList<>();
        for (int index = 0; index < this.aspects.length; index++) {
            if (this.aspects[index] != null && this.amounts[index] > 0) {
                AspectList single = new AspectList();
                single.add(this.aspects[index], this.amounts[index]);
                out.add(single);
            }
        }
        return out;
    }

    /**
     * This tank's contents, as rows the port window can list.
     *
     * <p>Essentia has no registry entry, so each row carries the aspect's tag and the stack type
     * this mod uses for it.</p>
     */
    public List<GTReactionStack> windowContents() {
        List<GTReactionStack> rows = new ArrayList<>();
        for (int index = 0; index < this.aspects.length; index++) {
            if (this.aspects[index] == null || this.amounts[index] <= 0) {
                continue;
            }
            rows.add(GTReactionStack.essentia(this.aspects[index].getTag(), this.amounts[index]));
        }
        return rows;
    }

    /** The tank already holding this aspect, or {@code null}. */
    @Nullable
    private Integer tankOf(Aspect aspect) {
        for (int index = 0; index < this.aspects.length; index++) {
            if (this.aspects[index] == aspect) {
                return index;
            }
        }
        return null;
    }

    /**
     * The aspect a game id names, or {@code null} when Thaumcraft has no such aspect.
     *
     * <p>Aspects are not registry entries: they are a static table keyed by tag, so an id like
     * {@code thaumcraft:aer} is resolved by its path.</p>
     */
    @Nullable
    private static Aspect aspectOf(ResourceLocation id) {
        return Aspect.getAspect(id.getPath());
    }

    /** How much of an aspect this port holds. */
    public long amountOf(ResourceLocation id) {
        Aspect aspect = aspectOf(id);
        Integer tank = aspect == null ? null : tankOf(aspect);
        return tank == null ? 0L : this.amounts[tank];
    }

    /**
     * The aspects currently held, as game ids.
     *
     * <p>The inverse of {@link #aspectOf}: {@code Aspect} has no registry entry, so the id is rebuilt
     * from the aspect's tag in the {@code thaumcraft} namespace. Used by an active port walking its
     * own contents when pushing to a neighbour.</p>
     */
    public List<ResourceLocation> aspectIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        for (int index = 0; index < this.aspects.length; index++) {
            if (this.aspects[index] != null && this.amounts[index] > 0) {
                ids.add(ResourceLocation.fromNamespaceAndPath("thaumcraft", this.aspects[index].getTag()));
            }
        }
        return ids;
    }

    /**
     * Removes up to {@code amount} of an aspect.
     *
     * @return how much was actually taken
     */
    public long takeById(ResourceLocation id, long amount) {
        Aspect aspect = aspectOf(id);
        if (aspect == null || amount <= 0) {
            return 0L;
        }
        int wanted = (int) Math.min(amount, Integer.MAX_VALUE);
        return take(aspect, wanted);
    }

    /**
     * Adds up to {@code amount} of an aspect.
     *
     * @return how much was actually accepted
     */
    public long addById(ResourceLocation id, long amount) {
        Aspect aspect = aspectOf(id);
        if (aspect == null || amount <= 0) {
            return 0L;
        }
        int offered = (int) Math.min(amount, Integer.MAX_VALUE);
        return add(aspect, offered);
    }

    /**
     * How much of an aspect this port would still accept, capped at {@code wanted}.
     *
     * <p>Walked over the tanks the same way {@link #add} walks them, so a forecast and the real
     * insertion cannot disagree — a hatch that reports room must actually take the product.</p>
     */
    public long roomFor(ResourceLocation id, long wanted) {
        Aspect aspect = aspectOf(id);
        if (aspect == null || wanted <= 0) {
            return 0L;
        }
        long room = 0L;
        boolean started = false;
        for (int index = 0; index < this.aspects.length && room < wanted && inQuota(index); index++) {
            boolean same = this.aspects[index] == aspect;
            boolean free = this.aspects[index] == null || this.amounts[index] <= 0;
            // The first tank may hold this aspect or be empty; after that only that same tank counts,
            // because the stacking mode decides whether a second one may be opened at all.
            if (same) {
                room += this.tankCapacity - this.amounts[index];
                started = true;
            } else if (!started && free && !this.singleCell.getAsBoolean()) {
                room += this.tankCapacity;
                started = true;
            }
        }
        return Math.min(room, wanted);
    }

    /**
     * The tank this aspect may enter, or {@code null} when it must be turned away.
     *
     * <p>Same rule as the fluid hatch: the tank already holding this aspect always takes more; in
     * single-cell mode only an empty tank may start holding something new.</p>
     */
    @Nullable
    private Integer admit(Aspect aspect) {
        Integer existing = tankOf(aspect);
        if (existing != null) {
            return existing;
        }
        if (!this.singleCell.getAsBoolean()) {
            for (int index = 0; index < this.aspects.length; index++) {
                if (inQuota(index) && this.amounts[index] < this.tankCapacity) {
                    return index;
                }
            }
            return null;
        }
        for (int index = 0; index < this.aspects.length; index++) {
            if (inQuota(index) && (this.aspects[index] == null || this.amounts[index] <= 0)) {
                return index;
            }
        }
        return null;
    }

    /**
     * Accepts up to {@code amount} of an aspect, returning how much was taken.
     *
     * <p>The whole-port entry point: the stacking mode is applied here, so a pipe asking for room
     * gets the same answer the machine's own delivery path does.</p>
     */
    public int add(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return 0;
        }
        // Clipped first, so a kind borrowing another's share cannot pass the part's total.
        int allowed = this.room.applyAsInt(amount);
        if (allowed <= 0) {
            return 0;
        }
        if (!this.singleCell.getAsBoolean()) {
            int accepted = 0;
            int remaining = allowed;
            for (int index = 0; index < this.aspects.length && remaining > 0 && inQuota(index); index++) {
                int moved = addTo(index, aspect, remaining);
                accepted += moved;
                remaining -= moved;
            }
            return accepted;
        }
        Integer tank = admit(aspect);
        return tank == null ? 0 : addTo(tank, aspect, allowed);
    }

    private int addTo(int tank, Aspect aspect, int amount) {
        if (this.aspects[tank] != aspect) {
            if (this.aspects[tank] != null && this.amounts[tank] > 0) {
                return 0;
            }
            this.aspects[tank] = aspect;
            this.amounts[tank] = 0;
        }
        int room = this.tankCapacity - this.amounts[tank];
        int moved = Math.min(room, amount);
        if (moved > 0) {
            this.amounts[tank] += moved;
            this.changedHook.run();
        }
        return moved;
    }

    /** Removes up to {@code amount} of an aspect, returning how much was actually taken. */
    public int take(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return 0;
        }
        int taken = 0;
        int remaining = amount;
        for (int index = 0; index < this.aspects.length && remaining > 0; index++) {
            if (this.aspects[index] != aspect) {
                continue;
            }
            int moved = Math.min(this.amounts[index], remaining);
            this.amounts[index] -= moved;
            taken += moved;
            remaining -= moved;
            if (this.amounts[index] <= 0) {
                this.aspects[index] = null;
            }
        }
        if (taken > 0) {
            this.changedHook.run();
        }
        return taken;
    }

    // ---- IAspectContainer: what the pipes see ----

    @Override
    public AspectList getAspects() {
        AspectList list = new AspectList();
        for (int index = 0; index < this.aspects.length; index++) {
            if (this.aspects[index] != null && this.amounts[index] > 0) {
                list.add(this.aspects[index], this.amounts[index]);
            }
        }
        return list;
    }

    @Override
    public void setAspects(AspectList list) {
        for (int index = 0; index < this.aspects.length; index++) {
            this.aspects[index] = null;
            this.amounts[index] = 0;
        }
        if (list != null) {
            for (Aspect aspect : list.getAspects()) {
                if (aspect != null) {
                    add(aspect, list.getAmount(aspect));
                }
            }
        }
        this.changedHook.run();
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return admit(aspect) != null;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return amount - add(aspect, amount);
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return take(aspect, amount) >= amount;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return containerContains(aspect) >= amount;
    }

    @Override
    public int containerContains(Aspect aspect) {
        int total = 0;
        for (int index = 0; index < this.aspects.length; index++) {
            if (this.aspects[index] == aspect) {
                total += this.amounts[index];
            }
        }
        return total;
    }

    // ---- IEssentiaTransport: what the tubes reach ----

    @Override
    public boolean isConnectable(Direction side) {
        return true;
    }

    /**
     * Whether essentia may enter from this side.
     *
     * <p>Both directions are open. A port's own input/output switch already decides which half of
     * the machine it belongs to; refusing here as well would make a correctly oriented tank
     * silently refuse its own supply.</p>
     */
    @Override
    public boolean canInputFrom(Direction side) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return true;
    }

    /** Suction is computed from the mode, so an external write has nothing to set. */
    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    /**
     * The aspect this port asks for, or {@code null} when it is not asking.
     *
     * <p>An input names the aspect it already holds so the tube knows what to bring; a passive port
     * and an output both answer {@code null}, because neither is a sink.</p>
     */
    @Override
    public Aspect getSuctionType(Direction side) {
        if (this.output.getAsBoolean() || suctionAmount() == 0) {
            return null;
        }
        return getEssentiaType(side);
    }

    /**
     * How hard this port pulls, in the units a tube compares.
     *
     * <p>Zero while passive: the port is a buffer the machine feeds, and advertising suction would
     * make it fight the pipes for essentia it does not want. Once active it matches a reservoir —
     * except in output mode, where the sign flips because the port is a source, not a sink.</p>
     */
    @Override
    public int getSuctionAmount(Direction side) {
        return suctionAmount();
    }

    private int suctionAmount() {
        if (!this.active.getAsBoolean()) {
            return 0;
        }
        // A full input has no room, so it stops asking — the same rule the reservoir uses.
        if (!this.output.getAsBoolean() && totalAmount() >= totalCapacity()) {
            return 0;
        }
        return this.output.getAsBoolean() ? -RESERVOIR_SUCTION : RESERVOIR_SUCTION;
    }

    /** Room across every tank. */
    private int totalCapacity() {
        return this.aspects.length * this.tankCapacity;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction side) {
        return take(aspect, amount);
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction side) {
        return add(aspect, amount);
    }

    /**
     * 探测版写入：存储总线与样板供应器先用它问「能收多少」。
     *
     * <p><b>这个覆写是必需的。</b>{@code IEssentiaTransport} 给的默认实现是
     * {@code return 0}（就两条字节码：{@code iconst_0 / ireturn}），而
     * {@code EssentiaContainerStorage.insert} 在 {@code SIMULATE} 阶段只信这个返回值：
     * 拿到 0 就判定「目标收不了」，于是根本不发起真正的写入。</p>
     *
     * <p>表现就是：ME 存储总线贴着端口时，要素停在网络里或样板供应器里不动；
     * 而在网络里塞一个能存要素的元件，要素改从网络内部消化，看起来就"正常"了 ——
     * 那只是绕开了这条判定，不是修好了它。</p>
     */
    @Override
    public int simulateAddEssentia(Aspect aspect, int amount, Direction side) {
        if (aspect == null || amount <= 0) {
            return 0;
        }
        Integer tank = admit(aspect);
        if (tank == null) {
            return 0;
        }
        int room = Math.max(0, Math.min(amount, this.tankCapacity - this.amounts[tank]));
        if (room > 0 && !this.simulateLogged) {
            this.simulateLogged = true;
            net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                    "[GT] simulateAddEssentia({}) accepted probe: room {} of {}",
                    aspect.getTag(), room, amount);
        }
        return room;
    }

    /**
     * 探测版抽取，与 {@link #simulateAddEssentia} 对称。
     *
     * <p>默认实现同样是 {@code return 0}，不覆写的话任何经 {@code IEssentiaTransport}
     * 从端口抽要素的装置（存储总线、导出总线）都会认为端口是空的。</p>
     */
    @Override
    public int simulateTakeEssentia(Aspect aspect, int amount, Direction side) {
        if (aspect == null || amount <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(amount, containerContains(aspect)));
    }

    /** 只打印一次，避免每 tick 刷屏。 */
    private boolean simulateLogged;

    /** The first aspect this port holds, which is what a tube asks before deciding to pull. */
    @Override
    public Aspect getEssentiaType(Direction side) {
        for (Aspect aspect : this.aspects) {
            if (aspect != null) {
                return aspect;
            }
        }
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction side) {
        return totalAmount();
    }

    /**
     * The floor a tube must beat to connect.
     *
     * <p>Reported only while active, for the same reason as the face figure: a passive port is not
     * part of the suction competition at all.</p>
     */
    @Override
    public int getMinimumSuction() {
        if (!this.active.getAsBoolean()) {
            return 0;
        }
        return this.output.getAsBoolean() ? -RESERVOIR_SUCTION : RESERVOIR_SUCTION;
    }

    // ---- persistence ----

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (int index = 0; index < this.aspects.length; index++) {
            if (this.aspects[index] == null || this.amounts[index] <= 0) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            // The aspect's own tag is its identity; there is no registry id behind it.
            entry.putString(TAG_ASPECT, this.aspects[index].getTag());
            entry.putInt(TAG_AMOUNT, this.amounts[index]);
            list.add(entry);
        }
        tag.put(TAG_TANKS, list);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        for (int index = 0; index < this.aspects.length; index++) {
            this.aspects[index] = null;
            this.amounts[index] = 0;
        }
        ListTag list = tag.getList(TAG_TANKS, Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size() && index < this.aspects.length; index++) {
            CompoundTag entry = list.getCompound(index);
            Aspect aspect = Aspect.getAspect(entry.getString(TAG_ASPECT));
            if (aspect == null) {
                continue;
            }
            this.aspects[index] = aspect;
            // Clamped rather than trusted, the same way the fluid hatch clamps a shrinking tank.
            this.amounts[index] = Math.min(entry.getInt(TAG_AMOUNT), this.tankCapacity);
        }
    }
}
