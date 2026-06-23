package net.fodoth.skina.goldentweaks.compat.kaleidoscope;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GarbageStationFloorBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE =
            BooleanProperty.create("active");

    public static final EnumProperty<VillageType> VILLAGE_TYPE =
            EnumProperty.create(
                    "village_type",
                    VillageType.class
            );


    public static final MapCodec<GarbageStationFloorBlock> CODEC =
            simpleCodec(GarbageStationFloorBlock::new);

    public GarbageStationFloorBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(ACTIVE, false)
                        .setValue(VILLAGE_TYPE, VillageType.PLAINS)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, VILLAGE_TYPE);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new GarbageStationFloorBlockEntity(pos, state);
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        BlockEntityType<?> expectedType =
                KaleidoAdditionalBlockEntities.GARBAGE_STATION_FLOOR.get();

        if (type != expectedType) {
            return null;
        }

        return (lvl, pos, blockState, blockEntity) ->
                GarbageStationFloorBlockEntity.tick(
                        lvl,
                        pos,
                        blockState,
                        (GarbageStationFloorBlockEntity) blockEntity
                );
    }

    @Override
    public void setPlacedBy(
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @Nullable LivingEntity placer,
            @NotNull ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof GarbageStationFloorBlockEntity floor) {
                floor.refreshState();
            }
        }
    }
}