package net.fodoth.skina.goldentweaks.content.multiblock;

import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.fodoth.skina.goldentweaks.compat.mekanism.GTPortChemicalStorage;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTPortEssentiaStorage;
import net.fodoth.skina.goldentweaks.compat.thaumicenergistics.GTInterfaceEssentia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.EssentiaCapabilities;
import thaumcraft.api.aspects.IEssentiaTransport;

import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Creates the storage for the two kinds that depend on optional mods.
 *
 * <p>The port entity keeps either storage as an {@code Object} and reaches the mod's types through
 * here, so a world without Mekanism or Thaumcraft never has to resolve their classes.</p>
 *
 * <p>Everything that names a mod type sits inside a nested class. A class is resolved the first
 * time a method that touches it runs, and those methods only run after the mod check above them has
 * passed 鈥?which is what keeps an unrelated world from failing to load this one.</p>
 */
public final class GTPortStorages {

    private static final String MEKANISM = "mekanism";
    private static final String THAUMCRAFT = "thaumcraft";
    private static final String THAUMIC_ENERGISTICS = "thaumicenergistics";

    private GTPortStorages() {
    }

    /** The chemical handler for a hopper, or {@code null} without Mekanism. */
    @Nullable
    public static Object createChemical(int tanks, long tankCapacity, BooleanSupplier singleCell,
                                        Runnable changedHook) {
        if (!ModList.get().isLoaded(MEKANISM)) {
            return null;
        }
        return MekanismBridge.create(tanks, tankCapacity, singleCell, changedHook);
    }

    /**
     * The essentia container for a tank, or {@code null} without Thaumcraft.
     *
     * <p>The two extra suppliers are the port's reach-through switch and its direction: suction is
     * only advertised while active, and inverted for an output. Passed as suppliers rather than
     * values so a toggle applies on the very next tick.</p>
     */
    @Nullable
    public static Object createEssentia(int tanks, int tankCapacity, BooleanSupplier singleCell,
                                        BooleanSupplier active, BooleanSupplier output,
                                        Runnable changedHook) {
        if (!ModList.get().isLoaded(THAUMCRAFT)) {
            return null;
        }
        return ThaumcraftBridge.create(tanks, tankCapacity, singleCell, active, output, changedHook);
    }

    // ---- the four operations the entity needs, each forwarded to a bridge ----

    public static CompoundTag saveChemical(@Nullable Object storage, HolderLookup.Provider registries) {
        return storage == null ? new CompoundTag() : MekanismBridge.save(storage, registries);
    }

    public static void loadChemical(@Nullable Object storage, CompoundTag tag,
                                    HolderLookup.Provider registries) {
        if (storage != null) {
            MekanismBridge.load(storage, tag, registries);
        }
    }

    public static List<GTReactionStack> chemicalContents(@Nullable Object storage) {
        return storage == null ? List.of() : MekanismBridge.contents(storage);
    }

    public static CompoundTag saveEssentia(@Nullable Object storage, HolderLookup.Provider registries) {
        return storage == null ? new CompoundTag() : ThaumcraftBridge.save(storage, registries);
    }

    public static void loadEssentia(@Nullable Object storage, CompoundTag tag,
                                    HolderLookup.Provider registries) {
        if (storage != null) {
            ThaumcraftBridge.load(storage, tag, registries);
        }
    }

    public static List<GTReactionStack> essentiaContents(@Nullable Object storage) {
        return storage == null ? List.of() : ThaumcraftBridge.contents(storage);
    }

    // ---- moving amounts in and out by kind, for the machines that consume and produce them ----

    /** How much of an aspect a port holds, or 0 without Thaumcraft. */
    public static long essentiaAmount(@Nullable Object storage, ResourceLocation aspect) {
        return storage == null ? 0L : ThaumcraftBridge.amount(storage, aspect);
    }

    /**
     * Removes up to {@code amount} of an aspect.
     *
     * @return how much was actually taken
     */
    public static long takeEssentia(@Nullable Object storage, ResourceLocation aspect, long amount) {
        return storage == null ? 0L : ThaumcraftBridge.take(storage, aspect, amount);
    }

    /**
     * Adds up to {@code amount} of an aspect.
     *
     * @return how much was actually accepted
     */
    public static long addEssentia(@Nullable Object storage, ResourceLocation aspect, long amount) {
        return storage == null ? 0L : ThaumcraftBridge.add(storage, aspect, amount);
    }

    /**
     * How much of an aspect this port could still accept.
     *
     * <p>Asked before a craft starts, so a full tank stalls the machine instead of consuming a recipe
     * whose product would then be destroyed.</p>
     */
    public static long essentiaRoom(@Nullable Object storage, ResourceLocation aspect, long wanted) {
        return storage == null ? 0L : ThaumcraftBridge.room(storage, aspect, wanted);
    }

    /**
     * The aspects a port currently holds, so an active port can walk them one by one.
     *
     * <p>Essentia is addressed per aspect rather than by slot, so the reach-through needs the list
     * of what is inside before it can offer any of it to a neighbour.</p>
     */
    public static List<ResourceLocation> essentiaAspects(@Nullable Object storage) {
        return storage == null ? List.of() : ThaumcraftBridge.aspects(storage);
    }

    /**
     * Moves essentia between this port and the block in front of it.
     *
     * <p>Kept here rather than in the port entity so the Thaumcraft types stay behind
     * {@link ThaumcraftBridge} and a world without the mod never links them. One aspect at a time,
     * up to {@code budget}, in the direction {@code input} says.</p>
     *
     * @return how much was actually moved
     */
    public static long reachEssentia(@Nullable Object ours, ResourceLocation aspect, int amount,
                                     Level level, BlockPos target,
                                     Direction side, boolean input) {
        return ThaumcraftBridge.reach(ours, aspect, amount, level, target, side, input);
    }

    /** The aspects the block in front of a port holds, so an input port knows what to ask for. */
    public static List<ResourceLocation> neighbourEssentiaAspects(Level level,
                                                                  BlockPos target,
                                                                  Direction side) {
        return ThaumcraftBridge.neighbourAspects(level, target, side);
    }

    /** How much of a chemical a port holds, or 0 without Mekanism. */
    public static long chemicalAmount(@Nullable Object storage, ResourceLocation chemical) {
        return storage == null ? 0L : MekanismBridge.amount(storage, chemical);
    }

    /**
     * Removes up to {@code amount} of a chemical.
     *
     * @return how much was actually taken
     */
    public static long takeChemical(@Nullable Object storage, ResourceLocation chemical, long amount) {
        return storage == null ? 0L : MekanismBridge.take(storage, chemical, amount);
    }

    /**
     * Adds up to {@code amount} of a chemical.
     *
     * @return how much was actually accepted
     */
    public static long addChemical(@Nullable Object storage, ResourceLocation chemical, long amount) {
        return storage == null ? 0L : MekanismBridge.add(storage, chemical, amount);
    }

    /** How much of a chemical this port could still accept, of at most {@code wanted}. */
    public static long chemicalRoom(@Nullable Object storage, ResourceLocation chemical, long wanted) {
        return storage == null ? 0L : MekanismBridge.room(storage, chemical, wanted);
    }

    /** The chemicals a port currently holds, so an active hopper can offer them one by one. */
    public static List<ResourceLocation> chemicalIds(@Nullable Object storage) {
        return storage == null ? List.of() : MekanismBridge.ids(storage);
    }

    /**
     * Moves up to {@code amount} of one chemical between this port and the block in front of it.
     *
     * <p>Kept here for the same reason as the essentia counterpart: Mekanism's types stay behind
     * {@link MekanismBridge} so a world without the mod never links them.</p>
     *
     * @return how much was actually moved
     */
    public static long reachChemical(@Nullable Object ours, ResourceLocation chemical, long amount,
                                     Level level, BlockPos target, Direction side, boolean input) {
        return MekanismBridge.reach(ours, chemical, amount, level, target, side, input);
    }

    /** The chemicals the block in front of a hopper holds, so an input knows what to ask for. */
    public static List<ResourceLocation> neighbourChemicalIds(Level level, BlockPos target, Direction side) {
        return MekanismBridge.neighbourIds(level, target, side);
    }

    /** Only resolved once Mekanism is known to be loaded. */
    private static final class MekanismBridge {
        private static Object create(int tanks, long tankCapacity, BooleanSupplier singleCell,
                                     Runnable changedHook) {
            var storage = new GTPortChemicalStorage(
                    tanks, tankCapacity, singleCell);
            storage.setChangedHook(changedHook);
            return storage;
        }

        private static CompoundTag save(Object storage, HolderLookup.Provider registries) {
            return ((GTPortChemicalStorage) storage)
                    .save(registries);
        }

        private static void load(Object storage, CompoundTag tag, HolderLookup.Provider registries) {
            ((GTPortChemicalStorage) storage)
                    .load(tag, registries);
        }

        private static List<GTReactionStack> contents(Object storage) {
            return ((GTPortChemicalStorage) storage)
                    .windowContents();
        }

        private static long amount(Object storage, ResourceLocation chemical) {
            return ((GTPortChemicalStorage) storage)
                    .amountOf(chemical);
        }

        private static long take(Object storage, ResourceLocation chemical, long amount) {
            return ((GTPortChemicalStorage) storage)
                    .extractById(chemical, amount);
        }

        private static long add(Object storage, ResourceLocation chemical, long amount) {
            return ((GTPortChemicalStorage) storage)
                    .insertById(chemical, amount);
        }

        private static long room(Object storage, ResourceLocation chemical, long wanted) {
            return ((GTPortChemicalStorage) storage)
                    .roomFor(chemical, wanted);
        }

        private static List<ResourceLocation> ids(Object storage) {
            return ((GTPortChemicalStorage) storage).chemicalIds();
        }

        /** The chemicals the neighbour exposes on that face. */
        private static List<ResourceLocation> neighbourIds(Level level, BlockPos target, Direction side) {
            mekanism.api.chemical.IChemicalHandler handler = level.getCapability(
                    mekanism.common.capabilities.Capabilities.CHEMICAL.block(), target, side);
            if (handler == null) {
                return List.of();
            }
            List<ResourceLocation> out = new java.util.ArrayList<>();
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                mekanism.api.chemical.ChemicalStack stack = handler.getChemicalInTank(tank);
                if (stack.isEmpty()) {
                    continue;
                }
                ResourceLocation id = stack.getChemicalHolder().unwrapKey()
                        .map(key -> key.location()).orElse(null);
                if (id != null && !out.contains(id)) {
                    out.add(id);
                }
            }
            return out;
        }

        /** Moves up to {@code amount} of one chemical in the direction {@code input} says. */
        private static long reach(Object ours, ResourceLocation chemical, long amount,
                                  Level level, BlockPos target, Direction side, boolean input) {
            if (ours == null || amount <= 0) {
                return 0L;
            }
            mekanism.api.chemical.IChemicalHandler handler = level.getCapability(
                    mekanism.common.capabilities.Capabilities.CHEMICAL.block(), target, side);
            if (handler == null) {
                return 0L;
            }
            GTPortChemicalStorage storage = (GTPortChemicalStorage) ours;
            if (input) {
                long room = storage.roomFor(chemical, amount);
                if (room <= 0) {
                    return 0L;
                }
                // Pull: ask the neighbour for the slice, offer it here, then take only what landed.
                mekanism.api.chemical.ChemicalStack wanted = byChemicalId(chemical, Math.min(room, amount));
                if (wanted.isEmpty()) {
                    return 0L;
                }
                mekanism.api.chemical.ChemicalStack drained = handler.extractChemical(wanted, mekanism.api.Action.EXECUTE);
                if (drained.isEmpty()) {
                    return 0L;
                }
                long inserted = storage.insertById(chemical, drained.getAmount());
                long unused = drained.getAmount() - inserted;
                if (unused > 0 && inserted > 0) {
                    // Hand back whatever the tanks refused rather than voiding it.
                    handler.insertChemical(drained.copyWithAmount(unused), mekanism.api.Action.EXECUTE);
                }
                return inserted;
            }
            long held = storage.amountOf(chemical);
            if (held <= 0) {
                return 0L;
            }
            long moved = Math.min(held, amount);
            mekanism.api.chemical.ChemicalStack wanted = byChemicalId(chemical, moved);
            if (wanted.isEmpty()) {
                return 0L;
            }
            mekanism.api.chemical.ChemicalStack accepted = handler.insertChemical(wanted, mekanism.api.Action.EXECUTE);
            long taken = moved - accepted.getAmount();
            if (taken <= 0) {
                return 0L;
            }
            return storage.extractById(chemical, taken);
        }

        /** A stack of at most {@code amount} of the chemical an id names. */
        private static mekanism.api.chemical.ChemicalStack byChemicalId(ResourceLocation id, long amount) {
            return mekanism.api.MekanismAPI.CHEMICAL_REGISTRY.getHolder(id)
                    .map(holder -> new mekanism.api.chemical.ChemicalStack(holder, amount))
                    .orElse(mekanism.api.chemical.ChemicalStack.EMPTY);
        }
    }

    /** Only resolved once Thaumcraft is known to be loaded. */
    private static final class ThaumcraftBridge {
        private static Object create(int tanks, int tankCapacity, BooleanSupplier singleCell,
                                     BooleanSupplier active, BooleanSupplier output,
                                     Runnable changedHook) {
            var storage = new GTPortEssentiaStorage(
                    tanks, tankCapacity, singleCell, active, output);
            storage.setChangedHook(changedHook);
            return storage;
        }

        private static CompoundTag save(Object storage, HolderLookup.Provider registries) {
            return ((GTPortEssentiaStorage) storage)
                    .save(registries);
        }

        private static void load(Object storage, CompoundTag tag, HolderLookup.Provider registries) {
            ((GTPortEssentiaStorage) storage)
                    .load(tag, registries);
        }

        private static List<GTReactionStack> contents(Object storage) {
            return ((GTPortEssentiaStorage) storage)
                    .windowContents();
        }

        private static long amount(Object storage, ResourceLocation aspect) {
            return ((GTPortEssentiaStorage) storage)
                    .amountOf(aspect);
        }

        private static long take(Object storage, ResourceLocation aspect, long amount) {
            return ((GTPortEssentiaStorage) storage)
                    .takeById(aspect, amount);
        }

        private static long add(Object storage, ResourceLocation aspect, long amount) {
            return ((GTPortEssentiaStorage) storage)
                    .addById(aspect, amount);
        }

        private static long room(Object storage, ResourceLocation aspect, long wanted) {
            return ((GTPortEssentiaStorage) storage)
                    .roomFor(aspect, wanted);
        }

        private static List<ResourceLocation> aspects(Object storage) {
            return ((GTPortEssentiaStorage) storage)
                    .aspectIds();
        }

        /** Only resolved once ThaumicEnergistics is known to be loaded. */
        private static final class InterfaceBridge {
            static boolean isActive(Level level, BlockPos target, Direction side) {
                return ModList.get().isLoaded(THAUMIC_ENERGISTICS)
                        && GTInterfaceEssentia.isActive(level, target, side);
            }

            static ResourceLocation stockId(Level level, BlockPos target, Direction side) {
                return GTInterfaceEssentia.idOf(GTInterfaceEssentia.stockAspect(level, target, side));
            }

            static int take(Level level, BlockPos target, Direction side, Aspect aspect, int amount) {
                return GTInterfaceEssentia.take(level, target, side, aspect, amount);
            }

            static int accept(Level level, BlockPos target, Direction side, Aspect aspect, int amount) {
                return GTInterfaceEssentia.accept(level, target, side, aspect, amount);
            }
        }

        /**
         * The aspects the neighbour holds.
         *
         * <p>Read through {@code IEssentiaTransport.getEssentiaType}, which reports the one aspect a
         * connectable container offers on that side; a neighbour exposing several still reports the
         * first, and the loop keeps asking after each successful pull.</p>
         */
        private static List<ResourceLocation> neighbourAspects(Level level,
                                                               BlockPos target,
                                                               Direction side) {
            // An interface with the essentia card is not reachable through the Thaumcraft
            // capability, so it is asked directly first.
            if (InterfaceBridge.isActive(level, target, side)) {
                ResourceLocation id = InterfaceBridge.stockId(level, target, side);
                return id == null ? List.of() : List.of(id);
            }
            IEssentiaTransport transport =
                    EssentiaCapabilities.find(level, target, side);
            if (transport == null || !transport.isConnectable(side)
                    || !transport.canOutputTo(side)) {
                return List.of();
            }
            ResourceLocation id = ThaumcraftAspectIds.idOf(transport.getEssentiaType(side));
            return id == null ? List.of() : List.of(id);
        }

        /** Moves up to {@code amount} of one aspect in the direction {@code input} says. */
        private static long reach(Object ours, ResourceLocation aspect, int amount,
                                  Level level, BlockPos target,
                                  Direction side, boolean input) {
            if (ours == null || amount <= 0) {
                return 0L;
            }
            GTPortEssentiaStorage storage = (GTPortEssentiaStorage) ours;
            Aspect resolved = ThaumcraftAspectIds.find(aspect);
            if (resolved == null) {
                return 0L;
            }

            // An interface carrying the essentia card exposes no Thaumcraft transport capability —
            // that is registered only for the mod's export bus — so it is driven through its own
            // logic instead.
            if (InterfaceBridge.isActive(level, target, side)) {
                if (input) {
                    long room = storage.roomFor(aspect, amount);
                    if (room <= 0) {
                        return 0L;
                    }
                    int taken = InterfaceBridge.take(level, target, side, resolved,
                            (int) Math.min(room, amount));
                    return taken <= 0 ? 0L : storage.addById(aspect, taken);
                }
                long held = storage.amountOf(aspect);
                if (held <= 0) {
                    return 0L;
                }
                int accepted = InterfaceBridge.accept(level, target, side, resolved,
                        (int) Math.min(held, amount));
                return accepted <= 0 ? 0L : storage.takeById(aspect, accepted);
            }

            IEssentiaTransport transport = EssentiaCapabilities.find(level, target, side);
            if (transport == null || !transport.isConnectable(side)) {
                return 0L;
            }
            if (input) {
                // Pull: only from a neighbour that offers on this side.
                if (!transport.canOutputTo(side)) {
                    return 0L;
                }
                long room = storage.roomFor(aspect, amount);
                if (room <= 0) {
                    return 0L;
                }
                int wanted = (int) Math.min(room, amount);
                int taken = transport.takeEssentia(resolved, wanted, side);
                if (taken <= 0) {
                    return 0L;
                }
                return storage.addById(aspect, taken);
            }
            // Push: only into a neighbour that accepts on this side.
            if (!transport.canInputFrom(side)) {

                return 0L;
            }
            long held = storage.amountOf(aspect);
            if (held <= 0) {
                return 0L;
            }
            int offered = (int) Math.min(held, amount);
            int accepted = transport.addEssentia(resolved, offered, side);

            if (accepted <= 0) {
                return 0L;
            }
            return storage.takeById(aspect, accepted);
        }
    }
}
