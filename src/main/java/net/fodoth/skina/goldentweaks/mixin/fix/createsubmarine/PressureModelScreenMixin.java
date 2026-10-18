package net.fodoth.skina.goldentweaks.mixin.fix.createsubmarine;

import com.maxenonyme.createsubmarine.submarine.client.PressureModelScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 首次进入主菜单时弹出的「水压模型」选择界面（CLASSIC / HYBRID / REALISTIC）。
 * 取消接管后不再询问，{@code PRESSURE_MODEL} 保持配置文件里的默认值不变。
 */
@Mixin(PressureModelScreen.class)
public class PressureModelScreenMixin {

    @Inject(
            method = "onScreenOpening",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$disablePressureModelScreen(
            ScreenEvent.Opening event,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}
