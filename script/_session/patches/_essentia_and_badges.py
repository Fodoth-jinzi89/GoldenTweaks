"""Essentia and chemicals render with an icon and a badge on the EMI page.

Essentia produced `null`, which the page turns into an empty cell — the aspect simply was not
there. It now returns the custom stack written for it, so the icon draws and hover names it.

Chemicals already resolve through Mekanism's own EMI stack, so they draw; what they lacked is the
amount badge. EMI only prints a count for its item flavour, so the grid has to add it — the same
thing it already does for fluids.
"""
import io

# ---- 1. essentia becomes a real ingredient ----------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTReactionStacks.java"
s = io.open(p, encoding="utf-8").read()

old = '''            // Essentia has no EMI stack: Thaumcraft aspects are not registry entries and carry no
            // item of their own. The list draws the entry by name instead of by icon.
            case ESSENTIA -> null;'''
new = '''            // Essentia has no stack of its own — aspects are not registry entries and ship no item
            // — so one is supplied here. It draws through Thaumcraft's renderer and prints the
            // amount as a whole-unit badge, matching the port window.
            case ESSENTIA -> new GTEssentiaEmiStack(stack.essentiaTag(), stack.amount());'''
assert old in s
s = s.replace(old, new, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("essentia resolves to a drawable stack")

# ---- 2. the page keeps the raw stacks, for the badge -----------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionEmiRecipe.java"
s2 = io.open(p2, encoding="utf-8").read()

s2 = s2.replace('''    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> outputs;''',
'''    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> outputs;
    /** The data-pack entries behind the grids, kept for the amount badges. */
    private final List<GTReactionStack> inputStacks;
    private final List<GTReactionStack> outputStacks;''', 1)

s2 = s2.replace('''        this.inputs = convert(recipe.inputs(), registries);
        this.outputs = convert(recipe.outputs(), registries);''',
'''        this.inputStacks = recipe.inputs();
        this.outputStacks = recipe.outputs();
        this.inputs = convert(recipe.inputs(), registries);
        this.outputs = convert(recipe.outputs(), registries);''', 1)

s2 = s2.replace('''        widgets.add(new GTChemicalReactionPagedGrid(
                this.inputs, GTChemicalReactionLayout.INPUT_X, GTChemicalReactionLayout.GRID_Y, widgets));
        widgets.add(new GTChemicalReactionPagedGrid(
                this.outputs, GTChemicalReactionLayout.OUTPUT_X, GTChemicalReactionLayout.GRID_Y, widgets));''',
'''        widgets.add(new GTChemicalReactionPagedGrid(
                this.inputs, this.inputStacks,
                GTChemicalReactionLayout.INPUT_X, GTChemicalReactionLayout.GRID_Y, widgets));
        widgets.add(new GTChemicalReactionPagedGrid(
                this.outputs, this.outputStacks,
                GTChemicalReactionLayout.OUTPUT_X, GTChemicalReactionLayout.GRID_Y, widgets));''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("page passes the raw stacks to its grids")
