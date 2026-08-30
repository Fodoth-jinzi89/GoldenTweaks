package net.fodoth.skina.goldentweaks.mixin.fix.ae2autopatternupload;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import com.gali.ae2_auto_pattern_upload.mixin.ScreenAccessor;
import net.fodoth.skina.goldentweaks.compat.ae2autopatternupload.AutoPatternUploadButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEBaseScreen.class, priority = 900)
public abstract class AEBaseScreenMixin {
    @Inject(method = "init", at = @At("TAIL"), order = 2000)
    private void gt$moveUploadButton(CallbackInfo ci) {
        if (!((Object) this instanceof PatternEncodingTermScreen<?> screen)) {
            return;
        }
        ScreenAccessor accessor = (ScreenAccessor) this;
        accessor.ae2apu$getRenderables().removeIf(AutoPatternUploadButton.class::isInstance);
        accessor.ae2apu$getChildren().removeIf(AutoPatternUploadButton.class::isInstance);
        int x = screen.getGuiLeft() + screen.getXSize();
        int y = screen.getGuiTop() + screen.getYSize() - 153;
        AutoPatternUploadButton button = new AutoPatternUploadButton(x, y);
        accessor.ae2apu$getRenderables().add(button);
        accessor.ae2apu$getChildren().add(button);
    }
}
