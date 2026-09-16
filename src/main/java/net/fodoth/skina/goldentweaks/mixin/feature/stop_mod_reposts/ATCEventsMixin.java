package net.fodoth.skina.goldentweaks.mixin.feature.stop_mod_reposts;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xxrexraptorxx.allthecompatibility.utils.Events;

@Mixin(Events.class)
public abstract class ATCEventsMixin {

    /**
     * @reason Cancels the supporter rewards event that checks against external
     *         patron lists hosted on GitHub. These HTTP requests can time out
     *         or hang indefinitely in regions with restricted internet access,
     *         negatively affecting the login experience.
     * @author Fodoth_jinzi89
     */
    @Inject(
            method = "SupporterRewards",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void SupporterRewards(PlayerEvent.PlayerLoggedInEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * @reason Cancels the player login event that displays the Stop Mod Reposts
     *         warning message. While we fully support the Stop Mod Reposts
     *         campaign and its goals, we have temporarily disabled this prompt
     *         to improve the overall user experience for modpack players.
     * @author Fodoth_jinzi89
     */
    @Inject(
            method = "onPlayerLogin",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
