package net.fodoth.skina.goldentweaks.mixin.fix.ftbultimine;

import dev.ftb.mods.ftbultimine.crops.VanillaCropLikeHandler;
import dev.ftb.mods.ftbultimine.api.util.ItemCollector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import tb.common.block.ThaumicCropBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlaxBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VanillaCropLikeHandler.class)
public abstract class VanillaCropLikeHandlerMixin {

    @Inject(method = "isApplicable", at = @At("HEAD"), cancellable = true)
    private void gt$includeThaumicCrop(Level level, BlockPos pos, BlockState state,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof ThaumicCropBlock crop) {
            cir.setReturnValue(state.getValue(crop.ageProperty()) < crop.matureAge());
        }
    }

    @Inject(method = "doHarvesting", at = @At("HEAD"), cancellable = true)
    private void gt$harvestThaumicCrop(Player player, BlockPos pos, BlockState state,
                                        ItemCollector collector, CallbackInfoReturnable<Boolean> cir) {
        if (!(state.getBlock() instanceof ThaumicCropBlock crop)
                || !(player.level() instanceof ServerLevel server)) return;
        for (var drop : Block.getDrops(state, server, pos, null, player, net.minecraft.world.item.ItemStack.EMPTY)) {
            if (!drop.isEmpty() && !drop.is(state.getBlock().asItem())) collector.add(drop);
        }
        server.setBlock(pos, crop.stateForAge(0), Block.UPDATE_ALL);
        cir.setReturnValue(true);
    }

    @Inject(method = "isApplicable", at = @At("HEAD"), cancellable = true)
    private void gt$excludeUpperFlax(Level level, BlockPos pos, BlockState state,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof FlaxBlock
                && state.getValue(FlaxBlock.HALF) == DoubleBlockHalf.UPPER) {
            cir.setReturnValue(false);
        }
    }
}
