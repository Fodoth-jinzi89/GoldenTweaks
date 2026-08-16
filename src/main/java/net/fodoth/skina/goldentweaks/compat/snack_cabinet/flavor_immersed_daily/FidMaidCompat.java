package net.fodoth.skina.goldentweaks.compat.snack_cabinet.flavor_immersed_daily;

import com.flavor_immersed_daily.block.block.food.MultiStageInteractiveBlock;
import com.github.tartaricacid.touhoulittlemaid.api.block.IMaidEdibleBlock;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily.accessor.MultiStageInteractiveBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FID 模组食物女仆兼容适配器
 */
public class FidMaidCompat implements IMaidEdibleBlock {

    @Override
    public boolean shouldMoveTo(EntityMaid maid, BlockPos pos, BlockState state) {
        // 判断是否为 FID 食物方块
        return state.getBlock() instanceof MultiStageInteractiveBlock
                && IMaidEdibleBlock.belowIsSnackStand(maid, pos);
    }

    @Override
    public int getFavorabilityPoints(EntityMaid maid, BlockPos pos, BlockState state) {
        // 固定 3 点好感度
        return 3;
    }

    @Override
    public boolean consume(EntityMaid maid, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        // 判断是否为 FID 食物方块
        if (!(block instanceof MultiStageInteractiveBlock fidBlock)) {
            return false;
        }

        Level level = maid.level();

        // 获取当前阶段
        int currentStage = state.getValue(MultiStageInteractiveBlock.STAGE);

        // 模拟原模组的交互逻辑
        if (currentStage < 2) {
            // 阶段 0 或 1：增加阶段并喂食女仆
            BlockState newState = state.setValue(MultiStageInteractiveBlock.STAGE, currentStage + 1);
            level.setBlock(pos, newState, 3);

            // 使用 Accessor 获取对应的食物物品并喂给女仆
            MultiStageInteractiveBlockAccessor accessor = (MultiStageInteractiveBlockAccessor) fidBlock;
            Item foodItem = accessor.invokeFoodItem();
            ItemStack foodStack = new ItemStack(foodItem);

            if (!foodStack.isEmpty()) {
                // 让女仆食用食物
                maid.eat(level, foodStack.copyWithCount(1));
            }

        } else {
            // 阶段 2：移除方块（完全消耗）
            level.destroyBlock(pos, true);
        }
        return true;
    }

    @Override
    public boolean canPlaceAsFood(EntityMaid maid, ItemStack stack, int slotIndex) {
        // 使用工具类判断是否为 FID 食物物品
        return stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof MultiStageInteractiveBlock;
    }

    @Override
    public boolean placeAsFood(EntityMaid maid, BlockPos pos, ItemStack stack, int slotIndex) {

        var availableInv = maid.getAvailableInv(true);
        ItemStack extracted = availableInv.extractItem(slotIndex, 1, false);

        if (extracted.isEmpty()) {
            return false;
        }

        if (!(extracted.getItem() instanceof BlockItem extractedItem)
                || !(extractedItem.getBlock() instanceof MultiStageInteractiveBlock)) {
            return false;
        }

        // 使用工具类获取对应的方块
        Block block = extractedItem.getBlock();

        if (block == null || block == Blocks.AIR) {
            return false;
        }

        // 放置方块
        return maid.placeItemBlock(pos, new ItemStack(block));
    }
}
