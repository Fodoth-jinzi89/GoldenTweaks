"""Give the JEI category a per-machine uid and icon.

Both were fixed to the basic kettle, so JEI would have one category listing the basic machine's
recipes only, with no way to show the advanced set. The category now takes both, defaulting to the
basic machine so the existing construction site keeps working unchanged.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionJeiCategory.java"
s = io.open(p, encoding="utf-8").read()

old = '''    public static final RecipeType<GTChemicalReactionRecipe> TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, GTChemicalReactionRecipe.ID),
            GTChemicalReactionRecipe.class);

    /** One arrow cycle per craft, but never so short that it reads as static. */
    private static final int MIN_ARROW_TICKS = 10;

    private final IDrawable icon;

    public GTChemicalReactionJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get()));
    }

    @Override
    public @NotNull RecipeType<GTChemicalReactionRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("emi.category.goldentweaks.chemical_reaction");
    }'''

new = '''    /** The basic kettle's category. */
    public static final RecipeType<GTChemicalReactionRecipe> TYPE = type("chemical_reaction");

    /** The advanced kettle's category: same panel, entered separately. */
    public static final RecipeType<GTChemicalReactionRecipe> ADVANCED_TYPE =
            type("advanced_chemical_reaction");

    private static RecipeType<GTChemicalReactionRecipe> type(String path) {
        return new RecipeType<>(
                ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, path),
                GTChemicalReactionRecipe.class);
    }

    /** One arrow cycle per craft, but never so short that it reads as static. */
    private static final int MIN_ARROW_TICKS = 10;

    private final IDrawable icon;
    private final RecipeType<GTChemicalReactionRecipe> type;
    private final String titleKey;

    public GTChemicalReactionJeiCategory(IGuiHelper guiHelper) {
        this(guiHelper, TYPE, "chemical_reaction",
                new ItemStack(GTChemicalReactorMultiblock.MACHINE_ITEM.get()));
    }

    /**
     * @param type     the category this instance answers for
     * @param titleKey path under {@code emi.category.goldentweaks.} naming it
     * @param icon     the machine this set belongs to
     */
    public GTChemicalReactionJeiCategory(IGuiHelper guiHelper, RecipeType<GTChemicalReactionRecipe> type,
                                         String titleKey, ItemStack icon) {
        this.icon = guiHelper.createDrawableItemStack(icon);
        this.type = type;
        this.titleKey = "emi.category.goldentweaks." + titleKey;
    }

    @Override
    public @NotNull RecipeType<GTChemicalReactionRecipe> getRecipeType() {
        return this.type;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable(this.titleKey);
    }'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("JEI category takes its uid and icon")
