"""Register the advanced kettle's EMI page alongside the basic one.

Two categories, each with its own workstation and its own recipe list, so the sidebar shows the two
machines separately instead of filing advanced recipes under the basic entry.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionEmiPlugin.java"
s = io.open(p, encoding="utf-8").read()

old = '''        registry.addCategory(GTChemicalReactionEmiRecipe.CATEGORY);
        registry.addWorkstation(GTChemicalReactionEmiRecipe.CATEGORY,
                EmiStack.of(new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get())));

        @Nullable HolderLookup.Provider registries = registries();
        registry.getRecipeManager()
                .getAllRecipesFor(GTChemicalReactionRecipes.TYPE.get())
                .forEach(holder -> registry.addRecipe(toEmiRecipe(holder, registries)));'''
new = '''        @Nullable HolderLookup.Provider registries = registries();

        registry.addCategory(GTChemicalReactionEmiRecipe.CATEGORY);
        registry.addWorkstation(GTChemicalReactionEmiRecipe.CATEGORY,
                EmiStack.of(new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get())));
        registry.getRecipeManager()
                .getAllRecipesFor(GTChemicalReactionRecipes.TYPE.get())
                .forEach(holder -> registry.addRecipe(
                        new GTChemicalReactionEmiRecipe(holder.id(), holder.value(), registries)));

        // The advanced kettle has its own entry: same panel, but filing its recipes under the basic
        // one would suggest the basic machine can run them.
        registry.addCategory(GTChemicalReactionEmiRecipe.ADVANCED_CATEGORY);
        registry.addWorkstation(GTChemicalReactionEmiRecipe.ADVANCED_CATEGORY,
                EmiStack.of(new ItemStack(GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get())));
        registry.getRecipeManager()
                .getAllRecipesFor(GTAdvancedChemicalReactionRecipes.TYPE.get())
                .forEach(holder -> registry.addRecipe(new GTChemicalReactionEmiRecipe(
                        holder.id(), holder.value(), registries,
                        GTChemicalReactionEmiRecipe.ADVANCED_CATEGORY)));'''
assert old in s
s = s.replace(old, new, 1)

# the old helper is unused now
old_helper = '''    private static GTChemicalReactionEmiRecipe toEmiRecipe(
            RecipeHolder<GTChemicalReactionRecipe> holder, @Nullable HolderLookup.Provider registries) {
        return new GTChemicalReactionEmiRecipe(holder.id(), holder.value(), registries);
    }

'''
s = s.replace(old_helper, "", 1)

for imp in [
    "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactionRecipes;",
    "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;",
]:
    if imp not in s:
        s = s.replace("import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipe;",
                      imp + "\nimport net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactionRecipe;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("advanced EMI category registered")
