"""Workstations: the basic page lists both machines, the advanced page only the advanced one.

The advanced kettle runs the basic recipes too — that was the point of giving it both recipe types
— so a player looking at a basic reaction should see it as a valid machine for that page. The
advanced page is specific to the advanced recipes, so only the advanced kettle belongs there.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionEmiPlugin.java"
s = io.open(p, encoding="utf-8").read()

old = '''        registry.addCategory(GTChemicalReactionEmiRecipe.CATEGORY);
        registry.addWorkstation(GTChemicalReactionEmiRecipe.CATEGORY,
                EmiStack.of(new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get())));'''
new = '''        registry.addCategory(GTChemicalReactionEmiRecipe.CATEGORY);
        // The advanced kettle accepts the basic recipe set as well, so it is a valid machine for
        // this page — a player reading a basic reaction should see both.
        registry.addWorkstation(GTChemicalReactionEmiRecipe.CATEGORY,
                EmiStack.of(new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get())));
        registry.addWorkstation(GTChemicalReactionEmiRecipe.CATEGORY,
                EmiStack.of(new ItemStack(GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get())));'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("basic page lists both machines")

# ---- JEI catalyst lines, for packs without EMI ------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionJeiPlugin.java"
s2 = io.open(p2, encoding="utf-8").read()

old2 = '''        registration.addRecipeCatalyst(
                new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get()),
                GTChemicalReactionJeiCategory.TYPE);'''
new2 = '''        // Both machines serve the basic page: the advanced kettle runs the basic set too.
        registration.addRecipeCatalyst(
                new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get()),
                GTChemicalReactionJeiCategory.TYPE);
        registration.addRecipeCatalyst(
                new ItemStack(GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get()),
                GTChemicalReactionJeiCategory.TYPE);'''
assert old2 in s2
s2 = s2.replace(old2, new2, 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("JEI basic page lists both machines")
