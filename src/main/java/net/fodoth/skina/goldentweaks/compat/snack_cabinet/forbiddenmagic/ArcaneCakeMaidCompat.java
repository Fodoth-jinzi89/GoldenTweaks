package net.fodoth.skina.goldentweaks.compat.snack_cabinet.forbiddenmagic;

import com.github.tartaricacid.touhoulittlemaid.api.block.IMaidEdibleBlock;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.spitefulfox.forbiddenmagic.common.block.ArcaneCakeBlock;
import com.spitefulfox.forbiddenmagic.common.registry.FMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ArcaneCakeMaidCompat implements IMaidEdibleBlock {

    @Override
    public boolean shouldMoveTo(EntityMaid maid, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof ArcaneCakeBlock
                && state.getValue(CakeBlock.BITES) < CakeBlock.MAX_BITES
                && IMaidEdibleBlock.belowIsSnackStand(maid, pos);
    }

    @Override
    public int getFavorabilityPoints(EntityMaid maid, BlockPos pos, BlockState state) {
        return 1;
    }

    @Override
    public boolean consume(EntityMaid maid, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof ArcaneCakeBlock)
                || state.getValue(CakeBlock.BITES) >= CakeBlock.MAX_BITES) {
            return false;
        }

        int bites = state.getValue(CakeBlock.BITES);
        Level level = maid.level();
        level.setBlock(pos, state.setValue(CakeBlock.BITES, bites + 1), Block.UPDATE_ALL);
        maid.spawnItemParticles(new ItemStack(FMBlocks.ARCANE_CAKE.asItem()), 8);
        maid.playSound(SoundEvents.GENERIC_EAT);
        return true;
    }

    @Override
    public boolean canPlaceAsFood(EntityMaid maid, ItemStack stack, int slotIndex) {
        return stack.is(FMBlocks.ARCANE_CAKE.asItem());
    }
}
