package net.fodoth.skina.goldentweaks.mixin.feature.stop_mod_reposts;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xxrexraptorxx.allthecompatibility.utils.Events;

@Mixin(Events.class)
public abstract class ATCEventsClientMixin {

    /**
     * @reason Cancels the client tick event that performs version update checks.
     *         These network requests originate from external servers
     *         and can cause significant startup delays for users
     *         with poor connectivity or DNS resolution issues.
     * @author Fodoth_jinzi89
     */
    @Inject(
            method = "onClientTick",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onClientTick(ClientTickEvent.Pre event, CallbackInfo ci) {
        ci.cancel();
    }
}
