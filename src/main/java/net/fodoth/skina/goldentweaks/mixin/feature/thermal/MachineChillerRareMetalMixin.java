package net.fodoth.skina.goldentweaks.mixin.feature.thermal;

import cofh.thermal.core.util.managers.machine.ChillerRecipeManager;
import cofh.thermal.expansion.common.block.entity.machine.MachineChillerBlockEntity;
import cofh.thermal.lib.util.recipes.IThermalInventory;
import cofh.thermal.lib.util.recipes.internal.IMachineRecipe;
import net.fodoth.skina.goldentweaks.compat.thermal.GTThermalFantasyItems;
import net.fodoth.skina.goldentweaks.compat.thermal.RefineryRecipeWrapper;
import net.fodoth.skina.goldentweaks.compat.thermal.ThermalSpecializations;
import net.fodoth.skina.goldentweaks.mixin.feature.thermal.accessor.AugmentableBlockEntityInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 急速冷冻机装上「组件：骤冷析晶程序」后，只运行浓缩金属冷却结晶配方，并把耗能压到 25%、产出翻倍。
 *
 * <p>{@code cacheRecipe()} 只向 {@link ChillerRecipeManager} 取配方。这里接管那一次取配方：装了本组件时，
 * 非浓缩金属配方返回 {@code null}（机器停机），目标配方用 {@link RefineryRecipeWrapper} 包装后再交给机器，
 * 后续的能耗、产出与输出校验都会走包装后的数值。</p>
 */
@Mixin(value = MachineChillerBlockEntity.class, remap = false)
public abstract class MachineChillerRareMetalMixin {

    private static final float ENERGY_SCALE = 0.25F;
    private static final int OUTPUT_SCALE = 2;

    @Redirect(
            method = "cacheRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lcofh/thermal/core/util/managers/machine/ChillerRecipeManager;getRecipe(Lcofh/thermal/lib/util/recipes/IThermalInventory;)Lcofh/thermal/lib/util/recipes/internal/IMachineRecipe;"
            )
    )
    private IMachineRecipe gt$concentratedOnly(ChillerRecipeManager manager, IThermalInventory inventory) {

        IMachineRecipe recipe = manager.getRecipe(inventory);

        if (!(inventory instanceof AugmentableBlockEntityInvoker augmentable)
                || !ThermalSpecializations.isActive(
                        augmentable.gt$getAugmentsAsList(),
                        GTThermalFantasyItems.RARE_METAL_CRYSTALLIZATION_AUGMENT.get())) {
            return recipe;
        }

        if (recipe == null || !RefineryRecipeWrapper.isConcentratedMetal(recipe)) {
            return null;
        }
        return new RefineryRecipeWrapper(recipe, ENERGY_SCALE, OUTPUT_SCALE);
    }
}
