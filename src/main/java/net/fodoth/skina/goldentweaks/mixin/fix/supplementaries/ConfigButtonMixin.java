package net.fodoth.skina.goldentweaks.mixin.fix.supplementaries;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.mehvahdjukaar.supplementaries.common.events.platform.ClientEventsForge;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Removes Supplementaries' config button from the bottom-left corner of the main menu and the pause menu.
 *
 * <p>Supplementaries registers {@code ClientEventsForge#onScreenInit(ScreenEvent.Init.Post)} for the client
 * screen-init event. That handler does exactly one thing - it builds the listener list and calls
 * {@code ClientEvents.addConfigButton(screen, listeners, onAdd, onRemove)} - so cancelling it removes the
 * gear button and nothing else. Verified against the mod's bytecode before writing this
 * ({@code javap -c} on {@code ClientEventsForge#onScreenInit}: {@code getScreen} + {@code getListenersList}
 * + two lambdas + the single {@code addConfigButton} call).</p>
 *
 * <p>The handler is {@code public static}, matching the target exactly (a mismatch there is what crashed the
 * server twice in the TLM attempts, so the descriptor was taken from javap rather than guessed).</p>
 */
@Mixin(value = ClientEventsForge.class, remap = false)
public class ConfigButtonMixin {

    /** One debug line per game session, so this can be confirmed without adding log noise. */
    private static final AtomicBoolean LOGGED = new AtomicBoolean();

    @Inject(method = "onScreenInit", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$skipConfigButton(ScreenEvent.Init.Post event, CallbackInfo ci) {
        if (LOGGED.compareAndSet(false, true)) {
            GoldenTweaks.LOGGER.debug("[Supplementaries 兼容] 已禁用主界面/暂停界面的设置按钮（本条只打印一次）");
        }

        ci.cancel();
    }
}
