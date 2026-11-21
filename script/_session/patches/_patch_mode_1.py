import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------- 1. mode ordering helpers, placed after channelGroups() ----------
anchor = '''    /**
     * The channels a bound port declares, read from the port's own block entity.'''
new_helpers = '''    /**
     * The modes to try, in the order to try them.
     *
     * <p>Derived from the ports of the current group, ranked by the same priority order the ports
     * themselves use. A port's mode field lists the modes it serves, and {@code 0} is the wildcard:
     * such a port serves every mode and therefore contributes no entry of its own.</p>
     *
     * <p>So a group whose ports are ranked {@code -2, 3, 4, 21, 1, 0} yields the mode order
     * {@code -2, 3, 4, 21, 1} — the wildcard port adds nothing to the list. The wildcard mode is
     * still appended last, so a group made only of wildcard ports still has one mode to run in.</p>
     */
    private List<Integer> modeOrder() {
        if (this.structure == null) {
            return List.of(GTPortChannels.WILDCARD);
        }

        // Walk the ports in priority order, so the modes come out in the same ranking the ports do.
        // Entries are added once, in first-seen order: a mode named by several ports belongs to all
        // of them and needs only one attempt.
        LinkedHashSet<Integer> ordered = new LinkedHashSet<>();
        boolean anyWildcardPort = false;
        for (MultiblockPortBinding port : sortedPorts()) {
            List<Integer> modes = modesOf(port);
            if (modes.contains(GTPortChannels.WILDCARD)) {
                anyWildcardPort = true;
                continue;
            }
            ordered.addAll(modes);
        }

        List<Integer> modes = new ArrayList<>(ordered);
        // The wildcard mode is last for the same reason the wildcard channel is: it can match what
        // every other mode already tried, so it is the fallback rather than the first choice.
        modes.add(GTPortChannels.WILDCARD);
        return modes;
    }

    /** The modes a bound port declares, read from the port's own block entity. */
    private List<Integer> modesOf(MultiblockPortBinding port) {
        if (this.level != null
                && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
            return entity.modes();
        }
        return List.of(GTPortChannels.WILDCARD);
    }

    /**
     * The channels a bound port declares, read from the port's own block entity.'''

s = s.replace(anchor, new_helpers, 1)

# ---------- 2. route the search through the mode order ----------
old_find = '''    @Nullable
    private MatchedMultiblockRecipe findLockedRecipe(Level level) {
        for (int channel : channelGroups()) {
            this.selectedChannel = channel;
            MatchedMultiblockRecipe found = matchOneGroup(level);
            if (found != null) {
                return found;
            }
        }
        this.selectedChannel = null;
        return null;
    }'''
new_find = '''    @Nullable
    private MatchedMultiblockRecipe findLockedRecipe(Level level) {
        for (int channel : channelGroups()) {
            this.selectedChannel = channel;
            // Within a group the ports are ranked, and the modes are tried in that same ranking:
            // the most important port's mode gets the first attempt, then the next, and so on down.
            // A mode that yields nothing simply hands over to the one below it.
            for (int mode : modeOrder()) {
                this.selectedMode = mode;
                MatchedMultiblockRecipe found = matchOneGroup(level);
                if (found != null) {
                    return found;
                }
            }
        }
        this.selectedChannel = null;
        this.selectedMode = null;
        return null;
    }'''
s = s.replace(old_find, new_find, 1)

# ---------- 3. mode filtering inside one group's attempt ----------
old_match = '''    /** One group's attempt: the locked recipe if there is one, otherwise a full search. */
    @Nullable
    private MatchedMultiblockRecipe matchOneGroup(Level level) {
        if (!this.lockRecipe || this.lockedRecipeId == null) {
            return findRecipe(level);
        }

        RecipeHolder<?> holder = level.getRecipeManager().byKey(this.lockedRecipeId).orElse(null);
        if (holder == null || !(holder.value() instanceof GTChemicalReactionRecipe recipe)) {
            return null;
        }'''
new_match = '''    /** One group's attempt: the locked recipe if there is one, otherwise a full search. */
    @Nullable
    private MatchedMultiblockRecipe matchOneGroup(Level level) {
        if (!this.lockRecipe || this.lockedRecipeId == null) {
            return findRecipeForMode(level);
        }

        RecipeHolder<?> holder = level.getRecipeManager().byKey(this.lockedRecipeId).orElse(null);
        if (holder == null || !(holder.value() instanceof GTChemicalReactionRecipe recipe)) {
            return null;
        }
        // A locked recipe still has to belong to the mode being tried: the lock names a recipe, not
        // a permission to ignore the ports that make it.
        if (!modeAccepts(this.selectedMode, recipe.mode())) {
            return null;
        }'''
s = s.replace(old_match, new_match, 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
