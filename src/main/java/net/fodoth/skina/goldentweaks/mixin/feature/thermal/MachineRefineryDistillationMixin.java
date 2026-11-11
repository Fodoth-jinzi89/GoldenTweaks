package net.fodoth.skina.goldentweaks.mixin.feature.thermal;

import cofh.thermal.core.util.managers.machine.RefineryRecipeManager;
import cofh.thermal.expansion.common.block.entity.machine.MachineRefineryBlockEntity;
import cofh.thermal.lib.util.recipes.IThermalInventory;
import cofh.thermal.lib.util.recipes.internal.IMachineRecipe;
import net.fodoth.skina.goldentweaks.compat.thermal.GTThermalFantasyItems;
import net.fodoth.skina.goldentweaks.compat.thermal.RefineryRecipeWrapper;
import net.fodoth.skina.goldentweaks.compat.thermal.ThermalSpecializations;
import net.fodoth.skina.goldentweaks.mixin.feature.thermal.accessor.AugmentableBlockEntityInvoker;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * 流体精炼机的专业化升级接管配方选择。
 *
 * <p>「组件：塔板分馏提取」：只运行浓缩金属分馏配方，耗能 25%、产出 200%。<br>
 * 「组件：精密炼化分馏」：只运行油类炼化配方，耗能 200%、产出 400%。</p>
 *
 * <p>{@code cacheRecipe()} 只向 {@link RefineryRecipeManager} 取配方。这里接管那一次取配方：先确定该机器上
 * 唯一生效的专业化升级（升级槽位里最先出现的那个），不匹配的配方返回 {@code null}（机器停机），匹配的配方用
 * {@link RefineryRecipeWrapper} 包装后再交给机器，后续的能耗、产出与输出校验都会走包装后的数值。</p>
 */
@Mixin(value = MachineRefineryBlockEntity.class, remap = false)
public abstract class MachineRefineryDistillationMixin {

    private static final float CONCENTRATED_ENERGY_SCALE = 0.25F;
    private static final int CONCENTRATED_OUTPUT_SCALE = 2;
    private static final float OIL_ENERGY_SCALE = 2.0F;
    private static final int OIL_OUTPUT_SCALE = 4;

    @Redirect(
            method = "cacheRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lcofh/thermal/core/util/managers/machine/RefineryRecipeManager;getRecipe(Lcofh/thermal/lib/util/recipes/IThermalInventory;)Lcofh/thermal/lib/util/recipes/internal/IMachineRecipe;"
            )
    )
    private IMachineRecipe gt$specializedRecipe(RefineryRecipeManager manager, IThermalInventory inventory) {

        IMachineRecipe recipe = manager.getRecipe(inventory);

        if (!(inventory instanceof AugmentableBlockEntityInvoker augmentable)) {
            return recipe;
        }

        List<ItemStack> augments = augmentable.gt$getAugmentsAsList();

        if (ThermalSpecializations.isActive(augments,
                GTThermalFantasyItems.TRAY_DISTILLATION_EXTRACTION_AUGMENT.get())) {
            return recipe != null && RefineryRecipeWrapper.isConcentratedMetal(recipe)
                    ? new RefineryRecipeWrapper(recipe, CONCENTRATED_ENERGY_SCALE, CONCENTRATED_OUTPUT_SCALE)
                    : null;
        }

        if (ThermalSpecializations.isActive(augments,
                GTThermalFantasyItems.OIL_DISTILLATION_AUGMENT.get())) {
            return recipe != null && RefineryRecipeWrapper.isOilRefining(recipe)
                    ? new RefineryRecipeWrapper(recipe, OIL_ENERGY_SCALE, OIL_OUTPUT_SCALE)
                    : null;
        }

        if (ThermalSpecializations.isActive(augments,
                GTThermalFantasyItems.AIR_DISTILLATION_AUGMENT.get())) {
            return recipe != null && RefineryRecipeWrapper.isAirFractionation(recipe) ? recipe : null;
        }

        return recipe;
    }
}
