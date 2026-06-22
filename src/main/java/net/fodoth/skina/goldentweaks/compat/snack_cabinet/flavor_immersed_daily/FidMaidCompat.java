package net.fodoth.skina.goldentweaks.compat.snack_cabinet.flavor_immersed_daily;

import com.github.tartaricacid.touhoulittlemaid.api.block.IMaidEdibleBlock;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.fodoth.skina.goldentweaks.util.FidFoodMappingUtil;
import net.mcreator.flavorimmerseddaily.procedures.吃菜Procedure;
import net.minecraft.core.BlockPos;
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
        // 使用工具类判断是否为 FID 食物方块
        return FidFoodMappingUtil.isFidFoodBlock(state.getBlock())
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

        // 使用工具类判断是否为 FID 食物方块
        if (!FidFoodMappingUtil.isFidFoodBlock(block)) {
            return false;
        }

        Level level = maid.level();

        // 使用工具类获取对应的食物物品
        ItemStack foodStack = FidFoodMappingUtil.getItemStackByBlock(block)
                .orElse(ItemStack.EMPTY);

        if (foodStack.isEmpty()) {
            return false;
        }

        // 调用原模组的吃菜逻辑（处理方块状态变化）
        吃菜Procedure.execute(level, pos.getX(), pos.getY(), pos.getZ(), maid);

        return true;
    }

    @Override
    public boolean canPlaceAsFood(EntityMaid maid, ItemStack stack, int slotIndex) {
        // 使用工具类判断是否为 FID 食物物品
        return FidFoodMappingUtil.isFidFoodItemStack(stack);
    }

    @Override
    public boolean placeAsFood(EntityMaid maid, BlockPos pos, ItemStack stack, int slotIndex) {
        // 使用工具类判断是否为 FID 食物物品
        if (!FidFoodMappingUtil.isFidFoodItemStack(stack)) {
            return false;
        }

        var availableInv = maid.getAvailableInv(true);
        ItemStack extracted = availableInv.extractItem(slotIndex, 1, false);

        if (extracted.isEmpty()) {
            return false;
        }

        // 使用工具类获取对应的方块
        Block block = FidFoodMappingUtil.getBlockByItemStack(extracted)
                .orElse(Blocks.AIR);

        if (block == Blocks.AIR) {
            return false;
        }

        // 放置方块
        return maid.placeItemBlock(pos, new ItemStack(block));
    }
}