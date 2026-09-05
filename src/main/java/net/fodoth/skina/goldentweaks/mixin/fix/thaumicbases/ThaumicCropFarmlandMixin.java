package net.fodoth.skina.goldentweaks.mixin.fix.thaumicbases;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tb.common.block.ThaumicCropBlock;

@Mixin(ThaumicCropBlock.class)
public abstract class ThaumicCropFarmlandMixin {
    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void gt$allowRichSoil(BlockState state, BlockGetter level, BlockPos pos,
                                  CallbackInfoReturnable<Boolean> cir) {
        if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals("farmersdelight:rich_soil_farmland")) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void gt$surviveOnRichSoil(BlockState state, LevelReader level, BlockPos pos,
                                      CallbackInfoReturnable<Boolean> cir) {
        if (BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos.below()).getBlock()).toString().equals("farmersdelight:rich_soil_farmland")) {
            cir.setReturnValue(true);
        }
    }
}
