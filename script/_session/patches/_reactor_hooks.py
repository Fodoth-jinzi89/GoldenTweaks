"""Let a subclass name itself and pick its own recipe type.

The name key and the recipe type are the last two machine-specific bits. Both become hooks with
the basic kettle's values as defaults, so existing behaviour is unchanged.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    @Override
    public Component getDisplayName() {
        return this.customName != null
                ? this.customName
                : Component.translatable("block.goldentweaks.chemical_plant_controller");
    }''',
'''    /** The translation key this machine is named by when the player has not renamed it. */
    protected String nameKey() {
        return "block.goldentweaks.chemical_plant_controller";
    }

    @Override
    public Component getDisplayName() {
        return this.customName != null ? this.customName : Component.translatable(nameKey());
    }''', 1)

s = s.replace('''    protected List<IMultiblockRecipeSource> getRecipeSources() {
        return List.of(IMultiblockRecipeSource.multiblock(GTChemicalReactionRecipes.TYPE.get()));
    }''',
'''    protected List<IMultiblockRecipeSource> getRecipeSources() {
        return List.of(IMultiblockRecipeSource.multiblock(recipeType()));
    }

    /**
     * The recipe type this machine searches.
     *
     * <p>A hook rather than a constant so a second machine can hold its own recipes: the search
     * code below works against whatever is returned here.</p>
     */
    protected net.minecraft.world.item.crafting.RecipeType<GTChemicalReactionRecipe> recipeType() {
        return GTChemicalReactionRecipes.TYPE.get();
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("name and recipe type are hooks")

# ---------------- the recipe manager lookups must follow the same hook ----------------
for needle, replacement in (
    ('''                .getAllRecipesFor(GTChemicalReactionRecipes.TYPE.get())) {''',
     '''                .getAllRecipesFor(recipeType())) {'''),
):
    s = io.open(p, encoding="utf-8").read()
    if needle in s:
        s = s.replace(needle, replacement)
        io.open(p, "w", encoding="utf-8", newline="\n").write(s)
        print("recipe lookup uses the hook")
