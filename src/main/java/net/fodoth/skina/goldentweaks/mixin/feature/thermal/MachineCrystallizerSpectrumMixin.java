package net.fodoth.skina.goldentweaks.mixin.feature.thermal;

import cofh.thermal.core.util.managers.machine.CrystallizerRecipeManager;
import cofh.thermal.expansion.common.block.entity.machine.MachineCrystallizerBlockEntity;
import cofh.thermal.lib.util.recipes.IThermalInventory;
import cofh.thermal.lib.util.recipes.internal.IMachineRecipe;
import net.fodoth.skina.goldentweaks.compat.thermal.SpectrumCrystallizerRecipe;
import net.fodoth.skina.goldentweaks.compat.thermal.ThermalCrystallarieumAugment;
import net.fodoth.skina.goldentweaks.mixin.feature.thermal.accessor.AugmentableBlockEntityInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 热力结晶器装上「专业化升级：光谱墨法结晶」后，除自身配方外还能运行光谱的消耗型结晶配方。
 *
 * <p>{@code cacheRecipe()} 只向 {@link CrystallizerRecipeManager} 取配方，取不到就停机。这里接管那一次取配方：
 * 原配方匹配不到、且机器上装了本升级组件时，改由 {@link SpectrumCrystallizerRecipe} 按「输入种子 + 罐里的培养液」
 * 去找一条 {@code spectrum:crystallarieum_growing}。返回的机器配方照旧被 {@code cacheRecipe()} 写进 {@code curRecipe}
 * 并据此算出消耗；由于它的培养液消耗为 0，培养液只是存在要求、不会被扣掉。</p>
 */
@Mixin(value = MachineCrystallizerBlockEntity.class, remap = false)
public abstract class MachineCrystallizerSpectrumMixin {

    @Redirect(
            method = "cacheRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lcofh/thermal/core/util/managers/machine/CrystallizerRecipeManager;getRecipe(Lcofh/thermal/lib/util/recipes/IThermalInventory;)Lcofh/thermal/lib/util/recipes/internal/IMachineRecipe;"
            )
    )
    private IMachineRecipe gt$spectrumRecipe(CrystallizerRecipeManager manager, IThermalInventory inventory) {

        IMachineRecipe recipe = manager.getRecipe(inventory);
        if (recipe != null) {
            return recipe;
        }

        if (!(inventory instanceof AugmentableBlockEntityInvoker augmentable)
                || !ThermalCrystallarieumAugment.isInstalled(augmentable.gt$getAugmentsAsList())) {
            return null;
        }
        return SpectrumCrystallizerRecipe.findFor(inventory, manager);
    }
}
