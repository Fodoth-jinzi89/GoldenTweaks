package net.fodoth.skina.goldentweaks.mixin.fix;

import dev.shadowsoffire.apotheosis.mobs.ApothMobEvents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ApothMobEvents.class)
public class ApothesisClientLanguageMixin {

    @Redirect(
            method = "finalizeMobSpawns",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;getString()Ljava/lang/String;"
            )
    )
    private String goldentweaks$safeGetString(Component instance) {
        try {
            return instance.getString();
        } catch (Throwable ignored) {
            return "unknown";
        }
    }
}