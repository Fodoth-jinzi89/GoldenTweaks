package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.thermal.core.util.recipes.machine.CrucibleRecipe;
import cofh.thermal.expansion.compat.jei.machine.CrucibleRecipeCategory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * 「熔岩炉：躁动火山」页面。复用热力熔岩炉的页签外观，只把配方换成
 * {@link GTCrucibleLavaSpecializationJeiPlugin} 生成的、按专业化组件换算后的版本。
 *
 * <p>组件本身不画在配方里，只与熔岩炉一起作为分类催化剂。</p>
 */
final class GTCrucibleLavaSpecializationJeiCategory extends CrucibleRecipeCategory {

    private static final Component TITLE =
            Component.translatable("goldentweaks.crucible_lava_specialization.category");

    private GTCrucibleLavaSpecializationJeiCategory(IGuiHelper guiHelper) {
        super(guiHelper,
                new ItemStack(GTThermalFantasyItems.LAVA_PRODUCTION_AUGMENT.get()),
                GTCrucibleLavaSpecializationJeiPlugin.TYPE);
    }

    static void register(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new GTCrucibleLavaSpecializationJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public boolean isHandled(RecipeHolder<CrucibleRecipe> recipe) {
        return recipe != null;
    }
}
