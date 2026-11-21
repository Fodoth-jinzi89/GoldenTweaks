import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# Pick the parked craft up as soon as there is a shape and a world to resolve it in.
s = s.replace('''        if (isFormed()) {
            return;
        }
        if (--this.scanCountdown > 0) {''',
'''        if (isFormed()) {
            // The shape exists now, so a craft parked by the last save can be looked up and resumed.
            resumePendingRecipe(level);
            return;
        }
        if (--this.scanCountdown > 0) {''', 1)

# The resume itself, placed next to the other recipe-start paths.
s = s.replace('''    /**
     * Finds the recipe this machine may start, honouring the recipe lock and the channel groups.''',
'''    /**
     * Re-attaches the craft that was running when the world was saved.
     *
     * <p>The inputs were already taken when it started, so dropping it on load would destroy them
     * with nothing to show for it — the exact complaint this is here to answer. The recipe is looked
     * up by id: if a datapack removed it while the world was closed there is nothing to resume, and
     * the machine simply starts fresh.</p>
     *
     * <p>Progress is clamped to the stored length, which in turn is not trusted blindly: a stale
     * length larger than the recipe's own is replaced by the recipe's, so a hand-edited save cannot
     * make one craft run forever.</p>
     */
    private void resumePendingRecipe(Level level) {
        if (this.pendingRecipe == null) {
            return;
        }
        ResourceLocation wanted = this.pendingRecipe;
        // Cleared first: a recipe that cannot be found must not be retried every tick.
        this.pendingRecipe = null;

        if (this.activeRecipe != null) {
            return;
        }
        RecipeHolder<GTChemicalReactionRecipe> holder = level.getRecipeManager()
                .byKey(Registries.RECIPE, wanted)
                .filter(found -> found.value() instanceof GTChemicalReactionRecipe)
                .map(found -> new RecipeHolder<>(wanted, (GTChemicalReactionRecipe) found.value()))
                .orElse(null);
        if (holder == null) {
            this.pendingProgress = 0;
            this.pendingLength = 0;
            return;
        }

        MatchedMultiblockRecipe resumed = MatchedMultiblockRecipe.of(holder.value());
        int length = resumed.processingTime();
        // The saved length is only trusted as far as the recipe agrees with it: the overclock that
        // produced it may no longer be reachable, and a longer run would never end.
        this.maxProgress = Math.max(1, Math.min(this.pendingLength, overclockedLength(length,
                Math.max(0, this.activeOverclock))));
        this.progress = Math.max(0, Math.min(this.pendingProgress, this.maxProgress));
        this.activeRecipe = resumed;
        this.activeProducts = productsOf(resumed);
        this.activeProcessingTime = resumed.processingTime();
        this.pendingProgress = 0;
        this.pendingLength = 0;
        // The craft owns the ports again, so their channels are frozen exactly as they were.
        setChannelsLocked(true);
        this.setChanged();
        syncToClient();
        applyRunningState();
        broadcastProducts();
    }

    /**
     * Finds the recipe this machine may start, honouring the recipe lock and the channel groups.''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
