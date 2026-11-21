package net.fodoth.skina.goldentweaks.mixin.fix.iris;

import net.irisshaders.iris.compat.sodium.config.IrisConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = IrisConfig.class, remap = false)
public abstract class IrisVideoSettingsTranslationMixin {

    @Redirect(method = "registerConfigLate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"), require = 3)
    private @NotNull MutableComponent gt$translateLabel(@NotNull String text) {
        return switch (text) {
            case "Settings" -> Component.translatable("options.iris.settings");
            case "Packs" -> Component.translatable("options.iris.shaderPackList");
            case "None" -> Component.translatable("options.off");
            default -> Component.literal(text);
        };
    }
}
