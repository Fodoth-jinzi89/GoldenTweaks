package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.abstracts.AbstractMultiblockBlock;
import net.weibai.avaritia_more_items.common.init.AMITags;
import org.jetbrains.annotations.NotNull;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTCasingProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Basic chemical reaction kettle, hosted by {@code avaritia_more_items}' multiblock framework.
 *
 * <p>Modelled on {@code PhotolithographyMachineMainBlock}: the block only supplies its tile, and
 * the framework owns the hammer interaction, the structure scan and the machine menu.</p>
 *
 * <p><b>Why the facing handling is re-implemented:</b> {@code AbstractMultiblockBlock} declares its
 * own {@code FACING} as {@code HORIZONTAL_FACING} (four horizontal directions) and reads it
 * unconditionally in {@code getStateForPlacement}, {@code rotate} and {@code mirror}. We want the
 * kettle to be placeable in any of the six directions with a mirrored overlay, matching the
 * {@code facing}/{@code mirrored} blockstate it has always had, so every one of those methods is
 * overridden here with a six-way {@link BlockStateProperties#FACING} instead. AMI never reads the
 * facing of an arbitrary machine block — {@code AbstractMultiblockTile.syncFacing} guards its own
 * lookup with {@code hasProperty} — so dropping AMI's property is safe.</p>
 *
 * <p>{@link #FORMED} intentionally reuses AMI's property instance: {@code
 * AbstractConnectBlock.updateFormation} reads and writes that exact property, and looking a
 * distinct instance up by identity would fail.</p>
 */
public class GTChemicalReactorMachineBlock extends AbstractMultiblockBlock<GTChemicalReactorMachineBlockEntity>
        implements GTCasingProvider {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final BooleanProperty MIRRORED = BooleanProperty.create("mirrored");

    /**
     * Whether the machine is switched on. Only meaningful once the structure is formed.
     *
     * <p>Kept as a block state rather than a field so the front overlay can be resolved by the
     * blockstate JSON alone: unformed always looks unconstructed, formed and running looks idle,
     * formed and switched off looks off.</p>
     */
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");

    /**
     * Whether a reaction is being processed right now.
     *
     * <p>A third bit, because the machine has three distinct states and the front overlay has to
     * tell them apart: assembled-and-switched-on, assembled-and-crafting, assembled-and-switched-
     * off. {@link #RUNNING} alone answers the first and the third, so this one answers the second —
     * a player glancing at the kettle wants to know whether work is happening, not merely whether
     * the switch is up.</p>
     */
    public static final BooleanProperty CRAFTING = BooleanProperty.create("crafting");

    /**
     * Quarter turns around the machine's own facing axis, 0 to 3.
     *
     * <p>This is what lets the front face spin like an enhanced lever: the facing picks which side
     * the machine looks at, and the roll picks how far that face is turned about its own normal.
     * The structure matcher reads it, so a rolled machine only forms a correspondingly rolled
     * shape.</p>
     */
    public static final IntegerProperty ROLL = IntegerProperty.create("roll", 0, 3);

    public static final BooleanProperty FORMED = AbstractMultiblockBlock.FORMED;

    /**
     * The structure this machine assembles, relative to {@code data/<namespace>/multiblock}.
     *
     * <p>Carried by the block rather than looked up from a table of block ids to structure ids:
     * the tile already answers {@link GTChemicalReactorMachineBlockEntity#getMachineId()}, and a
     * second table would be one more thing to keep in step. The projector, the rotate helper and
     * the build pass all read this one field, so a second machine costs a constructor argument and
     * nothing else.</p>
     */
    private final String structurePath;

    /**
     * Builds the block entity for a placed host.
     *
     * <p>Carried by the block for the same reason as {@link #structurePath}: an entity type has to
     * match the block it sits in, and both machines share this block class, so the difference has
     * to come from the registration rather than from a branch here. With the factory in hand this
     * class needs no knowledge of any particular machine.</p>
     */
    private final BiFunction<BlockPos, BlockState, GTChemicalReactorMachineBlockEntity> tileFactory;

    /**
     * The casing this machine is built from and draws its base plate with.
     *
     * <p>Supplied at registration for the same reason as the structure and the tile factory: both
     * machines share this class, so anything that differs between them has to arrive as a constructor
     * argument. It is also what lets the base plate join the connected texture of the casing wall
     * around it.</p>
     */
    private final Supplier<? extends Block> casing;

    /**
     * @param structurePath the structure file name, e.g. {@code chemical_reactor}
     * @param tileFactory   constructs this machine's block entity
     * @param casing        the casing this machine is built from
     */
    public GTChemicalReactorMachineBlock(Properties properties, String structurePath,
                                         BiFunction<BlockPos, BlockState,
                                                 GTChemicalReactorMachineBlockEntity> tileFactory,
                                         Supplier<? extends Block> casing) {
        super(properties);
        this.structurePath = structurePath;
        this.tileFactory = tileFactory;
        this.casing = casing;
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(MIRRORED, false)
                .setValue(RUNNING, false)
                .setValue(CRAFTING, false)
                .setValue(ROLL, 0)
                .setValue(FORMED, false));
    }

    /**
     * The casing this machine's base plate draws.
     *
     * <p>Its front is an overlay on a casing plate, so the plate connects with the wall around it. The
     * casing comes from the registration, which is what keeps this class free of any particular
     * machine.</p>
     */
    @Override
    public Block gtCasing(BlockState state) {
        return this.casing.get();
    }

    /**
     * Resolves the structure file this host projects and builds.
     *
     * <p>Returns {@code null} when the namespace-qualified id cannot be formed, which only happens
     * for a path that is not a valid resource location.</p>
     */
    @Nullable
    public ResourceLocation structureId() {
        return ResourceLocation.tryBuild(GoldenTweaks.MODID, this.structurePath);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MIRRORED, RUNNING, CRAFTING, ROLL, FORMED);
    }

    /**
     * Places the front against the face the player is looking at, all six directions included.
     *
     * <p>Deliberately does not fall back to a horizontal direction when the clicked face is the
     * top or bottom: the blockstate and its models cover {@code up} and {@code down}, and the
     * structure matcher probes every orientation, so the kettle has to be placeable facing up or
     * down rather than silently snapping level.</p>
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean mirrored = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        Direction facing = context.getNearestLookingDirection().getOpposite();
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(MIRRORED, mirrored)
                .setValue(ROLL, initialRoll(context, facing));
    }

    /**
     * Roll the machine starts with.
     *
     * <p>A horizontal facing already encodes the direction it looks at, so it starts unrolled. Up
     * and down do not: the front face can spin on the spot, and which of those four spins feels
     * "not rotated" depends on where the player stands. The roll is therefore chosen so the face's
     * top edge points away from the player, giving them an upright screen.</p>
     */
    public static int initialRoll(BlockPlaceContext context, Direction facing) {
        if (!facing.getAxis().isVertical()) {
            return 0;
        }
        Direction playerFacing = context.getHorizontalDirection();
        // The player looks down at the face, so "upright" means the face's top edge points away
        // from them, along their own view direction. Pick the roll that puts the top there.
        for (int roll = 0; roll < GTChemicalReactorOrientation.QUARTER_TURNS; roll++) {
            if (GTChemicalReactorOrientation.up(facing, roll) == playerFacing) {
                return roll;
            }
        }
        return 0;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        Direction facing = rotation.rotate(state.getValue(FACING));
        int roll = state.getValue(ROLL);
        if (state.getValue(FACING).getAxis().isVertical()) {
            // A vertical axis takes the roll with it, because the rotation spun the face in place.
            roll = Math.floorMod(roll + rollOf(rotation), GTChemicalReactorOrientation.QUARTER_TURNS);
        }
        return state.setValue(FACING, facing).setValue(ROLL, roll);
    }

    /** Quarter turns a structural rotation adds around a vertical facing. */
    private static int rollOf(Rotation rotation) {
        return switch (rotation) {
            case NONE -> 0;
            case CLOCKWISE_90 -> 1;
            case CLOCKWISE_180 -> 2;
            case COUNTERCLOCKWISE_90 -> 3;
        };
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        if (mirror == Mirror.NONE) {
            return state;
        }
        // A reflection reverses handedness, so the front overlay is left-to-right swapped
        // whether the front stays on the same side or moves to the opposite one.
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
                .setValue(MIRRORED, !state.getValue(MIRRORED))
                .setValue(ROLL, Math.floorMod(-state.getValue(ROLL),
                        GTChemicalReactorOrientation.QUARTER_TURNS));
    }

    /**
     * Cycles the roll in place, which is how the wrench turns a vertical machine.
     *
     * @return the new state, with the next quarter turn applied
     */
    public BlockState cycledRoll(BlockState state) {
        int roll = state.getValue(ROLL);
        return state.setValue(ROLL, (roll + 1) % GTChemicalReactorOrientation.QUARTER_TURNS);
    }

    /**
     * Deliberately does not call {@code super}.
     *
     * <p>{@code AbstractMultiblockBlock#onPlace} runs {@code AbstractConnectBlock.updateFormation},
     * whose flood fill only follows the shell family ({@code AbstractConnectBlock} and
     * {@code AbstractMultiblockBlock}). Our casing and ports are plain blocks, so the machine block
     * on its own looks like a complete 1x1x1 structure and AMI would force {@code FORM_DED = true}
     * the instant it is placed — before any structure check has run.</p>
     *
     * <p>The kettle owns its assembly state instead:
     * {@link GTChemicalReactorMachineBlockEntity} scans the structure and mirrors the result onto
     * {@link #FORMED}, so the block starts unconstructed and only turns to the assembled texture
     * once the check actually passes.</p>
     */
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState,
                        boolean movedByPiston) {
    }

    /**
     * The entity for this block, built by the factory this machine was registered with.
     *
     * <p>An entity's registered type must match the block it sits in, and vanilla throws on
     * construction when it does not — so the factory has to be the one belonging to this machine,
     * which is why it comes from the registration.</p>
     */
    @Override
    protected GTChemicalReactorMachineBlockEntity machineTile(@NotNull BlockPos pos, @NotNull BlockState state) {
        return this.tileFactory.apply(pos, state);
    }

    /**
     * Spins the front face a quarter turn when the machine is wrenched while sneaking.
     *
     * <p>This is the enhanced lever: a vertical machine has no placement cue telling it which way
     * the front should be turned, so the wrench is the only way to set its roll. It is deliberately
     * limited to sneaking, because a plain right click opens the GUI and AMI uses the hammer for
     * assembling.</p>
     */
    @Override
    public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                    Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (player.isShiftKeyDown() && stack.is(AMITags.Items.Tools.wrench)) {
            if (!level.isClientSide) {
                BlockState rolled = cycledRoll(state);
                level.setBlock(pos, rolled, Block.UPDATE_ALL);
                // A roll changes which shape counts as correct, and the periodic check cannot see
                // that, so match again against the new orientation.
                if (level.getBlockEntity(pos) instanceof GTChemicalReactorMachineBlockEntity tile) {
                    tile.reorient();
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    /**
     * Opens the kettle's own GUI, assembled or not.
     *
     * <p>AMI's version prints {@code multiblock.unformed_hint} to the chat when the shape is
     * incomplete and then opens its own menu. The kettle has a dedicated screen that already
     * shows the machine state, so the hint is dropped and the position is written into the
     * opening packet for the menu factory to read back.</p>
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof GTChemicalReactorMachineBlockEntity tile) {
            serverPlayer.openMenu(tile, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.CONSUME;
    }

    /**
     * Remember the anvil name that was on the placed item, so the GUI title can show it.
     */
    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && stack.has(DataComponents.CUSTOM_NAME)
                && level.getBlockEntity(pos) instanceof GTChemicalReactorMachineBlockEntity tile) {
            tile.setCustomName(stack.getHoverName());
        }
    }

    /**
     * Returns the controller slot exactly the way the vanilla bird nest does it.
     *
     * <p>{@code Containers.dropContentsOnDestroy} is the only variant that is correct here: it
     * guards on the block actually changing — so replacing the kettle with itself, or a state
     * update, drops nothing — and it reads the tile's container view, which for us is the single
     * controller slot. Dropping the stack by hand would either miss that guard or double up with
     * AMI's own {@code drops()} call.</p>
     */
    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState,
                         boolean movedByPiston) {
        // Unfreeze the ports before this tile stops existing. Breaking the controller is the one
        // teardown that does not go through the machine's own dismantle path, so without this the
        // ports would keep the channel lock a running craft put on them.
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof GTChemicalReactorMachineBlockEntity machine) {
            machine.onBlockRemoved();
        }
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
