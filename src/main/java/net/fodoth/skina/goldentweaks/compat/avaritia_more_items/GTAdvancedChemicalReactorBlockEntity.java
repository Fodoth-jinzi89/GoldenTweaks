package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import java.util.List;

/**
 * The advanced chemical reaction kettle.
 *
 * <p>Identical to the basic kettle in every respect but three: it is built from polypropylene
 * casings, it reads its own structure file, and it holds its own recipes. All four of those are
 * answered by overrides; the several thousand lines of scheduling, storage and UI are inherited.</p>
 */
public class GTAdvancedChemicalReactorBlockEntity extends GTChemicalReactorMachineBlockEntity {

    /** Structure file this machine assembles, relative to {@code data/goldentweaks/multiblock}. */
    public static final String MACHINE_ID = "advanced_chemical_reactor";

    public GTAdvancedChemicalReactorBlockEntity(BlockPos pos, BlockState state) {
        this(GTAdvancedChemicalReactorMultiblock.MACHINE_ENTITY.get(), pos, state);
    }

    public GTAdvancedChemicalReactorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public String getMachineId() {
        return MACHINE_ID;
    }

    /** Polypropylene rather than polyethylene: the material is what makes this the advanced one. */
    @Override
    protected Block casingBlock() {
        return GTMultiblockBlocks.SHELL_PP.get();
    }

    @Override
    protected String nameKey() {
        return "block.goldentweaks.advanced_chemical_plant_controller";
    }

    /**
     * Its own recipes, then the basic kettle's.
     *
     * <p>Own set first so a pattern defined for both resolves to the advanced one — that is the
     * file written specifically for this machine. The basic set follows so upgrading a basic build
     * keeps every pattern the player already made working.</p>
     */
    @Override
    protected List<RecipeType<GTChemicalReactionRecipe>> recipeTypes() {
        return List.of(
                GTAdvancedChemicalReactionRecipes.TYPE.get(),
                GTChemicalReactionRecipes.TYPE.get());
    }

    /**
     * The advanced window, not the basic one.
     *
     * <p>The menu type is the client's cue for which screen to build. Without this override the
     * server opened the basic type, so the client had no screen for the menu it was handed and the
     * window closed on the tick it opened.</p>
     */
    @Override
    protected MenuType<?> menuType() {
        return GTAdvancedChemicalReactorMultiblock.MENU.get();
    }
}
