package net.fodoth.skina.goldentweaks.mixin.fix.ftbultimine;

import dev.ftb.mods.ftbultimine.crops.VanillaCropLikeHandler;
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
    private void gt$excludeUpperFlax(Level level, BlockPos pos, BlockState state,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof FlaxBlock
                && state.getValue(FlaxBlock.HALF) == DoubleBlockHalf.UPPER) {
            cir.setReturnValue(false);
        }
    }
}
