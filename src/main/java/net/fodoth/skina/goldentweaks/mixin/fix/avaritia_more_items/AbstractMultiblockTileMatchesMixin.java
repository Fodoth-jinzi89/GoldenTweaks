package net.fodoth.skina.goldentweaks.mixin.fix.avaritia_more_items;

import net.fodoth.skina.goldentweaks.content.multiblock.common.GTRecipeMatcherBridge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MatchedMultiblockRecipe;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MultiblockRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Sends the base tile's own re-checks through the recipe instead of its frozen item snapshot.
 *
 * <p>{@code MatchedMultiblockRecipe} copies a recipe's {@code ItemStack} inputs when it is built and
 * matches only against that copy, so a recipe that asks for an item by id alone (a worn coolant
 * cell, matched through {@code any_components}) can never pass. Both of the base tile's checks are
 * redirected here: {@code startRecipe} before a craft begins, and {@code consumeRecipeInputs} when
 * it finishes.</p>
 */
@Mixin(value = net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.abstracts.AbstractMultiblockTile.class,
        remap = false)
public abstract class AbstractMultiblockTileMatchesMixin {

    @Redirect(
            method = {"startRecipe", "consumeRecipeInputs"},
            at = @At(value = "INVOKE",
                    target = "Lnet/weibai/avaritia_more_items/common/blocks/abstracts/multiblock/recipe/MatchedMultiblockRecipe;matches(Lnet/weibai/avaritia_more_items/common/blocks/abstracts/multiblock/recipe/MultiblockRecipeInput;)Z"),
            remap = false)
    private boolean gt$matchThroughRecipe(MatchedMultiblockRecipe self, MultiblockRecipeInput input) {
        Level level = GTRecipeMatcherBridge.levelOf(this);
        return GTRecipeMatcherBridge.matches(self, input, level);
    }

    /**
     * Takes the items out with the recipe's own rules.
     *
     * <p>The finishing step drains the ports separately from its match check, through AMI's
     * exact-stack comparison. Left alone it would find no matching stack for a worn coolant cell
     * and fail the craft right at the end.</p>
     */
    @Redirect(
            method = "consumeRecipeInputs",
            at = @At(value = "INVOKE",
                    target = "Lnet/weibai/avaritia_more_items/common/blocks/abstracts/multiblock/recipe/MultiblockRecipeMatcher;consumeItems(Ljava/util/List;Ljava/util/List;)Z"),
            remap = false)
    private boolean gt$consumeThroughRecipe(List<ItemStack> wanted, List<IItemHandler> handlers) {
        return GTRecipeMatcherBridge.consumeItems(wanted, handlers, this.gt$recipe());
    }

    /** The recipe the tile is working on, or {@code null} before one has started. */
    @org.spongepowered.asm.mixin.Unique
    private net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MatchedMultiblockRecipe gt$recipe() {
        return ((net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.abstracts.AbstractMultiblockTile)
                (Object) this).getActiveRecipe();
    }
}
