import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    private void resumePendingRecipe(Level level) {
        if (this.pendingRecipe == null) {
            return;
        }
        ResourceLocation wanted = this.pendingRecipe;
        // Cleared first: a recipe that cannot be found must not be retried every tick.
        this.pendingRecipe = null;

        if (this.activeRecipe != null) {
            return;
        }
        // Walked rather than looked up by key: that is the same search the matcher already does, and
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
''',
'''    private void resumePendingRecipe(Level level) {
        if (this.pendingRecipe == null) {
            return;
        }
        // Something already owns the machine — never overwrite a running craft with a stale one.
        if (this.activeRecipe != null) {
            this.pendingRecipe = null;
            return;
        }

        ResourceLocation wanted = this.pendingRecipe;
        // Walked rather than looked up by key: that is the same search the matcher already does, and
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
            // Deliberately not cleared. A load hands the entity back before the recipe manager has
            // necessarily synced, so an empty result here means "ask again", not "the recipe is
            // gone" — giving up on the first tick would drop the craft on every single reload.
            if (++this.resumeAttempts < RESUME_ATTEMPTS) {
                return;
            }
            this.pendingRecipe = null;
            this.pendingProgress = 0;
            this.pendingLength = 0;
            return;
        }

        this.pendingRecipe = null;
        this.resumeAttempts = 0;
''', 1)

s = s.replace('''    private int settleTicks;''',
'''    private int settleTicks;

    /**
     * How many ticks the saved craft has been looked for without finding it yet.
     *
     * <p>A load returns the block entity before the recipe manager has synced on a cold start, so the
     * first few lookups legitimately find nothing. Counted rather than retried forever, because a
     * recipe that really was removed has to stop being asked about.</p>
     */
    private int resumeAttempts;''', 1)

s = s.replace('''    private static final int SETTLE_TICKS = 40;''',
'''    private static final int SETTLE_TICKS = 40;

    /** Lookups allowed before a saved craft is presumed gone. A cold start needs a handful. */
    private static final int RESUME_ATTEMPTS = 200;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
