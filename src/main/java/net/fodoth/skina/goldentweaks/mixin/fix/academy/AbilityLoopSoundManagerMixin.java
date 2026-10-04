package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.client.sound.AbilityLoopSoundManager;
import net.fodoth.skina.goldentweaks.compat.academy.AcademySoundGuard;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AbilityLoopSoundManager.tick 每个客户端刻都会无条件读取 AcademySounds 的音效 Holder，
 * 一旦学院音效未成功注册（DeferredHolder 未绑定），Holder.value() 会抛
 * "Trying to access unbound value" 导致客户端崩溃。
 * 这里在 tick 开头检查学院音效是否全部绑定，未绑定则跳过整个 tick。
 */
@Mixin(AbilityLoopSoundManager.class)
public class AbilityLoopSoundManagerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void gt$skipWhenSoundEventsUnbound(ClientTickEvent.Post event, CallbackInfo ci) {
        if (AcademySoundGuard.hasUnboundSoundEvent()) {
            ci.cancel();
        }
    }
}
