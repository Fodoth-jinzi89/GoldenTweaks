import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''            ResourceLocation id = recipeIdOf(this.activeRecipe);
            GoldenTweaks.LOGGER.info("[GT-resume] save: recipe={} id={} progress={}/{}",
                    this.activeRecipe.recipe(), id, this.progress, this.maxProgress);
            if (id != null) {''',
'''            ResourceLocation id = recipeIdOf(this.activeRecipe);
            if (id != null) {''', 1)

s = s.replace('''        ResourceLocation wanted = this.pendingRecipe;
        GoldenTweaks.LOGGER.info("[GT-resume] resume attempt {} for {}", this.resumeAttempts, wanted);
        // Walked rather than looked up by key''',
'''        ResourceLocation wanted = this.pendingRecipe;
        // Walked rather than looked up by key''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('GT-resume left:', s.count('GT-resume'))
