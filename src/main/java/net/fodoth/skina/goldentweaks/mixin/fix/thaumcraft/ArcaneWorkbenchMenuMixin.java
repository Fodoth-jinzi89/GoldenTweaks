package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.polymorph.PolymorphArcaneCompat;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
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

    @Shadow
    @Final
    private Player player;

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

    /**
     * 进入奥术分支前，把当前网格的所有可选奥术配方交给 Polymorph 重建选择列表。
     *
     * <p>挂在 {@code findMatchingArcaneRecipeAspects} 的调用上：只有原版配方没有命中、
     * 真正要算奥术配方时才会走到这里，避免每次预览都做额外的事。</p>
     */
    @Redirect(
            method = "refreshResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/lib/crafting/ThaumcraftCraftingManager;findMatchingArcaneRecipeAspects(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/player/Player;)Lthaumcraft/api/aspects/AspectList;"
            )
    )
    private AspectList gt$polymorphArcaneRefresh(Container workbench, Player player) {
        PolymorphArcaneCompat.refresh(player, workbench);
        return ThaumcraftCraftingManager.findMatchingArcaneRecipeAspects(workbench, player);
    }

    /**
     * 原版合成路径也交给 Polymorph：工作台里同时匹配多个原版配方时，玩家可以选择用哪个。
     * 原版配方本身就在 {@code RecipeManager} 中，因此直接走 Polymorph 的原生选择流程。
     */
    @SuppressWarnings("unchecked")
    @Redirect(
            method = "findVanillaRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"
            )
    )
    private Optional<RecipeHolder<CraftingRecipe>> gt$polymorphVanillaRecipe(
            RecipeManager recipeManager, RecipeType<?> type, RecipeInput input, Level level) {
        return PolymorphArcaneCompat.selectVanilla(this.player, recipeManager,
                (RecipeType<CraftingRecipe>) type, (CraftingInput) input, level);
    }
}
