package net.fodoth.skina.goldentweaks.mixin.fix.thaumictinkerer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumic.tinkerer.common.block.InfusedGrainBlock;

/**
 * Infused grain only accepted Thaumic Tinkerer's own infused farmland, so it could not be grown on
 * Farmers Delight's rich soil farmland.
 */
@Mixin(value = InfusedGrainBlock.class, remap = false)
public abstract class InfusedGrainBlockMixin {

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$allowRichSoilFarmland(BlockState state, BlockGetter level, BlockPos pos,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals("farmersdelight:rich_soil_farmland")) {
            cir.setReturnValue(true);
        }
    }
}
