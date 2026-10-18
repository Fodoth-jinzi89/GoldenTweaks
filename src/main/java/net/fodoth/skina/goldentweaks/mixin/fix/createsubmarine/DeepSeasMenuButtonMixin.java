package net.fodoth.skina.goldentweaks.mixin.fix.createsubmarine;

import com.maxenonyme.createsubmarine.submarine.client.DeepSeasMenuButton;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 主界面与暂停界面上，Create 菜单按钮下方那颗「深海设置」按钮（压力护目镜图标）。
 * 它的加入全部发生在 {@code onScreenInit} 里，取消后按钮既不被渲染也无法点击。
 */
@Mixin(DeepSeasMenuButton.class)
public class DeepSeasMenuButtonMixin {

    @Inject(
            method = "onScreenInit",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$disableDeepSeasMenuButton(
            ScreenEvent.Init.Post event,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}
