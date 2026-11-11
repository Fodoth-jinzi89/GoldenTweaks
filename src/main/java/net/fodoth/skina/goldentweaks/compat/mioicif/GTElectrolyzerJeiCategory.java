package net.fodoth.skina.goldentweaks.compat.mioicif;

import com.miophas.singularity_iteration.common.client.integration.jei.category.SiItemMachineCategory;
import com.miophas.singularity_iteration.common.client.integration.jei.category.SiJei;
import com.miophas.singularity_iteration.common.recipe.generic.ElectrolyzerRecipe;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/**
 * 电解机 JEI 分类。奇点迭代自带许多 {@code SiItemMachineCategory} 分类（粉碎机/压缩机/提取机…）
 * 却没有电解机页面，这里沿用同一套基类与布局补上；输入数量按配方 group 的 {@code input_count=N} 显示。
 */
final class GTElectrolyzerJeiCategory extends SiItemMachineCategory<ElectrolyzerRecipe> {

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath("goldentweaks_jei", "electrolyzer");

    private static final ResourceLocation ICON_ID =
            ResourceLocation.parse("mio_icif:producer/block_electrolyzer_elc");

    static final RecipeType<RecipeHolder<ElectrolyzerRecipe>> TYPE =
            RecipeType.createRecipeHolderType(UID);

    private GTElectrolyzerJeiCategory(IGuiHelper guiHelper) {
        super(guiHelper, TYPE, "goldentweaks.electrolyzer.category",
                BuiltInRegistries.ITEM.get(ICON_ID), 1);
    }

    static void register(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new GTElectrolyzerJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    protected List<ItemStack> inputs(ElectrolyzerRecipe recipe) {
        return SiJei.withCount(recipe.getIngredient(), GTElectrolyzerInputCount.parse(recipe.getGroup()));
    }

    @Override
    protected ItemStack result(ElectrolyzerRecipe recipe) {
        return recipe.getResult();
    }

    @Override
    protected int ticks(ElectrolyzerRecipe recipe) {
        return recipe.getProcessingTime();
    }

    @Override
    protected long euPerTick(ElectrolyzerRecipe recipe) {
        return recipe.getEnergyPerTick();
    }
}
