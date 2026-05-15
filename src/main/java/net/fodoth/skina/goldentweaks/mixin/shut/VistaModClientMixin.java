package net.fodoth.skina.goldentweaks.mixin.shut;

import net.mehvahdjukaar.vista.VistaModClient;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VistaModClient.class)
public class VistaModClientMixin {

    @Inject(method = "onFirstScreen", at = @At("HEAD"), cancellable = true)
    private static void goldentweaks$disableWelcomeScreen(Screen screen, CallbackInfo ci) {
        ci.cancel();
    }
}
