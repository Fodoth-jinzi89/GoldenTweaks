package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.CrucibleRecipeCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.CrucibleRecipe;

/**
 * 坩埚配方按 hash 查找的缓存（移植自 TC4Tweaks 的 FindCrucibleRecipe）。
 *
 * <p>{@code getCrucibleRecipeFromHash} 每次调用都全量遍历配方列表，坩埚下药/倾倒时
 * 会被频繁触发。HEAD 先查缓存、RETURN 把命中结果按 hash 回填。</p>
 */
@Mixin(value = ThaumcraftApi.class, remap = false)
public abstract class ThaumcraftApiMixin {

    @Inject(method = "getCrucibleRecipeFromHash", at = @At("HEAD"), cancellable = true)
    private static void gt$crucibleCacheHit(int hash, CallbackInfoReturnable<CrucibleRecipe> cir) {
        CrucibleRecipe cached = CrucibleRecipeCache.get(hash);
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "getCrucibleRecipeFromHash", at = @At("RETURN"))
    private static void gt$crucibleCacheStore(int hash, CallbackInfoReturnable<CrucibleRecipe> cir) {
        CrucibleRecipeCache.put(hash, cir.getReturnValue());
    }
}
