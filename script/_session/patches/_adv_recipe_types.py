"""The advanced kettle runs its own recipes and the basic kettle's.

Ordered with its own set first: when a pattern is defined in both, the machine-specific one should
win, since that is the file its author wrote for this machine.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''    @Override
    protected RecipeType<GTChemicalReactionRecipe> recipeType() {
        return GTAdvancedChemicalReactionRecipes.TYPE.get();
    }'''
new = '''    /**
     * Its own recipes, then the basic kettle's.
     *
     * <p>Own set first so a pattern defined for both resolves to the advanced one — that is the
     * file written specifically for this machine. The basic set follows so upgrading a basic build
     * keeps every pattern the player already made working.</p>
     */
    @Override
    protected List<RecipeType<GTChemicalReactionRecipe>> recipeTypes() {
        return List.of(
                GTAdvancedChemicalReactionRecipes.TYPE.get(),
                GTChemicalReactionRecipes.TYPE.get());
    }'''
assert old in s
s = s.replace(old, new, 1)

if "import java.util.List;" not in s:
    lines = s.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import "))
    lines.insert(last + 1, "import java.util.List;")
    s = "\n".join(lines)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("advanced kettle searches both recipe sets")
