package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.lib.common.fluid.FluidIngredient;
import cofh.thermal.core.init.registries.TCoreRecipeTypes;
import cofh.thermal.core.util.recipes.machine.ChillerRecipe;
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
 * 「急速冷冻机：冷却结晶」JEI 分类：把「组件：骤冷析晶程序」的浓缩金属冷却结晶配方单独列出来。
 *
 * <p>展示的是按组件效果换算后的配方（耗能 25%、产出 200%），并在每条配方上标出所需组件。
 * EMI / TMRV 会通过它们的 JEI 兼容层渲染同一个分类。</p>
 */
@JeiPlugin
public class GTChillerSpecializationJeiPlugin implements IModPlugin {

    static final RecipeType<RecipeHolder<ChillerRecipe>> TYPE = RecipeType.createRecipeHolderType(
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "chiller_specialization"));

    private static final String MACHINE_ID = "thermal:machine_chiller";

    private static final float ENERGY_SCALE = 0.25F;
    private static final int OUTPUT_SCALE = 2;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        // 不要用 goldentweaks 命名空间：本模组已有原生 EMI 分类，TMRV 会把同命名空间的 JEI 插件
        // 判为 DUPLICATE_NAMESPACE 并只做 partial load（跳过配方注册），页面就会是空的。
        return ResourceLocation.fromNamespaceAndPath("goldentweaks_jei", "chiller_specialization");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        if (present() && FMLEnvironment.dist.isClient()) {
            GTChillerSpecializationJeiCategory.register(registration);
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
                new ItemStack(GTThermalFantasyItems.RARE_METAL_CRYSTALLIZATION_AUGMENT.get()), TYPE);
        BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(MACHINE_ID))
                .ifPresent(item -> registration.addRecipeCatalyst(new ItemStack(item), TYPE));
    }

    /** 该配方所需的专业化组件。 */
    static ItemStack augmentFor(ChillerRecipe recipe) {
        return isConcentratedMetal(recipe)
                ? new ItemStack(GTThermalFantasyItems.RARE_METAL_CRYSTALLIZATION_AUGMENT.get())
                : ItemStack.EMPTY;
    }

    private static List<RecipeHolder<ChillerRecipe>> displayRecipes() {
        RecipeManager manager = recipeManager();
        if (manager == null) {
            return List.of();
        }

        List<RecipeHolder<ChillerRecipe>> recipes = new ArrayList<>();
        for (RecipeHolder<ChillerRecipe> holder : manager.getAllRecipesFor(TCoreRecipeTypes.CHILLER_RECIPE.get())) {
            if (!isConcentratedMetal(holder.value())) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    GoldenTweaks.MODID,
                    "chiller_specialization/" + holder.id().getNamespace() + "/" + holder.id().getPath());
            recipes.add(new RecipeHolder<>(id, scale(holder.value())));
        }
        return recipes;
    }

    private static boolean isConcentratedMetal(ChillerRecipe recipe) {
        for (FluidIngredient ingredient : recipe.getInputFluids()) {
            for (FluidStack stack : ingredient.getFluids()) {
                if (RefineryRecipeWrapper.isConcentratedFluid(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ChillerRecipe scale(ChillerRecipe recipe) {
        int energy = Math.max(1, Math.round(recipe.getEnergy() * ENERGY_SCALE));

        List<ItemStack> outputItems = new ArrayList<>(recipe.getOutputItems().size());
        for (ItemStack stack : recipe.getOutputItems()) {
            outputItems.add(stack.copyWithCount(stack.getCount() * OUTPUT_SCALE));
        }

        List<FluidStack> outputFluids = new ArrayList<>(recipe.getOutputFluids().size());
        for (FluidStack stack : recipe.getOutputFluids()) {
            outputFluids.add(stack.copyWithAmount(stack.getAmount() * OUTPUT_SCALE));
        }

        return new ChillerRecipe(
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

    /** Guarded before touching any Thermal class. */
    private static boolean present() {
        return ModList.get().isLoaded("thermal") && ModList.get().isLoaded("thermal_expansion");
    }
}
