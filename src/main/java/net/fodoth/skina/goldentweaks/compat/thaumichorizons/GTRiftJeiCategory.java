package net.fodoth.skina.goldentweaks.compat.thaumichorizons;

import com.kentington.thaumichorizons.common.planar.PlanarRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI category for rift crafting: one arrow in the middle of the page, mirroring Create's
 * "mysterious conversion" layout. The rift itself is only the category icon / catalyst.
 */
final class GTRiftJeiCategory implements IRecipeCategory<GTRiftDisplayRecipe> {

    private static final RecipeType<GTRiftDisplayRecipe> TYPE =
            RecipeType.create(GoldenTweaks.MODID, "rift_crafting", GTRiftDisplayRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    private GTRiftJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(GTRiftDisplayRecipes.WIDTH, GTRiftDisplayRecipes.HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(GTRiftDisplayRecipes.riftIcon());
        this.arrow = guiHelper.getRecipeArrow();
    }

    static void register(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new GTRiftJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    static void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(TYPE, GTRiftDisplayRecipes.all());
    }

    static void registerCatalyst(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(PlanarRegistry.VORTEX_ITEM.get(), TYPE);
    }

    @Override
    public RecipeType<GTRiftDisplayRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("goldentweaks.rift_crafting.category");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return GTRiftDisplayRecipes.WIDTH;
    }

    @Override
    public int getHeight() {
        return GTRiftDisplayRecipes.HEIGHT;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GTRiftDisplayRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder input = builder.addSlot(
                RecipeIngredientRole.INPUT,
                GTRiftDisplayRecipes.INPUT_X + 1,
                GTRiftDisplayRecipes.ROW_Y + 1
        );
        input.addItemStacks(inputStacks(recipe));

        if (recipe.hasOutput()) {
            builder.addSlot(
                            RecipeIngredientRole.OUTPUT,
                            GTRiftDisplayRecipes.OUTPUT_X + 1,
                            GTRiftDisplayRecipes.ROW_Y + 1
                    )
                    .addItemStack(recipe.output());
        }
    }

    @Override
    public void draw(
            GTRiftDisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        arrow.draw(guiGraphics, GTRiftDisplayRecipes.ARROW_X, GTRiftDisplayRecipes.ARROW_Y);

        if (recipe.hasNote()) {
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    recipe.note(),
                    GTRiftDisplayRecipes.NOTE_X,
                    GTRiftDisplayRecipes.NOTE_Y,
                    0x404040,
                    false
            );
        }
    }

    @Override
    public void getTooltip(
            ITooltipBuilder tooltip,
            GTRiftDisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        int x = GTRiftDisplayRecipes.ARROW_X;
        int y = GTRiftDisplayRecipes.ARROW_Y;
        int width = GTRiftDisplayRecipes.ARROW_WIDTH;
        int height = GTRiftDisplayRecipes.ARROW_HEIGHT;
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            tooltip.add(Component.translatable("goldentweaks.rift_crafting.rift"));
        }
    }

    /** Expands a tag input into the item stacks JEI can display. */
    private static List<ItemStack> inputStacks(GTRiftDisplayRecipe recipe) {
        TagKey<Item> tag = recipe.inputTag();
        if (tag == null) {
            ItemStack stack = recipe.inputStack();
            return stack == null ? List.of() : List.of(stack);
        }

        List<ItemStack> stacks = new ArrayList<>();
        BuiltInRegistries.ITEM.getTag(tag).ifPresent(holders ->
                holders.forEach(holder -> stacks.add(new ItemStack(holder.value()))));
        return stacks;
    }
}
