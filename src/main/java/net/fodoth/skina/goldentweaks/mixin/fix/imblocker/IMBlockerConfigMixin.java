package net.fodoth.skina.goldentweaks.mixin.fix.imblocker;

import io.github.reserveword.imblocker.common.IMBlockerConfig;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * IMBlocker 只会为它认识的界面/输入框开启系统输入法，
 * 因此女仆核心的 ApricityUI 控制台（K 键）输入框无法使用中文输入法。
 * 这里把 ApricityUI 界面加入 IMBlocker 的界面白名单，使其在界面打开时开启输入法。
 */
@Mixin(value = IMBlockerConfig.class, remap = false)
public class IMBlockerConfigMixin {

    private static final String APRICITY_SCREEN = "com.sighs.apricityui.screen.ApricityScreen";

    @Inject(method = "isScreenInWhitelist", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt$enableImeForApricityScreens(Object screen, CallbackInfoReturnable<Boolean> cir) {
        if (screen != null
                && GoldenTweaksClientConfig.doFixMaidSoulAuiIme()
                && gt$isApricityScreen(screen.getClass())) {
            cir.setReturnValue(true);
        }
    }

    private static boolean gt$isApricityScreen(Class<?> type) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            if (APRICITY_SCREEN.equals(c.getName())) {
                return true;
            }
        }
        return false;
    }
}
