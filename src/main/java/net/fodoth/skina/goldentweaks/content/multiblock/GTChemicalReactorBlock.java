package net.fodoth.skina.goldentweaks.content.multiblock;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Basic chemical reaction kettle used when {@code avaritia_more_items} is absent.
 *
 * <p>Its blockstate must stay identical to the multiblock host variant so a single
 * {@code chemical_plant_controller.json} blockstate serves both: a six-way {@code facing},
 * a {@code mirrored} flag that flips the front overlay, and a {@code formed} flag. The
 * multiblock host drives {@code formed} from its structure scan; here it is simply never set.</p>
 */
public class GTChemicalReactorBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty MIRRORED = BooleanProperty.create("mirrored");

    /**
     * Mirrors the host's property of the same name so one blockstate file serves both variants.
     * Without the multiblock framework nothing ever forms, so this stays {@code false} and the
     * block keeps the unconstructed overlay.
     */
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");

    /** Quarter turns about the facing axis; mirrors the host's property so one blockstate serves both. */
    public static final IntegerProperty ROLL = IntegerProperty.create("roll", 0, 3);

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public GTChemicalReactorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(MIRRORED, false)
                .setValue(RUNNING, false)
                .setValue(ROLL, 0)
                .setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MIRRORED, RUNNING, ROLL, FORMED);
    }

    /**
     * Places the front against the face the player is looking at, all six directions included.
     *
     * <p>No fallback to a horizontal direction for the top and bottom faces: the blockstate and
     * its models cover {@code up} and {@code down}, and the two variants of this block share one
     * blockstate, so they must agree on how placement resolves the facing.</p>
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean mirrored = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        return defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(MIRRORED, mirrored)
                .setValue(ROLL, 0);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        int roll = state.getValue(ROLL);
        if (state.getValue(FACING).getAxis().isVertical()) {
            // A vertical front spins in place under a structural rotation.
            roll = Math.floorMod(roll + switch (rotation) {
                case NONE -> 0;
                case CLOCKWISE_90 -> 1;
                case CLOCKWISE_180 -> 2;
                case COUNTERCLOCKWISE_90 -> 3;
            }, 4);
        }
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING))).setValue(ROLL, roll);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) {
            return state;
        }
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
                .setValue(MIRRORED, !state.getValue(MIRRORED))
                .setValue(ROLL, Math.floorMod(-state.getValue(ROLL), 4));
    }
}
