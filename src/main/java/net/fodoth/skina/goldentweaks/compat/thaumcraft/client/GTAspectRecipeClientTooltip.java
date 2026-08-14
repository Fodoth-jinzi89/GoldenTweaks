package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import thaumcraft.client.gui.AspectGuiRenderer;

/**
 * Renders {@link GTAspectRecipeTooltip} as the two component aspect icons laid
 * out side by side.
 */
public final class GTAspectRecipeClientTooltip implements ClientTooltipComponent {

    private static final int ICON_SIZE = 16;
    private static final int GAP = 6;

    private final GTAspectRecipeTooltip data;

    public GTAspectRecipeClientTooltip(GTAspectRecipeTooltip data) {
        this.data = data;
    }

    @Override
    public int getHeight() {
        return ICON_SIZE;
    }

    @Override
    public int getWidth(Font font) {
        return ICON_SIZE + GAP + ICON_SIZE;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        AspectGuiRenderer.draw(graphics, data.first(), x, y, ICON_SIZE, 1.0f);
        AspectGuiRenderer.draw(graphics, data.second(), x + ICON_SIZE + GAP, y, ICON_SIZE, 1.0f);
    }
}
