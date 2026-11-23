"""The mode-filtered search also walks every recipe type.

Kept as a separate patch because this loop has an early return: the outer pass over types must
stop as soon as any type yields a match, and must not let a cheaper rejected recipe from a later
type overwrite one recorded by an earlier type.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''        for (RecipeHolder<GTChemicalReactionRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(recipeType())) {
            GTChemicalReactionRecipe recipe = holder.value();
            if (recipe.mode() != (wanted == null ? GTPortChannels.WILDCARD : wanted)) {
                continue;
            }'''
new = '''        for (RecipeType<GTChemicalReactionRecipe> type : recipeTypes()) {
            for (RecipeHolder<GTChemicalReactionRecipe> holder : level.getRecipeManager()
                    .getAllRecipesFor(type)) {
                GTChemicalReactionRecipe recipe = holder.value();
                if (recipe.mode() != (wanted == null ? GTPortChannels.WILDCARD : wanted)) {
                    continue;
                }'''
assert old in s
s = s.replace(old, new, 1)

old_body = '''            // A recipe the ports cannot power is not a candidate at all. Checked here rather than at
            // start so the search moves past it and finds something the machine can actually run.
            if (!withinEnergyBudget(recipe.getEnergyPerTick())) {
                // Remembered so the status line can say why nothing started. A recipe skipped here is
                // indistinguishable from no recipe at all, otherwise.
                this.refusedForCost = true;
                // Zero doubles as "nothing recorded yet", so the first rejection wins outright.
                this.refusedCost = this.refusedCost == 0
                        ? recipe.getEnergyPerTick()
                        : Math.min(this.refusedCost, recipe.getEnergyPerTick());
                continue;
            }
            MatchedMultiblockRecipe matched = MatchedMultiblockRecipe.of(recipe);
            if (matched.matches(input)) {
                return matched;
            }
        }
        return null;'''
new_body = '''                // A recipe the ports cannot power is not a candidate at all. Checked here rather
                // than at start so the search moves past it and finds something the machine can
                // actually run.
                if (!withinEnergyBudget(recipe.getEnergyPerTick())) {
                    // Remembered so the status line can say why nothing started. A recipe skipped
                    // here is indistinguishable from no recipe at all, otherwise.
                    this.refusedForCost = true;
                    // Zero doubles as "nothing recorded yet", so the first rejection wins outright.
                    this.refusedCost = this.refusedCost == 0
                            ? recipe.getEnergyPerTick()
                            : Math.min(this.refusedCost, recipe.getEnergyPerTick());
                    continue;
                }
                MatchedMultiblockRecipe matched = MatchedMultiblockRecipe.of(recipe);
                if (matched.matches(input)) {
                    return matched;
                }
            }
        }
        return null;'''
assert old_body in s
s = s.replace(old_body, new_body, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("mode-filtered search walks every type")
