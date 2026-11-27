package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.processing.UuCraftingRecipes;
import com.miophas.singularity_iteration.common.processing.UuProcessingRecipes;
import com.miophas.singularity_iteration.common.processing.UuPricingLifecycle;
import com.miophas.singularity_iteration.core.runtime.uu.UuProcessingFamily.PricingPolicy;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Prices UU matter from the reference table alone when the solver is switched off.
 *
 * <p>SI derives every missing price by solving the whole crafting/processing recipe set as a system
 * of equations. That pass has a hard ceiling of 32768 recipes and throws as soon as a pack exceeds
 * it, which abandons the entire pricing run — so in a pack that ships JAOPCA (tens of thousands of
 * generated recipes) UU replication simply never works.</p>
 *
 * <p>Both {@code read} calls are redirected to an empty catalogs, which leaves the solver with
 * nothing but the reference prices it was seeded with. Those are authoritative anyway: the
 * {@code uu/reference} data pack states what a material is worth outright, and the solve only fills
 * in the items nobody priced. The result is a smaller quote book that still covers the reference
 * materials, rather than no quote book at all.</p>
 *
 * <p>Targeted at {@code rebuild} rather than at {@code read} itself: those two calls are its only
 * callers, and doing it here means the ceiling is never even reached, instead of being raised to
 * some larger number that a different pack could exceed again.</p>
 */
@Mixin(value = UuPricingLifecycle.class, remap = false)
public abstract class MioIcifUuReferenceOnlyMixin {

    @Redirect(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/miophas/singularity_iteration/common/processing/UuCraftingRecipes;read(Lnet/minecraft/world/item/crafting/RecipeManager;Lnet/minecraft/core/HolderLookup$Provider;)Lcom/miophas/singularity_iteration/common/processing/UuCraftingRecipes$Catalog;"),
            require = 1)
    private static UuCraftingRecipes.Catalog goldentweaks$skipCraftingRecipes(
            RecipeManager manager, HolderLookup.Provider registries) {
        if (GoldenTweaksCommonConfig.MIO_ICIF_DISABLE_UU_SOLVER.get()) {
            // Empty rule list and zero "unsupported": nothing to price, nothing to complain about.
            return new UuCraftingRecipes.Catalog(List.of(), 0);
        }
        return UuCraftingRecipes.read(manager, registries);
    }

    @Redirect(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/miophas/singularity_iteration/common/processing/UuProcessingRecipes;read(Lnet/minecraft/world/item/crafting/RecipeManager;Lnet/minecraft/core/HolderLookup$Provider;Lcom/miophas/singularity_iteration/core/runtime/uu/UuProcessingFamily$PricingPolicy;)Lcom/miophas/singularity_iteration/common/processing/UuProcessingRecipes$Catalog;"),
            require = 1)
    private static UuProcessingRecipes.Catalog goldentweaks$skipProcessingRecipes(
            RecipeManager manager, HolderLookup.Provider registries, PricingPolicy policy) {
        if (GoldenTweaksCommonConfig.MIO_ICIF_DISABLE_UU_SOLVER.get()) {
            return new UuProcessingRecipes.Catalog(List.of(), 0, 0);
        }
        return UuProcessingRecipes.read(manager, registries, policy);
    }
}
