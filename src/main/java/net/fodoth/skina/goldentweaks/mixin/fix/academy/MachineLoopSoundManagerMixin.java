package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.client.sound.MachineLoopSoundManager;
import com.mohistmc.academy.world.block.entity.ImagFusorBlockEntity;
import com.mohistmc.academy.world.block.entity.MetalFomerBlockEntity;
import net.fodoth.skina.goldentweaks.compat.academy.AcademySoundGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 与 {@link AbilityLoopSoundManager} 同因：机器循环音效未成功注册时，
 * tickMetalFormer / tickImagFusor 读取未绑定 Holder 会抛
 * "Trying to access unbound value"。未绑定时跳过对应机器的音效同步。
 */
@Mixin(MachineLoopSoundManager.class)
public class MachineLoopSoundManagerMixin {

    @Inject(method = "tickMetalFormer", at = @At("HEAD"), cancellable = true)
    private static void gt$skipMetalFormerWhenUnbound(MetalFomerBlockEntity blockEntity, CallbackInfo ci) {
        if (AcademySoundGuard.hasUnboundSoundEvent()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickImagFusor", at = @At("HEAD"), cancellable = true)
    private static void gt$skipImagFusorWhenUnbound(ImagFusorBlockEntity blockEntity, CallbackInfo ci) {
        if (AcademySoundGuard.hasUnboundSoundEvent()) {
            ci.cancel();
        }
    }
}
