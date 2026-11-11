package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.lib.common.fluid.FluidIngredient;
import cofh.thermal.core.init.registries.TCoreRecipeTypes;
import cofh.thermal.core.util.recipes.machine.RefineryRecipe;
import cofh.thermal.expansion.compat.jei.TExpJeiRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 流体精炼机的三个专业化页面：每个专业化组件各占一个 JEI/EMI 分类。
 *
 * <p>展示的是按组件效果换算后的配方（铂系大师 25% 耗能 / 200% 产出，原油工程 200% / 400%，冷冻空气分馏不变）。
 * 组件本身不画在配方里，只与机器一起作为分类催化剂。</p>
 *
 * <p>空气分馏配方会从热力原本的流体精炼机页中移除，只在「冷冻空气分馏」页里出现。</p>
 */
@JeiPlugin
public class GTRefinerySpecializationJeiPlugin implements IModPlugin {

    private static final String MACHINE_ID = "thermal:machine_refinery";

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        // 不要用 goldentweaks 命名空间：本模组已有原生 EMI 分类，TMRV 会把同命名空间的 JEI 插件
        // 判为 DUPLICATE_NAMESPACE 并只做 partial load（跳过配方注册与 onRuntimeAvailable）。
        return ResourceLocation.fromNamespaceAndPath("goldentweaks_jei", "refinery_specialization");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        if (present() && FMLEnvironment.dist.isClient()) {
            for (Specialization specialization : Specialization.values()) {
                GTRefinerySpecializationJeiCategory.register(
                        registration, specialization.type, specialization.augment(), specialization.title());
            }
        }
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (!present()) {
            return;
        }
        Map<Specialization, List<RecipeHolder<RefineryRecipe>>> displays = displayRecipes();
        for (Specialization specialization : Specialization.values()) {
            List<RecipeHolder<RefineryRecipe>> recipes = displays.get(specialization);
            if (recipes != null && !recipes.isEmpty()) {
                registration.addRecipes(specialization.type, recipes);
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (!present()) {
            return;
        }
        ItemStack machine = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(MACHINE_ID))
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
        for (Specialization specialization : Specialization.values()) {
            registration.addRecipeCatalyst(specialization.augment(), specialization.type);
            if (!machine.isEmpty()) {
                registration.addRecipeCatalyst(machine.copy(), specialization.type);
            }
        }
    }

    /** 空气分馏配方只在「冷冻空气分馏」页里展示，从热力原本的流体精炼机页隐藏。 */
    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        if (!present()) {
            return;
        }
        RecipeManager manager = recipeManager();
        if (manager == null) {
            return;
        }

        List<RecipeHolder<RefineryRecipe>> airRecipes = new ArrayList<>();
        for (RecipeHolder<RefineryRecipe> holder : manager.getAllRecipesFor(TCoreRecipeTypes.REFINERY_RECIPE.get())) {
            if (classify(holder.value()) == Specialization.AIR) {
                airRecipes.add(holder);
            }
        }
        if (!airRecipes.isEmpty()) {
            jeiRuntime.getRecipeManager().hideRecipes(TExpJeiRecipeTypes.REFINERY_TYPE, airRecipes);
        }
    }

    private static Map<Specialization, List<RecipeHolder<RefineryRecipe>>> displayRecipes() {
        Map<Specialization, List<RecipeHolder<RefineryRecipe>>> displays = new EnumMap<>(Specialization.class);
        RecipeManager manager = recipeManager();
        if (manager == null) {
            return displays;
        }

        for (RecipeHolder<RefineryRecipe> holder : manager.getAllRecipesFor(TCoreRecipeTypes.REFINERY_RECIPE.get())) {
            Specialization specialization = classify(holder.value());
            if (specialization == null) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    GoldenTweaks.MODID,
                    "refinery_specialization/" + specialization.name().toLowerCase()
                            + "/" + holder.id().getNamespace() + "/" + holder.id().getPath());
            displays.computeIfAbsent(specialization, key -> new ArrayList<>())
                    .add(new RecipeHolder<>(id, specialization.scale(holder.value())));
        }
        return displays;
    }

    private static Specialization classify(RefineryRecipe recipe) {
        for (FluidIngredient ingredient : recipe.getInputFluids()) {
            for (FluidStack stack : ingredient.getFluids()) {
                if (RefineryRecipeWrapper.isConcentratedFluid(stack)) {
                    return Specialization.CONCENTRATED;
                }
                if (RefineryRecipeWrapper.isOilFluid(stack)) {
                    return Specialization.OIL;
                }
                if (RefineryRecipeWrapper.isAirFluid(stack)) {
                    return Specialization.AIR;
                }
            }
        }
        return null;
    }

    private static RecipeManager recipeManager() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.getRecipeManager();
    }

    private static boolean present() {
        return ModList.get().isLoaded("thermal") && ModList.get().isLoaded("thermal_expansion");
    }

    private enum Specialization {

        CONCENTRATED("refinery_concentrated", 0.25F, 2),
        OIL("refinery_oil", 2.0F, 4),
        AIR("refinery_air", 1.0F, 1);

        private final RecipeType<RecipeHolder<RefineryRecipe>> type;
        private final float energyScale;
        private final int outputScale;

        Specialization(String path, float energyScale, int outputScale) {
            this.type = RecipeType.createRecipeHolderType(
                    ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, path));
            this.energyScale = energyScale;
            this.outputScale = outputScale;
        }

        private ItemStack augment() {
            return switch (this) {
                case CONCENTRATED -> new ItemStack(GTThermalFantasyItems.TRAY_DISTILLATION_EXTRACTION_AUGMENT.get());
                case OIL -> new ItemStack(GTThermalFantasyItems.OIL_DISTILLATION_AUGMENT.get());
                case AIR -> new ItemStack(GTThermalFantasyItems.AIR_DISTILLATION_AUGMENT.get());
            };
        }

        private Component title() {
            return switch (this) {
                case CONCENTRATED -> Component.translatable("goldentweaks.refinery_concentrated.category");
                case OIL -> Component.translatable("goldentweaks.refinery_oil.category");
                case AIR -> Component.translatable("goldentweaks.refinery_air.category");
            };
        }

        private RefineryRecipe scale(RefineryRecipe recipe) {
            int energy = Math.max(1, Math.round(recipe.getEnergy() * this.energyScale));

            List<ItemStack> outputItems = new ArrayList<>(recipe.getOutputItems().size());
            for (ItemStack stack : recipe.getOutputItems()) {
                outputItems.add(stack.copyWithCount(stack.getCount() * this.outputScale));
            }

            List<FluidStack> outputFluids = new ArrayList<>(recipe.getOutputFluids().size());
            for (FluidStack stack : recipe.getOutputFluids()) {
                outputFluids.add(stack.copyWithAmount(stack.getAmount() * this.outputScale));
            }

            return new RefineryRecipe(
                    energy,
                    recipe.getXp(),
                    recipe.getInputItems(),
                    recipe.getInputFluids(),
                    outputItems,
                    recipe.getOutputItemChances(),
                    outputFluids);
        }
    }
}
