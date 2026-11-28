package net.fodoth.skina.goldentweaks.mixin.fix.avaritia_more_items;

import com.llamalad7.mixinextras.sugar.Local;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.structure.MultiblockStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Lets AMI read the tier of this mod's own ports.
 *
 * <p>{@code MultiblockStructure.collectPorts} decides a port's tier with
 * {@code block instanceof MultiblockPortBlock ? getPortTier() : MultiblockPortTier.I}, so any port
 * that is not one of AMI's blocks is flattened to tier I. This mod's ports are deliberately not AMI
 * blocks — inheriting them would drag AMI's three-coincident-cube renderer back in, which is the
 * whole thing being replaced.</p>
 *
 * <p>The tier local is overwritten just after it is computed. Its {@code LocalVariableTable} entry
 * is present on the target ({@code tier}, slot 6), so {@code @ModifyVariable} addresses it directly
 * rather than guessing at a bytecode offset.</p>
 */
@Mixin(MultiblockStructure.class)
public abstract class GTMultiblockPortTierMixin {

    /**
     * Replaces the tier written by {@code collectPorts} for this mod's ports.
     *
     * <p>{@code ordinal = 0} is the only {@code MultiblockPortTier} local in the method. The block
     * is read from the state passed alongside, which the scan has already fetched for the same
     * position.</p>
     */
    @ModifyVariable(method = "collectPorts", at = @At("STORE"), ordinal = 0)
    private static MultiblockPortTier goldentweaks$portTier(MultiblockPortTier original,
                                                            @Local(type = BlockState.class) BlockState state) {
        Block block = state.getBlock();
        if (block instanceof GTPortBlock) {
            return GTPortBlock.tierOf(state);
        }
        return original;
    }
}
