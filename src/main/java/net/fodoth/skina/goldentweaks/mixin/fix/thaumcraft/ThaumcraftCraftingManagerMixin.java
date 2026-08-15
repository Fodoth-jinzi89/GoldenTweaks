package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.ArcaneCraftingCache;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

/**
 * 奥术合成缓存（移植自 TC4Tweaks 的 FindRecipes / ArcaneCraftingHistory）。
 *
 * <p>{@code findMatchingArcaneRecipeEntry} 是移植版奥术配方查找的唯一全量扫描入口
 * （{@code findMatchingArcaneRecipe} 与 {@code findMatchingArcaneRecipeAspects}
 * 都经由 {@code findRecipe} 汇聚到它），因此在此做"先查缓存、未命中再全量扫描、命中后
 * 入缓存"的包装即可让工作台预览、取物、insufficient 提示等所有调用方共享缓存，
 * 显著降低大整合包中反复全量遍历配方的开销。</p>
 */
@Mixin(value = ThaumcraftCraftingManager.class, remap = false)
public abstract class ThaumcraftCraftingManagerMixin {

    @Inject(method = "findMatchingArcaneRecipeEntry", at = @At("HEAD"), cancellable = true)
    private static void gt$arcaneRecipeCacheHit(Container workbench, Player player, CallbackInfoReturnable<IArcaneRecipe> cir) {
        IArcaneRecipe cached = ArcaneCraftingCache.find(workbench, player);
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "findMatchingArcaneRecipeEntry", at = @At("RETURN"))
    private static void gt$arcaneRecipeCacheStore(Container workbench, Player player, CallbackInfoReturnable<IArcaneRecipe> cir) {
        IArcaneRecipe recipe = cir.getReturnValue();
        if (recipe != null) {
            ArcaneCraftingCache.add(recipe);
        }
    }
}
