package net.fodoth.skina.goldentweaks.mixin.fix.maidsoulcore;

import com.maidsoul.brain.forge.client.NoviceTutorialAutoPopup;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 女仆核心首次进入世界时会自动弹出新手教程界面，
 * 关闭「禁用女仆核心新手教程弹窗」后不再自动弹出（仍可从控制台菜单手动打开）。
 */
@Mixin(value = NoviceTutorialAutoPopup.class, remap = false)
public class NoviceTutorialAutoPopupMixin {

    @Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$disableAutoPopup(ClientTickEvent.Post event, CallbackInfo ci) {
        if (GoldenTweaksClientConfig.doDisableMaidSoulTutorialPopup()) {
            ci.cancel();
        }
    }
}
