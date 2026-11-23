package net.fodoth.skina.goldentweaks.content.multiblock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block that draws a casing plate, and says which casing that is.
 *
 * <p>The connected-texture behaviour has to answer "does the neighbour continue this plate?", and it
 * asks both sides the same question. A shell block answers with itself; a port answers with the casing
 * its state selects; a machine asks its tile. Putting that behind one method keeps the behaviour free
 * of any knowledge of which machines exist.</p>
 *
 * <p>The state is passed rather than read from a field because a port's casing is a state value: one
 * port block serves every casing, and the plate it draws changes with the block state.</p>
 */
public interface GTCasingProvider {

    /** The casing whose plate this state draws. */
    Block gtCasing(BlockState state);
}
