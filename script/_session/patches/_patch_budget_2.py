import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- the unlocked search: skip recipes that cannot be powered ----
s = s.replace('''            if (recipe.mode() != (wanted == null ? GTPortChannels.WILDCARD : wanted)) {
                continue;
            }
            MatchedMultiblockRecipe matched = MatchedMultiblockRecipe.of(recipe);
            if (matched.matches(input)) {
                return matched;
            }''',
'''            if (recipe.mode() != (wanted == null ? GTPortChannels.WILDCARD : wanted)) {
                continue;
            }
            // A recipe the ports cannot power is not a candidate at all. Checked here rather than at
            // start so the search moves past it and finds something the machine can actually run.
            if (!withinEnergyBudget(recipe.getEnergyPerTick())) {
                continue;
            }
            MatchedMultiblockRecipe matched = MatchedMultiblockRecipe.of(recipe);
            if (matched.matches(input)) {
                return matched;
            }''', 1)

# ---- the locked path: refuse a lock that outgrew the ports ----
s = s.replace('''        if (!modeAccepts(this.selectedMode, recipe.mode())) {
            return null;
        }''',
'''        if (!modeAccepts(this.selectedMode, recipe.mode())) {
            return null;
        }
        // The lock names a recipe, not permission to drain a budget the ports no longer have — a
        // player who removes an energy hatch must not keep running the old, costlier recipe.
        if (!withinEnergyBudget(recipe.getEnergyPerTick())) {
            return null;
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('budget checks:', s.count('withinEnergyBudget('))
