package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftAdditionalItems;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import thaumcraft.api.aspects.Aspect;
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
        drawIcon(graphics, data.first(), x, y);
        drawIcon(graphics, data.second(), x + ICON_SIZE + GAP, y);
    }

    /** The {@code cosmic} aspect icon is drawn with its cosmic proxy item model. */
    private static void drawIcon(GuiGraphics graphics, Aspect aspect, int x, int y) {
        if (net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry.isCosmic(aspect.tag())) {
            graphics.renderItem(GTThaumcraftAdditionalItems.cosmicIconStack(), x, y);
        } else {
            AspectGuiRenderer.draw(graphics, aspect, x, y, ICON_SIZE, 1.0f);
        }
    }
}
