package net.fodoth.skina.goldentweaks.compat.ae2helpers;

import appeng.menu.me.items.CraftingTermMenu;
import appeng.util.CraftingRecipeUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import rearth.ae2helpers.client.AutoCraftingWatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Shared bridge for ae2helpers' auto-import feature inside WCWT.
 * Both WCWT's JEI handlers and its EMI handler funnel crafting transfers through
 * WirelessComprehensiveWorkTerminalMenu, but they never reach AE2's own
 * CraftingHelper that ae2helpers normally hooks. This helper re-applies the same
 * pending-slot registration on the actual WCWT transfer path.
 */
public final class WcwtPendingHelper {
    private static Map<Integer, Ingredient> stagedPending = Map.of();
    private static Set<Integer> stagedCraftable = Set.of();

    private WcwtPendingHelper() {
    }

    public static void setPending(CraftingTermMenu menu, CraftingRecipe recipe) {
        if (!AutoCraftingWatcher.INSTANCE.isAutoInsertEnabled()) {
            return;
        }
        if (!recipe.canCraftInDimensions(3, 3)) {
            return;
        }

        NonNullList<Ingredient> matrix = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);
        Map<Integer, Ingredient> pending = new HashMap<>();
        for (int i = 0; i < matrix.size(); i++) {
            Ingredient ingredient = matrix.get(i);
            if (!ingredient.isEmpty()) {
                pending.put(i, ingredient);
            }
        }

        AutoCraftingWatcher.INSTANCE.setPending(pending, menu.findMissingIngredients(pending).craftableSlots());
    }

    public static void stagePending(CraftingTermMenu menu, CraftingRecipe recipe) {
        stagedPending = Map.of();
        stagedCraftable = Set.of();
        if (!AutoCraftingWatcher.INSTANCE.isAutoInsertEnabled() || !recipe.canCraftInDimensions(3, 3)) {
            return;
        }

        NonNullList<Ingredient> matrix = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);
        Map<Integer, Ingredient> pending = new HashMap<>();
        for (int i = 0; i < matrix.size(); i++) {
            Ingredient ingredient = matrix.get(i);
            if (!ingredient.isEmpty()) {
                pending.put(i, ingredient);
            }
        }

        Set<Integer> craftable = menu.findMissingIngredients(pending).craftableSlots();
        if (!craftable.isEmpty()) {
            stagedPending = Map.copyOf(pending);
            stagedCraftable = Set.copyOf(craftable);
        }
    }

    public static void activateStaged() {
        if (!stagedCraftable.isEmpty()) {
            AutoCraftingWatcher.INSTANCE.setPending(stagedPending, stagedCraftable);
        }
        stagedPending = Map.of();
        stagedCraftable = Set.of();
    }
}
