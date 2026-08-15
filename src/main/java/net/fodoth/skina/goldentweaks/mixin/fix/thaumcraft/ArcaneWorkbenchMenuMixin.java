package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.menu.ArcaneWorkbenchMenu;

import java.util.Optional;

/**
 * 奥术工作台禁用原版合成（移植自 TC4Tweaks 的 checkWorkbenchRecipes）。
 *
 * <p>移植版工作台本身支持用原版配方合成（{@code findVanillaRecipe} 在每次刷新结果时
 * 都会查询 RecipeManager 并优先于奥术配方）。当配置
 * {@code balance.thaumcraft.arcane_workbench_vanilla_crafting} 为 false 时，
 * 直接令其返回空 Optional，使工作台只执行奥术配方。刷新逻辑只在服务端运行，
 * 因此普通（common）配置即可生效。</p>
 */
@Mixin(value = ArcaneWorkbenchMenu.class, remap = false)
public abstract class ArcaneWorkbenchMenuMixin {

    @Inject(method = "findVanillaRecipe", at = @At("HEAD"), cancellable = true)
    private void gt$disableVanillaCrafting(CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
        if (!GoldenTweaksCommonConfig.isArcaneWorkbenchVanillaCrafting()) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
