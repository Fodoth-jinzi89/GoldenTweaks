package net.fodoth.skina.goldentweaks.mixin.shut;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Auto-dismisses c2me's "experimental version" warning screen.
 *
 * <p>c2me's base module unconditionally pushes {@code ModExperimentalWarningScreen} into the client's initial-screen
 * list ({@code MixinMinecraftClient} → {@code Minecraft.addInitialScreens}), so every launch shows a
 * "You are running a experimental version of C2ME / Be careful! / …" screen that has to be clicked away.</p>
 *
 * <p>Instead of trying to unhook that (the add happens inside a mixin-merged method), this simply presses the screen's
 * own button for you: at the end of {@code init()} the screen's {@code onClose} runnable is queued on the client
 * thread, which is exactly what the "OK" button does ({@code onClose.run()}). Queuing via
 * {@code Minecraft#execute} keeps it out of the {@code setScreen} re-entrancy.</p>
 *
 * <p>{@code targets} is addressed by name so the c2me jar is not needed at compile time, and {@code require = 0}
 * means a future rename degrades to "warning visible again" rather than a crash.</p>
 */
@Mixin(targets = "com.ishland.c2me.base.common.client_warning.ModExperimentalWarningScreen", remap = false)
public class C2meExperimentalWarningScreenMixin {

    @Shadow
    @Final
    private Runnable onClose;

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void gt$autoDismiss(CallbackInfo ci) {
        Minecraft.getInstance().execute(() -> this.onClose.run());
    }
}
