package net.fodoth.skina.goldentweaks.mixin.fix.languagereload;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityToStringMixin {

    @Redirect(
            method = "toString",
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
