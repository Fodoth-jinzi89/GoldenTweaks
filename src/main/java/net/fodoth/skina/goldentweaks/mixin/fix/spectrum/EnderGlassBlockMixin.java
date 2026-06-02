package net.fodoth.skina.goldentweaks.mixin.fix.spectrum;

import de.dafuqs.spectrum.blocks.redstone.EnderGlassBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderGlassBlock.class)
public abstract class EnderGlassBlockMixin {

    @Inject(
            method = "neighborChanged",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$preventCrash(
            BlockState state,
            Level world,
            BlockPos pos,
            Block block,
            BlockPos fromPos,
            boolean notify,
            CallbackInfo ci
    ) {
        if (!world.isClientSide
                && block instanceof EnderGlassBlock) {

            BlockState fromState = world.getBlockState(fromPos);

            if (!fromState.hasProperty(
                    EnderGlassBlock.TRANSPARENCY_STATE
            )) {
                ci.cancel();
            }
        }
    }
}
