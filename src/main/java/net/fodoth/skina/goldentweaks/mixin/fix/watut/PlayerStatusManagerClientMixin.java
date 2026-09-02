package net.fodoth.skina.goldentweaks.mixin.fix.watut;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.corosus.watut.PlayerStatusManagerClient", remap = false)
public class PlayerStatusManagerClientMixin {
    @Redirect(method = "getTypingPlayers", at = @At(value = "FIELD", target = "Lcom/corosus/watut/config/ConfigClient;screenTypingText:Ljava/lang/String;"))
    private String goldentweaks$localizedTypingText() {
        return Component.translatable("goldentweaks.watut.screen_typing_text").getString();
    }

    @Redirect(method = "getTypingPlayers", at = @At(value = "FIELD", target = "Lcom/corosus/watut/config/ConfigClient;screenTypingMultiplePlayersText:Ljava/lang/String;"))
    private String goldentweaks$localizedMultipleTypingText() {
        return Component.translatable("goldentweaks.watut.screen_typing_multiple_players").getString();
    }
}
