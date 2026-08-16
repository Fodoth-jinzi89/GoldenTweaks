package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.flavor_immersed_daily.block.block.machine.EggBreakingMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EggBreakingMachineBlock.class)
public abstract class EggBreakingMachineBlockMixin {

    /**
     * @author Fodoth_jinzi89
     * @reason Sable compat
     */
    @Overwrite
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean movedByPiston) {

        Containers.dropContentsOnDestroy(state, newState, level, pos);

        if (state.hasBlockEntity() && !state.is(newState.getBlock())) {
            level.removeBlockEntity(pos);
        }
    }
}
