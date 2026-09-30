package net.fodoth.skina.goldentweaks.mixin.fix.kaleidoscope;

import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Kaleidoscope Cookery 1.5.1 的 {@link SoupBaseManager#registerSoupBase} 在名字已存在时直接抛
 * {@link IllegalArgumentException}。当汤底注册被重复触发（同一 reload 内再次执行 {@code ModSoupBases.registerAll}）
 * 时，这会变成一个致命的同步工作队列异常（{@code Soup base with name minecraft:milk already exists!}）。
 *
 * <p>这里改为幂等：重复注册直接跳过，保留先注册的那份，不再中断加载。</p>
 */
@Mixin(SoupBaseManager.class)
public abstract class SoupBaseManagerMixin {

    @Inject(method = "registerSoupBase", at = @At("HEAD"), cancellable = true)
    private static void goldentweaks$skipDuplicateSoupBase(ISoupBase soupBase, CallbackInfo ci) {

        if (SoupBaseManager.containsSoupBase(soupBase.getName())) {
            GoldenTweaks.LOGGER.warn(
                    "[GoldenTweaks] Skipped duplicate Kaleidoscope soup base {}",
                    soupBase.getName()
            );
            ci.cancel();
        }
    }
}
