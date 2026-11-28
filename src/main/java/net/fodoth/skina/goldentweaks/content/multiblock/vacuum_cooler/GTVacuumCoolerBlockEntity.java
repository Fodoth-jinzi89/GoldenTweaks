package net.fodoth.skina.goldentweaks.content.multiblock.vacuum_cooler;

import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTChemicalReactionRecipe;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineBlockEntity;

/**
 * The vacuum cooler: the basic kettle's machine, housed in aluminium, with a narrower port set.
 *
 * <p>Everything about the craft — the parallel and batch scheduling, the overclock, the typed
 * storage, the window — is inherited. Three things differ, and all three are answered by overrides:
 * the structure it assembles, the casing it is built from, and the recipes it reads.</p>
 *
 * <p>The port restriction is not here but in the structure's own keys: {@code vacuum_cooler_casing}
 * lists only the item, fluid and energy parts plus the aluminium shell, so a chemical hopper or an
 * essentia tank simply is not a wall of this machine and the scan rejects it. Expressing it that way
 * means the projector, the auto-build pass and the validator all agree without any of them needing
 * to know the rule.</p>
 */
public class GTVacuumCoolerBlockEntity extends GTMachineBlockEntity {

    /** Structure file this machine assembles, relative to {@code data/goldentweaks/multiblock}. */
    public static final String MACHINE_ID = "vacuum_cooler";

    public GTVacuumCoolerBlockEntity(BlockPos pos, BlockState state) {
        this(GTVacuumCoolerMultiblock.MACHINE_ENTITY.get(), pos, state);
    }

    public GTVacuumCoolerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public String getMachineId() {
        return MACHINE_ID;
    }

    /** Aluminium rather than polyethylene: the material is what makes this the cooler. */
    @Override
    protected Block casingBlock() {
        return GTMultiblockBlocks.SHELL_ALUMINUM.get();
    }

    @Override
    protected String nameKey() {
        return "block.goldentweaks.vacuum_cooler_controller";
    }

    /**
     * Its own cooling set, and only that.
     *
     * <p>The cooler is not a re-housing of the kettle: it runs cooling reactions, so the kettle's
     * reactions are deliberately not included. A shared set would have made every kettle pattern
     * runnable in a machine whose whole point is that it does something else.</p>
     */
    @Override
    protected List<RecipeType<GTChemicalReactionRecipe>> recipeTypes() {
        return List.of(GTVacuumCoolingRecipes.TYPE.get());
    }

    /** Its own window instance, so the client can tell the two machines apart. */
    @Override
    protected MenuType<?> menuType() {
        return GTVacuumCoolerMultiblock.MENU.get();
    }
}
