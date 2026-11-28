package net.fodoth.skina.goldentweaks.content.multiblock.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MatchedMultiblockRecipe;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MultiblockRecipeInput;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MultiblockRecipeMatcher;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Re-checks a multiblock recipe the way the recipe itself asks to be checked.
 *
 * <p>AMI's {@link MatchedMultiblockRecipe} freezes a recipe's {@code ItemStack} inputs when it is
 * built and matches only against that copy. That cannot express a recipe that names an item by id
 * alone, so anything holding a {@code MatchedMultiblockRecipe} has to ask the recipe behind it
 * instead. This is the single place that switch happens.</p>
 */
public final class GTRecipeMatcherBridge {

    private GTRecipeMatcherBridge() {
    }

    /**
     * Whether the ports satisfy {@code matched}, deferring to the recipe's own matching when it is
     * one of ours.
     *
     * <p>Recipes from other mods keep AMI's comparison: they never set the per-entry component
     * rules this class exists to honour, and their {@code matches} may not expect to be called
     * against a bare input.</p>
     */
    public static boolean matches(MatchedMultiblockRecipe matched,
                                  MultiblockRecipeInput input,
                                  @Nullable Level level) {
        if (matched.recipe() instanceof GTChemicalReactionRecipe reaction && level != null) {
            return reaction.matches(input, level);
        }
        return matched.matches(input);
    }

    /**
     * The level the tile stands in, for callers that only have the tile.
     *
     * <p>A tile can be examined before it is placed, so this is nullable and the caller falls back
     * to AMI's comparison rather than guessing.</p>
     */
    @Nullable
    public static Level levelOf(Object tile) {
        if (tile instanceof net.minecraft.world.level.block.entity.BlockEntity entity) {
            return entity.getLevel();
        }
        return null;
    }

    /**
     * Takes a recipe's item inputs out of the given handlers, honouring per-entry component rules.
     *
     * <p>Used by the base tile's finish step, which otherwise consumes through AMI's exact-stack
     * comparison and would take the wrong items — or none at all.</p>
     */
    public static boolean consumeItems(List<ItemStack> wanted,
                                       List<IItemHandler> handlers,
                                       @Nullable MatchedMultiblockRecipe active) {
        if (active == null || !(active.recipe() instanceof GTChemicalReactionRecipe reaction)) {
            return MultiblockRecipeMatcher.consumeItems(wanted, handlers);
        }
        List<ItemStack> pool = MultiblockRecipeMatcher.snapshotHandlers(handlers);
        int[] plan = GTReactionMatcher.plan(reaction.inputs(), 1L, pool);
        if (plan == null) {
            return false;
        }
        // Applied slot by slot against the live handlers, in the same order the snapshot used.
        int slot = 0;
        for (IItemHandler handler : handlers) {
            for (int i = 0; i < handler.getSlots(); i++, slot++) {
                if (slot >= plan.length || plan[slot] <= 0) {
                    continue;
                }
                handler.extractItem(i, plan[slot], false);
            }
        }
        return true;
    }
}
