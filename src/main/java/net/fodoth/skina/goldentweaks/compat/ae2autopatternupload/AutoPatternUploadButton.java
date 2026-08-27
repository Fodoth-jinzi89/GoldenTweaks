package net.fodoth.skina.goldentweaks.compat.ae2autopatternupload;

import appeng.client.gui.Icon;
import com.gali.ae2_auto_pattern_upload.network.RequestProvidersListC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class AutoPatternUploadButton extends Button {
    public AutoPatternUploadButton(int x, int y) {
        super(x, y, 18, 20, Component.empty(), button ->
                PacketDistributor.sendToServer(RequestProvidersListC2SPacket.INSTANCE), DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int hoverOffset = isHovered() ? 1 : 0;
        Icon background = isHovered()
                ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                : isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS : Icon.TOOLBAR_BUTTON_BACKGROUND;
        background.getBlitter().dest(getX() - 1, getY() + hoverOffset, width, height).zOffset(100).blit(guiGraphics);
        Icon.ARROW_UP.getBlitter().dest(getX(), getY() + 1 + hoverOffset).zOffset(200).blit(guiGraphics);
        if (isHovered()) {
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font,
                    List.of(Component.translatable("ae2_auto_pattern_upload.button.upload")), mouseX, mouseY);
        }
    }
}
