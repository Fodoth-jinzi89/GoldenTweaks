package net.fodoth.skina.goldentweaks.mixin.fix.sable;

import dev.ryanhcode.sable.physics.chunk.VoxelNeighborhoodState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VoxelNeighborhoodState.class, remap = false)
public class VoxelNeighborhoodStateMixin {

    @Inject(
            method = "isSolid",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void goldentweaks$fixBrokenSolidCache(
            BlockGetter blockGetter,
            BlockPos pos,
            BlockState state,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (state.isAir()) {
            cir.setReturnValue(false);
            return;
        }

        if (state.getBlock() instanceof MovingPistonBlock) {
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(
                !state.getCollisionShape(blockGetter, BlockPos.ZERO).isEmpty()
        );
    }
}