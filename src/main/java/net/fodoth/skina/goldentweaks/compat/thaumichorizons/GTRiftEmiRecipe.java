package net.fodoth.skina.goldentweaks.compat.thaumichorizons;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * EMI recipe for one rift crafting row. The rift itself is drawn as its block icon instead of the
 * usual question mark placeholder.
 */
final class GTRiftEmiRecipe implements EmiRecipe {

    private static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "rift_crafting"),
            EmiStack.of(GTRiftDisplayRecipes.riftIcon()));

    static void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(GTRiftDisplayRecipes.riftIcon()));

        for (GTRiftDisplayRecipe recipe : GTRiftDisplayRecipes.all()) {
            registry.addRecipe(new GTRiftEmiRecipe(recipe));
        }
    }

    private final GTRiftDisplayRecipe recipe;
    private final EmiIngredient input;
    private final EmiStack output;

    private GTRiftEmiRecipe(GTRiftDisplayRecipe recipe) {
        this.recipe = recipe;
        this.input = toIngredient(recipe);
        this.output = recipe.hasOutput() ? EmiStack.of(recipe.output()) : EmiStack.EMPTY;
    }

    private static EmiIngredient toIngredient(GTRiftDisplayRecipe recipe) {
        TagKey<Item> tag = recipe.inputTag();
        if (tag != null) {
            return EmiIngredient.of(tag);
        }

        ItemStack stack = recipe.inputStack();
        return stack == null ? EmiStack.EMPTY : EmiStack.of(stack);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return recipe.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return recipe.hasOutput() ? List.of(output) : List.of();
    }

    @Override
    public int getDisplayWidth() {
        return GTRiftDisplayRecipes.WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return GTRiftDisplayRecipes.HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(input, GTRiftDisplayRecipes.INPUT_X, GTRiftDisplayRecipes.ROW_Y);
        widgets.addFillingArrow(GTRiftDisplayRecipes.ARROW_INPUT_X, GTRiftDisplayRecipes.ARROW_Y, 40);
        drawRift(widgets);
        widgets.addFillingArrow(GTRiftDisplayRecipes.ARROW_RIFT_X, GTRiftDisplayRecipes.ARROW_Y, 40);

        if (recipe.hasOutput()) {
            widgets.addSlot(output, GTRiftDisplayRecipes.OUTPUT_X, GTRiftDisplayRecipes.ROW_Y);
        }

        if (recipe.hasNote()) {
            widgets.addText(recipe.note(), GTRiftDisplayRecipes.NOTE_X, GTRiftDisplayRecipes.NOTE_Y, 0x404040, false);
        }
    }

    private void drawRift(WidgetHolder widgets) {
        int x = GTRiftDisplayRecipes.RIFT_X;
        int y = GTRiftDisplayRecipes.ROW_Y;
        ItemStack icon = GTRiftDisplayRecipes.riftIcon();

        widgets.addDrawable(x, y, 16, 16, (graphics, mouseX, mouseY, delta) -> graphics.renderItem(icon, x, y));
        widgets.addTooltipText(List.of(Component.translatable("goldentweaks.rift_crafting.rift")), x, y, 16, 16);
    }
}
