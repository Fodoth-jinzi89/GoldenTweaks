package net.fodoth.skina.goldentweaks.mixin.fix.aeronautics_curios_compat;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.titammods.aeronautics_curios_compat.event.client.ClientKeyInputHandler", remap = false)
public abstract class ClientKeyInputHandlerMixin {
    @Redirect(
            method = "onClientTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z")
    )
    private static boolean gt$skipUnregisteredKey(KeyMapping keyMapping) {
        return keyMapping != null && keyMapping.consumeClick();
    }
}
