package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.common.blocks.ThaumatoriumBlock;
import thaumcraft.common.blocks.ThaumatoriumTopBlock;
import thaumcraft.common.registry.TCBlocks;

@Mixin(value = {ThaumatoriumBlock.class, ThaumatoriumTopBlock.class}, remap = false)
public class ThaumatoriumStructureBreakMixin {

    @Inject(method = "onRemove", at = @At("HEAD"))
    private void gt$removeOtherHalf(BlockState state, Level level, BlockPos pos, BlockState newState,
                                    boolean movedByPiston, CallbackInfo ci) {
        if (level.isClientSide || state.is(newState.getBlock())) {
            return;
        }

        BlockPos otherPos = state.is(TCBlocks.THAUMATORIUM.get()) ? pos.above() : pos.below();
        Block otherBlock = state.is(TCBlocks.THAUMATORIUM.get())
                ? TCBlocks.THAUMATORIUM_TOP.get()
                : TCBlocks.THAUMATORIUM.get();
        if (level.getBlockState(otherPos).is(otherBlock)) {
            level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
