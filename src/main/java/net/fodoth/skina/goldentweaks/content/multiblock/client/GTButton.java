package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * One sprite button: a plate that reacts to the mouse, an icon for the state it is in, and the
 * words that explain it.
 *
 * <p>The two layers are independent. The plate follows the cursor being <em>held</em> down on the
 * button — not merely hovering it — and the icon follows whatever the button toggles, which the
 * caller passes in. That separation is what makes a click read as a press while the icon keeps
 * saying which way the switch is set; folding both into one sprite made a switch look permanently
 * held down the moment the cursor went near it.</p>
 *
 * <p>The tooltip lives here too, because a button's wording is a property of the button: its state
 * changes with the thing it toggles, and keeping the sentence next to the plate is what stops a
 * window from owning a second, parallel switch that has to be kept in step with this one.</p>
 *
 * <p>Geometry is given in panel coordinates, in the sheet's own pixels. Sprites keep transparent
 * padding so every one of them is the same size as the button it belongs to, which is why the icon
 * is blitted at the plate's full size at the same origin rather than positioned by hand.</p>
 */
public final class GTButton {

    /** Where the plate and its icon are cut from. */
    public record Sprite(int u, int v, int width, int height, int pressedU) {
    }

    private final Sprite plate;
    private final Sprite icon;
    private final int x;
    private final int y;
    @Nullable
    private final Supplier<List<Component>> tooltip;

    /**
     * @param plate the base plate, and the column its pressed twin sits in
     * @param icon  the icon layer, whose {@code u} is chosen per state by the caller
     */
    public GTButton(Sprite plate, Sprite icon, int x, int y) {
        this(plate, icon, x, y, null);
    }

    /**
     * @param tooltip the words for the state the button is currently in, evaluated when the cursor
     *                is on it. A supplier rather than a fixed string because the answer changes as
     *                the switch is flipped.
     */
    public GTButton(Sprite plate, Sprite icon, int x, int y,
                    @Nullable Supplier<List<Component>> tooltip) {
        this.plate = plate;
        this.icon = icon;
        this.x = x;
        this.y = y;
        this.tooltip = tooltip;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public int width() {
        return this.plate.width();
    }

    public int height() {
        return this.plate.height();
    }

    /**
     * The same button, carrying a tooltip.
     *
     * <p>A copy rather than a setter: geometry is declared once and shared, and each window attaches
     * only the wording it can answer for.</p>
     */
    public GTButton withTooltip(Supplier<List<Component>> text) {
        return new GTButton(this.plate, this.icon, this.x, this.y, text);
    }

    /** Whether the cursor is inside this button, in the same coordinates the drawing uses. */
    public boolean hits(double mouseX, double mouseY, int panelLeft, int panelTop) {
        int left = panelLeft + this.x;
        int top = panelTop + this.y;
        return mouseX >= left && mouseX < left + width()
                && mouseY >= top && mouseY < top + height();
    }

    /**
     * Draws the button.
     *
     * @param sheet     the sheet both layers are cut from
     * @param held      whether the cursor is holding this button down right now
     * @param iconU     the icon's column for the state being shown
     * @param iconV     the icon's row, or {@code null} to skip the icon layer
     * @param panelLeft window origin, since the button stores panel coordinates
     * @param panelTop  window origin
     */
    public void draw(GuiGraphics graphics, ResourceLocation sheet,
                     boolean held, int iconU, @Nullable Integer iconV,
                     int panelLeft, int panelTop) {
        drawPlate(graphics, sheet, held, panelLeft, panelTop);
        if (iconV != null) {
            graphics.blit(sheet, panelLeft + this.x, panelTop + this.y,
                    iconU, iconV, width(), height());
        }
    }

    /** The plate on its own, for a button whose icon is drawn some other way. */
    public void drawPlate(GuiGraphics graphics, ResourceLocation sheet, boolean held,
                          int panelLeft, int panelTop) {
        graphics.blit(sheet, panelLeft + this.x, panelTop + this.y,
                held ? this.plate.pressedU() : this.plate.u(), this.plate.v(),
                width(), height());
    }

    /**
     * Shows this button's tooltip if the cursor is on it.
     *
     * @return whether a tooltip was drawn, so the caller can stop looking at the next button
     */
    public boolean renderTooltip(GuiGraphics graphics, Font font,
                                 double mouseX, double mouseY, int panelLeft, int panelTop) {
        List<Component> lines = tooltipAt(mouseX, mouseY, panelLeft, panelTop);
        if (lines == null || lines.isEmpty()) {
            return false;
        }
        // A single line goes through the ordinary call so it wraps and positions exactly like every
        // other tooltip in the game; several go through the list form.
        if (lines.size() == 1) {
            graphics.renderTooltip(font, lines.getFirst(), (int) mouseX, (int) mouseY);
        } else {
            graphics.renderComponentTooltip(font, lines, (int) mouseX, (int) mouseY);
        }
        return true;
    }

    /** The tooltip for where the cursor is now, or {@code null} when it is not on this button. */
    @Nullable
    public List<Component> tooltipAt(double mouseX, double mouseY, int panelLeft, int panelTop) {
        if (this.tooltip == null || !hits(mouseX, mouseY, panelLeft, panelTop)) {
            return null;
        }
        return this.tooltip.get();
    }

    /**
     * Draws the first button whose tooltip applies.
     *
     * <p>Buttons never overlap, so the first hit is the only hit; offering this as a static keeps
     * every window from re-writing the same loop.</p>
     */
    public static void renderTooltips(GuiGraphics graphics, Font font, List<GTButton> buttons,
                                      double mouseX, double mouseY,
                                      int panelLeft, int panelTop) {
        for (GTButton button : buttons) {
            if (button.renderTooltip(graphics, font, mouseX, mouseY, panelLeft, panelTop)) {
                return;
            }
        }
    }
}
