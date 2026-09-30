package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.ObjectTagsCache;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.aspects.ItemAspectRegistry;

/**
 * 物品要素标签缓存（移植自 TC4Tweaks 的 ObjectTagsLagFix）。
 *
 * <p>{@code getObjectTags(ItemStack)} 无缓存，每次调用都递归计算基础/配方派生要素。
 * 在方法前后分别做缓存命中与回填；配方索引重建时清空缓存，保证配方派生要素不会过期。</p>
 *
 * <p>配方索引有三条刷新入口：{@code bindRecipeManager}（客户端每个 tick 都会调用，
 * 管理器未变时原版内部直接返回）、{@code refreshRecipeManager}（客户端收到配方更新 /
 * 退出存档）与 {@code clearServerRuntimeCaches}（服务端运行期状态重置）。只在管理器
 * 实例真正更换时清缓存，否则客户端的缓存会被每 tick 清空而失去作用；后两条入口不会
 * 经过 {@code bindRecipeManager}，必须单独挂钩，否则重载后物品要素会一直用旧值。</p>
 */
@Mixin(value = ItemAspectRegistry.class, remap = false)
public abstract class ItemAspectRegistryMixin {

    /** 最近一次绑定过的配方管理器，用于识别 {@code bindRecipeManager} 的重复调用。 */
    @Unique
    private static RecipeManager gt$boundRecipeManager;

    @Inject(
            method = "getObjectTags(Lnet/minecraft/world/item/ItemStack;)Lthaumcraft/api/aspects/AspectList;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gt$objectTagsCacheHit(ItemStack stack, CallbackInfoReturnable<AspectList> cir) {
        AspectList cached = ObjectTagsCache.get(stack);
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(
            method = "getObjectTags(Lnet/minecraft/world/item/ItemStack;)Lthaumcraft/api/aspects/AspectList;",
            at = @At("RETURN")
    )
    private static void gt$objectTagsCacheStore(ItemStack stack, CallbackInfoReturnable<AspectList> cir) {
        ObjectTagsCache.put(stack, cir.getReturnValue());
    }

    @Inject(method = "bindRecipeManager", at = @At("HEAD"))
    private static void gt$objectTagsCacheInvalidate(RecipeManager recipeManager, HolderLookup.Provider registries,
                                                     CallbackInfo ci) {
        if (recipeManager != null && recipeManager != gt$boundRecipeManager) {
            gt$boundRecipeManager = recipeManager;
            ObjectTagsCache.clear();
        }
    }

    @Inject(method = "refreshRecipeManager", at = @At("HEAD"))
    private static void gt$objectTagsCacheInvalidateOnRefresh(RecipeManager recipeManager, HolderLookup.Provider registries,
                                                              CallbackInfo ci) {
        gt$boundRecipeManager = recipeManager;
        ObjectTagsCache.clear();
    }

    @Inject(method = "clearServerRuntimeCaches", at = @At("HEAD"))
    private static void gt$objectTagsCacheInvalidateOnServerReset(CallbackInfo ci) {
        ObjectTagsCache.clear();
    }
}
