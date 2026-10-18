package net.fodoth.skina.goldentweaks.mixin.fix.createsubmarine;

import com.maxenonyme.createsubmarine.submarine.client.DeepSeasSafetyScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 进入主菜单时，{@code onScreenOpening} 会把标题界面换成光敏性安全须知界面再弹一次。
 * 取消这次接管，主菜单保持原样；玩家状态也不会被标记成「已确认过」。
 */
@Mixin(DeepSeasSafetyScreen.class)
public class DeepSeasSafetyScreenMixin {

    @Inject(
            method = "onScreenOpening",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$disableSafetyScreen(
            ScreenEvent.Opening event,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}
