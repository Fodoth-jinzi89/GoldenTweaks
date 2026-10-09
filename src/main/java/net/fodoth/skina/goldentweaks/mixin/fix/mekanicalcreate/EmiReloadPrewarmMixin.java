package net.fodoth.skina.goldentweaks.mixin.fix.mekanicalcreate;

import dev.emi.emi.runtime.EmiReloadManager;
import net.fodoth.skina.goldentweaks.compat.mekanicalcreate.GTMekanicalDeferredRecipes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 一进入世界、EMI 刚开始重载时就把 mekanicalcreate 那两趟配方目录计算丢到后台，
 * 让它与整段重载（插件注册、物品索引、搜索索引……）重叠，见
 * {@link GTMekanicalDeferredRecipes}。
 *
 * <p>{@code prewarm()} 幂等；三个入口都挂上是为了不依赖到底是哪条路径触发的重载。</p>
 */
@Mixin(value = EmiReloadManager.class, remap = false)
public abstract class EmiReloadPrewarmMixin {

    @Inject(method = "reload", at = @At("HEAD"))
    private static void goldentweaks$prewarmOnReload(CallbackInfo ci) {
        GTMekanicalDeferredRecipes.prewarm();
    }

    @Inject(method = "reloadTags", at = @At("HEAD"))
    private static void goldentweaks$prewarmOnReloadTags(CallbackInfo ci) {
        GTMekanicalDeferredRecipes.prewarm();
    }

    @Inject(method = "reloadRecipes", at = @At("HEAD"))
    private static void goldentweaks$prewarmOnReloadRecipes(CallbackInfo ci) {
        GTMekanicalDeferredRecipes.prewarm();
    }
}
