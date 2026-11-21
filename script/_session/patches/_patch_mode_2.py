import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------- field next to selectedChannel ----------
old = '''    private Integer activeChannel;'''
new = '''    private Integer activeChannel;

    /**
     * The mode being tried, or {@code null} when no mode is in force.
     *
     * <p>Set alongside {@link #selectedChannel} during a search and pinned with it when a craft
     * starts, so the run keeps matching the mode it began under.</p>
     */
    @Nullable
    private Integer selectedMode;

    /** The mode pinned for the running craft; {@code null} when none. */
    @Nullable
    private Integer activeMode;'''
s = s.replace(old, new, 1)

# ---------- the search itself ----------
anchor = '''    /** The id of a matched recipe, or {@code null} when the manager does not know it. */'''
new_search = '''    /**
     * Searches the applied recipe type, skipping recipes whose mode the current mode cannot serve.
     *
     * <p>AMI's own search walks every recipe and stops at the first that matches the ports. Adding a
     * mode filter means the walk has to be ours: the recipe list is the same, but a candidate is
     * only tried when {@link #modeAccepts} allows it. Everything else — how a candidate is matched
     * against the ports — still goes through AMI's matcher.</p>
     */
    @Nullable
    private MatchedMultiblockRecipe findRecipeForMode(Level level) {
        MultiblockRecipeInput input = createRecipeInput();
        if (input.isEmpty()) {
            return null;
        }
        for (RecipeHolder<GTChemicalReactionRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(GTChemicalReactionRecipes.TYPE.get())) {
            GTChemicalReactionRecipe recipe = holder.value();
            if (!modeAccepts(this.selectedMode, recipe.mode())) {
                continue;
            }
            MatchedMultiblockRecipe matched = MatchedMultiblockRecipe.of(recipe);
            if (matched.matches(input)) {
                return matched;
            }
        }
        return null;
    }

    /**
     * Whether a recipe's mode is eligible while {@code mode} is the one being tried.
     *
     * <p>Two wildcards meet here. A recipe marked mode {@code 0} serves every mode, and a {@code 0}
     * mode is itself "any": so a wildcard recipe runs under any mode, and the wildcard mode accepts
     * any recipe. A {@code null} mode means no filter is in force at all.</p>
     */
    private static boolean modeAccepts(@Nullable Integer mode, int recipeMode) {
        if (mode == null || mode == GTPortChannels.WILDCARD) {
            return true;
        }
        return recipeMode == GTPortChannels.WILDCARD || recipeMode == mode;
    }

    /** The id of a matched recipe, or {@code null} when the manager does not know it. */'''
s = s.replace(anchor, new_search, 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
