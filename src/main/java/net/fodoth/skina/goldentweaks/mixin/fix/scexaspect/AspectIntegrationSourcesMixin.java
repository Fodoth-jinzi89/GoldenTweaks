package net.fodoth.skina.goldentweaks.mixin.fix.scexaspect;

import dev.scex.aspectjei.AspectCatalog;
import dev.scex.aspectjei.AspectIntegration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 「要素来源（物品含量）」页（{@code scex_aspect_jei:sources}）在 EMI 下的配方补注册。
 *
 * <p>rc7 把这个页面的配方完全交给 {@code SourceRecipeManagerPlugin}
 * （{@code ISimpleRecipeManagerPlugin}，按输入/输出焦点动态返回），
 * {@code AspectIntegration.recipes} 里只 {@code addRecipes(COMBINATIONS, …)}、
 * 把 catalog 存进 {@code sourceCatalog} 供那个插件用，<b>不再</b>注册 sources 的配方
 * （日志里那句 {@code sourceAspects=110} 只是计数）。</p>
 *
 * <p>真 JEI 下没问题：用户查某个要素时 JEI 带着焦点去问插件。但 EMI + TMRV 是在
 * <b>没有焦点</b>时一次性 materialise 插件结果，而且这一步发生在 {@code recipes()} 之前 ——
 * 那时 {@code sourceCatalog} 还是 null，于是 {@code scex_aspect_jei:sources} 分类里
 * 一条配方都没有，页面就是空的（实测 {@code tmrv … recipes=0}，而
 * {@code combinations} 走 {@code addRecipes} 有 104 条）。</p>
 *
 * <p>这里在 {@code recipes()} 末尾把已经建好的 catalog 直接 {@code addRecipes(SOURCES, …)}
 * 一遍，让 EMI 侧拿到全部来源配方。若以后版本自己又注册了，TMRV 的配方表按对象去重，
 * 重复添加不会产生重复页。</p>
 */
@Mixin(targets = "dev.scex.aspectjei.AspectIntegration", remap = false)
public abstract class AspectIntegrationSourcesMixin {

    @Shadow
    private static AspectCatalog.Catalog sourceCatalog;

    @Inject(method = "recipes", at = @At("TAIL"))
    private static void gt$registerSources(IRecipeRegistration registration, CallbackInfo ci) {
        try {
            AspectCatalog.Catalog catalog = sourceCatalog;
            if (catalog == null) {
                return;
            }
            List<AspectCatalog.Sources> sources = catalog.sources();
            if (sources.isEmpty()) {
                return;
            }
            registration.addRecipes(AspectIntegration.SOURCES, sources);
            GoldenTweaks.LOGGER.info(
                    "[GT] scex_aspect_jei: registered {} aspect-source recipe(s) for EMI"
                            + " (rc7 only exposes them through the focus-driven recipe-manager plugin)",
                    sources.size());
        } catch (Throwable throwable) {
            GoldenTweaks.LOGGER.warn("[GT] failed to register scex aspect-source recipes", throwable);
        }
    }
}
