package net.fodoth.skina.goldentweaks.mixin.fix.ae2autopatternupload;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import net.fodoth.skina.goldentweaks.compat.ae2autopatternupload.AutoPatternUploadButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.client.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mixin(PatternEncodingAccessTermScreen.class)
public abstract class PatternEncodingAccessTermScreenMixin<C extends PatternEncodingAccessTermMenu> extends AEBaseScreen<C> {
    protected PatternEncodingAccessTermScreenMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void gt$addAutoUploadButton(CallbackInfo ci) {
        int x = (width - imageWidth) / 2 + imageWidth;
        int y = (height - imageHeight) / 2 + imageHeight - 153;
        addRenderableWidget(new AutoPatternUploadButton(x, y));
    }
}
