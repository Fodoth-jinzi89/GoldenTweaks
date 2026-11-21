package net.fodoth.skina.goldentweaks.content.multiblock;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Multiblock component with a front face that can point at any of the six sides.
 *
 * <p>Every component renders the shared PE shell as its bottom layer and draws its
 * own overlay on the front face. The shell layer uses the same
 * {@code goldentweaks:block/shell_pe} texture as the plain shell block, so the
 * Continuity definition in {@code optifine/ctm/shell_pe.properties} (which matches
 * on that tile and uses {@code connect=tile}) joins components and shells into one
 * continuous surface.</p>
 */
public class GTFacingBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public GTFacingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
