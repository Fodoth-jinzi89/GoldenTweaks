"""The EMI page can belong to more than one category.

The category was a single static constant, so every page — including the advanced kettle's — was
filed under the basic one. The category is now a constructor argument, with the basic one kept as
the default so existing call sites are unchanged.

The static constant stays as the basic kettle's category; the advanced page passes its own.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionEmiRecipe.java"
s = io.open(p, encoding="utf-8").read()

old_cat = '''    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, GTChemicalReactionRecipe.ID),
            EmiStack.of(new net.minecraft.world.item.ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get())));'''
new_cat = '''    /** The basic kettle's page. */
    public static final EmiRecipeCategory CATEGORY = category("chemical_reaction",
            GTChemicalReactorMultiblock.MACHINE_ITEM.get());

    /** The advanced kettle's page: same layout, its own entry in the sidebar. */
    public static final EmiRecipeCategory ADVANCED_CATEGORY = category("advanced_chemical_reaction",
            GTAdvancedChemicalReactorMultiblock.MACHINE_ITEM.get());

    private static EmiRecipeCategory category(String path, net.minecraft.world.item.Item icon) {
        return new EmiRecipeCategory(
                ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, path),
                EmiStack.of(new net.minecraft.world.item.ItemStack(icon)));
    }'''
assert old_cat in s
s = s.replace(old_cat, new_cat, 1)

old_fields = '''    private final ResourceLocation id;
    private final GTChemicalReactionRecipe recipe;'''
new_fields = '''    private final ResourceLocation id;
    private final GTChemicalReactionRecipe recipe;
    private final EmiRecipeCategory category;'''
assert old_fields in s
s = s.replace(old_fields, new_fields, 1)

old_ctor = '''    public GTChemicalReactionEmiRecipe(
            ResourceLocation id, GTChemicalReactionRecipe recipe, @Nullable HolderLookup.Provider registries) {
        this.id = id;
        this.recipe = recipe;'''
new_ctor = '''    public GTChemicalReactionEmiRecipe(
            ResourceLocation id, GTChemicalReactionRecipe recipe, @Nullable HolderLookup.Provider registries) {
        this(id, recipe, registries, CATEGORY);
    }

    public GTChemicalReactionEmiRecipe(
            ResourceLocation id, GTChemicalReactionRecipe recipe,
            @Nullable HolderLookup.Provider registries, EmiRecipeCategory category) {
        this.id = id;
        this.recipe = recipe;
        this.category = category;'''
assert old_ctor in s
s = s.replace(old_ctor, new_ctor, 1)

old_getcat = '''    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }'''
new_getcat = '''    @Override
    public EmiRecipeCategory getCategory() {
        return this.category;
    }'''
assert old_getcat in s
s = s.replace(old_getcat, new_getcat, 1)

if "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;" not in s:
    s = s.replace(
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMultiblock;",
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;\n"
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMultiblock;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("EMI page takes its category")
