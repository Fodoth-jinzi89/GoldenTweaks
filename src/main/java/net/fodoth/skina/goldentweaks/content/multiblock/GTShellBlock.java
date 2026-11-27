package net.fodoth.skina.goldentweaks.content.multiblock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A casing block: the wall a multiblock is built from, and the plate every port and machine front
 * draws.
 *
 * <p>It draws nothing but itself, so {@link GTCasingProvider} answers with {@code this}. The connected
 * texture is not defined here — it comes from Create's CT model wrapped around the block's model, the
 * same one the ports use, so a wall and the ports set into it continue into one another.</p>
 *
 * <p>A block may name its CT layout explicitly. Normally the layout is read from the sheet's size, but
 * several layouts share a size — CROSS, ROOF, ROOF_STAIR and RECTANGLE are all 4x4 — so a size alone
 * cannot tell them apart. Naming it is what lets one material be built in every layout at once for
 * comparison; when nothing is named, the sheet decides as before.</p>
 */
public class GTShellBlock extends Block implements GTCasingProvider {

    @Nullable
    private final String ctLayout;

    public GTShellBlock(Properties properties) {
        this(properties, null);
    }

    public GTShellBlock(Properties properties, @Nullable String ctLayout) {
        super(properties);
        this.ctLayout = ctLayout;
    }

    /** The CT layout this casing asks for by name, or {@code null} to let the sheet's size decide. */
    @Nullable
    public String ctLayout() {
        return this.ctLayout;
    }

    @Override
    public Block gtCasing(BlockState state) {
        return this;
    }
}
