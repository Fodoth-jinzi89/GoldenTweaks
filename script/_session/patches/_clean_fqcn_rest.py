"""Replace the remaining inline package names in this session's files.

Each substitution is a straight text swap plus the import that makes it legal.
"""
import io

def add_import(s, line, anchor=None):
    if line in s:
        return s
    if anchor and anchor in s:
        return s.replace(anchor, line + "\n" + anchor, 1)
    # Fall back to placing it after the last existing import.
    lines = s.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import "))
    lines.insert(last + 1, line)
    return "\n".join(lines)


def patch(path, replacements, imports):
    s = io.open(path, encoding="utf-8").read()
    for old, new in replacements:
        s = s.replace(old, new)
    for line in imports:
        s = add_import(s, line)
    io.open(path, "w", encoding="utf-8", newline="\n").write(s)
    residue = [l for l in s.split("\n")
               if not l.startswith(("import ", "package ", "*", "//", "/*"))
               and any(t in l for t in ("net.minecraft.", "net.neoforged.", "thaumcraft.",
                                        "mekanism.", "dev.emi."))]
    print(path.split("/")[-1], "residue:", len(residue))
    for r in residue[:5]:
        print("   ", r.strip()[:100])


# ---------------- the entity ----------------
patch(
    r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java",
    [
        ("net.minecraft.world.level.block.entity.BlockEntityType<?> type,",
         "BlockEntityType<?> type,"),
        ("protected net.minecraft.world.level.block.Block casingBlock() {",
         "protected Block casingBlock() {"),
        ("protected net.minecraft.world.item.crafting.RecipeType<GTChemicalReactionRecipe> recipeType() {",
         "protected RecipeType<GTChemicalReactionRecipe> recipeType() {"),
    ],
    [
        "import net.minecraft.world.level.block.entity.BlockEntityType;",
        "import net.minecraft.world.item.crafting.RecipeType;",
    ],
)

# ---------------- the advanced entity ----------------
patch(
    r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorBlockEntity.java",
    [
        ("protected net.minecraft.world.item.crafting.RecipeType<GTChemicalReactionRecipe> recipeType() {",
         "protected RecipeType<GTChemicalReactionRecipe> recipeType() {"),
    ],
    ["import net.minecraft.world.item.crafting.RecipeType;"],
)

# ---------------- the advanced recipe registry ----------------
patch(
    r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactionRecipes.java",
    [
        ("""                    () -> RecipeType.simple(
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                    GoldenTweaks.MODID, DIRECTORY)));""",
         """                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, DIRECTORY)));"""),
    ],
    ["import net.minecraft.resources.ResourceLocation;"],
)

# ---------------- the screen ----------------
patch(
    r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java",
    [
        ("long phase = net.minecraft.Util.getMillis() %",
         "long phase = Util.getMillis() %"),
        ("var aspect = thaumcraft.api.aspects.Aspect.getAspect(tag);",
         "var aspect = GTAspectNames.of(tag);"),
        ("return aspect == null\n                    ? Component.literal(tag)\n                    : Component.literal(aspect.getName());",
         "return aspect == null ? Component.literal(tag) : aspect;"),
    ],
    [
        "import net.minecraft.Util;",
        "import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectNames;",
    ],
)

# ---------------- the client events ----------------
patch(
    r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorClientEvents.java",
    [
        ("""        event.register(net.fodoth.skina.goldentweaks.compat.avaritia_more_items
                        .GTAdvancedChemicalReactorMultiblock.MENU.get(),
                GTAdvancedChemicalReactorScreen::new);""",
         """        event.register(GTAdvancedChemicalReactorMultiblock.MENU.get(),
                GTAdvancedChemicalReactorScreen::new);"""),
    ],
    ["import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;"],
)
