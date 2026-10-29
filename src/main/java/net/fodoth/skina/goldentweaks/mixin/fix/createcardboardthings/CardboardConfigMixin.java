package net.fodoth.skina.goldentweaks.mixin.fix.createcardboardthings;

import net.chaolux.createcardboardthings.Config;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * createcardboardthings registers its config as a server config, but its client sound handler
 * ({@code CardboardJukeboxEvents#onPlaySound}) calls {@code cardboardJukebox()} for every sound it
 * sees — including the ones fired on the loading / title screen, before any world exists. NeoForge
 * then throws {@code IllegalStateException: Cannot get config value before config is loaded.} for
 * every such sound (logged as an EventBus error).
 * <p>
 * Return the option's default until the config is actually loaded.
 */
@Mixin(value = Config.class, remap = false)
public abstract class CardboardConfigMixin {

    @Shadow
    private static ModConfigSpec SPEC;

    @Shadow
    private static ModConfigSpec.BooleanValue CARDBOARD_JUKEBOX;

    @Inject(method = "cardboardJukebox", at = @At("HEAD"), cancellable = true)
    private static void gt$defaultWhenConfigNotLoaded(CallbackInfoReturnable<Boolean> cir) {
        if (!SPEC.isLoaded()) {
            cir.setReturnValue(CARDBOARD_JUKEBOX.getDefault());
        }
    }
}
