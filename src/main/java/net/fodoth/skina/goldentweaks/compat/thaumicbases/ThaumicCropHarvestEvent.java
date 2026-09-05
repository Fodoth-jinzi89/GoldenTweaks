package net.fodoth.skina.goldentweaks.compat.thaumicbases;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import tb.common.block.ThaumicCropBlock;

/** Enables the vanilla right-click harvest interaction for Thaumic Bases crops. */
public final class ThaumicCropHarvestEvent {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || event.getLevel().isClientSide()
                || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof ThaumicCropBlock crop)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (state.getValue(crop.ageProperty()) < crop.matureAge()) {
            return;
        }

        for (ItemStack drop : Block.getDrops(state, level, pos, level.getBlockEntity(pos),
                event.getEntity(), event.getItemStack())) {
            if (!drop.isEmpty() && !drop.is(state.getBlock().asItem())) {
                Block.popResource(level, pos, drop);
            }
        }
        level.setBlock(pos, crop.stateForAge(0), Block.UPDATE_ALL);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private ThaumicCropHarvestEvent() {
    }
}
