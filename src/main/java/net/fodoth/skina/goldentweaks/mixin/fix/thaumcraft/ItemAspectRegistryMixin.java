package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.ObjectTagsCache;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
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
 * 在方法前后分别做缓存命中与回填；配方管理器重载（{@code bindRecipeManager}）时
 * 清空缓存，保证配方派生要素不会过期。</p>
 */
@Mixin(value = ItemAspectRegistry.class, remap = false)
public abstract class ItemAspectRegistryMixin {

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
    private static void gt$objectTagsCacheInvalidate(CallbackInfo ci) {
        ObjectTagsCache.clear();
    }
}
