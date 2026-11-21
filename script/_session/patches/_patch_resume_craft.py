import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------------- tags ----------------
s = s.replace('''    private static final String TAG_RUNNING = "Running";''',
'''    private static final String TAG_RUNNING = "Running";
    /** The craft in flight, so leaving the world mid-reaction does not eat its inputs. */
    private static final String TAG_ACTIVE_RECIPE = "ActiveRecipe";
    private static final String TAG_ACTIVE_PROGRESS = "ActiveProgress";
    private static final String TAG_ACTIVE_LENGTH = "ActiveLength";
    private static final String TAG_ACTIVE_OVERCLOCK = "ActiveOverclock";''', 1)

# ---------------- save ----------------
s = s.replace('''        tag.putInt(TAG_PARALLEL_LIMIT, this.parallelLimit);
    }

    /**
     * AMI's update tag carries only the progress, the formed flag and its own buffers, so the''',
'''        tag.putInt(TAG_PARALLEL_LIMIT, this.parallelLimit);
        // A craft's inputs are taken when it starts, so a run that is not saved is a run whose
        // ingredients are destroyed by logging out. Saved by id rather than by value: the recipe
        // manager owns the instances, and one that no longer exists must simply not resume.
        if (this.activeRecipe != null) {
            ResourceLocation id = recipeIdOf(this.activeRecipe);
            if (id != null) {
                tag.putString(TAG_ACTIVE_RECIPE, id.toString());
                tag.putInt(TAG_ACTIVE_PROGRESS, this.progress);
                tag.putInt(TAG_ACTIVE_LENGTH, this.maxProgress);
                tag.putInt(TAG_ACTIVE_OVERCLOCK, this.activeOverclock);
            }
        }
    }

    /**
     * AMI's update tag carries only the progress, the formed flag and its own buffers, so the''', 1)

# ---------------- load ----------------
s = s.replace('''        // The shape has to be re-resolved after a load, and that scan must not switch the machine on.
        this.restoredFromDisk = true;''',
'''        // A craft that was in flight resumes where it stopped. Only the identity and the counters
        // are read here — the ports' contents were saved with the ports themselves, and the recipe
        // is looked up again once the shape exists, because there is no world to look it up in yet.
        this.pendingRecipe = tag.contains(TAG_ACTIVE_RECIPE)
                ? ResourceLocation.tryParse(tag.getString(TAG_ACTIVE_RECIPE))
                : null;
        this.pendingProgress = tag.getInt(TAG_ACTIVE_PROGRESS);
        this.pendingLength = tag.getInt(TAG_ACTIVE_LENGTH);
        this.activeOverclock = tag.getInt(TAG_ACTIVE_OVERCLOCK);
        // The shape has to be re-resolved after a load, and that scan must not switch the machine on.
        this.restoredFromDisk = true;''', 1)

# ---------------- fields ----------------
s = s.replace('''    private GTReactorStatus stopReason = GTReactorStatus.OFF;''',
'''    private GTReactorStatus stopReason = GTReactorStatus.OFF;

    /**
     * The craft read back from disk, waiting for the shape to be re-resolved.
     *
     * <p>{@code loadAdditional} runs before the world is reachable enough to ask the recipe manager
     * for a recipe, so the id is parked here and picked up on the first tick that has a structure.
     * Held as an id rather than a value because a datapack may have removed the recipe while the
     * world was closed, and that has to mean "nothing to resume" rather than a stale match.</p>
     */
    @Nullable
    private ResourceLocation pendingRecipe;
    private int pendingProgress;
    private int pendingLength;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
