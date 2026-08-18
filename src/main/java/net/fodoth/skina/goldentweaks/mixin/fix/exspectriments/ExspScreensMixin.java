package net.fodoth.skina.goldentweaks.mixin.fix.exspectriments;

import io.github.chromonym.exspectriments.ExspScreenHandlers;
import io.github.chromonym.exspectriments.ExspScreens;
import io.github.chromonym.exspectriments.screens.PigmentExtractorScreen;
import net.fodoth.skina.goldentweaks.mixin.fix.exspectriments.accessor.MenuScreensInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExspScreens.class)
public class ExspScreensMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$skipBrokenPrinterScreen(CallbackInfo ci) {
        MenuScreensInvoker.gt$register(
                ExspScreenHandlers.PIGMENT_EXTRACTOR_SCREEN_HANDLER,
                PigmentExtractorScreen::new
        );
        ci.cancel();
    }
}
