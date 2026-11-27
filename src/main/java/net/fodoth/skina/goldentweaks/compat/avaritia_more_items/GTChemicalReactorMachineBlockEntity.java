package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import java.math.BigInteger;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortEnergyTiers;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortBlock;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortBlockEntity;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortChannels;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortKind;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortStorages;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortTier;
import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.fodoth.skina.goldentweaks.network.packet.S2CReactionProductsPacket;
import net.fodoth.skina.goldentweaks.network.packet.S2CRecipeLockPacket;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.abstracts.AbstractMultiblockTile;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.pattern.AMiBlockPattern;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.pattern.AMiBlockPattern.BlockPatternMatch;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.pattern.AMiBlockPatternReloadListener;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTypes;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.PortMode;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.IMultiblockRecipeSource;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MatchedMultiblockRecipe;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MultiblockRecipeInput;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MultiblockRecipeMatcher;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.structure.MultiblockPortBinding;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.structure.MultiblockStructure;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.structure.PortRequirement;
import net.weibai.avaritia_more_items.common.items.matter_cluster.BetterMatterClusterItem;
import net.weibai.avaritia_more_items.common.items.matter_cluster.FluidMatterClusterItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.inventory.MenuType;

/**
 * Tile of the basic chemical reaction kettle.
 *
 * <p>The pattern id is resolved against our own namespace so it picks up our
 * {@code multiblock/structures} entry. AMI's own machines derive that id from
 * {@code APILang.rl}, which hard-codes their namespace.</p>
 *
 * <p>No recipe source is supplied yet: the machine only has to assemble, so
 * {@code AbstractMultiblockTile} keeps its empty default and the tile stays idle once formed.</p>
 *
 * <p><b>Structure checks run every ten seconds, in both directions.</b> AMI revalidates an
 * assembled shape through {@link #getStructureCheckInterval()} and, when that fails, invalidates
 * it; it never assembles a shape on its own, so assembly would otherwise require right-clicking
 * with the multiblock hammer. Overriding the interval to
 * {@value #STRUCTURE_CHECK_INTERVAL_TICKS} ticks slows the formed-side revalidation down, and
 * {@link #serverTick} reuses the same period to look for a shape while the machine is unformed.
 * Keeping one interval for both directions means the machine cannot be destroyed and rebuilt
 * within the same window and flicker.</p>
 *
 * <p><b>Why the block state is written from here:</b> AMI's
 * {@code AbstractConnectBlock.updateFormation} propagates its {@code formed} flag only across
 * blocks of the shell family. Our allocation ports are plain blocks, so nothing would ever flip
 * the flag on the kettle. Driving {@code formed} and {@code running} from the actual assembly
 * state keeps the front overlay honest and gives the blockstate enough information to pick
 * between the unconstructed, idle and off textures on its own.</p>
 */
public class GTChemicalReactorMachineBlockEntity extends AbstractMultiblockTile implements Nameable {

    public static final String MACHINE_ID = "chemical_reactor";

    /** Structure check period, in both directions: ten seconds. */
    public static final int STRUCTURE_CHECK_INTERVAL_TICKS = 200;

    /** The controller slot behaves like an ordinary storage slot, so it stacks to a full stack. */
    public static final int CONTROLLER_SLOT_LIMIT = 64;

    /**
     * Why the machine last switched itself off.
     *
     * <p>Kept apart from {@link #running} because "off" alone cannot say whether the player pressed
     * the button or a trip did it, and the status line in the GUI has to tell them apart. Reset when
     * the machine starts again, so it always describes the most recent stop.</p>
     */
    private GTReactorStatus stopReason = GTReactorStatus.OFF;

    /**
     * The craft read back from disk, waiting for the shape to be re-resolved.
     *
     * <p>{@code loadAdditional} runs before the world is reachable enough to ask the recipe manager
     * for a recipe, so the id is parked here and picked up on the first tick that has a structure.
     * Held as an id rather than a value because a datapack may have removed the recipe while the
     * world was closed, and that has to mean "nothing to resume" rather than a stale match.</p>
     */
    @Nullable
    private ResourceLocation pendingRecipe;
    private int pendingProgress;
    private int pendingLength;
    /** Copies the saved craft ran, read back so a resumed craft keeps its parallel bill. */
    private int pendingCopies = 1;
    /** The group the parked craft belonged to; unlimited when the save did not name one. */
    @Nullable
    private Integer pendingChannel;
    @Nullable
    private Integer pendingMode;

    /**
     * Ticks a freshly loaded machine waits before the base tick may start anything.
     *
     * <p>Not the same thing as the resume: this guards against a <em>new</em> craft. A chunk comes
     * back before its neighbours, so for the first moments after a load the machine can look
     * assembled with ports that have not been read yet. Matching in that window takes ingredients
     * that the real contents would not have paid for.</p>
     */
    private int settleTicks;

    /**
     * How many ticks the saved craft has been looked for without finding it yet.
     *
     * <p>A load returns the block entity before the recipe manager has synced on a cold start, so the
     * first few lookups legitimately find nothing. Counted rather than retried forever, because a
     * recipe that really was removed has to stop being asked about.</p>
     */
    private int resumeAttempts;

    /**
     * Products of the running reaction, resolved for the GUI.
     *
     * <p>Not persisted: they describe the craft in flight, which is rebuilt from the inputs after a
     * reload. A stale list would make the GUI quote outputs for a reaction that is not running.</p>
     */
    private List<GTReactionStack> activeProducts = List.of();

    /** Length of the running craft, needed to turn per-craft amounts into a rate. */
    private int activeProcessingTime;

    private static final String TAG_CUSTOM_NAME = "CustomName";
    private static final String TAG_CONTROLLER = "Controller";
    private static final String TAG_LOCK_RECIPE = "LockRecipe";
    private static final String TAG_LOCKED_RECIPE = "LockedRecipe";
    private static final String TAG_VOID_OUTPUT = "VoidOutput";
    private static final String TAG_BATCH_MODE = "BatchMode";
    private static final String TAG_ISOLATE_INPUT = "IsolateInput";
    private static final String TAG_RUNNING = "Running";
    /** The craft in flight, so leaving the world mid-reaction does not eat its inputs. */
    private static final String TAG_ACTIVE_RECIPE = "ActiveRecipe";
    private static final String TAG_ACTIVE_PROGRESS = "ActiveProgress";
    private static final String TAG_ACTIVE_LENGTH = "ActiveLength";
    private static final String TAG_ACTIVE_OVERCLOCK = "ActiveOverclock";
    /** How many copies the saved craft ran; without it a resumed craft would bill a single one. */
    private static final String TAG_ACTIVE_COPIES = "ActiveCopies";
    /** The group the craft was matched against, so it keeps drawing from and delivering to it. */
    private static final String TAG_ACTIVE_CHANNEL = "ActiveChannel";
    private static final String TAG_ACTIVE_MODE = "ActiveMode";
    /** How far this machine may overclock; see {@link #NO_OVERCLOCK_LIMIT}. */
    private static final String TAG_OVERCLOCK_LIMIT = "OverclockLimit";
    /** Parallel ceiling; see {@link #ALWAYS_MAX_PARALLEL}. */
    private static final String TAG_PARALLEL_LIMIT = "ParallelLimit";

    /**
     * Starts at one so the very first server tick performs a check.
     *
     * <p>That covers both "placed the machine" and "chunk reloaded": in either case the block
     * should resolve its assembly state immediately instead of showing the unassembled texture
     * for a further ten seconds.</p>
     */
    private int scanCountdown = 1;

    /**
     * The machine's single inventory slot, shown as the controller slot.
     *
     * <p>It accepts any item and stacks up to 64 like an ordinary storage slot.</p>
     */
    private final ItemStackHandler controllerSlot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return CONTROLLER_SLOT_LIMIT;
        }
    };

    @Nullable
    private Component customName;

    private boolean lockRecipe;

    /**
     * Id of the reaction {@link #lockRecipe} pins the machine to.
     *
     * <p>Held by id rather than by object: recipes are reloaded by data pack changes, and a stale
     * instance would quietly stop matching. The id survives a reload and simply fails to resolve if
     * the recipe is removed, which reads as "locked to something that no longer exists".</p>
     */
    @Nullable
    private ResourceLocation lockedRecipeId;
    private boolean voidOutput;
    private boolean batchMode = true;

    /**
     * The "input isolated" switch, on by default.
     *
     * <p>Isolated means each port answers only for the channels it names, which is what lets one
     * machine carry several independent production lines. Turning it off makes the machine ignore
     * every port's channel field and treat them all as channel {@code 0}, so the whole structure
     * behaves as one line — useful when a layout was built with channels the recipe does not
     * distinguish.</p>
     *
     * <p>On by default because that is the behaviour a channel-less layout already had: every port
     * defaults to channel 0, which joins everything.</p>
     */
    private boolean isolateInput = true;

    /**
     * The channel group the running craft belongs to, or {@code null} when idle.
     *
     * <p>Locked when a recipe starts and held until it finishes, so the products go back to the
     * group that supplied the inputs and the energy is drawn from that same group throughout. A
     * player cannot change channels mid-craft because the window refuses edits while the machine is
     * busy; this field is what makes the products land where the inputs came from even if the ports
     * are rearranged in some other way.</p>
     */
    @Nullable
    private Integer activeChannel;

    /**
     * The mode being tried, or {@code null} when no mode is in force.
     *
     * <p>Set alongside {@link #selectedChannel} during a search and pinned with it when a craft
     * starts, so the run keeps matching the mode it began under.</p>
     */
    @Nullable
    private Integer selectedMode;

    /** The mode pinned for the running craft; {@code null} when none. */
    @Nullable
    private Integer activeMode;

    /**
     * Overclock levels this craft runs at, pinned when it started.
     *
     * <p>Pinned rather than read live: the limit comes from the energy ports, and a player adding a
     * port mid-craft must not change how much power the run in flight is billed.</p>
     */
    private int activeOverclock;

    /**
     * How many copies of the recipe this craft runs, from parallel times batch.
     *
     * <p>Recorded rather than re-derived: essentia and chemical amounts are not part of the matched
     * item/fluid lists, so recovering the multiplier from those lists only works while a recipe has
     * item outputs at all. Keeping the figure makes a purely typed recipe scale exactly like an
     * item one.</p>
     *
     * <p>Not persisted: it describes the craft in flight, which is rebuilt after a reload.</p>
     */
    private int runCopies = 1;

    /**
     * The exact per-tick bill of the running craft, before any overclock.
     *
     * <p>{@code BigInteger} because the product of a large multiplier and a costly recipe has no
     * reason to fit an {@code int} — and saturating it here would silently make a huge machine
     * behave like a merely large one. Billing saturates only when it reaches the FE API.</p>
     */
    private BigInteger runCost = BigInteger.ZERO;

    /**
     * The channel group whose ports may be used, or {@code null} when every port is allowed.
     *
     * <p>Every port query — input handlers, output ports, energy — filters through this, so a port
     * on another channel is invisible to the machine while a group is being tried. Idle, it is
     * {@code null} and the whole machine is visible, which is what the window shows when it has no
     * craft to report.</p>
     */
    @Nullable
    private Integer selectedChannel;

    /** Channel text of every port as of the last tick, used to notice edits. */
    private String lastChannelSignature = "";

    /**
     * Source of the tie-breaking shuffle among equal-priority ports.
     *
     * <p>Reseeded when a craft starts, so the order is fixed for that craft — the inputs it drew
     * and the ports it will deliver to stay the same — but differs between crafts, which is what
     * keeps one port from always being the one drained first.</p>
     */
    private Random portShuffle = new Random();

    /** Ports of the group in force, in the order fixed for the current craft. */
    @Nullable
    private List<MultiblockPortBinding> cachedPorts;

    /** Group the cached order was built for. */
    @Nullable
    private Integer cachedPortGroup;

    /** Bumped whenever the cached order is dropped, so a stale group check cannot reuse it. */
    private int portGeneration;

    /** Value of {@link #portGeneration} the cached order was built at. */
    private int portGenerationCache = -1;

    /**
     * The channels this machine can run, in the order they are tried.
     *
     * <p>Gathered from every port's channel field: each named channel contributes a group, and the
     * groups are sorted ascending with the wildcard last. The wildcard is last because a group made
     * only of wildcard ports is a subset of every other group — it can never match a recipe the
     * others failed on — but it is still tried, since a machine whose ports are all wildcards has
     * no other group at all.</p>
     *
     * <p>Duplicate groups are dropped: two channels that happen to select the same ports would
     * otherwise each pay for a full recipe search.</p>
     */
    private List<Integer> channelGroups() {
        if (this.structure == null) {
            return List.of(GTPortChannels.WILDCARD);
        }
        if (!this.isolateInput) {
            // Isolation off: the ports' channel fields are ignored, so there is exactly one group to
            // try — the wildcard — and every port joins it. Searching the declared channels would
            // only repeat the same match against the same ports.
            return List.of(GTPortChannels.WILDCARD);
        }
        TreeSet<Integer> seen = new TreeSet<>();
        boolean anyWildcard = false;
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            // Skipped for the same reason it is skipped in portAllowed: a dynamo belonging to
            // channel 3 would otherwise put a group 3 on the list that has nothing in it.
            if (isDynamo(port)) {
                continue;
            }
            for (int channel : channelsOf(port)) {
                if (channel == GTPortChannels.WILDCARD) {
                    anyWildcard = true;
                } else {
                    seen.add(channel);
                }
            }
        }
        List<Integer> groups = new ArrayList<>(seen);
        if (anyWildcard) {
            groups.add(GTPortChannels.WILDCARD);
        }
        if (groups.isEmpty()) {
            groups.add(GTPortChannels.WILDCARD);
        }
        return dropDuplicateGroups(groups);
    }

    /**
     * The modes to try, in the order to try them.
     *
     * <p>Derived from the ports of the current group, ranked by the same priority order the ports
     * themselves use. A port's mode field lists the modes it serves, and {@code 0} is the wildcard:
     * such a port serves every mode and therefore contributes no entry of its own.</p>
     *
     * <p>So a group whose ports are ranked {@code -2, 3, 4, 21, 1, 0} yields the mode order
     * {@code -2, 3, 4, 21, 1} — the wildcard port adds nothing to the list. The wildcard mode is
     * still appended last, so a group made only of wildcard ports still has one mode to run in.</p>
     */
    private List<Integer> modeOrder() {
        if (this.structure == null) {
            return List.of(GTPortChannels.WILDCARD);
        }

        // Walk the ports in priority order, so the modes come out in the same ranking the ports do.
        // Entries are added once, in first-seen order: a mode named by several ports belongs to all
        // of them and needs only one attempt.
        LinkedHashSet<Integer> ordered = new LinkedHashSet<>();
        @SuppressWarnings("unused") boolean anyWildcardPort = false;
        for (MultiblockPortBinding port : sortedPorts()) {
            List<Integer> modes = modesOf(port);
            if (modes.contains(GTPortChannels.WILDCARD)) {
                //noinspection UnusedAssignment
                anyWildcardPort = true;
                continue;
            }
            ordered.addAll(modes);
        }

        List<Integer> modes = new ArrayList<>(ordered);
        // The wildcard mode is last for the same reason the wildcard channel is: it can match what
        // every other mode already tried, so it is the fallback rather than the first choice.
        modes.add(GTPortChannels.WILDCARD);
        return modes;
    }

    /** The modes a bound port declares, read from the port's own block entity. */
    private List<Integer> modesOf(MultiblockPortBinding port) {
        if (this.level != null
                && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
            return entity.modes();
        }
        return List.of(GTPortChannels.WILDCARD);
    }

    /**
     * The channels a bound port declares, read from the port's own block entity.
     *
     * <p>Read through the level rather than through a binding interface: the binding only records
     * where the port is and what kind it is, and the channel lives on this mod's port entity. A
     * port that is not one of ours — a legacy AMI port, say — has no channel field and is treated
     * as a wildcard, which keeps old builds working.</p>
     */
    private List<Integer> channelsOf(MultiblockPortBinding port) {
        if (this.level != null
                && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
            return entity.channels();
        }
        return List.of(GTPortChannels.WILDCARD);
    }

    /**
     * Removes groups that select exactly the same ports as an earlier one.
     *
     * <p>Two channels can name the same set when every port on one is also on the other, which is
     * common with a wildcard-heavy layout. Matching a recipe twice against the same port set would
     * only repeat a search that already failed.</p>
     */
    private List<Integer> dropDuplicateGroups(List<Integer> groups) {
        List<Integer> kept = new ArrayList<>();
        Set<String> signatures = new HashSet<>();
        for (int group : groups) {
            if (signatures.add(portSignatureFor(group))) {
                kept.add(group);
            }
        }
        return kept;
    }

    /** A stable description of which ports a channel group would select. */
    private String portSignatureFor(int channel) {
        StringBuilder out = new StringBuilder();
        if (this.structure != null) {
            for (MultiblockPortBinding port : this.structure.getPorts()) {
                if (!isDynamo(port) && portJoins(port, channel)) {
                    out.append(port.pos().asLong()).append(';');
                }
            }
        }
        return out.toString();
    }

    /**
     * Whether a port is a dynamo hatch — a legal part of the shape that this machine never uses.
     *
     * <p>Only energy <em>input</em> hatches power a reaction. A dynamo may still be placed, because
     * the pattern's {@code any} cell accepts one, but it is not part of the machine: it neither
     * supplies energy nor receives products, and it must not claim a channel group of its own.
     * Every port query starts here, so one predicate covers all of them.</p>
     */
    private static boolean isDynamo(MultiblockPortBinding port) {
        return port.type() == MultiblockPortTypes.ENERGY && !port.isInput();
    }

    /**
     * Whether a port belongs to {@code channel}'s group. A {@code null} channel means "all".
     *
     * <p>This is the single place port membership is decided, so the "input isolated" switch is
     * applied here and nowhere else: one answer covers the recipe search, the running craft's
     * energy draw and its product delivery alike.</p>
     */
    private boolean portJoins(MultiblockPortBinding port, @Nullable Integer channel) {
        if (channel == null) {
            return true;
        }
        if (!this.isolateInput) {
            // Isolation off: every port is treated as if its channel field said 0, so the whole
            // machine behaves as one group and the ports' own channel settings are ignored.
            return true;
        }
        List<Integer> channels = channelsOf(port);
        return channels.contains(GTPortChannels.WILDCARD) || channels.contains(channel);
    }

    /**
     * The ports of the group in force, ordered so the machine prefers the most important ones.
     *
     * <p>Order matters because every consumer of this list works through it in turn: the recipe
     * matcher draws inputs from the first ports that can supply them, and delivery fills the first
     * ports that have room. Sorting by descending priority therefore means "use the high-priority
     * ports first, and only spill into the low-priority ones when those cannot cover it" — which is
     * what allows one craft to draw across several ports.</p>
     *
     * <p>The result is cached for the current group and craft. Recomputing it per call would reshuffle
     * the equal-priority band each time, so within a single craft the matcher would draw inputs from
     * one port and the delivery code would fill a different one. The cache is dropped when the group
     * changes and when a craft starts, which is the only moment a new order is wanted.</p>
     */
    private List<MultiblockPortBinding> sortedPorts() {
        if (this.structure == null) {
            return List.of();
        }

        Integer group = effectiveChannel();
        if (this.cachedPorts != null && Objects.equals(this.cachedPortGroup, group)
                && this.portGenerationCache == this.portGeneration) {
            return this.cachedPorts;
        }

        List<MultiblockPortBinding> ports = new ArrayList<>();
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (portAllowed(port)) {
                ports.add(port);
            }
        }
        // A stable sort by descending priority; ties keep their relative order and are then shuffled
        // within each equal-priority band below.
        ports.sort(Comparator.comparingInt(this::priorityOfPort).reversed());

        Random random = this.portShuffle;
        int start = 0;
        while (start < ports.size()) {
            int end = start + 1;
            int priority = priorityOfPort(ports.get(start));
            while (end < ports.size() && priorityOfPort(ports.get(end)) == priority) {
                end++;
            }
            if (end - start > 1) {
                Collections.shuffle(ports.subList(start, end), random);
            }
            start = end;
        }

        List<MultiblockPortBinding> frozen = List.copyOf(ports);
        this.cachedPorts = frozen;
        this.cachedPortGroup = group;
        this.portGenerationCache = this.portGeneration;
        return frozen;
    }

    /**
     * Drops the cached port order, so the next query re-sorts and re-shuffles.
     *
     * <p>Called when a craft starts and whenever the grouping changes. Between those points the
     * order must stay put: the recipe was matched against one arrangement, and delivering to another
     * would put products in ports the player never pointed at.</p>
     */
    private void invalidatePortOrder() {
        this.cachedPorts = null;
        this.portGeneration++;
    }

    /**
     * The group currently in force.
     *
     * <p>A running craft outranks a search: while {@link #activeChannel} is set, every port query
     * answers for that group, so the recipe keeps drawing energy and delivering products to the
     * line it started on. Between crafts the search sets {@link #selectedChannel} group by group.
     * {@code null} means no group is in force and the whole machine is visible.</p>
     */
    @Nullable
    private Integer effectiveChannel() {
        return this.activeChannel != null ? this.activeChannel : this.selectedChannel;
    }

    /** A port's priority, or zero for ports that are not ours. */
    private int priorityOfPort(MultiblockPortBinding port) {
        if (this.level != null
                && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
            return entity.priority();
        }
        return 0;
    }

    /**
     * Freezes or unfreezes every port's channel field.
     *
     * <p>Done while a craft runs so the grouping it was matched against cannot change underneath it.
     * The lock is also released whenever the machine stops for any reason, because a port left
     * frozen would look broken to the player.</p>
     */
    private void setChannelsLocked(boolean locked) {
        if (this.structure == null || this.level == null) {
            return;
        }
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
                entity.setChannelLocked(locked);
            }
        }
    }

    /**
     * Whether a port takes part in the craft under the current selection.
     *
     * <p>Two independent filters, and both are necessary. The channel decides which production line
     * the port belongs to; the port's own run switch decides whether it is in use at all. A
     * switched-off port keeps its capability — a pipe may still fill or drain it — but the machine
     * neither draws from it nor delivers into it, which is what "taken out of service" means for a
     * part that sits in a shared wall.</p>
     */
    private boolean portAllowed(MultiblockPortBinding port) {
        if (isDynamo(port)) {
            return false;
        }
        if (this.level != null
                && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity
                && !entity.isEnabled()) {
            return false;
        }
        return portJoins(port, effectiveChannel());
    }

    /**
     * True while a craft is under way.
     *
     * <p>Reported to the port window, which refuses channel edits during a run: the channels decide
     * where a craft's inputs and products go, and moving them mid-craft would redirect a reaction
     * that was matched against a different set of ports.</p>
     */
    @SuppressWarnings("unused")
    public boolean isBusy() {
        return this.activeRecipe != null;
    }

    /** The channel the running craft belongs to, or {@code null} when idle. */
    @Nullable
    public Integer activeChannel() {
        return this.activeChannel;
    }

    /** What the running craft will produce, or an empty list when nothing is running. */
    public List<GTReactionStack> activeProducts() {
        return this.activeProducts;
    }

    /** Energy held by the input ports, summed over the ports of the group in force. */
    public int storedEnergy() {
        return getStoredInputEnergy();
    }

    /** Total energy the input ports can hold, over the group in force. */
    public int energyCapacity() {
        return getInputEnergyCapacity();
    }

    /** The channel currently being tried by the search, or {@code null}. */
    @SuppressWarnings("unused")
    @Nullable
    public Integer selectedChannel() {
        return this.selectedChannel;
    }

    /**
     * A description of every port's channel, mode and run switch, used to notice edits.
     *
     * <p>The switch belongs here for the same reason the channel does: flipping it changes which
     * ports take part, so the cached grouping and port order no longer describe the machine.</p>
     */
    /**
     * A description of every port's channel and mode fields, used to notice edits.
     *
     * <p>The recipe cache is keyed on what the ports hold, which does not change when a player edits
     * a channel or a mode. Folding both fields into this signature and comparing it every tick is
     * what makes such an edit invalidate that cache, so the next look at the machine re-groups its
     * ports instead of reusing a match made against the old layout.</p>
     */
    private String groupingSignature() {
        if (this.structure == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            out.append(port.pos().asLong()).append('=');
            if (this.level != null
                    && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
                // Mode and the run switch are part of the signature as well: they decide which
                // recipes a group may run and which ports take part, so editing either has to drop
                // the cached match exactly like a channel edit does.
                out.append(entity.channelText()).append('/').append(entity.modeText())
                        .append('/').append(entity.isEnabled() ? '1' : '0');
            } else {
                out.append(GTPortChannels.WILDCARD_TEXT).append('/')
                        .append(GTPortChannels.WILDCARD_TEXT);
            }
            out.append(';');
        }
        return out.toString();
    }

    /**
     * Drops the recipe cache when the ports' channels have changed since the last tick.
     *
     * <p>Called before the search runs. Clearing the same fields AMI clears on a reload is what
     * makes the next search look again rather than reuse a stale one.</p>
     */
    private void invalidateIfGroupingChanged() {
        String signature = groupingSignature();
        if (signature.equals(this.lastChannelSignature)) {
            return;
        }
        this.lastChannelSignature = signature;
        this.lastRecipeSearchVersion = -1;
        this.lastInputSignature = Integer.MIN_VALUE;
        // A channel edit changes which ports belong to each group, so the cached order no longer
        // describes the machine and has to be rebuilt.
        invalidatePortOrder();
    }

    /**
     * Whether the machine is switched on, as the GUI switch sees it.
     *
     * <p>Not the same thing as the {@code running} block state: this flag survives an invalidated
     * structure (so the switch does not silently flip back on its own), while the block state is
     * only ever true for a formed machine. {@link #applyRunningState} is the single place that
     * reconciles the two.</p>
     */
    private boolean running;

    /**
     * How long a reloaded machine leaves its ports alone, in ticks.
     *
     * <p>Two seconds. Short on purpose: it only has to outlast the chunk and neighbour restore, not
     * be a visible pause. The saved craft is resumed ahead of the base tick and does not wait for
     * this; only starting something <em>new</em> does.</p>
     */
    private static final int SETTLE_TICKS = 40;

    /** Lookups allowed before a saved craft is presumed gone. A cold start needs a handful. */
    private static final int RESUME_ATTEMPTS = 200;

    /** Casings the shape needs at least. Mirrored in the item tooltip, which states the same floor. */
    private static final int MIN_CASINGS = 8;

    /** Value of {@link #overclockLimit} meaning "no ceiling". */
    public static final int NO_OVERCLOCK_LIMIT = -1;

    /**
     * How far this machine may overclock, as the ratio's denominator.
     *
     * <p>{@link #NO_OVERCLOCK_LIMIT} means unlimited, which is also the default: a machine nobody
     * configured behaves exactly as it did before the setting existed.</p>
     */
    private int overclockLimit = NO_OVERCLOCK_LIMIT;

    /** Value of {@link #parallelLimit} meaning "always run at the maximum". */
    public static final int ALWAYS_MAX_PARALLEL = 0;

    /**
     * Largest multiplier the batch/parallel search will ever consider.
     *
     * <p>A recipe with no countable inputs divides to no ceiling at all, and scaling a record by an
     * unbounded figure would build lists big enough to exhaust the heap before the fit check could
     * refuse them. The cap is far above any port count a structure can hold, so it never limits a
     * real machine.</p>
     */
    private static final int MAX_SEARCHED_MULTIPLIER = 1 << 16;

    /**
     * The ceiling on how many batches may run at once.
     *
     * <p>{@link #ALWAYS_MAX_PARALLEL} is the default and means no ceiling of its own: the machine
     * runs as many as its speed allows, which is what every machine did before this setting
     * existed.</p>
     */
    private int parallelLimit = ALWAYS_MAX_PARALLEL;

    /** The parallel ceiling; {@link #ALWAYS_MAX_PARALLEL} for no ceiling of its own. */
    public int getParallelLimit() {
        return this.parallelLimit;
    }

    /**
     * Stores the parallel ceiling.
     *
     * <p>Clamped rather than validated: a negative count has no meaning, so it collapses to the
     * "always maximum" value instead of being rejected.</p>
     *
     * <p>Also capped at what this machine's hardware grants, when it grants anything at all. The
     * window clamps the box for the same reason, but a packet can carry any int — the limit the
     * player sets and the limit the scheduler enforces have to be the same number, so the check
     * belongs here rather than only in the window.</p>
     */
    public void setParallelLimit(int limit) {
        int sane = Math.max(ALWAYS_MAX_PARALLEL, limit);
        int ceiling = machineParallelCeiling();
        if (ceiling > 0 && sane > ceiling) {
            sane = ceiling;
        }
        if (this.parallelLimit == sane) {
            return;
        }
        this.parallelLimit = sane;
        setChanged();
        syncToClient();
    }

    /** How far this machine may overclock; {@link #NO_OVERCLOCK_LIMIT} for no ceiling. */
    public int getOverclockLimit() {
        return this.overclockLimit;
    }

    /**
     * The highest energy port tier present, as a one-based level.
     *
     * <p>This is what caps overclocking: a machine is only as fast as the best energy hatch it can
     * draw from. Zero when there is no energy input port at all.</p>
     */
    public int maxOverclock() {
        return energyPortCensus().topTier();
    }

    /**
     * The ceiling this machine's class puts on parallel copies, or zero for none.
     *
     * <p>Separate from {@link #getParallelLimit()}, which is the player's setting: this is what the
     * hardware grants. Zero means "no ceiling of its own", which is what every machine did before
     * this hook existed — a subclass that does cap says so by returning a count.</p>
     */
    public int machineParallelCeiling() {
        return 0;
    }

    private record EnergyCensus(int topTier, int topCount) {
        static final EnergyCensus NONE = new EnergyCensus(0, 0);
    }

    /**
     * The highest energy input port tier, and how many ports share it.
     *
     * <p>Both the overclock ceiling and the energy budget read this, so it is computed once per
     * query rather than twice. Anything below the top tier contributes nothing to either: it cannot
     * carry what the top one cannot, and counting it would let a pile of cheap ports beat one good
     * one.</p>
     *
     * <p>An assembly counts only while its own switch is on — the same rule
     * {@link #getEnergyInputHandlers()} applies, since the two have to agree: the census decides what
     * the machine may be asked to run, and counting a part that is never drawn from would let a
     * craft start that cannot be powered.</p>
     */
    private EnergyCensus energyPortCensus() {
        if (this.structure == null || this.level == null) {
            return EnergyCensus.NONE;
        }
        int top = 0;
        int count = 0;

        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.ENERGY) {
                continue;
            }
            if (!(this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            if (entity.kind() == GTPortKind.GENERAL && !entity.joinsEnergyChannel()) {
                // Switched out of the census by the player: still a part the machine can use, just
                // not one that contributes power to this line.
                continue;
            }
            // Our own level, not AMI's tier: AMI's enum ends at IX and this one clamps to it, so
            // every port above UV would be counted as IX and the upper half of the ladder would
            // read as one tier.
            int tier = entity.portTier().level();
            if (tier > top) {
                top = tier;
                count = 1;
            } else if (tier == top) {
                count++;
            }
        }
        return new EnergyCensus(top, count);
    }

    /** Set when the last search skipped at least one recipe for cost alone. */
    private boolean refusedForCost;

    /**
     * The cheapest rejected recipe of the last search, in FE per tick.
     *
     * <p>The smallest, not the largest: it is the cheapest one the player is closest to affording,
     * so naming it says how much more power is actually needed. Reporting a costly outlier would
     * suggest a bigger shortfall than the one that matters.</p>
     *
     * <p>Only meaningful when every matching recipe was rejected — if any was affordable the machine
     * runs that one instead, and this never surfaces.</p>
     */
    private int refusedCost;

    /**
     * The ceiling on a recipe's base energy cost, in FE per tick.
     *
     * <p>"Dual hatch boost": a hatch supplies 2 A but one may only be drawn at 1 A for a recipe, so
     * a lone MV hatch covers half an MV's throughput while a second of the same tier unlocks the
     * whole of it — which is one tier's worth, the next voltage up.</p>
     *
     * <p>So the booster is not a bonus on top of the hatch's rating: it is what makes a pair of
     * hatches usable at their own stated voltage. A lone one runs the same recipes at half that
     * rate.</p>
     */
    public BigInteger maxRecipeCost() {
        EnergyCensus census = energyPortCensus();
        if (census.topTier() == 0) {
            return BigInteger.ZERO;
        }
        BigInteger single = GTPortEnergyTiers.transferExact(census.topTier());
        // Two or more of the top tier double it; a lone one only reaches half of that doubling.
        return census.topCount() >= 2 ? single.shiftLeft(1) : single.shiftRight(1);
    }

    /**
     * The voltage the machine runs at, with the dual-hatch boost counted.
     *
     * <p>The lone-hatch current limit does not lower this. A single hatch is still a hatch of its
     * own voltage — it reads MV because it is an MV part, and what it loses is throughput, not
     * rating. Reporting a downgraded voltage made the label disagree with the part the player had
     * installed and with the power figure beside it, and a player reading "LV" next to an MV hatch
     * had no way to tell the difference between a current limit and a wrong part.</p>
     *
     * <p>A pair reaches one tier above, since two hatches at 1 A each make a full 2 A of the next
     * voltage. Capped at the top of the ladder: the boost cannot invent a tier above MAX.</p>
     */
    public int effectiveEnergyTier() {
        EnergyCensus census = energyPortCensus();
        if (census.topTier() == 0) {
            return 0;
        }
        int tier = census.topTier();
        if (census.topCount() >= 2) {
            tier++;
        }
        // Our own ladder, not AMI's: ours has the tiers above UV, and clamping to AMI's length
        // would report every one of them as IX.
        return Math.max(1, Math.min(GTPortTier.values().length, tier));
    }

    /** The cheapest rejected recipe of the last search, in FE per tick; zero when none. */
    public int refusedCost() {
        return this.refusedCost;
    }

    /**
     * Whether a recipe's base cost fits what the energy ports can carry.
     *
     * <p>Compared against the base figure, not the overclocked one. Overclocking is the player's
     * choice to trade power for speed, so it must not decide which recipes exist for the machine —
     * judging by the boosted cost would leave a deliberately overclocked machine unable to start
     * anything at the top of its budget. The boosted bill is what the store has to sustain, and a
     * shortfall there trips the breaker as it would for any other power failure.</p>
     *
     * <p>Checked before the craft starts rather than discovered mid-run: a recipe that cannot be
     * powered should never begin, because starting it consumes the inputs and then trips the breaker
     * on the first tick.</p>
     */
    public boolean withinEnergyBudget(int baseCostPerTick) {
        return baseCostPerTick <= 0
                || BigInteger.valueOf(baseCostPerTick).compareTo(maxRecipeCost()) <= 0;
    }

    /**
     * The most steps the player may ask for, ignoring their current setting.
     *
     * <p>This is the number the input box is capped at. Deliberately not the setting-aware figure
     * below: capping a <em>setting</em> against a value derived from that same setting would make
     * the control bite its own tail — lowering it once would forbid raising it again.</p>
     */
    public int overclockCeiling() {
        return maxOverclock();
    }

    /**
     * The most steps this machine may apply, before a recipe has a say.
     *
     * <p>Answered by the tier and the player's setting alone: {@code -1} means "as far as the tier
     * goes", anything else is the player's own ceiling. What a given recipe can actually afford is
     * a separate question, answered per recipe by {@link #overclockSteps(int)} — a step quadruples
     * the bill, so a costly recipe may only reach a fraction of this number.</p>
     */
    public int reachableOverclock() {
        int tierCeiling = maxOverclock();
        if (tierCeiling <= 0) {
            return 0;
        }
        int wanted = this.overclockLimit < 0 ? tierCeiling : this.overclockLimit;
        return Math.clamp(wanted, 0, tierCeiling);
    }

    /** The steps in force for whatever is running, or the ceiling while idle. */
    public int effectiveOverclock() {
        return this.activeRecipe == null
                ? reachableOverclock() : this.activeOverclock;
    }

    /**
     * Energy one tick costs at {@code steps} overclocks.
     *
     * <p>Each step quadruples the rate while halving the run time, so one craft costs twice as much
     * in total. Exact, with no ceiling: the figure only has to be comparable while deciding how
     * many copies and steps fit, and capping it there would quietly make a huge machine behave like
     * a merely large one. Where an {@code int} is unavoidable — the FE API — the value is saturated
     * at the point of use instead.</p>
     */
    private static BigInteger overclockedCost(BigInteger baseCost, int steps) {
        return baseCost.multiply(BigInteger.valueOf(4).pow(steps));
    }

    /**
     * Run time in ticks at {@code steps} overclocks, never below one.
     *
     * <p>The floor is the whole point of the "more overclock only costs more" rule: once a craft is
     * down to a single tick there is nothing left to halve, so further steps keep raising the bill
     * and leave the speed alone.</p>
     */
    private static int overclockedLength(int baseLength, int steps) {
        int length = baseLength;
        for (int step = 0; step < steps; step++) {
            length = Math.max(1, length >> 1);
        }
        return Math.max(1, length);
    }

    /** Applies the pinned overclock to a freshly started craft. */
    private void applyOverclock(MatchedMultiblockRecipe recipe) {
        // The record's int is already saturated, so the exact bill is rebuilt from runCost, which
        // parallelRecipe filled in. A batch does not change the bill, so the parallel figure carries
        // straight over; when nothing was parallelised runCost is zero and the record is used.
        if (this.runCost.signum() <= 0) {
            this.runCost = BigInteger.valueOf(recipe.energyPerTick());
        }

        BigInteger ceiling = maxParallelPower();
        int steps = reachableOverclock();
        while (steps > 0 && overclockedCost(this.runCost, steps).compareTo(ceiling) > 0) {
            steps--;
        }

        this.activeOverclock = steps;
        this.maxProgress = overclockedLength(recipe.processingTime(), this.activeOverclock);
        this.progress = 0;
        this.setChanged();
        syncToClient();
    }

    /**
     * Stores the overclock ceiling.
     *
     * <p>Clamped rather than validated: anything below {@code -1} has no meaning, so it collapses to
     * the unlimited value instead of being rejected — a typo in the box should leave the machine
     * usable, not silently inert.</p>
     */
    public void setOverclockLimit(int limit) {
        int sane = Math.max(limit, NO_OVERCLOCK_LIMIT);
        if (this.overclockLimit == sane) {
            return;
        }
        this.overclockLimit = sane;
        setChanged();
        syncToClient();
    }

    /**
     * True when this entity was just read back from disk and has not yet re-scanned its shape.
     *
     * <p>The scan that runs after a load looks exactly like the scan after a player finishes
     * building the machine, but the two must not do the same thing: a machine switched off by hand
     * has to stay off. This flag is what tells them apart — set in {@link #loadAdditional}, cleared
     * the first time {@link #formStructure} succeeds.</p>
     */
    private boolean restoredFromDisk;

    public GTChemicalReactorMachineBlockEntity(BlockPos pos, BlockState state) {
        this(GTChemicalReactorMultiblock.MACHINE_ENTITY.get(), pos, state);
    }

    /**
     * The constructor a second machine uses.
     *
     * <p>Everything below this line is shared; a subclass exists only to answer the four
     * questions in {@link #getPatternId}, {@link #getMachineId}, {@link #casingBlock} and
     * {@link #getDisplayName}.</p>
     */
    protected GTChemicalReactorMachineBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected ResourceLocation getPatternId() {
        return ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, getMachineId());
    }

    @Override
    public String getMachineId() {
        return MACHINE_ID;
    }

    /**
     * The wall block this machine's shape is built from.
     *
     * <p>Read here rather than compared inline so a subclass can answer with its own casing without
     * the structure check needing to know which machine it is running for.</p>
     */
    protected Block casingBlock() {
        return GTMultiblockBlocks.SHELL_PE.get();
    }

    /** The translation key this machine is named by when the player has not renamed it. */
    protected String nameKey() {
        return "block.goldentweaks.chemical_plant_controller";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return this.customName != null ? this.customName : Component.translatable(nameKey());
    }

    @Override
    public @NotNull Component getName() {
        return getDisplayName();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.customName;
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null;
    }

    public void setCustomName(@Nullable Component customName) {
        this.customName = customName;
        setChanged();
        // The GUI title comes from the client copy of this tile, so push the name right away
        // instead of waiting for the chunk to be resent.
        syncToClient();
    }

    /**
     * The machine does not buffer energy of its own.
     *
     * <p>Energy lives in the energy input ports, and AMI already sums those in
     * {@code getStoredInputEnergy} and {@code getInputEnergyCapacity}. A zero sized internal
     * buffer keeps its own contribution at zero, so the GUI trough shows the ports only �?which
     * is empty when the structure has no energy port at all.</p>
     */
    @Override
    protected int getEnergyCapacity() {
        return 0;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        // Catch the player up on the running craft: the product broadcast only fires on transitions,
        // so without this the output list would stay empty until the current batch ended.
        if (player instanceof ServerPlayer serverPlayer) {
            sendProductsTo(serverPlayer);
        }
        return GTChemicalReactorMenu.create(menuType(), containerId, inventory, this);
    }

    /**
     * The window type this machine opens.
     *
     * <p>A hook because the menu type decides which screen the client builds: opening the advanced
     * kettle with the basic type would ask for a window registered under a different id, and the
     * GUI would not appear.</p>
     */
    protected MenuType<?> menuType() {
        return GTChemicalReactorMultiblock.MENU.get();
    }

    /**
     * Whether a recipe may be looked for.
     *
     * <p>The switch gates only <em>starting</em> work: a recipe already running is allowed to
     * finish after the machine is switched off, because stopping it mid-craft would silently eat
     * the inputs. The block state still flips to off immediately, so the GUI button and the texture
     * agree with the player's click.</p>
     */
    private boolean mayStartRecipe() {
        return this.running;
    }

    /**
     * Drops the current recipe as a power failure.
     *
     * <p>Called when a tick's energy draw cannot be met, or when the shape turns out to be broken.
     * Per the machine's contract this is destructive: the progress is reset, the already consumed
     * inputs are <em>not</em> returned, and nothing is produced. The machine also switches itself
     * off and does not restart on its own, so the player has to look at it.</p>
     */
    private void tripBreaker() {
        tripBreaker(GTReactorStatus.NO_ENERGY);
    }

    /**
     * Drops the current recipe and switches the machine off, recording why.
     *
     * <p>Per the machine's contract this is destructive: progress is reset, the already consumed
     * inputs are <em>not</em> returned, and nothing is produced. The machine also does not restart
     * on its own, so the player has to look at it.</p>
     */
    private void tripBreaker(GTReactorStatus reason) {
        this.activeRecipe = null;
        // Cleared with the craft, for the same reason completeRecipe clears them: applyOverclock
        // prices the next craft from runCost, so a leftover figure would be billed for a craft that
        // is no longer running.
        this.runCost = BigInteger.ZERO;
        this.runCopies = 1;
        this.progress = 0;
        this.maxProgress = 0;
        // Force a fresh search once the player switches the machine back on.
        this.lastRecipeSearchVersion = -1;
        this.lastInputSignature = Integer.MIN_VALUE;
        this.running = false;
        this.stopReason = reason;
        // The craft is gone, so the ports are editable again.
        this.activeChannel = null;
        this.activeMode = null;
        this.selectedChannel = null;
        setChannelsLocked(false);
        setChanged();
        syncToClient();
        applyRunningState();
        // A tripped machine is no longer running anything, so the GUI stops listing outputs.
        this.activeProducts = List.of();
        this.activeProcessingTime = 0;
        broadcastProducts();
    }

    /**
     * What the status line should show right now.
     *
     * <p>The order encodes precedence: an unassembled machine never claims to be processing, and a
     * genuinely running craft outranks the plain idle look.</p>
     *
     * <p>{@link GTReactorStatus#STARTING} is deliberately not produced yet. It is reserved for the
     * warm-up phase (energy still flowing in before the first tick can be paid), which the screen
     * already knows how to draw; until that phase exists, a switched-on machine counts as idle.</p>
     */
    public GTReactorStatus getStatus() {
        if (!isFormed()) {
            return GTReactorStatus.BROKEN_STRUCTURE;
        }
        // The craft comes first. A switched-off machine still finishes the batch it already took
        // the inputs for, so reporting the switch's reason while it does would tell the player the
        // craft had been dropped when it is in fact still running.
        if (this.activeRecipe != null) {
            return GTReactorStatus.PROCESSING;
        }
        if (!this.running) {
            return this.stopReason;
        }
        if (this.refusedForCost) {
            return GTReactorStatus.INSUFFICIENT_ENERGY_HATCH;
        }
        return GTReactorStatus.IDLE;
    }

    /** Progress of the active recipe in ticks, or zero when nothing is running. */
    public int getRecipeProgress() {
        return this.activeRecipe == null ? 0 : this.progress;
    }

    /** Total length of the active recipe in ticks, or zero when nothing is running. */
    public int getRecipeLength() {
        return this.activeRecipe == null ? 0 : this.maxProgress;
    }

    /**
     * Drives the recipe, replacing AMI's per-tick rule with the kettle's own.
     *
     * <p>Three behaviours differ from the base implementation. Work is only picked up while the
     * switch is on. A failed energy draw is a hard failure instead of a silent wait �?AMI simply
     * returns and retries next tick, which would let a machine sit at "almost done" forever. And a
     * shape that is gone trips it at once, so a reaction cannot finish into a structure that no
     * longer exists.</p>
     *
     * <p>Energy is taken before progress, so it is "pay, then advance": a tick that cannot be paid
     * never advances the recipe.</p>
     */
    @Override
    protected void tickRecipe(Level level) {
        if (this.settleTicks > 0) {
            this.settleTicks--;
            return;
        }
        if (this.structure == null) {
            return;
        }

        if (this.activeRecipe == null) {
            // Off means no new work; it does not stop a craft that is already running.
            if (!mayStartRecipe()) {
                return;
            }
            // A player may have re-channeled a port since the last attempt. The cache is keyed on
            // contents, which a channel edit does not touch, so it has to be dropped here or the
            // machine would keep matching against the old grouping.
            invalidateIfGroupingChanged();
            // Chunk reloads and port changes can leave the shape stale; a stale shape must never
            // start a reaction.
            if (!this.structure.isFullyLoaded(level) || !this.structure.stillValid(level)) {
                return;
            }
            MatchedMultiblockRecipe found = findLockedRecipe(level);
            if (found == null) {
                return;
            }
            // Parallel first, then batch. Parallel multiplies the per-tick bill and leaves the
            // length alone, so it has to happen before the overclock looks for room in that bill;
            // batching only stretches a short recipe and costs nothing extra, so it goes last.
            MatchedMultiblockRecipe parallelised = parallelRecipe(found);
            MatchedMultiblockRecipe batched = batchRecipe(parallelised);
            if (!startRecipe(batched)) {
                // Not enough inputs or no room for the results; try again on a later tick.
                this.selectedChannel = null;
                this.selectedMode = null;
                return;
            }
            // Pin the group this recipe came from. Everything the craft does from here — drawing
            // energy, delivering products — stays inside it, so a later rearrangement of the ports
            // cannot move the results to a different production line.
            this.activeChannel = this.selectedChannel;
            this.activeMode = this.selectedMode;
            // A fresh shuffle for this craft only: the ports keep one order for its whole run, so
            // the inputs drawn and the outputs delivered stay consistent, but the next craft may
            // drain them in a different order.
            this.portShuffle = new Random(level.getRandom().nextLong());
            invalidatePortOrder();
            setChannelsLocked(true);
            // Inputs leave the ports as the craft starts, so what is in the machine always reflects
            // what is still owed. Charging at completion let a player pull the ingredients back out
            // mid-craft and still receive the products.
            consumeInputs(batched);
            // A machine that locked without a target adopts whatever it starts next, which is what
            // makes "lock now, decide later" work.
            if (this.lockRecipe && this.lockedRecipeId == null) {
                this.lockedRecipeId = recipeIdOf(batched);
                setChanged();
                syncToClient();
                broadcastRecipeLock();
            }
            // Only now is the reaction really running, so this is when clients start showing it.
            // The batched recipe is reported, so the listed amounts and rate match what is actually
            // consumed and produced by this run rather than the single-craft figures.
            this.activeProducts = productsOf(batched);
            this.activeProcessingTime = batched.processingTime();
            // Shortens the run and raises the per-tick bill. Done after startRecipe, which set the
            // unmodified length, so the two never disagree about which craft is running.
            applyOverclock(batched);
            // The texture follows the craft now, so it has to be refreshed the moment one begins —
            // there is no longer a standing "switch is on" state to make it look right by accident.
            applyRunningState();
            broadcastProducts();
            return;
        }

        // Mid-craft: a broken shape fails the batch immediately rather than at the next scan.
        if (!this.structure.stillValid(level)) {
            tripBreaker();
            invalidateStructure();
            return;
        }

        // Billed at the rate pinned when the craft started, which is what the overclock bought.
        BigInteger bill = activePowerDraw();
        if (bill.signum() > 0 && !consumeEnergy(bill)) {
            tripBreaker();
            return;
        }

        this.progress++;
        this.setChanged();
        if (this.progress < this.maxProgress) {
            return;
        }

        completeRecipe();
        // Falling through instead of returning means the next craft can start on this very tick.
        // Returning here cost a tick of idleness between every batch, which on a fast recipe is a
        // visible stutter in the output rate.
        if (!mayStartRecipe()) {
            return;
        }
        if (!this.structure.isFullyLoaded(level) || !this.structure.stillValid(level)) {
            return;
        }
        MatchedMultiblockRecipe next = findLockedRecipe(level);
        if (next == null) {
            return;
        }
        // Parallel first, then batch: parallel multiplies the per-tick bill and leaves the length
        // alone, so it has to be priced in before the overclock looks for room in that bill.
        MatchedMultiblockRecipe nextParallel = parallelRecipe(next);
        MatchedMultiblockRecipe nextBatched = batchRecipe(nextParallel);
        if (!startRecipe(nextBatched)) {
            this.selectedChannel = null;
            this.selectedMode = null;
            return;
        }
        this.activeChannel = this.selectedChannel;
        this.activeMode = this.selectedMode;
        this.portShuffle = new Random(level.getRandom().nextLong());
        invalidatePortOrder();
        setChannelsLocked(true);
        consumeInputs(nextBatched);
        this.activeProducts = productsOf(nextBatched);
        this.activeProcessingTime = nextBatched.processingTime();
        applyOverclock(nextBatched);
        applyRunningState();
        broadcastProducts();
    }

    /**
     * Whether the products of a finished recipe can be handed over.
     *
     * <p>Decided against the output ports only. AMI's own version also counts its internal output
     * buffer, which does not exist for this machine any more, so leaning on it would let a craft
     * start with nowhere to put the results.</p>
     *
     * <p>With "void excess" on this always says yes: whatever the ports cannot take is destroyed on
     * delivery, so a full output becomes a throughput decision rather than a stall.</p>
     */
    @Override
    protected boolean canOutput(MatchedMultiblockRecipe recipe) {
        if (this.voidOutput) {
            return true;
        }
        if (this.structure == null) {
            return false;
        }

        List<ItemStack> items = recipe.itemOutputs();
        List<FluidStack> fluids = recipe.fluidOutputs();
        // Every output handler is offered the whole product list at once, rather than each being
        // asked on its own. That is what makes the spill-over work: a product that does not fit the
        // first hatch is tried against the next. Asking per handler would refuse a craft that two
        // hatches can plainly hold between them.
        List<IItemHandler> itemHandlers = new ArrayList<>();
        List<IFluidHandler> fluidHandlers = new ArrayList<>();
        for (MultiblockPortBinding port : sortedPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = null;
                if (this.level != null) {
                    capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                }
                if (capability instanceof IItemHandler handler) {
                    itemHandlers.add(handler);
                }
            } else if (port.type() == MultiblockPortTypes.FLUID) {
                Object capability = null;
                if (this.level != null) {
                    capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
                }
                if (capability instanceof IFluidHandler handler) {
                    fluidHandlers.add(handler);
                }
            }
        }
        // Items have a list overload that already spans the hatches; fluids do not, so the spill is
        // simulated here the same way it is performed on delivery. Either way the question answered
        // is "does everything fit somewhere", not "does everything fit in one hatch".
        if (!MultiblockRecipeMatcher.canInsertItems(items, itemHandlers)
                || !fitsFluids(fluidHandlers, fluids)) {
            return false;
        }
        // The other two kinds have no handler in AMI's view, so their room is asked of the ports
        // directly. Without this a full essentia or chemical hatch would let the craft start and then
        // lose that part of the product.
        return typedOutputsFit(recipe);
    }

    /**
     * Whether the output ports have room for a recipe's essentia and chemicals.
     *
     * <p>Asked as "would the current contents plus the product still fit", which is the same question
     * the item and fluid checks answer. The port's own stacking rules decide whether a second kind may
     * join a tank, so this leans on the storage rather than re-implementing it.</p>
     */
    private boolean typedOutputsFit(MatchedMultiblockRecipe matched) {
        if (this.level == null || !(matched.recipe() instanceof GTChemicalReactionRecipe reaction)) {
            return true;
        }
        // The record is already scaled by everything that ran before this point — parallel and, when
        // applied, batch — so the figure is read from the recorded copies rather than multiplied
        // again here.
        int multiplier = copiesOf(matched);
        for (GTReactionStack product : reaction.outputs()) {
            if (product.kind() != GTReactionStack.Kind.ESSENTIA
                    && product.kind() != GTReactionStack.Kind.CHEMICAL) {
                continue;
            }
            long remaining = product.amount() * multiplier;
            for (GTPortBlockEntity stored : typedPorts(false)) {
                if (remaining <= 0) {
                    break;
                }
                if (product.kind() == GTReactionStack.Kind.ESSENTIA
                        && stored.kind().holdsEssentia()) {
                    remaining -= GTPortStorages.essentiaRoom(
                            stored.essentiaStorage(), product.id(), remaining);
                } else if (product.kind() == GTReactionStack.Kind.CHEMICAL
                        && stored.kind().holdsChemical()) {
                    remaining -= GTPortStorages.chemicalRoom(
                            stored.chemicalStorage(), product.id(), remaining);
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * The power the running craft actually draws each tick, after its overclock.
     *
     * <p>The figure the machine is really billed, not the recipe's authored cost: a craft at two
     * overclock steps pays sixteen times its base rate, and a tooltip quoting the base would be off
     * by that factor on every overclocked run. Zero while nothing is running.</p>
     */
    public BigInteger activePowerDraw() {
        if (this.activeRecipe == null) {
            return BigInteger.ZERO;
        }
        // The exact parallel/batch bill, not the record's int: a huge craft would otherwise be
        // billed as if its multiplier had been capped.
        return overclockedCost(this.runCost, this.activeOverclock);
    }

    private void completeRecipe() {
        MatchedMultiblockRecipe finished = this.activeRecipe;
        this.progress = 0;
        this.maxProgress = 0;
        this.activeRecipe = null;
        // Cleared with the craft: applyOverclock reads runCost to rebuild the bill, so a leftover
        // figure would price the next craft — which may be a different recipe — at this one's rate.
        this.runCost = BigInteger.ZERO;
        this.runCopies = 1;
        this.lastRecipeSearchVersion = -1;
        this.lastInputSignature = Integer.MIN_VALUE;

        if (finished != null) {
            deliver(finished.itemOutputs(), finished.fluidOutputs());
            deliverTyped(typedOutputsOf(finished));
        }
        this.setChanged();
        syncToClient();
        // A batch that finishes while the machine is switched off must drop the running look now.
        applyRunningState();
        // The craft is over, so no group owns the machine any more and the next search is free to
        // pick whichever one matches.
        this.activeChannel = null;
        this.activeMode = null;
        this.selectedChannel = null;
        setChannelsLocked(false);
        // The GUI stops listing the outputs as soon as nothing is running.
        this.activeProducts = List.of();
        this.activeProcessingTime = 0;
        broadcastProducts();
    }

    /**
     * Re-attaches the craft that was running when the world was saved.
     *
     * <p>The inputs were already taken when it started, so dropping it on load would destroy them
     * with nothing to show for it — the exact complaint this is here to answer. The recipe is looked
     * up by id: if a datapack removed it while the world was closed there is nothing to resume, and
     * the machine simply starts fresh.</p>
     *
     * <p>Progress is clamped to the stored length, which in turn is not trusted blindly: a stale
     * length larger than the recipe's own is replaced by the recipe's, so a hand-edited save cannot
     * make one craft run forever.</p>
     */
    private void resumePendingRecipe(Level level) {
        if (this.pendingRecipe == null) {
            return;
        }
        // Something already owns the machine — never overwrite a running craft with a stale one.
        if (this.activeRecipe != null) {
            this.pendingRecipe = null;
            return;
        }

        ResourceLocation wanted = this.pendingRecipe;
        // Walked rather than looked up by key: that is the same search the matcher already does, and
        // it keeps this independent of the recipe manager's lookup overloads.
        GTChemicalReactionRecipe found = null;
        for (RecipeType<GTChemicalReactionRecipe> type : recipeTypes()) {
            for (RecipeHolder<GTChemicalReactionRecipe> holder : level.getRecipeManager()
                    .getAllRecipesFor(type)) {
                if (holder.id().equals(wanted)) {
                    found = holder.value();
                    break;
                }
            }
            if (found != null) {
                break;
            }
        }
        if (found == null) {
            // Deliberately not cleared. A load hands the entity back before the recipe manager has
            // necessarily synced, so an empty result here means "ask again", not "the recipe is
            // gone" — giving up on the first tick would drop the craft on every single reload.
            if (++this.resumeAttempts < RESUME_ATTEMPTS) {
                return;
            }
            this.pendingRecipe = null;
            this.pendingProgress = 0;
            this.pendingLength = 0;
            this.pendingCopies = 1;
            return;
        }

        this.pendingRecipe = null;
        this.resumeAttempts = 0;

        MatchedMultiblockRecipe resumed = MatchedMultiblockRecipe.of(found);
        int length = resumed.processingTime();
        // The saved length is only trusted as far as the recipe agrees with it: the overclock that
        // produced it may no longer be reachable, and a longer run would never end.
        this.maxProgress = Math.max(1, Math.min(this.pendingLength, overclockedLength(length,
                Math.max(0, this.activeOverclock))));
        this.progress = Math.max(0, Math.min(this.pendingProgress, this.maxProgress));
        this.activeRecipe = resumed;
        // Billing is driven by runCost, and this path rebuilds the record from the recipe
        // definition, which knows nothing of the copies that were running, so both figures are
        // restored from the save. Leaving runCost at zero would resume the craft for free.
        this.runCopies = Math.max(1, this.pendingCopies);
        this.runCost = BigInteger.valueOf(resumed.energyPerTick())
                .multiply(BigInteger.valueOf(this.runCopies));
        // Pinned before the first tick can run, or the resumed craft would be billed against every
        // port on the machine instead of the ones it was matched with.
        this.activeChannel = this.pendingChannel;
        this.activeMode = this.pendingMode;
        this.selectedChannel = this.pendingChannel;
        this.selectedMode = this.pendingMode;
        this.pendingChannel = null;
        this.pendingMode = null;
        this.activeProducts = productsOf(resumed);
        this.activeProcessingTime = resumed.processingTime();
        this.pendingProgress = 0;
        this.pendingLength = 0;
        this.pendingCopies = 1;
        // The craft owns the ports again, so their channels are frozen exactly as they were.
        setChannelsLocked(true);
        this.setChanged();
        syncToClient();
        applyRunningState();
        broadcastProducts();
    }

    /**
     * Finds the recipe this machine may start, honouring the recipe lock and the channel groups.
     *
     * <p>Each channel group is tried in turn, and the first group that yields a match wins: the
     * machine then runs on that group alone, for both its energy and its ports, until the craft
     * ends. That ordering is what lets one machine hold several independent production lines, with
     * a port on channel {@code 5} staying idle while a channel {@code 1} recipe runs.</p>
     *
     * <p>The wildcard ports join every group, which is why the wildcard group is tried last: made
     * only of wildcard ports, it is a subset of every other group and can only match when no
     * narrower one did. It is still tried, because an all-wildcard machine has nothing else.</p>
     */
    @Nullable
    private MatchedMultiblockRecipe findLockedRecipe(Level level) {
        this.refusedForCost = false;
        this.refusedCost = 0;
        for (int channel : channelGroups()) {
            this.selectedChannel = channel;
            // Within a group the ports are ranked, and the modes are tried in that same ranking:
            // the most important port's mode gets the first attempt, then the next, and so on down.
            // A mode that yields nothing simply hands over to the one below it.
            for (int mode : modeOrder()) {
                this.selectedMode = mode;
                MatchedMultiblockRecipe found = matchOneGroup(level);
                if (found != null) {
                    return found;
                }
            }
        }
        this.selectedChannel = null;
        this.selectedMode = null;
        return null;
    }

    /** One group's attempt: the locked recipe if there is one, otherwise a full search. */
    @Nullable
    private MatchedMultiblockRecipe matchOneGroup(Level level) {
        if (!this.lockRecipe || this.lockedRecipeId == null) {
            return findRecipeForMode(level);
        }

        RecipeHolder<?> holder = level.getRecipeManager().byKey(this.lockedRecipeId).orElse(null);
        if (holder == null || !(holder.value() instanceof GTChemicalReactionRecipe recipe)) {
            return null;
        }
        // A locked recipe still has to belong to the mode being tried: the lock names a recipe, not
        // a permission to ignore the ports that make it.
        if (!modeAccepts(this.selectedMode, recipe.mode())) {
            return null;
        }
        // The lock names a recipe, not permission to drain a budget the ports no longer have — a
        // player who removes an energy hatch must not keep running the old, costlier recipe.
        if (!withinEnergyBudget(recipe.getEnergyPerTick())) {
            return null;
        }

        // Ask the recipe itself: a MatchedMultiblockRecipe only knows the plain ItemStack snapshot
        // taken when it was built, so a lock on an "any_components" recipe would never re-validate.
        MultiblockRecipeInput input = createRecipeInput();
        if (!recipe.matches(input, level)) {
            return null;
        }
        MatchedMultiblockRecipe matched = MatchedMultiblockRecipe.of(recipe);
        // The recipe's own matching covers items and fluids. The other two kinds are checked here, or
        // the machine would happily start a craft whose essentia or chemicals are not in the ports and
        // then produce nothing it was supposed to consume.
        return typedInputsAvailable(recipe, 1) ? matched : null;
    }

    /**
     * Whether the ports hold the essentia and chemicals a recipe asks for.
     *
     * @param multiplier how many crafts are being folded into one run
     */
    private boolean typedInputsAvailable(GTChemicalReactionRecipe reaction, int multiplier) {
        if (this.level == null) {
            return false;
        }
        for (GTReactionStack input : reaction.inputs()) {
            if (input.kind() != GTReactionStack.Kind.ESSENTIA
                    && input.kind() != GTReactionStack.Kind.CHEMICAL) {
                continue;
            }
            long needed = input.amount() * multiplier;
            long held = 0L;
            for (GTPortBlockEntity stored : typedPorts(true)) {
                if (input.kind() == GTReactionStack.Kind.ESSENTIA
                        && stored.kind().holdsEssentia()) {
                    held += GTPortStorages.essentiaAmount(stored.essentiaStorage(), input.id());
                } else if (input.kind() == GTReactionStack.Kind.CHEMICAL
                        && stored.kind().holdsChemical()) {
                    held += GTPortStorages.chemicalAmount(stored.chemicalStorage(), input.id());
                }
            }
            if (held < needed) {
                return false;
            }
        }
        return true;
    }

    /**
     * Searches the applied recipe type, skipping recipes whose mode the current mode cannot serve.
     *
     * <p>AMI's own search walks every recipe and stops at the first that matches the ports. Adding a
     * mode filter means the walk has to be ours: the recipe list is the same, but a candidate is
     * only tried when {@link #modeAccepts} allows it. Each candidate is asked to match itself, so
     * per-entry component rules survive the search.</p>
     */
    @Nullable
    private MatchedMultiblockRecipe findRecipeForMode(Level level) {
        MultiblockRecipeInput input = createRecipeInput();
        if (input.isEmpty()) {
            return null;
        }

        // Two passes, not one. A wildcard recipe is legal under every mode, so a single pass would
        // let a wildcard candidate win simply by sitting earlier in the recipe list — the mode was
        // "checked" but never actually preferred. Tried in order of specificity instead: recipes
        // written for this exact mode first, wildcard recipes only once those have all failed.
        Integer mode = this.selectedMode;
        if (mode != null && mode != GTPortChannels.WILDCARD) {
            MatchedMultiblockRecipe exact = matchRecipesOfMode(level, input, mode);
            if (exact != null) {
                return exact;
            }
        }
        return matchRecipesOfMode(level, input, null);
    }

    /**
     * Searches recipes of one mode.
     *
     * <p>{@code wanted} of {@code null} means "the wildcard band" — recipes marked mode {@code 0}.
     * A non-null value means exactly that mode, so a wildcard recipe is <em>not</em> included; it is
     * the caller's second pass that offers those.</p>
     */
    @Nullable
    private MatchedMultiblockRecipe matchRecipesOfMode(
            Level level, MultiblockRecipeInput input, @Nullable Integer wanted) {
        for (RecipeType<GTChemicalReactionRecipe> type : recipeTypes()) {
            for (RecipeHolder<GTChemicalReactionRecipe> holder : level.getRecipeManager()
                    .getAllRecipesFor(type)) {
                GTChemicalReactionRecipe recipe = holder.value();
                if (recipe.mode() != (wanted == null ? GTPortChannels.WILDCARD : wanted)) {
                    continue;
                }
                // A recipe the ports cannot power is not a candidate at all. Checked here rather
                // than at start so the search moves past it and finds something the machine can
                // actually run.
                if (!withinEnergyBudget(recipe.getEnergyPerTick())) {
                    // Remembered so the status line can say why nothing started. A recipe skipped
                    // here is indistinguishable from no recipe at all, otherwise.
                    this.refusedForCost = true;
                    // Zero doubles as "nothing recorded yet", so the first rejection wins outright.
                    this.refusedCost = this.refusedCost == 0
                            ? recipe.getEnergyPerTick()
                            : Math.min(this.refusedCost, recipe.getEnergyPerTick());
                    continue;
                }
                // MatchedMultiblockRecipe is built from a recipe's plain ItemStack inputs and matches
                // only against that snapshot, so it can never honour a recipe's own matching. Asking
                // the recipe itself is what lets "any_components" entries be found at all.
                if (!recipe.matches(input, level)) {
                    continue;
                }
                // 物品和流体在上一行已经查过，但配方还可能要求要素或化学品，而 AMI 的匹配器
                // 只认前两者。漏掉这一句，端口里没有要素时配方照样会开始，然后在
                // consumeTypedInputs 里找不到货、静默跳过，最后把成品交出来 —— 表现就是
                // 「机器忽略了要素把配方跑完」。这里和 matchOneGroup 必须用同一个判据。
                if (!typedInputsAvailable(recipe, 1)) {
                    continue;
                }
                return MatchedMultiblockRecipe.of(recipe);
            }
        }
        return null;
    }

    /**
     * Whether a recipe's mode is eligible while {@code mode} is the one being tried.
     *
     * <p>Two wildcards meet here. A recipe marked mode {@code 0} serves every mode, and a {@code 0}
     * mode is itself "any": so a wildcard recipe runs under any mode, and the wildcard mode accepts
     * any recipe. A {@code null} mode means no filter is in force at all.</p>
     */
    private static boolean modeAccepts(@Nullable Integer mode, int recipeMode) {
        if (mode == null || mode == GTPortChannels.WILDCARD) {
            return true;
        }
        return recipeMode == GTPortChannels.WILDCARD || recipeMode == mode;
    }

    /** The id of a matched recipe, or {@code null} when the manager does not know it. */
    @Nullable
    private ResourceLocation recipeIdOf(MatchedMultiblockRecipe matched) {
        // Identity comparison is safe here: the manager holds one instance per recipe until the
        // next reload, and a reload also clears the lock's meaning for the old instance.
        if (this.level != null) {
            for (RecipeType<GTChemicalReactionRecipe> type : recipeTypes()) {
                for (RecipeHolder<GTChemicalReactionRecipe> holder : this.level.getRecipeManager()
                        .getAllRecipesFor(type)) {
                    if (holder.value() == matched.recipe()) {
                        return holder.id();
                    }
                }
            }
        }
        return null;
    }

    /**
     * The energy sources a reaction may draw from: the ports of the group in force.
     *
     * <p>Filtered by channel for the same reason the item and fluid handlers are: an energy port on
     * channel {@code 5} must not power a channel {@code 1} recipe. AMI's own implementation walks
     * every energy input port without asking which line it belongs to, so the totals it reports —
     * and the charge {@code consumeEnergy} draws — have to come from this list instead.</p>
     *
     * <p>An assembly is included or not by its own switch, and by nothing else: a player who leaves
     * one in the shape and turns it on means it to supply power, whatever else they placed beside
     * it. Read per call rather than cached, since the switch and the channel fields may both change
     * at any time.</p>
     */
    @Override
    public List<IEnergyStorage> getEnergyInputHandlers() {
        List<IEnergyStorage> handlers = new ArrayList<>();
        if (this.level == null || this.structure == null) {
            return handlers;
        }
        for (MultiblockPortBinding port : sortedPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.ENERGY) {
                continue;
            }
            if (this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity
                    && entity.kind() == GTPortKind.GENERAL
                    && !entity.joinsEnergyChannel()) {
                // Switched out of the power path: the part still holds and passes contents, it
                // just contributes no charge to this line.
                continue;
            }
            Object capability = MultiblockPortTypes.ENERGY.capability(this.level, port.pos());
            if (capability instanceof IEnergyStorage handler) {
                handlers.add(handler);
            }
        }
        return handlers;
    }

    /**
     * The item sources a reaction may draw from: the ports only, never an internal buffer.
     *
     * <p>AMI's base implementation puts its own item buffer first and appends the ports. That buffer
     * is filled from the ports by {@code tickPorts} and drawn down by crafting, which turns a simple
     * "feed the machine" into a two-stage shuffle with its own capacity and its own copy of the
     * contents. It also hides where the amounts actually come from, so a craft could start on
     * buffered items the player never placed in a port this tick.</p>
     *
     * <p>This machine stores everything in its ports: what the player puts in an input hatch is
     * exactly what a recipe sees, and what comes out is pushed straight to the output hatches. The
     * buffer is left unused rather than removed, because the field belongs to AMI and its own code
     * still touches it.</p>
     */
    @Override
    public List<IItemHandler> getItemInputHandlers() {
        List<IItemHandler> handlers = new ArrayList<>();
        if (this.level == null || this.structure == null) {
            return handlers;
        }
        for (MultiblockPortBinding port : sortedPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.ITEM) {
                continue;
            }
            Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
            if (capability instanceof IItemHandler handler) {
                handlers.add(handler);
            }
        }
        return handlers;
    }

    /** The fluid sources a reaction may draw from: the ports only, for the same reason. */
    @Override
    public List<IFluidHandler> getFluidInputHandlers() {
        List<IFluidHandler> handlers = new ArrayList<>();
        if (this.level == null || this.structure == null) {
            return handlers;
        }
        for (MultiblockPortBinding port : sortedPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.FLUID) {
                continue;
            }
            Object capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
            if (capability instanceof IFluidHandler handler) {
                handlers.add(handler);
            }
        }
        return handlers;
    }

    /**
     * Removes a finished recipe's inputs from the ports.
     *
     * <p>Called at completion rather than at start, so an interrupted craft costs nothing. Nothing
     * here can fail in normal play — the same amounts were matched when the craft began and the
     * ports were only read meanwhile — but the matcher's return value is still respected so a
     * partial removal is never paired with a full delivery.</p>
     */
    private void consumeInputs(MatchedMultiblockRecipe recipe) {
        if (this.level == null || this.structure == null) {
            return;
        }

        List<IItemHandler> itemHandlers = getItemInputHandlers();
        List<IFluidHandler> fluidHandlers = getFluidInputHandlers();

        // Items first: if they cannot all be taken there is nothing sensible to do with the fluids,
        // and the ports are left untouched for the next attempt.
        if (!consumeItems(recipe, itemHandlers)) {
            return;
        }
        MultiblockRecipeMatcher.consumeFluids(recipe.fluidInputs(), fluidHandlers);
        consumeTypedInputs(recipe);
    }

    /**
     * Takes a recipe's item inputs from the ports, honouring each entry's component rule.
     *
     * <p>Walks the handlers twice: the plan decides how many units come from which slot, then the
     * extraction actually performs it. Planning against a snapshot rather than extracting as it goes
     * keeps a half-taken craft from being left behind when the last entry turns out to be short.</p>
     */
    private boolean consumeItems(MatchedMultiblockRecipe recipe, List<IItemHandler> handlers) {
        List<ItemStack> pool = new ArrayList<>();
        for (IItemHandler handler : handlers) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                pool.add(handler.getStackInSlot(slot));
            }
        }

        int[] plan = GTReactionMatcher.plan(itemInputsOf(recipe), Math.max(1, this.runCopies), pool);
        if (plan == null) {
            return false;
        }

        int flat = 0;
        for (IItemHandler handler : handlers) {
            for (int slot = 0; slot < handler.getSlots(); slot++, flat++) {
                if (plan[flat] <= 0) {
                    continue;
                }
                handler.extractItem(slot, plan[flat], false);
            }
        }
        return true;
    }

    /**
     * The machine's own ports that can hold essentia or chemicals, in the order delivery follows.
     *
     * <p>Walked from the layout rather than from AMI's port list: that list holds only the kinds AMI
     * recognises, and these ports are our own block. It is also the same walk
     * {@link #applyCasingToPorts} uses, so a port that shows the machine's casing is exactly a port
     * its products can reach.</p>
     *
     * @param input {@code true} for ports that feed the machine, {@code false} for those that receive
     */
    private List<GTPortBlockEntity> typedPorts(boolean input) {
        if (this.level == null || this.structure == null) {
            return List.of();
        }
        List<GTPortBlockEntity> ports = new ArrayList<>();
        for (BlockPos pos : shapeCells()) {
            if (!(this.level.getBlockEntity(pos) instanceof GTPortBlockEntity port)) {
                continue;
            }
            if (port.kind() != GTPortKind.ESSENTIA && port.kind() != GTPortKind.CHEMICAL
                    && port.kind() != GTPortKind.GENERAL) {
                continue;
            }
            if ((port.mode() == PortMode.INPUT) != input) {
                continue;
            }
            ports.add(port);
        }
        return ports;
    }

    /**
     * Removes the essentia and chemicals a craft was matched against.
     *
     * <p>AMI's inputs carry only items and fluids, so these two kinds are drawn from the ports'
     * own storage. The amounts were already checked when the craft began, exactly like the items, so
     * this cannot come up short in normal play.</p>
     */
    private void consumeTypedInputs(MatchedMultiblockRecipe recipe) {
        if (this.level == null || !(recipe.recipe() instanceof GTChemicalReactionRecipe reaction)) {
            return;
        }
        int multiplier = copiesOf(recipe);
        for (GTReactionStack input : reaction.inputs()) {
            if (input.kind() != GTReactionStack.Kind.ESSENTIA
                    && input.kind() != GTReactionStack.Kind.CHEMICAL) {
                continue;
            }
            long remaining = input.amount() * multiplier;
            for (GTPortBlockEntity stored : typedPorts(true)) {
                if (remaining <= 0) {
                    break;
                }
                if (input.kind() == GTReactionStack.Kind.ESSENTIA
                        && stored.kind().holdsEssentia()) {
                    remaining -= GTPortStorages.takeEssentia(
                            stored.essentiaStorage(), input.id(), remaining);
                } else if (input.kind() == GTReactionStack.Kind.CHEMICAL
                        && stored.kind().holdsChemical()) {
                    remaining -= GTPortStorages.takeChemical(
                            stored.chemicalStorage(), input.id(), remaining);
                }
            }
        }
    }

    /**
     * Hands the products to the output ports.
     *
     * <p>There is no buffer to fall back on, so capacity is decided by the ports alone — which is
     * what makes a full output port actually stop the machine instead of quietly filling an
     * invisible tank behind it.</p>
     */
    private void deliver(List<ItemStack> items, List<FluidStack> fluids) {
        if (this.structure == null) {
            return;
        }

        // The same shape as the capacity check: every hatch is offered the whole list, so a product
        // spills over into the next one instead of loading up the first and dropping the rest. The
        // check and the delivery have to agree here, or a craft that passed would still lose output.
        List<IItemHandler> itemHandlers = new ArrayList<>();
        List<IFluidHandler> fluidHandlers = new ArrayList<>();
        for (MultiblockPortBinding port : sortedPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = null;
                if (this.level != null) {
                    capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                }
                if (capability instanceof IItemHandler handler) {
                    itemHandlers.add(handler);
                }
            } else if (port.type() == MultiblockPortTypes.FLUID) {
                Object capability = null;
                if (this.level != null) {
                    capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
                }
                if (capability instanceof IFluidHandler handler) {
                    fluidHandlers.add(handler);
                }
            }
        }
        // No list overload exists for insertion, so each hatch is filled in turn and whatever it
        // could not take is carried on to the next. Same order as the capacity check, which is the
        // point: a craft that passed must not silently drop output here.
        List<ItemStack> remainingItems = items;
        for (IItemHandler handler : itemHandlers) {
            if (remainingItems.isEmpty()) {
                break;
            }
            remainingItems = insertAndLeftover(handler, remainingItems);
        }
        List<FluidStack> remainingFluids = fluids;
        for (IFluidHandler handler : fluidHandlers) {
            if (remainingFluids.isEmpty()) {
                break;
            }
            remainingFluids = fillAndLeftover(handler, remainingFluids);
        }

        // Anything still left had no room; with "void excess" that is the requested behaviour, and
        // without it the craft would not have started.
    }

    /**
     * Hands the typed products — essentia and chemicals — to the output ports.
     *
     * <p>Kept beside {@link #deliver} rather than folded into it because AMI's matched recipe only
     * carries items and fluids: those two kinds reach the ports through its own handler interfaces,
     * while these reach the port's own storage. Splitting them here is what keeps this machine able to
     * produce all four kinds without AMI having to know about two of them.</p>
     *
     * <p>Each port is offered the whole remainder in turn, the same spill-over rule the items use, so a
     * product that does not fit one hatch moves on to the next.</p>
     */
    private void deliverTyped(List<GTReactionStack> products) {
        if (this.structure == null) {
            return;
        }
        for (GTReactionStack product : products) {
            if (product.kind() != GTReactionStack.Kind.ESSENTIA
                    && product.kind() != GTReactionStack.Kind.CHEMICAL) {
                continue;
            }
            long remaining = product.amount();
            for (GTPortBlockEntity stored : typedPorts(false)) {
                if (remaining <= 0) {
                    break;
                }
                if (product.kind() == GTReactionStack.Kind.ESSENTIA
                        && stored.kind().holdsEssentia()) {
                    remaining -= GTPortStorages.addEssentia(
                            stored.essentiaStorage(), product.id(), remaining);
                } else if (product.kind() == GTReactionStack.Kind.CHEMICAL
                        && stored.kind().holdsChemical()) {
                    remaining -= GTPortStorages.addChemical(
                            stored.chemicalStorage(), product.id(), remaining);
                }
            }
        }
    }

    /**
     * Runs as many copies of a recipe at once as the hatches can power.
     *
     * <p><b>Parallel is not batch.</b> A batch merges N copies into one longer run at unchanged
     * power, so the same energy is spent over N times the ticks. Parallel runs N copies
     * <em>simultaneously</em>: the craft takes exactly as long as one, and the cost is multiplied by
     * N. That is why this happens before {@link #batchRecipe}, and before the overclock is pinned —
     * the boosted power is what the overclock then has to fit into.</p>
     *
     * <p>The number of copies is bounded by three things: the player's parallel ceiling, the power
     * the hatches can deliver ({@link #maxRecipeCost()}), and how many copies the inputs and output
     * space actually allow. The first two are checked here arithmetically, the last through
     * {@link #fits}, because stacking rules and port space are only known to the matcher.</p>
     *
     * <p>Down-counting rather than dividing: {@code fits} is the authority on how far a multiplier
     * can go, and a machine asked for 64 copies should not loop 64 times over a recipe that only
     * allows two.</p>
     */
    private MatchedMultiblockRecipe parallelRecipe(MatchedMultiblockRecipe recipe) {
        // Reset first: a recipe that ends up un-parallelised must not inherit the previous craft's
        // copies or bill.
        this.runCopies = 1;
        this.runCost = BigInteger.ZERO;

        int wanted = this.parallelLimit == ALWAYS_MAX_PARALLEL
                ? Integer.MAX_VALUE : this.parallelLimit;
        // A machine may cap parallelism on its own, below whatever the player asked for: the
        // advanced kettle grants four copies per voltage tier it can reach. Zero means its class
        // imposes no ceiling, which is the base kettle.
        int machineCeiling = machineParallelCeiling();
        if (machineCeiling > 0) {
            wanted = Math.min(wanted, machineCeiling);
        }
        int affordable = affordableParallelism(recipe.energyPerTick());
        int limit = (int) Math.min(wanted, affordable);
        if (limit <= 1) {
            return recipe;
        }

        int multiplier = Math.min(limit, maxFeasibleMultiplier(recipe, limit));
        if (multiplier <= 1) {
            return recipe;
        }

        // Same merge-then-scale shape as a batch, but the length is untouched: the copies run at the
        // same time, so the only figure that grows is the power bill.
        List<ItemStack> inputs = mergeItems(scaleItems(recipe.itemInputs(), multiplier));
        List<FluidStack> fluidInputs = mergeFluids(scaleFluids(recipe.fluidInputs(), multiplier));
        List<ItemStack> outputs = mergeItems(scaleItems(recipe.itemOutputs(), multiplier));
        List<FluidStack> fluidOutputs = mergeFluids(scaleFluids(recipe.fluidOutputs(), multiplier));

        // The true bill is kept alongside the record. AMI's field is an int, so a large multiplier
        // could not be stored there; the exact figure lives both in runCost (which billing uses) and
        // on the record itself through the exact-energy hook, so anything else reading the recipe
        // sees the same number.
        BigInteger cost = BigInteger.valueOf(recipe.energyPerTick())
                .multiply(BigInteger.valueOf(multiplier));
        this.runCopies = multiplier;
        this.runCost = cost;

        MatchedMultiblockRecipe scaled = new MatchedMultiblockRecipe(recipe.recipe(), inputs,
                fluidInputs, outputs, fluidOutputs, recipe.processingTime(), saturate(cost));
        // Through Object: the record is final and does not declare the interface, so javac would
        // reject a direct cast even though the mixin adds it at runtime.
        ((GTExactEnergy) (Object) scaled).gt$setExactEnergy(cost);
        return scaled;
    }

    /** An exact figure as the {@code int} an API insists on, saturated rather than wrapped. */
    private static int saturate(BigInteger value) {
        if (value.compareTo(BIG_INT_MAX) >= 0) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, value.intValue());
    }

    /**
     * How many copies of a recipe the energy hatches can power at once.
     *
     * <p><b>Parallel raises the current, not the voltage.</b> The tier decides which recipes a
     * machine may run at all — that is {@link #maxRecipeCost()}, and for a lone hatch it is only
     * half the rated power because the rest of the tick is the craft's own draw. Running N copies
     * at once does not make the machine any better at high tiers; it just pulls more current at the
     * same voltage. So the budget here is the hatches' whole rated throughput, which for a single
     * hatch is twice {@code maxRecipeCost} and for a pair is the same figure.</p>
     *
     * <p>That distinction is what a future 64 A hatch would exploit: it could never admit a
     * higher-tier recipe, but it could run a great many cheap ones in parallel.</p>
     */
    private int affordableParallelism(int baseCostPerTick) {
        if (baseCostPerTick <= 0) {
            return Integer.MAX_VALUE;
        }
        BigInteger budget = maxParallelPower();
        if (budget.signum() <= 0) {
            return 1;
        }
        BigInteger copies = budget.divide(BigInteger.valueOf(baseCostPerTick));
        return copies.compareTo(BIG_INT_MAX) >= 0 ? Integer.MAX_VALUE : Math.max(1, copies.intValue());
    }

    /** {@code Integer.MAX_VALUE} as a {@code BigInteger}, to keep the comparisons readable. */
    private static final BigInteger BIG_INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    /**
     * The throughput the energy hatches can supply to parallel copies, in FE per tick.
     *
     * <p>One top-tier hatch is worth its full rated transfer here, where the recipe ceiling only
     * counts half of it; two or more are worth twice that, matching
     * {@link #maxRecipeCost()}'s dual-hatch boost so the two stay consistent.</p>
     */
    public BigInteger maxParallelPower() {
        EnergyCensus census = energyPortCensus();
        if (census.topTier() == 0) {
            return BigInteger.ZERO;
        }
        BigInteger single = GTPortEnergyTiers.transferExact(census.topTier());
        return census.topCount() >= 2 ? single.shiftLeft(1) : single;
    }

    /**
     * Scales a short reaction up so one run lasts at least the configured minimum.
     *
     * <p>The multiplier is the smallest whole number that reaches the floor, which is what makes a
     * batch "just long enough" instead of needlessly long. It is then lowered as far as the machine
     * actually allows: inputs on hand and free output space both cap how many crafts can be folded
     * into one run. That lowering is what keeps the feature usable at all — a recipe asking for a
     * stack of inputs could otherwise never start.</p>
     *
     * <p>When the multiplier lands on one, the recipe is returned untouched, so a long reaction and
     * a machine without batch mode behave exactly as before.</p>
     *
     * <p>Runs after {@link #parallelRecipe} deliberately: parallel multiplies the power bill, and a
     * batch does not, so folding copies together first and then multiplying the price would charge
     * for copies that were never actually run at once.</p>
     */
    private MatchedMultiblockRecipe batchRecipe(MatchedMultiblockRecipe recipe) {
        if (!this.batchMode) {
            return recipe;
        }

        int baseTime = Math.max(1, recipe.processingTime());
        int floorTicks = GoldenTweaksCommonConfig.CHEMICAL_REACTOR_BATCH_MIN_TICKS.get();
        if (baseTime >= floorTicks) {
            return recipe;
        }

        int wanted = (int) Math.ceil(floorTicks / (double) baseTime);
        int multiplier = Math.min(wanted, maxFeasibleMultiplier(recipe, wanted));
        if (multiplier <= 1) {
            return recipe;
        }

        // Merged, not merely scaled: a batch that produces four stacks of one item should present as
        // one entry of four hundred, otherwise the list, the space check and the delivery all see a
        // scatter of identical rows competing for separate slots.
        // The typed multiplier accumulates with parallel's: a batch of a parallel run is copies x
        // batch, and essentia has to follow exactly that.
        this.runCopies = (int) Math.min(Integer.MAX_VALUE, (long) this.runCopies * multiplier);
        List<ItemStack> inputs = mergeItems(scaleItems(recipe.itemInputs(), multiplier));
        List<FluidStack> fluidInputs = mergeFluids(scaleFluids(recipe.fluidInputs(), multiplier));
        List<ItemStack> outputs = mergeItems(scaleItems(recipe.itemOutputs(), multiplier));
        List<FluidStack> fluidOutputs = mergeFluids(scaleFluids(recipe.fluidOutputs(), multiplier));

        // Energy per tick is untouched: the run is longer, so the total cost grows with it, which is
        // what a bigger batch should cost.
        return new MatchedMultiblockRecipe(recipe.recipe(), inputs, fluidInputs,
                outputs, fluidOutputs, baseTime * multiplier, recipe.energyPerTick());
    }

    /**
     * Largest multiplier the machine can actually run, capped at {@code wanted}.
     *
     * <p>The search is narrowed twice before anything is tried. A division over the ports gives the
     * most copies the ingredients could ever cover — an upper bound, since entries may compete for
     * one stack and output space is ignored. Then the range is halved instead of walked: "does this
     * batch fit" is monotone in the multiplier, because every demand and every product grows with
     * it, so if a candidate fits, so does everything below it.</p>
     *
     * <p>Halving matters when the ports are short — the common case is a batch of one, which a
     * countdown reaches only after trying every rejected value first.</p>
     */
    private int maxFeasibleMultiplier(MatchedMultiblockRecipe recipe, int wanted) {
        if (wanted <= 1) {
            return 1;
        }
        MultiblockRecipeInput available = createRecipeInput();
        long coverable = GTReactionMatcher.capacity(itemInputsOf(recipe),
                Math.max(1, this.runCopies), available.getItems(), available.getFluids());
        // A recipe whose inputs all resolve to "no ceiling" reports Long.MAX_VALUE. Multiplying that
        // out would build a scaled copy list large enough to exhaust the heap, so the batch is capped
        // at a size the machines are physically able to run anyway.
        coverable = Math.min(coverable, MAX_SEARCHED_MULTIPLIER);
        int high = (int) Math.min(wanted, Math.min(coverable, Integer.MAX_VALUE));
        if (high <= 1) {
            return 1;
        }
        if (fits(recipe, high)) {
            return high;
        }

        // The search below treats 1 as a known fit, so it is checked rather than assumed: output
        // space can still refuse the plain batch, and answering 1 for a batch that cannot run at all
        // would start a craft that never finishes.
        if (!fits(recipe, 1)) {
            return 1;
        }

        // Invariant: low is known to fit, high is known not to. Closing the gap leaves low as the
        // answer, and every probe is a real fit check — no candidate is skipped on the way down.
        int low = 1;
        while (high - low > 1) {
            int middle = (low + high) >>> 1;
            if (fits(recipe, middle)) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return low;
    }

    /**
     * Whether the ports hold the recipe's item inputs.
     *
     * <p>Goes through {@link GTReactionMatcher} rather than AMI's matcher: a reaction may ask for an
     * item by id alone (a used coolant cell), which an exact-component comparison cannot express.</p>
     *
     * <p>The counts are the recipe's own, so the multiplier is parallel times the batch candidate —
     * the record's item lists were already scaled and cannot be used here without losing the
     * per-entry component rules.</p>
     */
    private boolean matchesItems(MatchedMultiblockRecipe matched, int batchCandidate,
                                 MultiblockRecipeInput available) {
        long copies = (long) Math.max(1, this.runCopies) * Math.max(1, batchCandidate);
        return GTReactionMatcher.plan(itemInputsOf(matched), copies, available.getItems()) != null;
    }

    /**
     * The matched recipe's item inputs, with the parallel and batch copies already folded in.
     *
     * <p>Read from the recipe rather than from the matched record so the per-entry component rules
     * survive; the record's item lists were flattened to plain stacks when it was built.</p>
     */
    private static List<GTReactionStack> itemInputsOf(MatchedMultiblockRecipe matched) {
        if (matched.recipe() instanceof GTChemicalReactionRecipe reaction) {
            return reaction.inputs();
        }
        // A recipe this machine did not define: fall back to the flattened item list, which matches
        // exactly — the only rule an unknown recipe type can carry.
        List<GTReactionStack> inputs = new ArrayList<>();
        for (ItemStack stack : matched.itemInputs()) {
            if (!stack.isEmpty()) {
                inputs.add(new GTReactionStack(GTReactionStack.Kind.ITEM,
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()),
                        stack.getCount(), java.util.Optional.empty()));
            }
        }
        return inputs;
    }

    /** The matched recipe's fluid inputs, already scaled. */
    private static List<GTReactionStack> fluidInputsOf(MatchedMultiblockRecipe matched) {
        if (matched.recipe() instanceof GTChemicalReactionRecipe reaction) {
            return reaction.inputs();
        }
        List<GTReactionStack> inputs = new ArrayList<>();
        for (FluidStack stack : matched.fluidInputs()) {
            if (!stack.isEmpty()) {
                inputs.add(new GTReactionStack(GTReactionStack.Kind.FLUID,
                        net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(stack.getFluid()),
                        stack.getAmount(), java.util.Optional.empty()));
            }
        }
        return inputs;
    }

    /** Whether every scaled input is available and every scaled output would fit. */
    private boolean fits(MatchedMultiblockRecipe recipe, int multiplier) {
        MultiblockRecipeInput available = createRecipeInput();
        if (!matchesItems(recipe, multiplier, available)) {
            return false;
        }
        if (!GTReactionMatcher.matchesFluids(
                fluidInputsOf(recipe),
                (long) Math.max(1, this.runCopies) * Math.max(1, multiplier),
                available.getFluids())) {
            return false;
        }

        List<ItemStack> outputs = mergeItems(scaleItems(recipe.itemOutputs(), multiplier));
        List<FluidStack> fluidOutputs = mergeFluids(scaleFluids(recipe.fluidOutputs(), multiplier));

        // With "void excess" on, output space no longer caps the batch: the surplus is destroyed on
        // delivery, so the multiplier is limited by inputs alone. The typed inputs still have to be
        // on hand, though — those are consumed, not spawned.
        if (this.voidOutput) {
            return fitsTypedInputs(recipe, multiplier);
        }

        // Item and fluid output ports, resolved the same way AMI resolves them: through the
        // structure's own port bindings, since the helper it uses internally is private. Both kinds
        // are checked here — a full fluid tank stalls the machine exactly like a full item slot,
        // and there is no buffer behind them to absorb the surplus any more.
        List<IItemHandler> itemHandlers = new ArrayList<>();
        List<IFluidHandler> fluidHandlers = new ArrayList<>();
        for (MultiblockPortBinding port : sortedPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = null;
                if (this.level != null) {
                    capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                }
                if (capability instanceof IItemHandler handler) {
                    itemHandlers.add(handler);
                }
            } else if (port.type() == MultiblockPortTypes.FLUID) {
                Object capability = null;
                if (this.level != null) {
                    capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
                }
                if (capability instanceof IFluidHandler handler) {
                    fluidHandlers.add(handler);
                }
            }
        }
        // Offered together, so a product may land in whichever hatch has room for it. Asking per
        // hatch would refuse a craft the hatches can plainly hold between them.
        if (!MultiblockRecipeMatcher.canInsertItems(outputs, itemHandlers)
                || !fitsFluids(fluidHandlers, fluidOutputs)) {
            return false;
        }
        // The other two kinds cap the batch the same way: a hatch already holding something else
        // cannot take a scaled-up product, and finding that out after the craft started would lose
        // it. Batch mode is therefore limited by them too, and by the parallel copies already
        // folded into the recipe — hence totalCopies, not just the batch candidate.
        return fitsTypedOutputs(recipe, multiplier) && fitsTypedInputs(recipe, multiplier);
    }

    /**
     * The total number of copies a fit check has to reserve typed space for.
     *
     * <p>{@code batchCandidate} is only the batch under consideration; the recipe has already been
     * scaled by parallel, so the true multiple is that field times this candidate. Using only the
     * candidate would let a recipe pass the check and then overrun the hatches when it finishes.</p>
     */
    private int totalCopies(int batchCandidate) {
        long total = (long) Math.max(1, this.runCopies) * Math.max(1, batchCandidate);
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    /** Whether the scaled essentia and chemical products fit the output ports. */
    private boolean fitsTypedOutputs(MatchedMultiblockRecipe matched, int batchCandidate) {
        if (!(matched.recipe() instanceof GTChemicalReactionRecipe reaction)) {
            return true;
        }
        if (this.level == null) {
            return false;
        }
        // Parallel multiplied the recipe before this check ran, so the reservation is the batch
        // candidate times what was already running.
        int multiplier = totalCopies(batchCandidate);
        for (GTReactionStack product : reaction.outputs()) {
            if (product.kind() != GTReactionStack.Kind.ESSENTIA
                    && product.kind() != GTReactionStack.Kind.CHEMICAL) {
                continue;
            }
            long remaining = product.amount() * multiplier;
            for (GTPortBlockEntity stored : typedPorts(false)) {
                if (remaining <= 0) {
                    break;
                }
                if (product.kind() == GTReactionStack.Kind.ESSENTIA
                        && stored.kind().holdsEssentia()) {
                    remaining -= GTPortStorages.essentiaRoom(
                            stored.essentiaStorage(), product.id(), remaining);
                } else if (product.kind() == GTReactionStack.Kind.CHEMICAL
                        && stored.kind().holdsChemical()) {
                    remaining -= GTPortStorages.chemicalRoom(
                            stored.chemicalStorage(), product.id(), remaining);
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    /** Whether the scaled essentia and chemical inputs are on hand. */
    private boolean fitsTypedInputs(MatchedMultiblockRecipe matched, int batchCandidate) {
        return matched.recipe() instanceof GTChemicalReactionRecipe reaction
                && typedInputsAvailable(reaction, totalCopies(batchCandidate));
    }

    /**
     * Whether every fluid in {@code stacks} fits across {@code handlers}, spilling as needed.
     *
     * <p>Simulated rather than performed: each tank is asked what it would accept, and the accepted
     * amount is deducted before the next tank is tried. The order matches delivery, so a craft that
     * passes here really does fit.</p>
     */
    private static boolean fitsFluids(List<IFluidHandler> handlers, List<FluidStack> stacks) {
        List<FluidStack> remaining = new ArrayList<>();
        for (FluidStack stack : stacks) {
            if (!stack.isEmpty()) {
                remaining.add(stack.copy());
            }
        }
        for (IFluidHandler handler : handlers) {
            if (remaining.isEmpty()) {
                break;
            }
            List<FluidStack> next = new ArrayList<>();
            for (FluidStack stack : remaining) {
                // One call against the whole handler, the same way delivery fills it: it already
                // spans its own tanks, so looping over them would only re-ask the first.
                int accepted = handler.fill(stack,
                        IFluidHandler.FluidAction.SIMULATE);
                if (accepted < stack.getAmount()) {
                    next.add(stack.copyWithAmount(stack.getAmount() - accepted));
                }
            }
            remaining = next;
        }
        return remaining.isEmpty();
    }

    /**
     * Inserts as much of {@code stacks} as {@code handler} will take, and returns the rest.
     *
     * <p>The accepted amount is measured by simulation first, because {@code insertItem} reports
     * leftovers per slot rather than per list: it is called slot by slot, and each call is given the
     * stack's running remainder. That is also how a single-cell handler refuses a kind it already
     * holds elsewhere, so the simulation honours the mode for free.</p>
     */
    private static List<ItemStack> insertAndLeftover(IItemHandler handler, List<ItemStack> stacks) {
        List<ItemStack> leftover = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remaining = stack.copy();
            for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
                remaining = handler.insertItem(slot, remaining, false);
            }
            if (!remaining.isEmpty()) {
                leftover.add(remaining);
            }
        }
        return leftover;
    }

    /** Fluid counterpart of {@link #insertAndLeftover}. */
    private static List<FluidStack> fillAndLeftover(IFluidHandler handler, List<FluidStack> stacks) {
        List<FluidStack> leftover = new ArrayList<>();
        for (FluidStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            // One call, not one per tank. The handler already spans its own tanks — filling it in a
            // loop just asked the same first tank for room again and again, so an entry larger than
            // a single tank could never spread across the empty ones beside it.
            int accepted = handler.fill(stack,
                    IFluidHandler.FluidAction.EXECUTE);
            if (accepted < stack.getAmount()) {
                leftover.add(stack.copyWithAmount(stack.getAmount() - accepted));
            }
        }
        return leftover;
    }

    /**
     * Folds entries of the same item into one, summing their counts.
     *
     * <p>Not the same thing as merging stacks down to the slot limit: this keeps the total in a
     * single entry so the rest of the machine reasons about "how much of this item" rather than
     * about how many rows it happened to be written as.</p>
     */
    private static List<ItemStack> mergeItems(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            boolean folded = false;
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    existing.setCount((int) Math.min(Integer.MAX_VALUE,
                            (long) existing.getCount() + stack.getCount()));
                    folded = true;
                    break;
                }
            }
            if (!folded) {
                merged.add(stack.copy());
            }
        }
        return merged;
    }

    /** Fluid counterpart of {@link #mergeItems}. */
    private static List<FluidStack> mergeFluids(List<FluidStack> stacks) {
        List<FluidStack> merged = new ArrayList<>();
        for (FluidStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            boolean folded = false;
            for (FluidStack existing : merged) {
                if (existing.is(stack.getFluid())) {
                    existing.setAmount((int) Math.min(Integer.MAX_VALUE,
                            (long) existing.getAmount() + stack.getAmount()));
                    folded = true;
                    break;
                }
            }
            if (!folded) {
                merged.add(stack.copy());
            }
        }
        return merged;
    }

    /** Multiplies item amounts, leaving empty entries alone. */
    private static List<ItemStack> scaleItems(List<ItemStack> stacks, int multiplier) {
        List<ItemStack> scaled = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack copy = stack.copy();
            // Saturating multiply: a huge batch must not wrap into a negative or tiny count.
            copy.setCount((int) Math.min(Integer.MAX_VALUE, (long) stack.getCount() * multiplier));
            scaled.add(copy);
        }
        return scaled;
    }

    /** Multiplies fluid amounts, leaving empty entries alone. */
    private static List<FluidStack> scaleFluids(List<FluidStack> stacks, int multiplier) {
        List<FluidStack> scaled = new ArrayList<>(stacks.size());
        for (FluidStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            FluidStack copy = stack.copy();
            copy.setAmount((int) Math.min(Integer.MAX_VALUE, (long) stack.getAmount() * multiplier));
            scaled.add(copy);
        }
        return scaled;
    }

    /**
     * Turns a running recipe into the plain list the GUI draws.
     *
     * <p>Read here, on the server, so the screen never needs to know any recipe class. Our own
     * reaction carries typed outputs that can include fluids and chemicals; any other recipe type
     * AMI matches is flattened from its item and fluid outputs instead, which keeps the list correct
     * even for a recipe this machine did not define.</p>
     */
    private List<GTReactionStack> productsOf(MatchedMultiblockRecipe matched) {
        // Our own reaction carries typed outputs, so it is used directly: scaling it is a
        // multiplication, where flattening it would lose the essentia and chemicals entirely — those
        // have no item or fluid form to flatten into.
        if (matched.recipe() instanceof GTChemicalReactionRecipe reaction) {
            List<GTReactionStack> authored = reaction.outputs();
            int multiplier = copiesOf(matched);
            if (multiplier <= 1) {
                return authored;
            }
            List<GTReactionStack> scaled = new ArrayList<>(authored.size());
            for (GTReactionStack stack : authored) {
                scaled.add(new GTReactionStack(stack.kind(), stack.id(),
                        stack.amount() * multiplier, stack.tag()));
            }
            return scaled;
        }

        List<GTReactionStack> flattened = new ArrayList<>();
        for (ItemStack stack : matched.itemOutputs()) {
            if (!stack.isEmpty()) {
                flattened.add(new GTReactionStack(GTReactionStack.Kind.ITEM,
                        BuiltInRegistries.ITEM.getKey(stack.getItem()),
                        stack.getCount(), Optional.empty()));
            }
        }
        for (FluidStack stack : matched.fluidOutputs()) {
            if (!stack.isEmpty()) {
                flattened.add(new GTReactionStack(GTReactionStack.Kind.FLUID,
                        BuiltInRegistries.FLUID.getKey(stack.getFluid()),
                        stack.getAmount(), Optional.empty()));
            }
        }
        return flattened;
    }

    /**
     * The essentia and chemical products of a finished craft, scaled to its batch size.
     *
     * <p>Read from the recipe rather than from AMI's matched view, which carries only items and
     * fluids. The batch multiplier is recovered from the item totals because that view is where it
     * was recorded.</p>
     */
    private List<GTReactionStack> typedOutputsOf(MatchedMultiblockRecipe finished) {
        return productsOf(finished);
    }


    /**
     * How many copies of the recipe a matched run represents.
     *
     * <p>Read from {@link #runCopies}, which parallel and batch each multiply as they scale. Deriving
     * it from the item lists instead — the previous approach — worked only for recipes that had item
     * outputs at all, and left essentia and chemicals unscaled for a recipe made purely of them.
     * Typed amounts therefore follow exactly the same multiplier as items and fluids.</p>
     *
     * <p>Falls back to one when the run was not scaled, and to the old derivation when a caller is
     * looking at a record matched before this craft started: guessing high would over-deliver.</p>
     */
    private int copiesOf(MatchedMultiblockRecipe matched) {
        if (this.runCopies > 1) {
            return this.runCopies;
        }
        if (!(matched.recipe() instanceof GTChemicalReactionRecipe reaction)) {
            return 1;
        }
        long authored = 0L;
        for (GTReactionStack stack : reaction.outputs()) {
            if (stack.kind() == GTReactionStack.Kind.ITEM) {
                authored += stack.amount();
            }
        }
        long delivered = 0L;
        for (ItemStack stack : matched.itemOutputs()) {
            delivered += stack.getCount();
        }
        if (authored <= 0 || delivered <= authored) {
            return 1;
        }
        long ratio = delivered / authored;
        // A merged list can lose a few to rounding, so the ratio is only trusted when it divides
        // cleanly; otherwise the run is treated as a single craft rather than over-delivering.
        return ratio * authored == delivered ? (int) Math.min(ratio, Integer.MAX_VALUE) : 1;
    }

    /**
     * Mirrors the product list to everyone close enough to have the GUI open.
     *
     * <p>Sent on the transitions only �?started and finished �?because the list is constant for the
     * whole craft; the progress the GUI shows comes from the menu's own data slots. A player who
     * opens the GUI mid-craft is caught up by {@link #sendProductsTo}.</p>
     */
    private void broadcastProducts() {
        if (!(this.level instanceof ServerLevel server)) {
            return;
        }
        S2CReactionProductsPacket packet = new S2CReactionProductsPacket(
                this.worldPosition, this.activeProducts, this.activeProcessingTime);
        PacketDistributor.sendToPlayersNear(server, null,
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D,
                64.0D, packet);
    }

    /**
     * Sends the current product list to one player.
     *
     * <p>Called when the GUI opens: the broadcast above only fires on transitions, so a player who
     * opens the machine halfway through a craft would otherwise see an empty list until it ends.</p>
     */
    public void sendProductsTo(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new S2CReactionProductsPacket(
                this.worldPosition, this.activeProducts, this.activeProcessingTime));
        // The lock id is sent with the products so a player opening the GUI sees the pinned recipe
        // immediately, without waiting for the next lock change.
        PacketDistributor.sendToPlayer(player,
                new S2CRecipeLockPacket(this.worldPosition, this.lockedRecipeId));
    }

    /**
     * Mirrors the pinned recipe id, or its absence, to nearby clients.
     *
     * <p>Sent on the transitions only: the id cannot change while the lock holds, so there is nothing
     * to repeat every tick.</p>
     */
    private void broadcastRecipeLock() {
        if (!(this.level instanceof ServerLevel server)) {
            return;
        }
        S2CRecipeLockPacket packet =
                new S2CRecipeLockPacket(this.worldPosition, this.lockedRecipeId);
        PacketDistributor.sendToPlayersNear(server, null,
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D,
                64.0D, packet);
    }

    /**
     * Takes {@code amount} FE from the energy input ports, all-or-nothing.
     *
     * <p>The stored total is checked first so a shortfall never leaves a partial payment behind:
     * ports cannot be topped up within the same tick, and a half-paid tick would be lost energy.</p>
     */
    private boolean consumeEnergy(BigInteger amount) {
        if (amount.signum() <= 0) {
            return true;
        }
        // The store and the FE API both speak int, so a bill beyond that cannot be paid: refusing it
        // trips the breaker exactly like any other shortfall, which is the honest answer.
        if (amount.compareTo(BIG_INT_MAX) > 0) {
            return false;
        }
        int needed = amount.intValue();
        if (this.getStoredInputEnergy() < needed) {
            return false;
        }
        int remaining = needed;
        for (var handler : getEnergyInputHandlers()) {
            if (remaining <= 0) {
                break;
            }
            remaining -= handler.extractEnergy(remaining, false);
        }
        return remaining <= 0;
    }

    public boolean isRunning() {
        return this.running;
    }

    /**
     * Total energy the machine can accept per tick.
     *
     * <p>Summed from the same handlers AMI uses for its own totals, so the tooltip and the machine
     * can never disagree. Our own buffer is zero-sized, so this is exactly the sum over the energy
     * input ports.</p>
     */
    public int getInputEnergyRate() {
        long total = 0L;
        for (var handler : getEnergyInputHandlers()) {
            total += handler.getMaxEnergyStored() > 0 ? handler.receiveEnergy(Integer.MAX_VALUE, true) : 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    public int getStateFlags() {
        int flags = 0;
        if (this.lockRecipe) {
            flags |= GTChemicalReactorMenu.FLAG_LOCK_RECIPE;
        }
        if (this.voidOutput) {
            flags |= GTChemicalReactorMenu.FLAG_VOID_OUTPUT;
        }
        if (this.batchMode) {
            flags |= GTChemicalReactorMenu.FLAG_BATCH_MODE;
        }
        if (this.isolateInput) {
            flags |= GTChemicalReactorMenu.FLAG_ISOLATE_INPUT;
        }
        if (this.running) {
            flags |= GTChemicalReactorMenu.FLAG_RUNNING;
        }
        return flags;
    }

    /**
     * Turns the recipe lock on or off.
     *
     * <p>Switching it on pins the machine immediately if a reaction is under way, so pressing the
     * button mid-craft keeps that recipe; otherwise the lock stays open and the next recipe to start
     * is adopted. Switching it off forgets the pinned id, because a lock that is off must not
     * silently keep restricting the search.</p>
     */
    private void toggleRecipeLock() {
        this.lockRecipe = !this.lockRecipe;
        if (this.lockRecipe) {
            // Only a running craft can name the recipe to pin; an idle machine decides on its next
            // start, which tickRecipe handles while the id is still null.
            if (this.activeRecipe != null) {
                this.lockedRecipeId = recipeIdOf(this.activeRecipe);
            }
        } else {
            this.lockedRecipeId = null;
        }
        broadcastRecipeLock();
    }

    /** Id of the pinned recipe, or {@code null} when nothing is pinned. */
    @SuppressWarnings("unused")
    @Nullable
    public ResourceLocation getLockedRecipeId() {
        return this.lockedRecipeId;
    }

    /**
     * Handles one of the GUI buttons. Called from
     * {@link GTChemicalReactorMenu#clickMenuButton} on the server.
     */
    public boolean handleMenuButton(Player player, int button) {
        switch (button) {
            case GTChemicalReactorMenu.BUTTON_TOGGLE_LOCK -> toggleRecipeLock();
            case GTChemicalReactorMenu.BUTTON_TOGGLE_VOID -> this.voidOutput = !this.voidOutput;
            case GTChemicalReactorMenu.BUTTON_TOGGLE_BATCH -> this.batchMode = !this.batchMode;
            case GTChemicalReactorMenu.BUTTON_TOGGLE_ISOLATE -> {
                this.isolateInput = !this.isolateInput;
                // The grouping just changed, so whatever group the last search settled on is stale.
                // Dropping it forces a fresh search on the next tick instead of letting the machine
                // keep drawing from ports the new setting would not include.
                this.selectedChannel = null;
                this.lastInputSignature = Integer.MIN_VALUE;
                this.lastRecipeSearchVersion = -1;
            }
            case GTChemicalReactorMenu.BUTTON_TOGGLE_RUNNING -> {
                if (!toggleRunning()) {
                    return false;
                }
            }
            case GTChemicalReactorMenu.BUTTON_REFRESH -> refreshStructure();
            case GTChemicalReactorMenu.BUTTON_DISMANTLE -> {
                // Dismantling removes this tile, so there is nothing left to mark dirty or sync
                // afterwards; the block removal itself is what tells the client.
                return dismantleStructure(player);
            }
            default -> {
                return false;
            }
        }
        setChanged();
        syncToClient();
        return true;
    }

    /**
     * The on/off switch.
     *
     * <p>Switching on is only allowed when the structure is actually there: the switch performs a
     * check first and falls back to off when that fails, so the switch can never disagree with the
     * shape around the machine.</p>
     *
     * <p>Switching off takes effect immediately for everything the player can see �?the button
     * turns to pause and the texture repaints �?but a reaction already under way keeps running
     * until it finishes. That is deliberate: aborting mid-craft would destroy the inputs the
     * machine has already consumed, which is a much worse surprise than finishing the batch. Once
     * it completes, nothing new starts.</p>
     */
    private boolean toggleRunning() {
        if (this.running) {
            this.running = false;
            this.stopReason = GTReactorStatus.OFF;
            applyRunningState();
            return true;
        }

        // Switching on re-verifies the shape: the player may have finished it since the last
        // ten second scan, or broken it, and the switch must reflect which.
        refreshStructure();
        if (!isFormed()) {
            this.running = false;
            this.stopReason = GTReactorStatus.BROKEN_STRUCTURE;
            applyRunningState();
            return true;
        }
        this.running = true;
        // Clear the previous failure, so the status line describes the current attempt.
        this.stopReason = GTReactorStatus.OFF;
        applyRunningState();
        return true;
    }

    /**
     * Runs a structure check immediately and syncs the result.
     *
     * <p>Backs the refresh button: the player may have changed the energy ports or completed or
     * broken the shape since the last periodic scan, so both the assembly state and the energy
     * totals shown by the GUI are recomputed at once.</p>
     *
     * <p>AMI's {@code formStructure} returns {@code true} without rescanning when a structure is
     * already assembled and still valid, so the two cases have to be handled apart: an assembled
     * shape gets its port bindings rebuilt, and only an unassembled one is scanned for.</p>
     */
    public void refreshStructure() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        MultiblockStructure structure = getStructure();
        if (structure != null) {
            if (!structure.isFullyLoaded(this.level)) {
                // The chunk holding part of the shape is not loaded; leave the state untouched
                // rather than tearing the machine down over a transient condition.
                return;
            }
            if (structure.stillValid(this.level)) {
                // Shape is intact: re-read the ports so the energy trough and the fluid buffers
                // pick up anything the player changed.
                // refreshPorts can hand back null when the shape no longer reads as assembled. That
                // is not a transient state to ignore: the machine has to drop the stale assembly,
                // otherwise the refresh button would leave it claiming a shape that is gone.
                this.structure = structure.refreshPorts(this.level);
            } else {
                invalidateStructure();
            }
        } else {
            formStructure(null);
        }

        this.scanCountdown = STRUCTURE_CHECK_INTERVAL_TICKS;

        // The plates follow the shape, and a refresh is exactly when the shape may have changed
        // hands: a port swapped for another kind, or the whole machine rebuilt in place.
        if (this.structure != null) {
            applyCasingToPorts();
        }

        applyRunningState();
        setChanged();
        // Energy totals come from the port bindings, so the client needs a fresh copy.
        syncToClient();
    }

    /**
     * Builds the casing of this machine's structure from the player's inventory.
     *
     * <p>The orientation comes from the blockstate rather than from a freshly matched structure:
     * the whole point is to build the shape when it is not there yet, so there is nothing matched
     * to read it from. Ports are skipped by {@link GTMultiblockBuilder}, and the player's supply is
     * the only limit �?a partial build is still useful.</p>
     */
    public GTMultiblockBuilder.Result buildStructure(Player player) {
        if (this.level == null || this.level.isClientSide) {
            return new GTMultiblockBuilder.Result(0, 0);
        }

        GTMultiblockPattern layout = GTMultiblockStructures.layout(getPatternId());
        if (layout == null) {
            GoldenTweaks.LOGGER.warn("[GT] No layout registered for {}", getPatternId());
            return new GTMultiblockBuilder.Result(0, 0);
        }

        BlockState state = getBlockState();
        Direction facing = state.getValue(GTChemicalReactorMachineBlock.FACING);
        int roll = state.getValue(GTChemicalReactorMachineBlock.ROLL);
        boolean mirrored = state.getValue(GTChemicalReactorMachineBlock.MIRRORED);

        GTMultiblockBuilder.Result result = GTMultiblockBuilder.build(
                this.level, this.worldPosition, layout,
                GTChemicalReactorOrientation.patternForwards(facing),
                GTChemicalReactorOrientation.up(facing, roll), mirrored, player);

        if (result.placed() > 0) {
            // Whatever is already complete may now form, so re-check instead of waiting out the
            // ten second scan.
            refreshStructure();
        }
        return result;
    }

    /**
     * Puts the ports back to the standard plate when the shape no longer matches.
     *
     * <p>Called from the invalidation path rather than from teardown alone: a machine can come apart
     * because a wall was broken, and those ports are still in the world and must not keep a plate
     * that belongs to a shape that is gone.</p>
     */
    private void clearCasingFromPorts() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        for (BlockPos pos : this.restyledPorts) {
            BlockState portState = this.level.getBlockState(pos);
            if (portState.getBlock() instanceof GTPortBlock
                    && portState.hasProperty(GTPortBlock.CASING)
                    && portState.getValue(GTPortBlock.CASING) != 0) {
                this.level.setBlock(pos, portState.setValue(GTPortBlock.CASING, 0),
                        Block.UPDATE_ALL);
            }
        }
        this.restyledPorts.clear();
    }

    /**
     * Re-matches the assembly after the host was rotated or mirrored.
     *
     * <p>{@link #refreshStructure()} is not enough here: it only asks whether the shape it already
     * matched is still intact, and that check does not involve the facing at all, so a rotated
     * machine would happily keep claiming to be assembled in its old orientation. The stored match
     * therefore has to be dropped and the pattern matched again against the new facing, roll and
     * mirror.</p>
     *
     * <p>The drop is done directly rather than through {@code invalidateStructure}, because that
     * broadcasts an "invalidated" notice to nearby players; being told the machine fell apart
     * every time it is turned would be wrong.</p>
     */
    public void reorient() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        // Turning the machine drops the old shape; formStructure re-matches it below.
        this.structure = null;
        this.running = false;
        applyRunningState();

        // Matches again from scratch against the blockstate's new orientation. formStructure sets
        // running back on when the shape fits, and leaves it off when it does not.
        formStructure(null);

        this.scanCountdown = STRUCTURE_CHECK_INTERVAL_TICKS;
        setChanged();
        syncToClient();
    }

    /**
     * Tears the whole shape down, including this host, and hands every block to the player.
     *
     * <p>The blocks are turned into items directly instead of going through
     * {@code destroyBlock}, which would spawn loose drops on the ground: the player asked for the
     * parts to end up in their inventory, and {@link ItemPickupUtil} is what the rest of the mod
     * uses to funnel stacks there, leaving whatever does not fit on the floor.</p>
     *
     * <p>When {@code avaritia_more_items} is present �?and this class only exists then �?the
     * collected stacks are packed into better matter clusters first.</p>
     */
    private boolean dismantleStructure(Player player) {
        if (this.level == null || this.level.isClientSide) {
            return false;
        }

        MultiblockStructure structure = getStructure();
        if (structure == null) {
            // Not assembled: nothing around the host belongs to us, so only the host is taken down.
            // Removing the whole shape here would delete a player's walls, which is exactly what a
            // dismantle button must never do.
            return dismantleHostOnly(player);
        }

        // Capture the layout before invalidating: AMI clears its structure field in there.
        List<BlockPos> shape = new ArrayList<>(structure.getBlocksExcludingMaster());

        List<ItemStack> parts = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        // Before the blocks are removed: the handlers live on those block entities.
        collectPortContents(parts, fluids);
        for (BlockPos pos : shape) {
            collectBlock(parts, pos);
        }

        // The host itself is part of the haul, so it is packed with the rest instead of arriving
        // as a separate stack.
        parts.add(createHostStack());

        // Take the stored controller item along, otherwise breaking the host would drop it on the
        // ground separately from the rest of the parts.
        ItemStack stored = this.controllerSlot.getStackInSlot(0);
        if (!stored.isEmpty()) {
            parts.add(stored.copy());
            this.controllerSlot.setStackInSlot(0, ItemStack.EMPTY);
        }

        // Clear the assembly directly instead of calling invalidateStructure: that path also
        // broadcasts an "invalidated" message to nearby players, which would be misleading here
        // because the machine is being taken apart on purpose. The blocks go away below anyway.
        // The ports are unfrozen first, while their positions are still known: the blocks are about
        // to be removed, and a lock left on them would make the returned items useless.
        setChannelsLocked(false);
        clearCasingFromPorts();
        this.activeChannel = null;
        this.activeMode = null;
        this.selectedChannel = null;
        this.structure = null;
        this.running = false;

        // Hand everything over before the blocks go away, so this tile is still intact here.
        giveToPlayer(player, parts, fluids);

        // Remove the shape, host last: the host is this block entity, which stops existing.
        for (BlockPos pos : shape) {
            this.level.removeBlock(pos, false);
        }
        this.level.removeBlock(this.worldPosition, false);
        return true;
    }

    /**
     * Takes down a half-built machine: the host plus every block in the pattern area that belongs to
     * it.
     *
     * <p>A partially placed machine is the common case when a player changes their mind, so the
     * dismantle button has to work there too. The blocks are found by walking the pattern geometry
     * around the host and testing each cell's predicate, which is the same test formation uses. Only
     * cells whose predicate accepts the block are removed, so a wall that merely sits inside the
     * bounding box — or a player's own casing used as a filler where the pattern expects something
     * else — is left alone.</p>
     */
    private boolean dismantleHostOnly(Player player) {
        // The same detail scan the full path uses, so a half-built machine's hatches give back what
        // they hold instead of being removed with the contents still inside.
        List<BlockPos> area = partialShapeBlocks();
        List<ItemStack> parts = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        collectPortContents(area, parts, fluids);

        for (BlockPos pos : area) {
            collectBlock(parts, pos);
        }

        parts.add(createHostStack());

        ItemStack stored = this.controllerSlot.getStackInSlot(0);
        if (!stored.isEmpty()) {
            parts.add(stored.copy());
            this.controllerSlot.setStackInSlot(0, ItemStack.EMPTY);
        }

        this.running = false;
        giveToPlayer(player, parts, fluids);

        // Blocks go last, so the tile is still valid while the parts are being collected above.
        for (BlockPos pos : area) {
            if (this.level != null) {
                this.level.removeBlock(pos, false);
            }
        }
        if (this.level != null) {
            this.level.removeBlock(this.worldPosition, false);
        }
        return true;
    }

    /**
     * Positions in the pattern area that currently hold a block belonging to this machine.
     *
     * <p>Walks our own structure layout, the same one the builder fills in and the projector draws,
     * so a half-placed machine is identified by exactly the cells the player was asked to place.
     * Wildcard cells are skipped: they explicitly mean "anything goes", so whatever stands there is
     * the player's own and must survive.</p>
     *
     * <p>The host is skipped because the caller removes it separately, and air is skipped because
     * there is nothing to take.</p>
     */
    private List<BlockPos> partialShapeBlocks() {
        List<BlockPos> blocks = new ArrayList<>();
        if (this.level == null) {
            return blocks;
        }

        GTMultiblockPattern layout = GTMultiblockStructures.layout(getPatternId());
        if (layout == null) {
            return blocks;
        }

        BlockState state = getBlockState();
        Direction facing = state.getValue(GTChemicalReactorMachineBlock.FACING);
        int roll = state.getValue(GTChemicalReactorMachineBlock.ROLL);
        boolean mirrored = state.getValue(GTChemicalReactorMachineBlock.MIRRORED);

        Direction forwards = GTChemicalReactorOrientation.patternForwards(facing);
        Direction up = GTChemicalReactorOrientation.up(facing, roll);

        // Same anchor and same cell mapping as GTMultiblockBuilder, so the cells are the ones the
        // player was told to fill.
        GTMultiblockPattern.Master master = layout.master();
        BlockPos anchor = GTChemicalReactorOrientation.frontTopLeft(
                this.worldPosition, forwards, up, mirrored,
                master.column(),
                layout.height() - 1 - master.layer(),
                layout.depth() - 1 - master.row());

        for (int layer = 0; layer < layout.height(); layer++) {
            for (int row = 0; row < layout.depth(); row++) {
                for (int column = 0; column < layout.width(); column++) {
                    char symbol = layout.symbolAt(column, layer, row);
                    if (layout.isWildcard(symbol)) {
                        continue;
                    }
                    String key = layout.keyAt(symbol);
                    if (key == null) {
                        continue;
                    }
                    BlockPos pos = GTChemicalReactorOrientation.cell(
                            anchor, forwards, up, mirrored,
                            layout.height(), layout.depth(), column, layer, row);
                    if (pos.equals(this.worldPosition)) {
                        continue;
                    }
                    BlockState here = this.level.getBlockState(pos);
                    // Only blocks that actually satisfy this cell's key are ours to take. A player's
                    // spare dirt used as filler, or a wall standing inside the bounding box, does
                    // not match and stays where it is.
                    if (here.isAir() || !matchesKey(key, here)) {
                        continue;
                    }
                    blocks.add(pos);
                }
            }
        }
        return blocks;
    }

    /**
     * Whether {@code state} satisfies a structure key: a block id or a {@code #tag}.
     *
     * <p>Kept identical to how the matcher resolves keys, so a cell counted here is a cell AMI would
     * also accept — otherwise dismantling could take a block that formation never asked for.</p>
     */
    private static boolean matchesKey(String key, BlockState state) {
        if (key.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.tryParse(key.substring(1));
            return tagId != null
                    && state.is(TagKey.create(Registries.BLOCK, tagId));
        }
        ResourceLocation blockId = ResourceLocation.tryParse(key);
        return blockId != null
                && BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(blockId);
    }

    /**
     * The host's own item, carrying the anvil name the GUI title was showing.
     *
     * <p>Kept out of the matter cluster: a cluster cannot carry a custom name, and the name is
     * meant to survive taking the machine apart.</p>
     *
     * <p>The item comes from the block this tile sits in, not from a named constant: both kettles
     * share one block class but are two different blocks, and dismantling an advanced machine used
     * to hand back the basic controller.</p>
     */
    private ItemStack createHostStack() {
        ItemStack host = new ItemStack(getBlockState().getBlock().asItem());
        if (this.customName != null) {
            host.set(DataComponents.CUSTOM_NAME, this.customName);
        }
        return host;
    }

    private void collectBlock(List<ItemStack> parts, BlockPos pos) {
        if (this.level == null) {
            return;
        }
        BlockState state = this.level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        // No loot roll and no fortune: each block yields exactly one item. The NeoForge overload
        // takes the hit result, which is null here because nothing was actually mined.
        ItemStack stack = state.getBlock().getCloneItemStack(
                state, null, this.level, pos, null);
        if (stack.isEmpty()) {
            stack = new ItemStack(state.getBlock());
        }
        if (!stack.isEmpty()) {
            // A renamed port has to come back renamed. Breaking one by hand keeps its name through
            // the block's own drop, but the dismantle path builds the items itself from the block
            // state, which carries no components — so the name is copied across here.
            if (this.level.getBlockEntity(pos) instanceof GTPortBlockEntity port
                    && port.getCustomName() != null) {
                stack.set(DataComponents.CUSTOM_NAME,
                        port.getCustomName());
            }
            parts.add(stack);
        }
    }

    /**
     * Empties every port into two clusters: one for items, one for fluids.
     *
     * <p>The whole machine's contents are pooled first and packed once, so a dismantle yields one
     * item cluster and one fluid cluster rather than one per hatch — which is what "a better matter
     * cluster" means to a player: the machine comes back as a couple of spheres, not a wall of
     * separate stacks.</p>
     *
     * <p>Energy is deliberately not carried: a spent hatch comes back as the hatch alone. Charge has
     * no container item to travel in, and inventing one would let a player move power around as an
     * inventory item.</p>
     */
    private void collectPortContents(List<ItemStack> items, List<FluidStack> fluids) {
        if (this.level == null) {
            return;
        }
        List<BlockPos> positions = new ArrayList<>();
        if (this.structure != null) {
            for (MultiblockPortBinding port : this.structure.getPorts()) {
                positions.add(port.pos());
            }
        }
        collectPortContents(positions, items, fluids);
    }

    /**
     * Pools the contents of the ports at {@code positions}.
     *
     * <p>Driven by positions rather than by the assembled structure, because a half-built machine
     * has no structure object yet — only blocks that answer to the pattern. Both teardown paths find
     * their own positions and hand them here, so neither can forget the contents.</p>
     */
    private void collectPortContents(
            List<BlockPos> positions, List<ItemStack> items, List<FluidStack> fluids) {
        if (this.level == null) {
            return;
        }
        for (BlockPos pos : positions) {
            if (!(this.level.getBlockEntity(pos) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            if (entity.itemHandler() != null) {
                IItemHandler handler = entity.itemHandler();
                if (handler != null) {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        ItemStack stack = handler.getStackInSlot(slot);
                        if (!stack.isEmpty()) {
                            items.add(stack.copy());
                            handler.extractItem(slot, stack.getCount(), false);
                        }
                    }
                }
            }
            if (entity.fluidHandler() != null) {
                IFluidHandler handler = entity.fluidHandler();
                if (handler != null) {
                    for (int tank = 0; tank < handler.getTanks(); tank++) {
                        FluidStack held = handler.getFluidInTank(tank);
                        if (held.isEmpty()) {
                            continue;
                        }
                        FluidStack drained = handler.drain(held,
                                IFluidHandler.FluidAction.EXECUTE);
                        if (!drained.isEmpty()) {
                            fluids.add(drained);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sends the collected parts to the player, packed into better matter clusters.
     *
     * <p>Every part goes in the same cluster, the host included. The cluster is what "更好的物质球"
     * refers to, and letting the cluster item do the packing means the machine does not have to
     * reimplement its capacity handling.</p>
     */
    @SuppressWarnings("unused")
    private void giveToPlayer(@Nullable Player player, List<ItemStack> parts) {
        giveToPlayer(player, parts, List.of());
    }

    /**
     * Sends the collected parts, and whatever the ports held, packed into matter clusters.
     *
     * <p>Two problems, two clusters: items go into a better matter cluster, fluids into a fluid one.
     * One of each is the whole point of a dismantle — the contents of a dozen hatches come back as
     * two spheres rather than a spill of stacks.</p>
     */
    private void giveToPlayer(
            @Nullable Player player, List<ItemStack> parts, List<FluidStack> fluids) {
        if (this.level == null) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        if (!parts.isEmpty()) {
            List<ItemStack> packed = BetterMatterClusterItem.makeClusters(parts);
            stacks.addAll(packed.isEmpty() ? parts : packed);
        }
        if (!fluids.isEmpty()) {
            List<ItemStack> packed = FluidMatterClusterItem.makeClusters(fluids);
            stacks.addAll(packed.isEmpty() ? List.of() : packed);
        }
        if (stacks.isEmpty()) {
            return;
        }

        Vec3 origin = Vec3.atCenterOf(this.worldPosition);
        if (player != null) {
            for (ItemStack stack : stacks) {
                ItemPickupUtil.collect(player, stack, this.level, origin);
            }
            return;
        }

        // No player to give them to: leave them in the world at the machine instead of voiding.
        for (ItemStack stack : stacks) {
            Block.popResource(this.level, this.worldPosition, stack);
        }
    }

    /**
     * Drives both AMI's formed-side revalidation and our own unformed-side scan.
     *
     * <p>AMI also uses this value as the retry period when it restores a structure after a chunk
     * reload, which is why it is kept at a sensible value rather than something much larger.</p>
     */
    @Override
    protected int getStructureCheckInterval() {
        return STRUCTURE_CHECK_INTERVAL_TICKS;
    }

    /**
     * Makes the kettle look up reactions of its own recipe type.
     *
     * <p>AMI's base implementation returns an empty list, so without this override the machine
     * would assemble but never craft anything. The source mirrors recipes into AMI's
     * {@code MatchedMultiblockRecipe}, which already handles item and fluid consumption through the
     * structure's ports.</p>
     */
    @Override
    protected List<IMultiblockRecipeSource> getRecipeSources() {
        return recipeTypes().stream()
                .map(IMultiblockRecipeSource::multiblock)
                .toList();
    }

    /**
     * The recipe types this machine searches, in priority order.
     *
     * <p>A list rather than a single type because a machine may accept more than one set: the
     * advanced kettle runs its own recipes <em>and</em> the basic kettle's, so that upgrading a
     * build does not orphan every pattern the player already made.</p>
     */
    protected List<RecipeType<GTChemicalReactionRecipe>> recipeTypes() {
        return List.of(GTChemicalReactionRecipes.TYPE.get());
    }

    /**
     * The recipe type this machine reports to AMI.
     *
     * <p>AMI's recipe source takes one type, so this reports the first of {@link #recipeTypes()}.
     * Deriving it rather than declaring it separately keeps the two from disagreeing about which
     * set is "the" one.</p>
     */
    protected RecipeType<GTChemicalReactionRecipe> recipeType() {
        return recipeTypes().getFirst();
    }

    @Override
    protected void serverTick(Level level) {
        // Before the base tick, not after it. AMI's tick is what starts recipes, so resuming only
        // afterwards meant the machine had already committed ingredients to a fresh craft by the time
        // the saved one was looked for — and that is the batch that went missing.
        resumePendingRecipe(level);
        super.serverTick(level);

        // Keep the block state authoritative. AMI's AbstractConnectBlock.updateFormation also
        // writes the formed property on neighbouring shell-family blocks, and the machine is one
        // of them, so it can stamp the flag on a lone block as if it were a complete 1x1x1
        // structure. Whatever the scan decided is the truth, so reassert it here.
        applyRunningState();

        // super already revalidated an assembled shape on this same interval and invalidated it if
        // a block went missing, so the rescan only matters while the machine is unformed.
        if (isFormed()) {
            // An assembled machine still needs its plates kept current: a port swapped for another
            // kind keeps the plate of the one it replaced until something asks again, and nothing
            // else does while the shape holds.
            if (--this.scanCountdown > 0) {
                return;
            }
            this.scanCountdown = STRUCTURE_CHECK_INTERVAL_TICKS;
            applyCasingToPorts();
            return;
        }
        if (--this.scanCountdown > 0) {
            return;
        }
        this.scanCountdown = STRUCTURE_CHECK_INTERVAL_TICKS;

        // A null player keeps the scan silent: it runs unattended, so there is nobody to notify.
        formStructure(null);
    }

    /**
     * Assembles the machine, but only in the orientation the host itself is placed in.
     *
     * <p>AMI's {@code formStructure} calls {@code MultiblockStructure.scan}, which asks the
     * pattern to {@code find} a match and that walks all twenty-four orientations and both
     * mirrorings. For an asymmetric shape that means a host facing one way would happily form the
     * mirrored structure of the host next to it, and a host spun by a quarter turn would form a
     * shape that does not line up with its own front. Both are wrong: the machine may only form
     * the shape that corresponds to its own {@code facing}, {@code roll} and {@code mirrored}
     * blockstate.</p>
     *
     * <p>So the search is replaced with a single {@code matches} call for exactly one orientation.
     * The orientation is derived from the blockstate through
     * {@link GTChemicalReactorOrientation}, which keeps the block, the models and the matcher on
     * one convention.</p>
     */
    @Override
    public boolean formStructure(Player player) {
        if (this.level == null || this.level.isClientSide) {
            return false;
        }

        ResourceLocation patternId = getPatternId();
        if (patternId == null) {
            return false;
        }

        // Already assembled and intact: AMI would report this without rescanning, so do the same.
        MultiblockStructure current = getStructure();
        if (current != null && current.isFullyLoaded(this.level) && current.stillValid(this.level)) {
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.avaritia_more_items.multiblock.already_formed"), true);
            }
            return true;
        }

        AMiBlockPattern pattern = AMiBlockPatternReloadListener.INSTANCE.getPattern(patternId);
        if (pattern == null) {
            return false;
        }

        BlockState state = getBlockState();
        Direction facing = state.getValue(GTChemicalReactorMachineBlock.FACING);
        int roll = state.getValue(GTChemicalReactorMachineBlock.ROLL);
        boolean mirrored = state.getValue(GTChemicalReactorMachineBlock.MIRRORED);

        // AMI's `forwards` points from the structure toward the viewer, which is the opposite of
        // the way our `facing` looks; patternForwards is the single place that flip lives.
        Direction forwards = GTChemicalReactorOrientation.patternForwards(facing);
        Direction up = GTChemicalReactorOrientation.up(facing, roll);

        // `matches` is anchored at the pattern's front-top-left cell, not at the master cell. The
        // master position has to be translated there first, exactly as AMI's own search does.
        BlockPos anchor = GTChemicalReactorOrientation.frontTopLeft(
                this.worldPosition, forwards, up, mirrored,
                pattern.getMasterX(), pattern.getMasterY(), pattern.getMasterZ());

        BlockPatternMatch match = pattern.matches(this.level, anchor, forwards, up, mirrored);
        if (match == null) {
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.avaritia_more_items.multiblock.unformed"), true);
            }
            return false;
        }

        MultiblockStructure structure =
                MultiblockStructure.of(this.level, this.worldPosition, pattern, match);

        // The pattern alone is not enough. Its "x" cells accept the casing tag, which also holds the
        // ports, so a bare frame of ports would shape up without a single real casing — and the
        // "any" cells accept anything at all, a dynamo hatch included. Both are rejected here.
        Component shapeProblem = checkShapeMaterials(structure);
        if (shapeProblem != null) {
            if (player != null) {
                player.displayClientMessage(shapeProblem, true);
            }
            return false;
        }

        PortRequirement unsatisfied = structure.firstUnsatisfied(getPortRequirements());
        if (unsatisfied != null) {
            if (player != null) {
                player.displayClientMessage(Component.translatable(
                        "message.avaritia_more_items.multiblock.missing_port",
                        unsatisfied.describe()), true);
            }
            return false;
        }

        this.structure = structure;
        this.patternVersion = AMiBlockPatternReloadListener.INSTANCE.getVersion();
        this.structureCheckTick = getStructureCheckInterval();

        // The ports' plates follow the machine they are set into, and that choice lives in their own
        // block state. Written here, on the tick the shape first matches, so the wall is correct
        // before the player looks at it.
        applyCasingToPorts();
        setChanged();
        syncToClient();

        // Assembling switches the machine on — but only when the player just built it. The scan
        // that runs after a chunk load is indistinguishable from the one after the last block is
        // placed, so `restoredFromDisk` is what separates them: a machine read back from disk keeps
        // whatever the switch said, including "off", instead of switching itself on at every load.
        if (this.restoredFromDisk) {
            this.restoredFromDisk = false;
        } else {
            this.running = true;
        }
        // A fresh assembly is a fresh attempt; the previous failure no longer describes it.
        this.stopReason = GTReactorStatus.OFF;
        this.scanCountdown = STRUCTURE_CHECK_INTERVAL_TICKS;
        applyRunningState();
        return true;
    }

    /**
     * Ports whose plate this machine has changed.
     *
     * <p>Remembered because the revert runs after the structure is gone: walking the pattern again
     * at that moment would need a shape that no longer matches. Only ports this machine actually
     * touched are listed, so a port the player built into another machine is never disturbed.</p>
     */
    private final Set<BlockPos> restyledPorts = new HashSet<>();

    /**
     * Stamps this machine's casing onto every port of the current shape.
     *
     * <p>The casing is written onto each port's block entity, which is where the renderer reads it:
     * a block state's value set is closed, so it cannot name a casing from an open set, and the
     * model asks the entity through its model data.</p>
     *
     * <p>The cells are walked from our own layout rather than AMI's port list, because that list
     * holds only what AMI recognised as a port — a hopper or canister is a port by our rules and
     * would otherwise be skipped.</p>
     */
    private void applyCasingToPorts() {
        if (this.level == null || this.level.isClientSide || this.structure == null) {
            return;
        }
        int casingIndex = GTMultiblockBlocks.casingIndex(casingBlock());

        // Walked from our own layout rather than AMI's port list: that list holds only what AMI
        // matched as a port, and a hopper or canister is a port by our rules, not by AMI's.
        for (BlockPos pos : shapeCells()) {
            BlockState portState = this.level.getBlockState(pos);
            if (!(portState.getBlock() instanceof GTPortBlock)
                    || !portState.hasProperty(GTPortBlock.CASING)) {
                continue;
            }
            if (portState.getValue(GTPortBlock.CASING) == casingIndex) {
                continue;
            }
            // The state carries it: the connected-texture hook that picks a face's sheet is given
            // the state and nothing else.
            this.level.setBlock(pos, portState.setValue(GTPortBlock.CASING, casingIndex),
                    Block.UPDATE_ALL);
            this.restyledPorts.add(pos.immutable());
        }

    }

    /**
     * Every position the current shape occupies.
     *
     * <p>Read from the layout and the host's own orientation, which is the same mapping the
     * structure check and the projector use.</p>
     */
    private List<BlockPos> shapeCells() {
        GTMultiblockPattern layout = GTMultiblockStructures.layout(getPatternId());
        if (layout == null) {
            return List.of();
        }

        BlockState state = getBlockState();
        Direction facing = state.getValue(GTChemicalReactorMachineBlock.FACING);
        int roll = state.getValue(GTChemicalReactorMachineBlock.ROLL);
        boolean mirrored = state.getValue(GTChemicalReactorMachineBlock.MIRRORED);

        Direction forwards = GTChemicalReactorOrientation.patternForwards(facing);
        Direction up = GTChemicalReactorOrientation.up(facing, roll);

        GTMultiblockPattern.Master master = layout.master();
        BlockPos anchor = GTChemicalReactorOrientation.frontTopLeft(
                this.worldPosition, forwards, up, mirrored,
                master.column(),
                layout.height() - 1 - master.layer(),
                layout.depth() - 1 - master.row());

        List<BlockPos> cells = new ArrayList<>();
        for (int layer = 0; layer < layout.height(); layer++) {
            for (int row = 0; row < layout.depth(); row++) {
                for (int column = 0; column < layout.width(); column++) {
                    cells.add(GTChemicalReactorOrientation.cell(
                            anchor, forwards, up, mirrored,
                            layout.height(), layout.depth(), column, layer, row));
                }
            }
        }
        return cells;
    }

    /**
     * Why the assembled shape is still not buildable, or {@code null} when it is.
     *
     * <p>Driven by the source layout rather than by the matched structure, because the two cells
     * differ in kind and AMI's pattern has forgotten which is which by the time it is matched:</p>
     *
     * <ul>
     *   <li>a casing cell needs a real casing, and the casing tag also lists the ports, so a frame
     *       of nothing but buses would otherwise shape up;</li>
     *   <li>an "any" cell is a structural gap, meant for filler or for a port, and contributes
     *       nothing to the casing count — a casing dropped there is not one of the required eight;</li>
     *   <li>a dynamo hatch belongs to neither kind. It is not part of this machine, so it may stand
     *       in a gap but never in a casing cell.</li>
     * </ul>
     */
    @SuppressWarnings("unused")
    @Nullable
    private Component checkShapeMaterials(MultiblockStructure structure) {
        GTMultiblockPattern layout = GTMultiblockStructures.layout(getPatternId());
        if (this.level == null || layout == null) {
            return null;
        }

        BlockState state = getBlockState();
        Direction facing = state.getValue(GTChemicalReactorMachineBlock.FACING);
        int roll = state.getValue(GTChemicalReactorMachineBlock.ROLL);
        boolean mirrored = state.getValue(GTChemicalReactorMachineBlock.MIRRORED);
        Direction forwards = GTChemicalReactorOrientation.patternForwards(facing);
        Direction up = GTChemicalReactorOrientation.up(facing, roll);

        // Same anchor and same cell mapping the projector uses, so the cells judged here are the
        // ones the player was told to fill.
        GTMultiblockPattern.Master master = layout.master();
        BlockPos anchor = GTChemicalReactorOrientation.frontTopLeft(
                this.worldPosition, forwards, up, mirrored,
                master.column(),
                layout.height() - 1 - master.layer(),
                layout.depth() - 1 - master.row());

        int casings = 0;
        for (int layer = 0; layer < layout.height(); layer++) {
            for (int row = 0; row < layout.depth(); row++) {
                for (int column = 0; column < layout.width(); column++) {
                    char symbol = layout.symbolAt(column, layer, row);
                    // An "any" cell is a gap. Whatever stands there is filler and does not count.
                    if (layout.isWildcard(symbol)) {
                        continue;
                    }
                    BlockPos pos = GTChemicalReactorOrientation.cell(
                            anchor, forwards, up, mirrored,
                            layout.height(), layout.depth(), column, layer, row);
                    if (pos.equals(this.worldPosition)) {
                        continue;
                    }

                    BlockState here = this.level.getBlockState(pos);
                    if (isDynamoBlock(here)) {
                        // Only reachable in a casing cell: an "any" cell was already skipped, so a
                        // dynamo here really is standing in for a casing.
                        return Component.translatable(
                                "message.goldentweaks.chemical_reactor.no_dynamo");
                    }
                    if (here.is(casingBlock())) {
                        casings++;
                    }
                }
            }
        }
        if (casings < MIN_CASINGS) {
            return Component.translatable(
                    "message.goldentweaks.chemical_reactor.not_enough_casing",
                    MIN_CASINGS);
        }
        return null;
    }

    /** Whether a state is one of this mod's energy output hatches. */
    private static boolean isDynamoBlock(BlockState state) {
        return state.getBlock() instanceof GTPortBlock port
                && port.kind() == GTPortKind.ENERGY
                && port.mode() == PortMode.OUTPUT;
    }

    /**
     * Releases everything the machine holds onto outside itself, before the block goes away.
     *
     * <p>Called from the block's {@code onRemove}, which is the one path that bypasses every other
     * teardown: a player breaking the controller with a pickaxe never reaches
     * {@code invalidateStructure} or the dismantle code. Without this the ports would keep the
     * channel lock that was applied while a craft ran, and the returned port items would be
     * uneditable for good.</p>
     */
    public void onBlockRemoved() {
        setChannelsLocked(false);
        this.activeChannel = null;
        this.activeMode = null;
        this.selectedChannel = null;
    }

    /**
     * Drops the assembly without announcing it.
     *
     * <p>AMI's version broadcasts {@code multiblock.invalidated} to every player within 32 blocks.
     * The machine already shows that state through its texture �?it turns unconstructed �?so the
     * chat line is noise, and it would fire repeatedly on a busy machine. The bookkeeping AMI's
     * version performs is reproduced here so nothing else breaks: clearing the stored structure
     * and the recipe-tracking fields.</p>
     */
    @Override
    public void invalidateStructure() {
        if (this.structure == null) {
            return;
        }

        // Unfreeze the ports before the structure reference goes away: the lock is held on the port
        // entities, and after this line there is no longer a list of them to walk. Skipping this
        // left every port of a dismantled machine permanently uneditable.
        setChannelsLocked(false);
        // Same reason as the lock: the ports are still in the world and must go back to the plain
        // plate, which needs the positions remembered while this machine still knows them.
        clearCasingFromPorts();
        this.activeChannel = null;
        this.activeMode = null;
        this.selectedChannel = null;

        this.structure = null;
        // Reproduce AMI's own bookkeeping: leaving any of this behind would keep a matched recipe
        // (and its progress) alive for a machine that no longer has a shape.
        this.lastRecipeSearchVersion = -1;
        this.lastInputSignature = Integer.MIN_VALUE;
        this.activeRecipe = null;
        this.progress = 0;
        this.maxProgress = 0;

        // Losing the shape switches the machine off; the player has to switch it back on. The
        // blockstate change is also what tells the player, instead of a chat message. The status
        // line names the reason, so it does not read as if the player had pressed the button.
        this.running = false;
        this.stopReason = GTReactorStatus.BROKEN_STRUCTURE;
        applyRunningState();
        // Nothing can be running in a shape that is gone.
        this.activeProducts = List.of();
        this.activeProcessingTime = 0;
        broadcastProducts();
    }

    @Override
    public boolean dismantle(Player player) {
        return dismantleStructure(player);
    }

    /**
     * Mirrors the assembly and switch state onto the block, which is what selects the overlay.
     *
     * <p>Three combinations matter and the blockstate JSON maps each to one texture: unformed
     * always shows the unconstructed look regardless of the switch, formed and running shows
     * idle, and formed and switched off shows off.</p>
     *
     * <p>One exception: a machine switched off while a reaction is still finishing keeps the
     * running look. The button has already flipped to pause and no new work starts, so the texture
     * is the player's only indication that the last batch is still being processed; it drops to the
     * off look once that batch completes.</p>
     *
     * <p>{@code RUNNING} mirrors the switch, not the craft. Those are three different states and the
     * block state only has room for two of them, so the texture reports what a player glancing over
     * wants to know first: whether the machine is working again. Assembled and switched on shows the
     * idle look, assembled and switched off shows the off look, and the craft in progress is the
     * window's own business — its status line and progress bar already say so.</p>
     *
     * <p>A switched-off machine keeps the running look while its last batch finishes, because work
     * really is still happening.</p>
     */
    private void applyRunningState() {
        if (this.level == null) {
            return;
        }
        boolean formed = isFormed();
        // The switch, plus a craft still in flight after the switch went off.
        boolean running = formed && (this.running || this.activeRecipe != null);

        BlockState state = getBlockState();
        BlockState desired = state
                .setValue(GTChemicalReactorMachineBlock.FORMED, formed)
                .setValue(GTChemicalReactorMachineBlock.RUNNING, running)
                // Crafting is the state the texture's middle look is for: assembled and switched on
                // but with nothing to do reads as idle, and only a live reaction reads as working.
                .setValue(GTChemicalReactorMachineBlock.CRAFTING,
                        formed && this.activeRecipe != null);
        if (desired != state) {
            // Flag 2 is a plain client sync: no neighbour updates, so this cannot re-enter the
            // structure scan while the machine is tearing its own shape down.
            this.level.setBlock(this.worldPosition, desired, 2);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        // A reloaded chunk should re-resolve its assembly state on the next tick rather than
        // wait out a fresh period.
        this.scanCountdown = 1;

        readDisplayState(tag, registries);
        if (tag.contains(TAG_CONTROLLER)) {
            this.controllerSlot.deserializeNBT(registries, tag.getCompound(TAG_CONTROLLER));
        }
        this.lockRecipe = tag.getBoolean(TAG_LOCK_RECIPE);
        this.lockedRecipeId = tag.contains(TAG_LOCKED_RECIPE)
                ? ResourceLocation.tryParse(tag.getString(TAG_LOCKED_RECIPE))
                : null;
        this.voidOutput = tag.getBoolean(TAG_VOID_OUTPUT);
        this.batchMode = !tag.contains(TAG_BATCH_MODE) || tag.getBoolean(TAG_BATCH_MODE);
        // Absent means "on": machines saved before this switch existed never chose a value, and the
        // isolated behaviour is the one they were running with.
        this.isolateInput = !tag.contains(TAG_ISOLATE_INPUT) || tag.getBoolean(TAG_ISOLATE_INPUT);
        this.running = tag.getBoolean(TAG_RUNNING);
        this.overclockLimit = tag.contains(TAG_OVERCLOCK_LIMIT)
                ? tag.getInt(TAG_OVERCLOCK_LIMIT)
                : NO_OVERCLOCK_LIMIT;
        this.parallelLimit = tag.contains(TAG_PARALLEL_LIMIT)
                ? tag.getInt(TAG_PARALLEL_LIMIT)
                : ALWAYS_MAX_PARALLEL;
        // A craft that was in flight resumes where it stopped. Only the identity and the counters
        // are read here — the ports' contents were saved with the ports themselves, and the recipe
        // is looked up again once the shape exists, because there is no world to look it up in yet.
        this.pendingRecipe = tag.contains(TAG_ACTIVE_RECIPE)
                ? ResourceLocation.tryParse(tag.getString(TAG_ACTIVE_RECIPE))
                : null;
        this.pendingProgress = tag.getInt(TAG_ACTIVE_PROGRESS);
        this.pendingLength = tag.getInt(TAG_ACTIVE_LENGTH);
        this.activeOverclock = tag.getInt(TAG_ACTIVE_OVERCLOCK);
        // Saved with the craft: the record is rebuilt from the recipe definition on load, which
        // knows nothing of how many copies were running, so without this a resumed parallel craft
        // would quietly fall back to billing one copy.
        this.pendingCopies = tag.contains(TAG_ACTIVE_COPIES) ? tag.getInt(TAG_ACTIVE_COPIES) : 1;
        this.pendingChannel = tag.contains(TAG_ACTIVE_CHANNEL) ? tag.getInt(TAG_ACTIVE_CHANNEL) : null;
        this.pendingMode = tag.contains(TAG_ACTIVE_MODE) ? tag.getInt(TAG_ACTIVE_MODE) : null;
        // Set unconditionally and never read back from the tag: waiting is what a load does, not
        // something a save records, and an older save has no field to record it in.
        this.settleTicks = SETTLE_TICKS;
        // The shape has to be re-resolved after a load, and that scan must not switch the machine on.
        this.restoredFromDisk = true;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        writeDisplayState(tag, registries);
        tag.put(TAG_CONTROLLER, this.controllerSlot.serializeNBT(registries));
        tag.putBoolean(TAG_LOCK_RECIPE, this.lockRecipe);
        if (this.lockedRecipeId != null) {
            tag.putString(TAG_LOCKED_RECIPE, this.lockedRecipeId.toString());
        }
        tag.putBoolean(TAG_VOID_OUTPUT, this.voidOutput);
        tag.putBoolean(TAG_BATCH_MODE, this.batchMode);
        tag.putBoolean(TAG_ISOLATE_INPUT, this.isolateInput);
        tag.putBoolean(TAG_RUNNING, this.running);
        tag.putInt(TAG_OVERCLOCK_LIMIT, this.overclockLimit);
        tag.putInt(TAG_PARALLEL_LIMIT, this.parallelLimit);
        // A craft's inputs are taken when it starts, so a run that is not saved is a run whose
        // ingredients are destroyed by logging out. Saved by id rather than by value: the recipe
        // manager owns the instances, and one that no longer exists must simply not resume.
        if (this.activeRecipe != null) {
            ResourceLocation id = recipeIdOf(this.activeRecipe);
            if (id != null) {
                tag.putString(TAG_ACTIVE_RECIPE, id.toString());
                tag.putInt(TAG_ACTIVE_PROGRESS, this.progress);
                tag.putInt(TAG_ACTIVE_LENGTH, this.maxProgress);
                tag.putInt(TAG_ACTIVE_OVERCLOCK, this.activeOverclock);
                // How many copies were running. Saved because the recipe alone does not say: a
                // resumed craft would otherwise bill one copy and run at a fraction of its cost.
                tag.putInt(TAG_ACTIVE_COPIES, Math.max(1, this.runCopies));
                // The ports the run is pinned to. Restoring the recipe without them would let a
                // resumed craft draw from a different group than the one it took its inputs from.
                if (this.activeChannel != null) {
                    tag.putInt(TAG_ACTIVE_CHANNEL, this.activeChannel);
                }
                if (this.activeMode != null) {
                    tag.putInt(TAG_ACTIVE_MODE, this.activeMode);
                }
            }
        }
    }

    /**
     * AMI's update tag carries only the progress, the formed flag and its own buffers, so the
     * custom name has to be appended here or the client GUI would keep showing the default one.
     */
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        writeDisplayState(tag, registries);
        tag.putBoolean(TAG_RUNNING, this.running);
        tag.putInt(TAG_OVERCLOCK_LIMIT, this.overclockLimit);
        tag.putInt(TAG_PARALLEL_LIMIT, this.parallelLimit);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.handleUpdateTag(tag, registries);
        readDisplayState(tag, registries);
        this.running = tag.getBoolean(TAG_RUNNING);
        this.overclockLimit = tag.contains(TAG_OVERCLOCK_LIMIT)
                ? tag.getInt(TAG_OVERCLOCK_LIMIT)
                : NO_OVERCLOCK_LIMIT;
        this.parallelLimit = tag.contains(TAG_PARALLEL_LIMIT)
                ? tag.getInt(TAG_PARALLEL_LIMIT)
                : ALWAYS_MAX_PARALLEL;
    }

    private void writeDisplayState(CompoundTag tag, HolderLookup.Provider registries) {
        if (this.customName != null) {
            tag.putString(TAG_CUSTOM_NAME, Component.Serializer.toJson(this.customName, registries));
        }
    }

    private void readDisplayState(CompoundTag tag, HolderLookup.Provider registries) {
        if (!tag.contains(TAG_CUSTOM_NAME)) {
            this.customName = null;
            return;
        }
        try {
            this.customName = Component.Serializer.fromJson(tag.getString(TAG_CUSTOM_NAME), registries);
        } catch (RuntimeException exception) {
            this.customName = null;
        }
    }

    // --- Single slot container view -------------------------------------------------------
    //
    // AMI maps the Container view onto its input and output handlers, which the kettle does not
    // use. Exposing only the controller slot here is what lets Containers.dropContentsOnDestroy
    // return it when the block is broken, and it is also the container the menu binds to.

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return this.controllerSlot.getStackInSlot(0);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        this.controllerSlot.setStackInSlot(0, stack);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack stack = this.controllerSlot.getStackInSlot(0);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = stack.split(amount);
        this.controllerSlot.setStackInSlot(0, stack);
        return taken;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.controllerSlot.getStackInSlot(0);
        this.controllerSlot.setStackInSlot(0, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void clearContent() {
        this.controllerSlot.setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    public boolean isEmpty() {
        return this.controllerSlot.getStackInSlot(0).isEmpty();
    }

    @Override
    public int getMaxStackSize() {
        return CONTROLLER_SLOT_LIMIT;
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return true;
    }

    /**
     * No face exposes the slot: the kettle stores the controller for its own GUI only, and
     * leaving it reachable would let any pipe drain the machine's own item.
     */
    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
