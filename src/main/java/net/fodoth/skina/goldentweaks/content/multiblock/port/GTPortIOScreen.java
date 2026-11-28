package net.fodoth.skina.goldentweaks.content.multiblock.port;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.client.GTButton;
import net.fodoth.skina.goldentweaks.network.packet.C2SPortTogglePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTEnergyFormat;

/**
 * Window for an item or fluid port.
 *
 * <p>{@code gt_port_io} carries a 3 by 3 storage grid with its rail, and the two switches that
 * apply to it: whether an entry spans several cells, and whether the port reaches through.</p>
 */
public final class GTPortIOScreen extends GTPortScreen {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID,
                    "textures/gui/gt_port_io.png");

    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 65;
    private static final int MODE_ICON_V = 195;

    private static final int ACTIVE_ICON_U_PASSIVE = 44;
    private static final int ACTIVE_ICON_U_ACTIVE = 65;
    private static final int ACTIVE_ICON_V = 212;

    /**
     * The stacking switch, on the first plate of the top-right block.
     *
     * <p>Only shown here: an energy port has no cells for an entry to span, so the setting controls
     * nothing there and its plate is given to the reach-through switch instead.</p>
     */
    private final GTButton modeButton = new GTButton(
            new GTButton.Sprite(44, 178, 21, 17, 65),
            new GTButton.Sprite(44, MODE_ICON_V, 21, 17, 65),
            154, 27,
            () -> List.of(Component.translatable(this.menu.isSingleCellMode()
                    ? "gui.goldentweaks.port.stack.single"
                    : "gui.goldentweaks.port.stack.multi")));

    /**
     * The reach-through switch, on the second plate.
     *
     * <p>Sitting it after the stacking switch keeps the row contiguous.</p>
     */
    private final GTButton activeButton = new GTButton(
            new GTButton.Sprite(44, 178, 21, 17, 65),
            new GTButton.Sprite(44, ACTIVE_ICON_V, 21, 17, 65),
            179, 27,
            () -> List.of(Component.translatable(this.menu.isActivePort()
                    ? "gui.goldentweaks.port.active.on"
                    : "gui.goldentweaks.port.active.off")));

    public GTPortIOScreen(GTPortMenu menu, Inventory inventory, Component title) {
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
    protected void drawMiddle(GuiGraphics graphics) {
        drawStacks(graphics);
        drawRail(graphics);
    }

    @Override
    protected void drawSwitches(GuiGraphics graphics, int mouseX, int mouseY) {
        drawModeButton(graphics, mouseX, mouseY);
        drawActiveButton(graphics, mouseX, mouseY);
    }

    @Override
    protected List<GTButton> switches() {
        return List.of(this.modeButton, this.activeButton);
    }

    @Override
    protected boolean clickSwitch(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        if (this.modeButton.hits(mouseX, mouseY, this.leftPos, this.topPos)) {
            toggle(C2SPortTogglePacket.Target.STACK_MODE);
            return true;
        }
        if (this.activeButton.hits(mouseX, mouseY, this.leftPos, this.topPos)) {
            toggle(C2SPortTogglePacket.Target.ACTIVE);
            return true;
        }
        return false;
    }

    /**
     * The capacity readout: how much is stored against how much the port can hold, in the compact
     * ladder so a high-tier figure stays readable, plus how many of the nine cells are taken.
     */
    @Override
    protected void drawCapacity(GuiGraphics graphics) {
        String unit = this.menu.kind() == GTPortKind.FLUID ? " mB" : "";
        String amount = GTEnergyFormat.format(this.menu.syncedAmount())
                + " / " + GTEnergyFormat.format(this.menu.syncedCapacity()) + unit;
        String cells = this.menu.usedCells() + " / " + this.menu.totalCells() + " "
                + Component.translatable("gui.goldentweaks.port.cells").getString();

        graphics.pose().pushPose();
        graphics.pose().translate(CAPTION_X, CAPTION_Y, 0.0F);
        graphics.pose().scale(CAPTION_SCALE, CAPTION_SCALE, 1.0F);
        graphics.drawString(this.font, amount, 0, 0, CAPTION_COLOUR, false);
        graphics.drawString(this.font, cells, 0, this.font.lineHeight, CAPTION_COLOUR, false);
        graphics.pose().popPose();
    }

    /** The stacking switch: base plate, then the icon for the mode it is in. */
    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean held = isPressed(this.leftPos + this.modeButton.x(),
                this.topPos + this.modeButton.y(),
                this.modeButton.width(), this.modeButton.height(), mouseX, mouseY);
        int iconU = this.menu.isSingleCellMode() ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI;
        this.modeButton.draw(graphics, TEXTURE, held, iconU, MODE_ICON_V,
                this.leftPos, this.topPos);
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
