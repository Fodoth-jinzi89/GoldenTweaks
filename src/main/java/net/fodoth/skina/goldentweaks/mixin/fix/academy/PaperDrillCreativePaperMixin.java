package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.skill.ability.telekinesis.PaperDrillEffect;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 念力·纸钻（{@code paper_drill}）在创造模式下本来就不需要、也不会消耗纸（{@code canStartCharging} 与
 * {@code consumePaper} 均已对 {@code instabuild} 放行），但结束/中止/资源不足时仍会调用 {@code givePaper}
 * 退回 64 张纸，等于凭空多出纸。这里在创造模式下跳过退纸。
 */
@Mixin(PaperDrillEffect.class)
public class PaperDrillCreativePaperMixin {

    @Inject(method = "givePaper", at = @At("HEAD"), cancellable = true)
    private static void gt$noPaperRefundInCreative(ServerPlayer player, CallbackInfo ci) {
        if (player.getAbilities().instabuild) {
            ci.cancel();
        }
    }
}
