package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.thermal.core.util.recipes.machine.ChillerRecipe;
import cofh.thermal.expansion.compat.jei.machine.ChillerRecipeCategory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * 「急速冷冻机：冷却结晶」页面。复用热力急速冷冻机的页签外观，只把配方换成
 * {@link GTChillerSpecializationJeiPlugin} 生成的、按专业化组件换算后的版本。
 *
 * <p>组件本身不画在配方里，只与机器一起作为分类催化剂。</p>
 */
final class GTChillerSpecializationJeiCategory extends ChillerRecipeCategory {

    private static final Component TITLE =
            Component.translatable("goldentweaks.chiller_specialization.category");

    private GTChillerSpecializationJeiCategory(IGuiHelper guiHelper) {
        super(guiHelper,
                new ItemStack(GTThermalFantasyItems.RARE_METAL_CRYSTALLIZATION_AUGMENT.get()),
                GTChillerSpecializationJeiPlugin.TYPE);
    }

    static void register(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new GTChillerSpecializationJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public boolean isHandled(RecipeHolder<ChillerRecipe> recipe) {
        return recipe != null;
    }
}
