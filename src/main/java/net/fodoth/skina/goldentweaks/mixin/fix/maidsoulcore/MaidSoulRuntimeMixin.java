package net.fodoth.skina.goldentweaks.mixin.fix.maidsoulcore;

import com.maidsoul.brain.forge.runtime.MaidSoulRuntime;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 「心契誓约大模型连接」关闭时，后台轮次（主动发起等）被拦截后不再弹「思考核心处理失败」，
 * 让玩家只看到设置提示，而不是错误。
 */
@Mixin(value = MaidSoulRuntime.class, remap = false)
public class MaidSoulRuntimeMixin {

    @Inject(method = "notifyRuntimeErrorOnce", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt$suppressErrorChatWhenLlmDisabled(String detail, CallbackInfo ci) {
        if (!GoldenTweaksClientConfig.isMaidLlmEnabled()) {
            ci.cancel();
        }
    }
}
