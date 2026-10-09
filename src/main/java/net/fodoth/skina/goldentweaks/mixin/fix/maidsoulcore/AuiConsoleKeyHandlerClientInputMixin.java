package net.fodoth.skina.goldentweaks.mixin.fix.maidsoulcore;

import com.maidsoul.brain.forge.client.AuiConsoleKeyHandler;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 女仆核心的 K / V 快捷键走 InputEvent.Key 直接匹配，不检查是否打开了界面，
 * 导致在聊天栏输入 /kill 时按到 K 会唤起对话框。这里在打开任意界面时直接跳过。
 */
@Mixin(value = AuiConsoleKeyHandler.ClientInput.class, remap = false)
public class AuiConsoleKeyHandlerClientInputMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$onlyInGame(InputEvent.Key event, CallbackInfo ci) {
        if (Minecraft.getInstance().screen != null
                && GoldenTweaksClientConfig.doBlockMaidHotkeysInGui()) {
            ci.cancel();
        }
    }
}
