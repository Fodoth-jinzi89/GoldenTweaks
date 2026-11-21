import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''        RecipeHolder<GTChemicalReactionRecipe> holder = level.getRecipeManager()
                .byKey(Registries.RECIPE, wanted)
                .filter(found -> found.value() instanceof GTChemicalReactionRecipe)
                .map(found -> new RecipeHolder<>(wanted, (GTChemicalReactionRecipe) found.value()))
                .orElse(null);
        if (holder == null) {
            this.pendingProgress = 0;
            this.pendingLength = 0;
            return;
        }

        MatchedMultiblockRecipe resumed = MatchedMultiblockRecipe.of(holder.value());''',
'''        // Walked rather than looked up by key: that is the same search the matcher already does, and
        // it keeps this independent of the recipe manager's lookup overloads.
        GTChemicalReactionRecipe found = null;
        for (RecipeHolder<GTChemicalReactionRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(GTChemicalReactionRecipes.TYPE.get())) {
            if (holder.id().equals(wanted)) {
                found = holder.value();
                break;
            }
        }
        if (found == null) {
            this.pendingProgress = 0;
            this.pendingLength = 0;
            return;
        }

        MatchedMultiblockRecipe resumed = MatchedMultiblockRecipe.of(found);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
