package net.fodoth.skina.goldentweaks.mixin.fix.extendedae_plus;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.AEBaseMenu;
import com.extendedae_plus.api.IExPatternPage;
import com.extendedae_plus.api.bridge.ExPatternProviderMenuPageBridge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEBaseScreen.class, priority = 800)
public abstract class AEBaseScreenMixin<T extends AEBaseMenu> extends AbstractContainerScreen<T> {

    @Shadow
    @Final
    protected ScreenStyle style;

    protected AEBaseScreenMixin(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void gt$renderPatternBetterPageNumber(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                                   float partialTick, CallbackInfo ci) {
        if (!((Object) this instanceof IExPatternPage page)
                || !(menu instanceof ExPatternProviderMenuPageBridge menuPage)) {
            return;
        }

        Component extendedAETitle = Component.translatable("itemGroup.extendedae");
        Component pageText = Component.translatable("gui.pattern_provider.page")
                .append(Integer.toString(page.eap$getCurrentPage() + 1))
                .append("/" + menuPage.eap$getAvailablePageCount());
        int color = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        guiGraphics.drawString(font, pageText,
                leftPos + font.width(extendedAETitle) + imageWidth / 10,
                topPos + imageHeight / 5 - 18,
                color, false);
    }
}
