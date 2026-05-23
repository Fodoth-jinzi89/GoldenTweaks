package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class GTConfirmReleaseScreen extends Screen {

    private final Screen parent;
    private final Component message;
    private final Consumer<Boolean> callback;

    public GTConfirmReleaseScreen(Screen parent, Component title, Component message, Consumer<Boolean> callback) {
        super(title);
        this.parent = parent;
        this.message = message;
        this.callback = callback;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 60;
        int buttonHeight = 20;
        int spacing = 10;

        int totalWidth = buttonWidth * 2 + spacing;
        int startX = (this.width - totalWidth) / 2;
        int buttonY = this.height / 2 + 20;

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.yes"), b -> {
                    callback.accept(true);
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(parent);
                    }
                }).bounds(startX, buttonY, buttonWidth, buttonHeight).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.cancel"), b -> {
                    callback.accept(false);
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(parent);
                    }
                }).bounds(startX + buttonWidth + spacing, buttonY, buttonWidth, buttonHeight).build()
        );
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        Component title = this.getTitle();
        int titleWidth = this.font.width(title);

        guiGraphics.drawString(
                this.font,
                title,
                (this.width - titleWidth) / 2,
                this.height / 2 - 40,
                0xFFFFFF
        );

        int msgWidth = this.font.width(message);
        guiGraphics.drawString(
                this.font,
                message,
                (this.width - msgWidth) / 2,
                this.height / 2 - 10,
                0xFFFFFF
        );

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xAA000000);

        int boxWidth = 300;
        int boxHeight = 120;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = (this.height - boxHeight) / 2;

        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0x90000000);
        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + 1, 0xFFFFFFFF);
        guiGraphics.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, 0xFFFFFFFF);
        guiGraphics.fill(boxX, boxY, boxX + 1, boxY + boxHeight, 0xFFFFFFFF);
        guiGraphics.fill(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, 0xFFFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            callback.accept(false);
            if (this.minecraft != null) {
                this.minecraft.setScreen(parent);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}