package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.crafting.InfusionStabilizer;

public class GTInfusionIntercepterBlock extends BaseEntityBlock implements InfusionStabilizer {

    public static final MapCodec<GTInfusionIntercepterBlock> CODEC =
            simpleCodec(GTInfusionIntercepterBlock::new);

    public GTInfusionIntercepterBlock(Properties properties) {
        super(properties);
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
        return new GTInfusionIntercepterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        BlockEntityType<?> expectedType =
                GTThaumcraftAdditionalBlockEntities.INFUSION_INTERCEPTER.get();

        if (type != expectedType) {
            return null;
        }

        return (lvl, pos, blockState, blockEntity) ->
                GTInfusionIntercepterBlockEntity.tick(
                        lvl,
                        pos,
                        blockState,
                        (GTInfusionIntercepterBlockEntity) blockEntity
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

        if (!level.isClientSide() && placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GTInfusionIntercepterBlockEntity intercepter) {
                intercepter.setOwner(player);
            }
        }
    }

    @Override
    public void onRemove(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GTInfusionIntercepterBlockEntity intercepter) {
                intercepter.removeStability();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean canStabilizeInfusion(
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        return true;
    }
}
