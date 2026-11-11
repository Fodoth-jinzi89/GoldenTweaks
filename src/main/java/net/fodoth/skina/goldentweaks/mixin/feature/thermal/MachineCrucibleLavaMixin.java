package net.fodoth.skina.goldentweaks.mixin.feature.thermal;

import cofh.thermal.core.util.managers.machine.CrucibleRecipeManager;
import cofh.thermal.expansion.common.block.entity.machine.MachineCrucibleBlockEntity;
import cofh.thermal.lib.util.recipes.IThermalInventory;
import cofh.thermal.lib.util.recipes.IMachineInventory;
import cofh.thermal.lib.util.recipes.internal.IMachineRecipe;
import net.fodoth.skina.goldentweaks.compat.thermal.GTThermalFantasyItems;
import net.fodoth.skina.goldentweaks.compat.thermal.RefineryRecipeWrapper;
import net.fodoth.skina.goldentweaks.compat.thermal.ThermalSpecializations;
import net.fodoth.skina.goldentweaks.mixin.feature.thermal.accessor.AugmentableBlockEntityInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 熔岩炉装上「组件：地核能量熔融」后，只运行产出熔岩的配方，并把耗能压到 10%、产出翻倍。
 *
 * <p>{@code cacheRecipe()} 只向 {@link CrucibleRecipeManager} 取配方。这里接管那一次取配方：装了本组件时，
 * 不产出熔岩的配方返回 {@code null}（机器停机），目标配方用 {@link RefineryRecipeWrapper} 包装后再交给机器，
 * 后续的能耗、产出与输出校验都会走包装后的数值。</p>
 */
@Mixin(value = MachineCrucibleBlockEntity.class, remap = false)
public abstract class MachineCrucibleLavaMixin {

    private static final float ENERGY_SCALE = 0.10F;
    private static final int OUTPUT_SCALE = 2;

    @Redirect(
            method = "cacheRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lcofh/thermal/core/util/managers/machine/CrucibleRecipeManager;getRecipe(Lcofh/thermal/lib/util/recipes/IThermalInventory;)Lcofh/thermal/lib/util/recipes/internal/IMachineRecipe;"
            )
    )
    private IMachineRecipe gt$lavaOnly(CrucibleRecipeManager manager, IThermalInventory inventory) {

        IMachineRecipe recipe = manager.getRecipe(inventory);

        if (!(inventory instanceof AugmentableBlockEntityInvoker augmentable)
                || !ThermalSpecializations.isActive(
                        augmentable.gt$getAugmentsAsList(),
                        GTThermalFantasyItems.LAVA_PRODUCTION_AUGMENT.get())) {
            return recipe;
        }

        if (recipe == null || !RefineryRecipeWrapper.producesLava(recipe, (IMachineInventory) inventory)) {
            return null;
        }
        return new RefineryRecipeWrapper(recipe, ENERGY_SCALE, OUTPUT_SCALE);
    }
}
