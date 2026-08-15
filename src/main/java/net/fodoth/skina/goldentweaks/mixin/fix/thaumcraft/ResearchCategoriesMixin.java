package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.ResearchCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;

/**
 * 研究条目查找缓存（移植自 TC4Tweaks 的 GetResearch）。
 *
 * <p>{@code getResearch} 每次调用都遍历所有研究分类。HEAD 先查缓存、RETURN 把结果
 * （含未找到的 {@code null}）回填；研究条目注册（{@code addResearch}）或刷新
 * （{@code refreshResearch}）时清空缓存，防止返回过期的研究数据。</p>
 */
@Mixin(value = ResearchCategories.class, remap = false)
public abstract class ResearchCategoriesMixin {

    @Inject(method = "getResearch", at = @At("HEAD"), cancellable = true)
    private static void gt$researchCacheHit(String key, CallbackInfoReturnable<ResearchItem> cir) {
        ResearchItem cached = ResearchCache.get(key);
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "getResearch", at = @At("RETURN"))
    private static void gt$researchCacheStore(String key, CallbackInfoReturnable<ResearchItem> cir) {
        ResearchCache.put(key, cir.getReturnValue());
    }

    @Inject(method = "addResearch", at = @At("HEAD"))
    private static void gt$researchCacheInvalidateOnAdd(CallbackInfo ci) {
        ResearchCache.clear();
    }

    @Inject(method = "refreshResearch", at = @At("HEAD"))
    private static void gt$researchCacheInvalidateOnRefresh(CallbackInfo ci) {
        ResearchCache.clear();
    }
}
