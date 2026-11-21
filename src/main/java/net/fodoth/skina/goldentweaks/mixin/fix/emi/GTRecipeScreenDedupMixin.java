package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.screen.RecipeScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collapses duplicate recipe instances inside one category before the tabs are built.
 *
 * <p>EMI reloads twice when entering a world — once for resources, once when the synced recipe
 * manager arrives. Both passes can end up contributing pages for the same category, and
 * {@code RecipeScreen} takes the per-category list at face value, so a single reaction ends up
 * drawn twice on the same page.</p>
 *
 * <p>Deduplication happens here rather than at registration because the duplicate is not visible
 * there: the recipe list is rebuilt between the two reloads, and both generations are already merged
 * by the time a screen is opened. The screen's map is the first place the whole list is in hand.</p>
 *
 * <p>The key is the recipe id, which is what identifies "the same page" to a player, and the first
 * occurrence wins so the surviving instance is the one the manager currently reports. The map is
 * replaced rather than mutated: {@code init} may run more than once, and mutating a list owned by
 * the caller would keep trimming it.</p>
 */
@Mixin(value = RecipeScreen.class, remap = false)
public abstract class GTRecipeScreenDedupMixin {

    @Shadow
    private Map<EmiRecipeCategory, List<EmiRecipe>> recipes;

    @Inject(method = "init", at = @At("HEAD"))
    private void goldentweaks$dedupRecipes(CallbackInfo ci) {
        if (this.recipes == null || this.recipes.isEmpty()) {
            return;
        }

        Map<EmiRecipeCategory, List<EmiRecipe>> deduped = new LinkedHashMap<>(this.recipes.size());
        boolean changed = false;
        for (Map.Entry<EmiRecipeCategory, List<EmiRecipe>> entry : this.recipes.entrySet()) {
            List<EmiRecipe> original = entry.getValue();
            List<EmiRecipe> kept = goldentweaks$dedup(original);
            deduped.put(entry.getKey(), kept);
            if (kept.size() != original.size()) {
                changed = true;
            }
        }
        if (changed) {
            this.recipes = deduped;
        }
    }

    /** Keeps the first recipe seen per id; recipes without an id are always kept. */
    @Unique
    private static List<EmiRecipe> goldentweaks$dedup(List<EmiRecipe> recipes) {
        List<EmiRecipe> kept = new ArrayList<>(recipes.size());
        java.util.Set<Object> seen = new java.util.HashSet<>(recipes.size());
        for (EmiRecipe recipe : recipes) {
            Object key = recipe.getId() == null ? recipe : recipe.getId();
            if (seen.add(key)) {
                kept.add(recipe);
            }
        }
        return kept.size() == recipes.size() ? recipes : kept;
    }
}
