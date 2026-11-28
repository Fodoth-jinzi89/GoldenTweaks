package net.fodoth.skina.goldentweaks.content.multiblock.port;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.client.GTButton;
import net.fodoth.skina.goldentweaks.network.packet.C2SPortTogglePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.PortMode;

import java.util.List;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTEnergyFormat;

/**
 * Window for a dedicated energy hatch.
 *
 * <p>{@code gt_port_energy} replaces the storage grid with an upright trough, and since there are
 * no cells for an entry to span it carries only the reach-through switch — on the first plate,
 * because nothing sits in front of it.</p>
 */
public final class GTPortEnergyScreen extends GTPortScreen {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID,
                    "textures/gui/gt_port_energy.png");

    /** The upright trough, and the sprite it is filled from. */
    private static final int TROUGH_X = 82;
    private static final int TROUGH_Y = 32;
    private static final int TROUGH_SPRITE_V = 178;

    /**
     * The four caption lines under the trough, left-aligned with it.
     *
     * <p>Two headings and two values: how much is stored and how fast the port accepts more. They
     * start at the trough's own left edge so the bar and the block read as one column.</p>
     */
    private static final int ENERGY_TEXT_X = 82;
    private static final int ENERGY_TEXT_TOP = 46;

    private static final int ACTIVE_ICON_U_PASSIVE = 44;
    private static final int ACTIVE_ICON_U_ACTIVE = 65;
    private static final int ACTIVE_ICON_V = 212;

    /**
     * The reach-through switch, on the first plate.
     *
     * <p>An energy hatch has no stacking switch, so this one takes the first plate rather than
     * leaving a gap.</p>
     */
    private final GTButton activeButton = new GTButton(
            new GTButton.Sprite(44, 178, 21, 17, 65),
            new GTButton.Sprite(44, ACTIVE_ICON_V, 21, 17, 65),
            154, 27,
            () -> List.of(Component.translatable(this.menu.isActivePort()
                    ? "gui.goldentweaks.port.active.on"
                    : "gui.goldentweaks.port.active.off")));

    public GTPortEnergyScreen(GTPortMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected ResourceLocation sheet() {
        return TEXTURE;
    }

    @Override
    protected int sheetHeight() {
        return GTPortMenu.IMAGE_HEIGHT;
    }

    @Override
    protected boolean scrolls() {
        return false;
    }

    @Override
    protected void drawMiddle(GuiGraphics graphics) {
        drawTrough(graphics, this.leftPos + TROUGH_X, this.topPos + TROUGH_Y, TROUGH_SPRITE_V);
        drawEnergyCaptions(graphics);
    }

    @Override
    protected void drawSwitches(GuiGraphics graphics, int mouseX, int mouseY) {
        drawActiveButton(graphics, mouseX, mouseY);
    }

    @Override
    protected List<GTButton> switches() {
        return List.of(this.activeButton);
    }

    @Override
    protected boolean clickSwitch(double mouseX, double mouseY, int button) {
        if (button == 0 && this.activeButton.hits(mouseX, mouseY, this.leftPos, this.topPos)) {
            toggle(C2SPortTogglePacket.Target.ACTIVE);
            return true;
        }
        return false;
    }

    /**
     * The capacity readout beside the name.
     *
     * <p>An energy hatch prints its figures on the four caption lines under the trough instead, so
     * the readout beside the name is left empty rather than repeating the same two numbers in two
     * places.</p>
     */
    @Override
    protected void drawCapacity(GuiGraphics graphics) {
    }

    /**
     * The four caption lines under the trough, left-aligned with it.
     *
     * <p>Both figures go through the translations, units included: the port window and the machine
     * tooltips have to spell "1.06 kFE/t" the same way, and only one place can own that wording.</p>
     */
    private void drawEnergyCaptions(GuiGraphics graphics) {
        Component storedText = Component.translatable("gui.goldentweaks.port.energy.stored_value",
                GTEnergyFormat.formatWithUnit(this.menu.syncedAmount(), "FE"),
                GTEnergyFormat.formatWithUnit(this.menu.syncedCapacity(), "FE"));
        Component rateText = Component.translatable("gui.goldentweaks.port.energy.rate_value",
                GTEnergyFormat.formatWithUnit(this.menu.syncedRate(), "FE/t"));

        int x = this.leftPos + ENERGY_TEXT_X;
        int headingLine = this.font.lineHeight;
        int scaledLine = Math.round(headingLine * CAPTION_SCALE);
        int spacing = 1;
        int cursor = this.topPos + ENERGY_TEXT_TOP;

        graphics.drawString(this.font,
                Component.translatable("gui.goldentweaks.port.energy.stored"),
                x, cursor, CAPTION_COLOUR, false);
        cursor += headingLine + spacing;

        graphics.pose().pushPose();
        graphics.pose().translate(x, cursor, 0.0F);
        graphics.pose().scale(CAPTION_SCALE, CAPTION_SCALE, 1.0F);
        graphics.drawString(this.font, storedText, 0, 0, CAPTION_COLOUR, false);
        graphics.pose().popPose();
        cursor += scaledLine + spacing;

        // The rate reads differently depending on which way the port faces: an input hatch takes
        // energy in, a dynamo hatch pushes it out, and calling both "max input" would mislabel half
        // the ports.
        boolean output = this.menu.port() != null
                && this.menu.port().mode() == PortMode.OUTPUT;
        graphics.drawString(this.font,
                Component.translatable(output
                        ? "gui.goldentweaks.port.energy.output"
                        : "gui.goldentweaks.port.energy.input"),
                x, cursor, CAPTION_COLOUR, false);
        cursor += headingLine + spacing;

        graphics.pose().pushPose();
        graphics.pose().translate(x, cursor, 0.0F);
        graphics.pose().scale(CAPTION_SCALE, CAPTION_SCALE, 1.0F);
        graphics.drawString(this.font, rateText, 0, 0, CAPTION_COLOUR, false);
        graphics.pose().popPose();
    }

    /** The reach-through switch: plate, then the icon for the state it is in. */
    private void drawActiveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean held = isPressed(this.leftPos + this.activeButton.x(),
                this.topPos + this.activeButton.y(),
                this.activeButton.width(), this.activeButton.height(), mouseX, mouseY);
        int iconU = this.menu.isActivePort() ? ACTIVE_ICON_U_ACTIVE : ACTIVE_ICON_U_PASSIVE;
        this.activeButton.draw(graphics, TEXTURE, held, iconU, ACTIVE_ICON_V,
                this.leftPos, this.topPos);
    }
}
