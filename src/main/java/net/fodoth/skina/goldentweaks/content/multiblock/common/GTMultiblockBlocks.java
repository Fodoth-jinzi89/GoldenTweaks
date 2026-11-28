package net.fodoth.skina.goldentweaks.content.multiblock.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.PortMode;

import java.util.EnumMap;
import java.util.Map;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.advanced_chemical_plant.GTAdvancedChemicalReactorMultiblock;
import net.fodoth.skina.goldentweaks.content.multiblock.basic_chemical_plant.GTChemicalReactorMultiblock;
import net.fodoth.skina.goldentweaks.content.multiblock.vacuum_cooler.GTVacuumCoolerMultiblock;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifStructures;
import net.fodoth.skina.goldentweaks.content.multiblock.basic_chemical_plant.StandaloneReactor;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortBlock;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortBlockEntity;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortItem;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortKind;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortMenu;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortTier;

public final class GTMultiblockBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(GoldenTweaks.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);

    public static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<GTShellBlock> SHELL_PE =
            BLOCKS.register("shell_pe", () -> new GTShellBlock(properties(), "OMNIDIRECTIONAL"));

    /** The polypropylene shell: the same part, one material tier up. */
    public static final DeferredBlock<GTShellBlock> SHELL_PP =
            BLOCKS.register("shell_pp", () -> new GTShellBlock(properties(), "OMNIDIRECTIONAL"));

    /** The aluminium shell: the same part, one material tier up again. */
    public static final DeferredBlock<GTShellBlock> SHELL_ALUMINUM =
            BLOCKS.register("shell_aluminum", () -> new GTShellBlock(properties(), "OMNIDIRECTIONAL"));


    /**
     * Every casing a port can show, in the order the block state's {@code casing} index refers to.
     *
     * <p>Index 0 is the default, so a port no machine has claimed needs no state change. The list is
     * ordered rather than a set because the state stores an index, and a state property can only hold
     * a bounded value.</p>
     *
     * <p>Adding a casing means appending here and giving it a connected-texture sheet; the port
     * models are not touched.</p>
     */
    public static final java.util.List<DeferredBlock<GTShellBlock>> CASINGS = java.util.List.of(
            SHELL_PE, SHELL_PP, SHELL_ALUMINUM);

    /** The index of a casing in {@link #CASINGS}, or 0 when it is not one of them. */
    public static int casingIndex(Block casing) {
        for (int index = 0; index < CASINGS.size(); index++) {
            if (CASINGS.get(index).get() == casing) {
                return index;
            }
        }
        return 0;
    }

    /** The casing at an index, clamped into the list. */
    public static Block casingAt(int index) {
        return CASINGS.get(Math.clamp(index, 0, CASINGS.size() - 1)).get();
    }

    public static final DeferredItem<BlockItem> SHELL_PE_ITEM =
            ITEMS.register("shell_pe", () -> new BlockItem(SHELL_PE.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> SHELL_PP_ITEM =
            ITEMS.register("shell_pp", () -> new BlockItem(SHELL_PP.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> SHELL_ALUMINUM_ITEM =
            ITEMS.register("shell_aluminum", () -> new BlockItem(SHELL_ALUMINUM.get(), new Item.Properties()));

    // =========================
    // 自有端口（取代 AMI 端口）
    // =========================

    /** Block entity type shared by every port, the same way AMI serves all its ports from one. */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GoldenTweaks.MODID);

    /** Port blocks keyed by kind, then mode, then tier. */
    public static final Map<GTPortKind,
            Map<PortMode, Map<GTPortTier, DeferredBlock<GTPortBlock>>>> PORT_BLOCKS =
            new EnumMap<>(GTPortKind.class);

    /** Port block items, in the same shape, so the creative tab can list them. */
    public static final Map<GTPortKind,
            Map<PortMode, Map<GTPortTier, DeferredItem<BlockItem>>>> PORT_ITEMS =
            new EnumMap<>(GTPortKind.class);

    /**
     * Shared block entity type for every port, bound to all six blocks.
     *
     * <p>Null when {@code avaritia_more_items} is absent: the port classes reference AMI's port
     * types, so nothing of theirs may be touched without it.</p>
     */
    @Nullable
    @SuppressWarnings("ConstantConditions")
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<GTPortBlockEntity>> PORT_ENTITY;

    /**
     * Declares the port blocks, items and their shared block entity type.
     *
     * <p>Called only when {@code avaritia_more_items} is loaded. The block registry entries
     * themselves would be harmless to declare, but {@link PortMode#values()} is an AMI class and
     * reaching it without the mod present throws. Everything here is therefore kept behind the
     * same check that guards the capability registration.</p>
     */
    @SuppressWarnings("ConstantConditions")
    private static void declarePorts() {
        for (GTPortKind kind : GTPortKind.values()) {
            if (!kindAvailable(kind)) {
                continue;
            }
            // One block and one item per tier: a tier in the block state would be invisible once the
            // part is an item, and the inventory has to name a ULV bus differently from a UV one.
            Map<PortMode, Map<GTPortTier, DeferredBlock<GTPortBlock>>> modes =
                    new EnumMap<>(PortMode.class);
            Map<PortMode, Map<GTPortTier, DeferredItem<BlockItem>>> items =
                    new EnumMap<>(PortMode.class);
            for (PortMode mode : PortMode.values()) {
                Map<GTPortTier, DeferredBlock<GTPortBlock>> tieredBlocks =
                        new EnumMap<>(GTPortTier.class);
                Map<GTPortTier, DeferredItem<BlockItem>> tieredItems =
                        new EnumMap<>(GTPortTier.class);
                for (GTPortTier tier : GTPortTier.values()) {
                    String name = "gt_port_" + kind.id() + "_"
                            + mode.name().toLowerCase(java.util.Locale.ROOT)
                            + "_" + tier.id();
                    DeferredBlock<GTPortBlock> block = BLOCKS.register(name,
                            () -> new GTPortBlock(properties(), kind, mode, tier));
                    tieredBlocks.put(tier, block);
                    tieredItems.put(tier, ITEMS.register(name,
                            () -> new GTPortItem(block.get(), new Item.Properties())));
                }
                modes.put(mode, tieredBlocks);
                items.put(mode, tieredItems);
            }
            PORT_BLOCKS.put(kind, modes);
            PORT_ITEMS.put(kind, items);
        }


        PORT_ENTITY = BLOCK_ENTITIES.register("gt_port",
                () -> BlockEntityType.Builder.of(GTPortBlockEntity::new,
                                PORT_BLOCKS.values().stream()
                                        .flatMap(modes -> modes.values().stream())
                                        .flatMap(tiers -> tiers.values().stream())
                                        .map(DeferredBlock::get)
                                        .toArray(net.minecraft.world.level.block.Block[]::new))
                        .build(null));
    }

    /**
     * Whether a port kind's backing mod is installed.
     *
     * <p>The two extended kinds hold Mekanism chemicals and Thaumcraft essentia, so registering
     * them without their mod would put a block in the game whose storage is always empty — and
     * whose compat class cannot even be loaded. Skipped rather than shipped broken.</p>
     */
    private static boolean kindAvailable(GTPortKind kind) {
        return switch (kind) {
            case CHEMICAL -> ModList.get().isLoaded("mekanism");
            case ESSENTIA -> ModList.get().isLoaded("thaumcraft");
            // The general assembly carries whatever kinds are installed, so it is always worth
            // registering: with neither mod it still hauls items, fluids and energy.
            default -> true;
        };
    }

    /**
     * Projects a multiblock layout in the world so the player can see where the parts go.
     *
     * <p>Right-clicking a host toggles a client-side projection of its shape; sneak-right-clicking
     * fills in the casing from the player's inventory instead, and left-clicking re-aims the host.
     * The projection is drawn only by the client, so no block is ever placed in the world.</p>
     */
    public static final DeferredItem<Item> PROJECTOR =
            ITEMS.register("projector", () -> new Item(new Item.Properties()));

    /** Crafting base for the multiblock parts; its grayscale mask is tinted on the client. */
    public static final DeferredItem<Item> CONTROL_CIRCUIT_BASE =
            ITEMS.register("control_circuit_base", () -> new Item(new Item.Properties()));

    /** Same base with the overclock arrow layered on top; only the base layer is tinted. */
    public static final DeferredItem<Item> CONTROL_CIRCUIT_OVERCLOCK_4_2 =
            ITEMS.register("control_circuit_overclock_4_2", () -> new Item(new Item.Properties()));

    /** Same base with the parallel-lines icon layered on top. */
    public static final DeferredItem<Item> CONTROL_CIRCUIT_PARALLEL_1 =
            ITEMS.register("control_circuit_parallel_1", () -> new Item(new Item.Properties()));

    /**
     * Same base with the fourfold-parallel icon layered on top.
     *
     * <p>Names the machine that carries it, not a fixed multiplier: what it grants is four parallels
     * per voltage tier the reactors can reach, so the ceiling moves with the hatches rather than
     * being a single number printed on the part.</p>
     */
    public static final DeferredItem<Item> CONTROL_CIRCUIT_PARALLEL_4X =
            ITEMS.register("control_circuit_parallel_4x", () -> new Item(new Item.Properties()));

    private GTMultiblockBlocks() {
    }

    /**
     * Applies a projector left click to a host block.
     *
     * <p>The host exists in two variants — the {@code avaritia_more_items} one and the plain
     * fallback — and each declares its own property instances, even though both blockstates use
     * the same file. The properties are therefore looked up by name on the state rather than
     * referenced from one variant's class, so the same helper serves both.</p>
     *
     * @param clickedFace the face the player hit
     * @param mirror      whether the click was a sneak-click, which flips the mirror instead
     * @return the new state, or {@code null} if this is not a host
     */
    @Nullable
    public static BlockState rotateHost(BlockState state, Direction clickedFace, boolean mirror) {
        BooleanProperty mirroredProperty = findByValueType(state, BooleanProperty.class, "mirrored");
        DirectionProperty facingProperty = findByValueType(state, DirectionProperty.class, "facing");
        IntegerProperty rollProperty = findByValueType(state, IntegerProperty.class, "roll");
        if (mirroredProperty == null || facingProperty == null || rollProperty == null) {
            return null;
        }

        if (mirror) {
            return state.setValue(mirroredProperty, !state.getValue(mirroredProperty));
        }

        Direction facing = state.getValue(facingProperty);
        if (clickedFace != facing) {
            // Re-aim: the clicked side becomes the front. The roll is kept because it is measured
            // around the front axis rather than in world space.
            return state.setValue(facingProperty, clickedFace);
        }

        int roll = state.getValue(rollProperty);
        return state.setValue(rollProperty, (roll + 1) % QUARTER_TURNS);
    }

    /**
     * The structure a host block projects, or {@code null} if this state is not a multiblock host.
     *
     * <p>Answered by the block, which carries the id it was registered with — so a new machine
     * declares its own structure and nothing here has to know it exists.</p>
     */
    @Nullable
    public static ResourceLocation structureForHost(BlockState state) {
        return state.getBlock() instanceof GTMachineBlock host ? host.structureId() : null;
    }

    /**
     * Whether the projector has a shape to show for this block.
     *
     * <p>Two sources answer yes: our own hosts, which name a structure file, and Singularity
     * Iteration's multiblock controllers, whose shapes are read from SI's in-code blueprints.
     * Each mod is only consulted when it is installed, so neither can throw when absent.</p>
     */
    public static boolean hasProjection(BlockState state) {
        if (ModList.get().isLoaded("avaritia_more_items") && structureForHost(state) != null) {
            return true;
        }
        return GTMioIcifStructures.available()
                && GTMioIcifStructures.layoutFor(state) != null;
    }

    /**
     * Whether this block is a Singularity Iteration multiblock controller.
     *
     * <p>SI's controllers open their own GUI on a plain right click and have no orientation
     * properties of ours, so they need their own handling instead of {@link #rotateHost}.</p>
     */
    public static boolean isMioIcifHost(BlockState state) {
        return GTMioIcifStructures.available()
                && GTMioIcifStructures.layoutFor(state) != null;
    }

    /** Current orientation of a host block, read from the blockstate. */
    public record HostOrientation(Direction facing, int roll, boolean mirrored, boolean formed) {
    }

    /**
     * Reads a host's orientation by property name, the same way {@link #rotateHost} does.
     *
     * @return the orientation, or {@code null} if any of the orientation properties is missing
     */
    @Nullable
    public static HostOrientation hostOrientation(BlockState state) {
        DirectionProperty facingProperty = findByValueType(state, DirectionProperty.class, "facing");
        IntegerProperty rollProperty = findByValueType(state, IntegerProperty.class, "roll");
        BooleanProperty mirroredProperty = findByValueType(state, BooleanProperty.class, "mirrored");
        BooleanProperty formedProperty = findByValueType(state, BooleanProperty.class, "formed");
        if (facingProperty == null || rollProperty == null || mirroredProperty == null) {
            return null;
        }
        return new HostOrientation(
                state.getValue(facingProperty),
                state.getValue(rollProperty),
                state.getValue(mirroredProperty),
                formedProperty != null && state.getValue(formedProperty));
    }

    /**
     * Finds a property on the state by its serialized name.
     *
     * <p>Blockstate properties are compared by identity, so the instance declared on a different
     * block class does not match. Walking the state's own definition is what makes this helper
     * variant-agnostic.</p>
     */
    @Nullable
    private static <T extends Property<?>> T findByValueType(
            BlockState state, Class<T> type, String name) {
        for (Property<?> property : state.getProperties()) {
            if (type.isInstance(property) && property.getName().equals(name)) {
                return type.cast(property);
            }
        }
        return null;
    }

    /** Quarter turns the projector applies per click. */
    public static final int QUARTER_TURNS = 4;

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);

        // The reaction kettle is registered by whomever can host it: with avaritia_more_items
        // present the AMI multiblock host owns the "chemical_plant_controller" id, otherwise a
        // plain block does. Both share one blockstate (facing/mirrored/formed), so exactly one
        // of the two may be registered.
        if (ModList.get().isLoaded("avaritia_more_items")) {
            // Ports implement AMI's port types, so like the kettle they only exist with AMI.
            declarePorts();
            BLOCK_ENTITIES.register(eventBus);
            GTPortMenu.MENU_TYPES.register(eventBus);
            GTChemicalReactorMultiblock.register(eventBus);
            GTAdvancedChemicalReactorMultiblock.register(eventBus);
            GTVacuumCoolerMultiblock.register(eventBus);
            // The kettle's recipes implement an AMI interface, so they are only registered when
            // the host itself is.
            GTChemicalReactionRecipes.register(eventBus);
        } else {
            StandaloneReactor.declare();
        }
    }
}
