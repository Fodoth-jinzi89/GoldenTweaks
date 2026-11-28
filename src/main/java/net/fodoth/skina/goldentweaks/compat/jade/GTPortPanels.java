package net.fodoth.skina.goldentweaks.compat.jade;

import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortBlock;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortKind;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;

/**
 * The one question both Jade cancellations ask: is this block a general assembly?
 *
 * <p>Kept out of the mixins because two of them need it and neither owns it. Written against the
 * block rather than the block entity on purpose: the kind is a field of the block, and Jade may be
 * looking at a position whose entity has not synced yet — asking the entity would answer "not an
 * assembly" for a real one.</p>
 *
 * <p>In a {@code compat} package, not {@code mixin}: this is ordinary shared code, and the mixin
 * tree is for injections only.</p>
 */
public final class GTPortPanels {

    private GTPortPanels() {
    }

    /** Whether the block Jade is looking at is one of our general assemblies. */
    public static boolean isAssembly(Accessor<?> accessor) {
        return accessor instanceof BlockAccessor block
                && block.getBlock() instanceof GTPortBlock port
                && port.kind() == GTPortKind.GENERAL;
    }
}
