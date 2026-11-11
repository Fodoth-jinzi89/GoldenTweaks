package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.thermal.lib.util.recipes.IMachineInventory;
import cofh.thermal.lib.util.recipes.internal.IMachineRecipe;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 热力机器专业化升级使用的配方视图：保留原配方的输入，按给定倍率改写耗能与产出。
 *
 * <p>同时提供各专业化升级的配方筛选：浓缩金属分馏、油类炼化与空气组分分馏。流体精炼机与急速冷冻机共用。</p>
 */
public final class RefineryRecipeWrapper implements IMachineRecipe {

    private static final Set<ResourceLocation> OIL_FLUIDS = Set.of(
            ResourceLocation.fromNamespaceAndPath("thermal", "crude_oil"),
            ResourceLocation.fromNamespaceAndPath("thermal", "heavy_oil"),
            ResourceLocation.fromNamespaceAndPath("thermal", "light_oil"),
            ResourceLocation.fromNamespaceAndPath("thermal", "creosote"),
            ResourceLocation.fromNamespaceAndPath("thermal", "coal"),
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "cracked_heavy_oil"),
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "cracked_light_oil"),
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "cracked_refined_oil"),
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "coal_tar"),
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "cracked_coal_tar")
    );

    private static final TagKey<Fluid> CRUDE_OIL_TAG =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "crude_oil"));

    private static final Set<ResourceLocation> AIR_FLUIDS = Set.of(
            ResourceLocation.fromNamespaceAndPath("mio_icif", "air"),
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "noble_gas")
    );

    private final IMachineRecipe delegate;
    private final float energyScale;
    private final int outputScale;

    public RefineryRecipeWrapper(IMachineRecipe delegate, float energyScale, int outputScale) {
        this.delegate = delegate;
        this.energyScale = energyScale;
        this.outputScale = outputScale;
    }

    /** 输入流体是本模组的浓缩金属（{@code goldentweaks:concentrated_*}）即为浓缩金属分馏配方。 */
    public static boolean isConcentratedMetal(IMachineRecipe recipe) {
        for (FluidStack stack : recipe.getInputFluids()) {
            if (isConcentratedFluid(stack)) {
                return true;
            }
        }
        return false;
    }

    /** 输入流体属于热力/本模组的油类，或落在 {@code c:crude_oil} 标签里，即为油类炼化配方。 */
    public static boolean isOilRefining(IMachineRecipe recipe) {
        for (FluidStack stack : recipe.getInputFluids()) {
            if (isOilFluid(stack)) {
                return true;
            }
        }
        return false;
    }

    /** 输入流体是压缩空气或稀有气体，即为空气组分分馏配方。 */
    public static boolean isAirFractionation(IMachineRecipe recipe) {
        for (FluidStack stack : recipe.getInputFluids()) {
            if (isAirFluid(stack)) {
                return true;
            }
        }
        return false;
    }

    /** 产出流体中包含熔岩，即为产出熔岩配方（熔岩炉专业化）。 */
    public static boolean producesLava(IMachineRecipe recipe, IMachineInventory inventory) {
        for (FluidStack stack : recipe.getOutputFluids(inventory)) {
            if (isLavaFluid(stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLavaFluid(FluidStack stack) {
        return stack.is(FluidTags.LAVA);
    }

    public static boolean isConcentratedFluid(FluidStack stack) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        return id != null && GoldenTweaks.MODID.equals(id.getNamespace())
                && id.getPath().startsWith("concentrated_");
    }

    public static boolean isOilFluid(FluidStack stack) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        return (id != null && OIL_FLUIDS.contains(id)) || stack.is(CRUDE_OIL_TAG);
    }

    public static boolean isAirFluid(FluidStack stack) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        return id != null && AIR_FLUIDS.contains(id);
    }

    @Override
    public List<ItemStack> getInputItems() {
        return delegate.getInputItems();
    }

    @Override
    public List<FluidStack> getInputFluids() {
        return delegate.getInputFluids();
    }

    @Override
    public List<ItemStack> getOutputItems(IMachineInventory inventory) {
        return scaleItems(delegate.getOutputItems(inventory));
    }

    @Override
    public List<FluidStack> getOutputFluids(IMachineInventory inventory) {
        return scaleFluids(delegate.getOutputFluids(inventory));
    }

    @Override
    public List<Float> getOutputItemChances(IMachineInventory inventory) {
        return delegate.getOutputItemChances(inventory);
    }

    @Override
    public List<Integer> getInputItemCounts(IMachineInventory inventory) {
        return delegate.getInputItemCounts(inventory);
    }

    @Override
    public List<Integer> getInputFluidCounts(IMachineInventory inventory) {
        return delegate.getInputFluidCounts(inventory);
    }

    @Override
    public int getEnergy(IMachineInventory inventory) {
        return Math.max(1, Math.round(delegate.getEnergy(inventory) * energyScale));
    }

    @Override
    public float getXp(IMachineInventory inventory) {
        return delegate.getXp(inventory);
    }

    private List<ItemStack> scaleItems(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return stacks;
        }
        List<ItemStack> scaled = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            scaled.add(stack.copyWithCount(stack.getCount() * outputScale));
        }
        return scaled;
    }

    private List<FluidStack> scaleFluids(List<FluidStack> stacks) {
        if (stacks.isEmpty()) {
            return stacks;
        }
        List<FluidStack> scaled = new ArrayList<>(stacks.size());
        for (FluidStack stack : stacks) {
            scaled.add(stack.copyWithAmount(stack.getAmount() * outputScale));
        }
        return scaled;
    }
}
