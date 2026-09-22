package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
 *
 * <p>另外跳过"合成格全空"时的原版配方查找：{@code RecipeManager#getRecipeFor} 会遍历
 * 整合包里全部原版合成配方，而空网格永远不可能匹配任何配方。打开界面（网格为空）以及
 * 每次取走成品后都会触发刷新，这一步是刷新里唯一没有上界的开销，跳过后可显著降低
 * 首次打开界面时的卡顿。语义不变：空网格下原版与奥术配方都不可能有结果。</p>
 */
@Mixin(value = ArcaneWorkbenchMenu.class, remap = false)
public abstract class ArcaneWorkbenchMenuMixin {

    @Shadow
    @Final
    private Container workbench;

    @Inject(method = "findVanillaRecipe", at = @At("HEAD"), cancellable = true)
    private void gt$disableVanillaCrafting(CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
        if (!GoldenTweaksCommonConfig.isArcaneWorkbenchVanillaCrafting()) {
            cir.setReturnValue(Optional.empty());
        }
    }

    @Inject(method = "findVanillaRecipe", at = @At("HEAD"), cancellable = true)
    private void gt$skipVanillaLookupForEmptyGrid(CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
        if (!GoldenTweaksCommonConfig.isArcaneWorkbenchVanillaCrafting()) {
            return;
        }
        for (int slot = 0; slot < 9; slot++) {
            if (!this.workbench.getItem(slot).isEmpty()) {
                return;
            }
        }
        cir.setReturnValue(Optional.empty());
    }
}
