package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.thermal.core.util.recipes.machine.RefineryRecipe;
import cofh.thermal.expansion.compat.jei.machine.RefineryRecipeCategory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * 单个流体精炼机专业化页面。复用热力流体精炼机的页签外观，只把配方换成按该专业化组件换算后的版本。
 *
 * <p>组件本身不画在配方里，而是由 {@link GTRefinerySpecializationJeiPlugin} 与机器一起注册为分类催化剂。</p>
 */
final class GTRefinerySpecializationJeiCategory extends RefineryRecipeCategory {

    private final Component title;

    private GTRefinerySpecializationJeiCategory(IGuiHelper guiHelper,
                                                RecipeType<RecipeHolder<RefineryRecipe>> type,
                                                ItemStack icon,
                                                Component title) {
        super(guiHelper, icon, type);
        this.title = title;
    }

    static void register(IRecipeCategoryRegistration registration,
                         RecipeType<RecipeHolder<RefineryRecipe>> type,
                         ItemStack icon,
                         Component title) {
        registration.addRecipeCategories(new GTRefinerySpecializationJeiCategory(
                registration.getJeiHelpers().getGuiHelper(), type, icon, title));
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public boolean isHandled(RecipeHolder<RefineryRecipe> recipe) {
        return recipe != null;
    }
}
