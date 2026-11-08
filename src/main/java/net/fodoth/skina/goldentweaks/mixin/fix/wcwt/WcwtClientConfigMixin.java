package net.fodoth.skina.goldentweaks.mixin.fix.wcwt;

import com.lhy.wcwt.config.WcwtClientConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * wcwt's toolkit HUD polls {@code WcwtClientConfig.showToolkitHotbars()} from its very first
 * {@code ClientTickEvent.Pre} ({@code WcwtToolkitHud#onClientTickPre} → {@code isHudVisible}),
 * while NeoForge is still loading client configs on a worker thread. {@code ConfigValue#get()}
 * then throws {@code IllegalStateException: Cannot get config value before config is loaded.}
 * and takes the whole client down at startup (intermittent — it depends on who wins the race).
 * <p>
 * Return the option's default until the spec is actually loaded, the same way GoldenTweaks already
 * handles createcardboardthings.
 */
@Mixin(value = WcwtClientConfig.class, remap = false)
public abstract class WcwtClientConfigMixin {

    @Shadow
    private static ModConfigSpec SPEC;

    @Shadow
    private static ModConfigSpec.BooleanValue SHOW_TOOLKIT_HOTBARS;

    @Inject(method = "showToolkitHotbars", at = @At("HEAD"), cancellable = true)
    private static void gt$defaultWhenConfigNotLoaded(CallbackInfoReturnable<Boolean> cir) {
        if (!SPEC.isLoaded()) {
            cir.setReturnValue(SHOW_TOOLKIT_HOTBARS.getDefault());
        }
    }
}
