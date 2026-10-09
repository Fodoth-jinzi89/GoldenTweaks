package net.fodoth.skina.goldentweaks.mixin.fix.tmrv;

import dev.nolij.toomanyrecipeviewers.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TMRV 的 {@code RecipeManager$Category$Recipe#setPlugin} 在"已经设过 plugin"时直接抛裸
 * {@code IllegalStateException}：
 *
 * <pre>
 * private synchronized void setPlugin(Plugin plugin) {
 *    if (this.plugin != null) throw new IllegalStateException();
 *    else this.plugin = plugin;
 * }
 * </pre>
 *
 * <p>星辉魔法会把同一条流明（Lumen）配方反复注册进不同分类，于是每次重载都有 21 条配方
 * 被 TMRV 判成 "Found a broken recipe" 丢掉（EMI 里看不到这些星辉配方）。</p>
 *
 * <p>改成幂等：已经设过就保留原有归属、不再抛。归属只用于展示与去重，保留原值不会出错。</p>
 */
@Mixin(targets = "dev.nolij.toomanyrecipeviewers.impl.jei.api.recipe.RecipeManager$Category$Recipe", remap = false)
public abstract class TmrvRecipeSetPluginMixin {

    @Shadow
    private Plugin plugin;

    @Inject(method = "setPlugin", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$idempotentSetPlugin(Plugin incoming, CallbackInfo ci) {
        if (this.plugin != null) {
            ci.cancel();
        }
    }
}
