package net.fodoth.skina.goldentweaks.mixin.fix.questshop;

import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.screen.ShopMenuScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShopMenuScreen.class)
public class ShopMenuScreenLocalizationMixin {


    @ModifyArg(
            method = "renderLabels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"
            ),
            index = 1
    )
    private Component gt$title(Component p_282131_) {
        return Component.translatable("gui.questshop.title");
    }

    @ModifyVariable(
            method = "renderLabels",
            at = @At("STORE"),
            name = "coins")
    private String gt$coins(String coins) {

        return Component.translatable(
                "gui.questshop.coins",
                ClientCoins.get()
        ).getString();
    }

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void gt$buy(CallbackInfo ci) {

        ShopMenuScreen self = (ShopMenuScreen)(Object)this;

        for (var w : self.children()) {
            if (w instanceof net.minecraft.client.gui.components.Button b) {
                if (b.getMessage().getString().equals("Buy")) {
                    b.setMessage(Component.translatable("gui.questshop.buy"));
                }
            }
        }
    }
}