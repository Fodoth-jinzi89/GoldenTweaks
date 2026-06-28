package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.mcreator.flavorimmerseddaily.block.MeatmincerBlock;
import net.mcreator.flavorimmerseddaily.block.entity.MeatmincerBlockEntity;
import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeatmincerBlock.class)
public class MeatmincerBlockMixin {

    @Final
    @Shadow
    public static IntegerProperty BLOCKSTATE;

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void onGetStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState originalState = cir.getReturnValue();
        if (originalState != null) {
            // 设置 BLOCKSTATE 为 1（不带盖子）
            BlockState newState = originalState.setValue(BLOCKSTATE, 1);
            cir.setReturnValue(newState);
        }
    }
    /**
     * 在onRemove方法开始时注入，处理盖子的掉落
     */
    @Inject(
            method = "onRemove",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void onRemoveHead(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        // 如果方块被替换，不处理
        if (state.getBlock() == newState.getBlock()) {
            return;
        }

        // 获取当前方块的状态值
        int currentState = getBlockState(state);

        // 获取BlockEntity
        if (world.getBlockEntity(pos) instanceof MeatmincerBlockEntity be) {
            // 如果当前状态为0（有盖子），掉落一个盖子
            if (currentState == 0) {
                ItemStack coverStack = new ItemStack(FlavorImmersedDailyModItems.MINCER_COVER.get());
                coverStack.setCount(1);
                Containers.dropItemStack(world,
                         pos.getX(),
                         pos.getY(),
                         pos.getZ(),
                        coverStack);
            }

            // 掉落容器内的所有物品（原版逻辑）
            Containers.dropContents(world, pos, be);

            // 更新邻居信号（原版逻辑）
            world.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        if (state.hasBlockEntity() && !state.is(newState.getBlock())) {
            world.removeBlockEntity(pos);
        }

        ci.cancel();
    }

    /**
     * 获取方块的状态值
     */
    @Unique
    private static int getBlockState(BlockState state) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty("blockstate");
        if (property instanceof IntegerProperty intProp) {
            return state.getValue(intProp);
        }
        return -1;
    }
}