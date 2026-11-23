"""Reorder so the type is declared before the serializer that names it.

Java rejects a forward reference from a field initialiser to a later field. `SERIALIZER` needs the
type, so the type has to come first — a plain reorder, not a workaround.
"""
import io

# ---- basic ------------------------------------------------------------------------------------
p1 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactionRecipes.java"
s1 = io.open(p1, encoding="utf-8").read()

old1 = '''    public static final DeferredHolder<RecipeSerializer<?>,
            GTChemicalReactionRecipe.Serializer> SERIALIZER =
            SERIALIZERS.register(GTChemicalReactionRecipe.ID,
                    () -> new GTChemicalReactionRecipe.Serializer(TYPE.get()));

    public static final DeferredHolder<RecipeType<?>, RecipeType<GTChemicalReactionRecipe>> TYPE =
            TYPES.register(GTChemicalReactionRecipe.ID,
                    () -> RecipeType.simple(
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                    GoldenTweaks.MODID, GTChemicalReactionRecipe.ID)));'''

new1 = '''    public static final DeferredHolder<RecipeType<?>, RecipeType<GTChemicalReactionRecipe>> TYPE =
            TYPES.register(GTChemicalReactionRecipe.ID,
                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(
                                    GoldenTweaks.MODID, GTChemicalReactionRecipe.ID)));

    // Declared after TYPE: the serializer names it, and a field initialiser may not forward-reference.
    public static final DeferredHolder<RecipeSerializer<?>,
            GTChemicalReactionRecipe.Serializer> SERIALIZER =
            SERIALIZERS.register(GTChemicalReactionRecipe.ID,
                    () -> new GTChemicalReactionRecipe.Serializer(TYPE.get()));'''
assert old1 in s1
s1 = s1.replace(old1, new1, 1)

if "import net.minecraft.resources.ResourceLocation;" not in s1:
    s1 = s1.replace("import net.minecraft.core.registries.Registries;",
                    "import net.minecraft.core.registries.Registries;\nimport net.minecraft.resources.ResourceLocation;", 1)

io.open(p1, "w", encoding="utf-8", newline="\n").write(s1)
print("basic: type declared before serializer")

# ---- advanced ---------------------------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactionRecipes.java"
s2 = io.open(p2, encoding="utf-8").read()

start = s2.index("    @SuppressWarnings(\"unused\")\n    public static final DeferredHolder<RecipeSerializer<?>,")
end = s2.index("    private GTAdvancedChemicalReactionRecipes() {")
block = s2[start:end]

new_block = '''    public static final DeferredHolder<RecipeType<?>, RecipeType<GTChemicalReactionRecipe>> TYPE =
            TYPES.register(DIRECTORY,
                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, DIRECTORY)));

    // Declared after TYPE: the serializer names it, and a field initialiser may not forward-reference.
    @SuppressWarnings("unused")
    public static final DeferredHolder<RecipeSerializer<?>,
            GTChemicalReactionRecipe.Serializer> SERIALIZER =
            SERIALIZERS.register(DIRECTORY,
                    () -> new GTChemicalReactionRecipe.Serializer(TYPE.get()));

'''
s2 = s2[:start] + new_block + s2[end:]
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("advanced: type declared before serializer")
