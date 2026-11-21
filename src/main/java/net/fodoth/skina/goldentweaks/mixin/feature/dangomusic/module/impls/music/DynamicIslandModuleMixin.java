package net.fodoth.skina.goldentweaks.mixin.feature.dangomusic.module.impls.music;

import me.sorihiko.dango.DangoClient;
import me.sorihiko.dango.module.impls.music.DynamicIslandModule;
import me.sorihiko.dango.module.values.FloatValue;
import me.sorihiko.dango.platform.Platform;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.config.HudAlignment;
import net.fodoth.skina.goldentweaks.util.GTState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = DynamicIslandModule.class, remap = false)
public abstract class DynamicIslandModuleMixin {

    @Shadow @Final private FloatValue scale;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void gt$hideInMainMenu(CallbackInfo ci) {
        if (GTState.isReady() && GoldenTweaksClientConfig.DANGO_STOP_ON_WORLD_LEAVE.get()
                && DangoClient.INSTANCE.getPlatform().getInMainMenu()) {
            ci.cancel();
        }
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE",
            target = "Lme/sorihiko/dango/ui/LayoutBounds;<init>(FFFF)V", ordinal = 0), require = 1, allow = 1)
    private void gt$positionIsland(Args args) {
        if (!GTState.isReady()) return;
        Platform platform = DangoClient.INSTANCE.getPlatform();
        float uiScale = Math.max(Math.min(platform.getWindowWidth() / 1280.0F,
                platform.getWindowHeight() / 800.0F), 0.5F);
        float canvasScale = uiScale * this.scale.getValue();
        float guiToCanvas = platform.getGuiScale() / canvasScale;
        HudAlignment alignment = GoldenTweaksClientConfig.DANGO_ISLAND_ALIGNMENT.get();
        float width = args.get(2);
        float height = args.get(3);
        args.set(0, (platform.getWindowWidth() / canvasScale - width) * alignment.horizontal
                + GoldenTweaksClientConfig.DANGO_ISLAND_X.get() * guiToCanvas);
        args.set(1, (platform.getWindowHeight() / canvasScale - height) * alignment.vertical
                + GoldenTweaksClientConfig.DANGO_ISLAND_Y.get() * guiToCanvas);
    }
}
