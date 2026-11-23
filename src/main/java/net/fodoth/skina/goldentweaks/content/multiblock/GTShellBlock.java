package net.fodoth.skina.goldentweaks.content.multiblock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A casing block: the wall a multiblock is built from, and the plate every port and machine front
 * draws.
 *
 * <p>It draws nothing but itself, so {@link GTCasingProvider} answers with {@code this}. The connected
 * texture is not defined here — it comes from Create's CT model wrapped around the block's model, the
 * same one the ports use, so a wall and the ports set into it continue into one another.</p>
 */
public class GTShellBlock extends Block implements GTCasingProvider {

    public GTShellBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Block gtCasing(BlockState state) {
        return this;
    }
}
