"""Register the advanced kettle's JEI category too."""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionJeiPlugin.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''        registration.addRecipeCategories(
                new GTChemicalReactionJeiCategory(registration.getJeiHelpers().getGuiHelper()));''',
'''        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new GTChemicalReactionJeiCategory(guiHelper),
                new GTChemicalReactionJeiCategory(guiHelper,
                        GTChemicalReactionJeiCategory.ADVANCED_TYPE, "advanced_chemical_reaction",
                        new ItemStack(GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get())));''', 1)

s = s.replace('''        var recipes = level.getRecipeManager()
                .getAllRecipesFor(GTChemicalReactionRecipes.TYPE.get())
                .stream()
                // addRecipes takes the recipe values, not the holders; the page needs no id.
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(GTChemicalReactionJeiCategory.TYPE, recipes);''',
'''        // addRecipes takes the recipe values, not the holders; the page needs no id.
        registration.addRecipes(GTChemicalReactionJeiCategory.TYPE,
                valuesOf(level, GTChemicalReactionRecipes.TYPE.get()));
        registration.addRecipes(GTChemicalReactionJeiCategory.ADVANCED_TYPE,
                valuesOf(level, GTAdvancedChemicalReactionRecipes.TYPE.get()));''', 1)

s = s.replace('''        registration.addRecipeCatalyst(
                new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get()),
                GTChemicalReactionJeiCategory.TYPE);''',
'''        registration.addRecipeCatalyst(
                new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get()),
                GTChemicalReactionJeiCategory.TYPE);
        registration.addRecipeCatalyst(
                new ItemStack(GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get()),
                GTChemicalReactionJeiCategory.ADVANCED_TYPE);''', 1)

s = s.replace('''    /** See the class comment: real JEI only, and only when the machine itself exists. */''',
'''    /** The recipes of one type, as plain values. */
    private static List<GTChemicalReactionRecipe> valuesOf(
            net.minecraft.world.level.Level level,
            net.minecraft.world.item.crafting.RecipeType<GTChemicalReactionRecipe> type) {
        return level.getRecipeManager().getAllRecipesFor(type).stream()
                .map(RecipeHolder::value)
                .toList();
    }

    /** See the class comment: real JEI only, and only when the machine itself exists. */''', 1)

for imp in [
    "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactionRecipes;",
    "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;",
    "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipe;",
    "import java.util.List;",
]:
    if imp not in s:
        s = s.replace("import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipes;",
                      imp + "\nimport net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipes;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("advanced JEI category registered")
