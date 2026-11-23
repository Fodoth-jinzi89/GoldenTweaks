"""Let a machine search more than one recipe type.

The advanced kettle is meant to run the basic kettle's recipes as well as its own, but every
search asked for exactly one type, supplied by `recipeType()`. Run the basic recipe set through a
second type and nothing looked for it.

`recipeTypes()` is now a list — one entry for the basic machine, two for the advanced — and the
three searches walk all of it. `recipeType()` stays as the single-value accessor for AMI's recipe
source, which takes one type per source; the base implementation reports the first of the list so
the two can never disagree about which is "the" type.

`getRecipeSources` also takes a source per type, so AMI's own matching pass sees both sets.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

# ---- 1. the hook becomes a list ---------------------------------------------------------------
old_hook = '''    /**
     * The recipe type this machine searches.
     *
     * <p>A hook rather than a constant so a second machine can hold its own recipes: the search
     * code below works against whatever is returned here.</p>
     */
    protected RecipeType<GTChemicalReactionRecipe> recipeType() {
        return GTChemicalReactionRecipes.TYPE.get();
    }'''
new_hook = '''    /**
     * The recipe types this machine searches, in priority order.
     *
     * <p>A list rather than a single type because a machine may accept more than one set: the
     * advanced kettle runs its own recipes <em>and</em> the basic kettle's, so that upgrading a
     * build does not orphan every pattern the player already made.</p>
     */
    protected List<RecipeType<GTChemicalReactionRecipe>> recipeTypes() {
        return List.of(GTChemicalReactionRecipes.TYPE.get());
    }

    /**
     * The recipe type this machine reports to AMI.
     *
     * <p>AMI's recipe source takes one type, so this reports the first of {@link #recipeTypes()}.
     * Deriving it rather than declaring it separately keeps the two from disagreeing about which
     * set is "the" one.</p>
     */
    protected RecipeType<GTChemicalReactionRecipe> recipeType() {
        return recipeTypes().getFirst();
    }'''
assert old_hook in s
s = s.replace(old_hook, new_hook, 1)

# ---- 2. AMI sources: one per type --------------------------------------------------------------
old_sources = '''        return List.of(IMultiblockRecipeSource.multiblock(recipeType()));'''
new_sources = '''        return recipeTypes().stream()
                .map(IMultiblockRecipeSource::multiblock)
                .toList();'''
assert old_sources in s
s = s.replace(old_sources, new_sources, 1)

# ---- 3. the three searches walk every type -----------------------------------------------------
old_resume = '''        GTChemicalReactionRecipe found = null;
        for (RecipeHolder<GTChemicalReactionRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(recipeType())) {
            if (holder.id().equals(wanted)) {
                found = holder.value();
                break;
            }
        }'''
new_resume = '''        GTChemicalReactionRecipe found = null;
        for (RecipeType<GTChemicalReactionRecipe> type : recipeTypes()) {
            for (RecipeHolder<GTChemicalReactionRecipe> holder : level.getRecipeManager()
                    .getAllRecipesFor(type)) {
                if (holder.id().equals(wanted)) {
                    found = holder.value();
                    break;
                }
            }
            if (found != null) {
                break;
            }
        }'''
assert old_resume in s
s = s.replace(old_resume, new_resume, 1)

old_id = '''        if (this.level != null) {
            for (RecipeHolder<GTChemicalReactionRecipe> holder : this.level.getRecipeManager()
                    .getAllRecipesFor(recipeType())) {
                if (holder.value() == matched.recipe()) {
                    return holder.id();
                }
            }
        }'''
new_id = '''        if (this.level != null) {
            for (RecipeType<GTChemicalReactionRecipe> type : recipeTypes()) {
                for (RecipeHolder<GTChemicalReactionRecipe> holder : this.level.getRecipeManager()
                        .getAllRecipesFor(type)) {
                    if (holder.value() == matched.recipe()) {
                        return holder.id();
                    }
                }
            }
        }'''
assert old_id in s
s = s.replace(old_id, new_id, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("three searches now walk every type")
print("recipeTypes occurrences:", s.count("recipeTypes()"))
