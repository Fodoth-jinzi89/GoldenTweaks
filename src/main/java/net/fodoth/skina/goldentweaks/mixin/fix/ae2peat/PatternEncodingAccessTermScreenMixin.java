package net.fodoth.skina.goldentweaks.mixin.fix.ae2peat;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import cn.dancingsnow.neoecoae.api.PatternEncodingTermMenuExtension;
import cn.dancingsnow.neoecoae.gui.widget.UploadButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.client.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mixin(PatternEncodingAccessTermScreen.class)
public class PatternEncodingAccessTermScreenMixin<C extends PatternEncodingAccessTermMenu> extends AEBaseScreen<C> {


    public PatternEncodingAccessTermScreenMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    // =========================
    // init injection
    // =========================
    @Inject(method = "init", at = @At("TAIL"))
    private void goldenTweaks$init(CallbackInfo ci) {

        int left = (this.width - this.imageWidth) / 2 + this.imageWidth;
        int top = (this.height - this.imageHeight) / 2 + this.imageHeight;

        this.addRenderableWidget(
                new UploadButton(
                        left,
                        top - 173,
                        b -> ((PatternEncodingTermMenuExtension) this.getMenu()).neoecoae$uploadPattern()
                )
        );
    }


}