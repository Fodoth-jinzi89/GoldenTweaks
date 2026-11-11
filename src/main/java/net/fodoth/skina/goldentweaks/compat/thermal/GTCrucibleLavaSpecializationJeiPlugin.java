package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.thermal.core.init.registries.TCoreRecipeTypes;
import cofh.thermal.core.util.recipes.machine.CrucibleRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 「熔岩炉：躁动火山」JEI/EMI 分类：只列出「组件：地核能量熔融」能运行的产出熔岩配方，
 * 并按组件效果换算数值（耗能 10%、产出 200%）。
 *
 * <p>组件本身不画在配方里，只与熔岩炉一起作为分类催化剂。</p>
 */
@JeiPlugin
public class GTCrucibleLavaSpecializationJeiPlugin implements IModPlugin {

    static final RecipeType<RecipeHolder<CrucibleRecipe>> TYPE = RecipeType.createRecipeHolderType(
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "crucible_lava_specialization"));

    private static final String MACHINE_ID = "thermal:machine_crucible";

    private static final float ENERGY_SCALE = 0.10F;
    private static final int OUTPUT_SCALE = 2;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        // 不要用 goldentweaks 命名空间：本模组已有原生 EMI 分类，TMRV 会把同命名空间的 JEI 插件
        // 判为 DUPLICATE_NAMESPACE 并只做 partial load（跳过配方注册）。
        return ResourceLocation.fromNamespaceAndPath("goldentweaks_jei", "crucible_lava_specialization");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        if (present() && FMLEnvironment.dist.isClient()) {
            GTCrucibleLavaSpecializationJeiCategory.register(registration);
        }
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (present()) {
            registration.addRecipes(TYPE, displayRecipes());
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (!present()) {
            return;
        }
        registration.addRecipeCatalyst(
                new ItemStack(GTThermalFantasyItems.LAVA_PRODUCTION_AUGMENT.get()), TYPE);
        BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(MACHINE_ID))
                .ifPresent(item -> registration.addRecipeCatalyst(new ItemStack(item), TYPE));
    }

    private static List<RecipeHolder<CrucibleRecipe>> displayRecipes() {
        RecipeManager manager = recipeManager();
        if (manager == null) {
            return List.of();
        }

        List<RecipeHolder<CrucibleRecipe>> recipes = new ArrayList<>();
        for (RecipeHolder<CrucibleRecipe> holder : manager.getAllRecipesFor(TCoreRecipeTypes.CRUCIBLE_RECIPE.get())) {
            if (!producesLava(holder.value())) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    GoldenTweaks.MODID,
                    "crucible_lava_specialization/" + holder.id().getNamespace() + "/" + holder.id().getPath());
            recipes.add(new RecipeHolder<>(id, scale(holder.value())));
        }
        return recipes;
    }

    private static boolean producesLava(CrucibleRecipe recipe) {
        for (FluidStack stack : recipe.getOutputFluids()) {
            if (RefineryRecipeWrapper.isLavaFluid(stack)) {
                return true;
            }
        }
        return false;
    }

    private static CrucibleRecipe scale(CrucibleRecipe recipe) {
        int energy = Math.max(1, Math.round(recipe.getEnergy() * ENERGY_SCALE));

        List<ItemStack> outputItems = new ArrayList<>(recipe.getOutputItems().size());
        for (ItemStack stack : recipe.getOutputItems()) {
            outputItems.add(stack.copyWithCount(stack.getCount() * OUTPUT_SCALE));
        }

        List<FluidStack> outputFluids = new ArrayList<>(recipe.getOutputFluids().size());
        for (FluidStack stack : recipe.getOutputFluids()) {
            outputFluids.add(stack.copyWithAmount(stack.getAmount() * OUTPUT_SCALE));
        }

        return new CrucibleRecipe(
                energy,
                recipe.getXp(),
                recipe.getInputItems(),
                recipe.getInputFluids(),
                outputItems,
                recipe.getOutputItemChances(),
                outputFluids);
    }

    private static RecipeManager recipeManager() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.getRecipeManager();
    }

    private static boolean present() {
        return ModList.get().isLoaded("thermal") && ModList.get().isLoaded("thermal_expansion");
    }
}
