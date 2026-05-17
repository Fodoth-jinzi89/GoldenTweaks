package net.fodoth.skina.goldentweaks.mixin.fix;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xxrexraptorxx.allthecompatibility.utils.Config;
import xxrexraptorxx.allthecompatibility.utils.Events;

@Pseudo
@Mixin(value = Events.class, remap = false)
public class ATCEventsMixin {

    @Inject(
            method = "onClientTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void goldentweaks$preventEarlyConfigAccess(
            ClientTickEvent.Pre event,
            CallbackInfo ci
    ) {
        try {
            // config object itself may still be null
            if (Config.UPDATE_CHECKER == null) {
                ci.cancel();
                return;
            }

            // force test whether config is loaded
            Config.UPDATE_CHECKER.get();

        } catch (IllegalStateException ignored) {
            // config not loaded yet
            ci.cancel();

        } catch (Throwable ignored) {
            ci.cancel();
        }
    }
}
