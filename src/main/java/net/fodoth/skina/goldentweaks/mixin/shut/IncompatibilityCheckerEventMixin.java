package net.fodoth.skina.goldentweaks.mixin.shut;

import com.gametechbc.traveloptics.setup.IncompatibilityCheckerEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(IncompatibilityCheckerEvent.class)
public class IncompatibilityCheckerEventMixin {

    @Redirect(
            method = "onPlayerLogin",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private static void cancelChatMessage(ServerPlayer instance, Component mesage) {
    }
}
