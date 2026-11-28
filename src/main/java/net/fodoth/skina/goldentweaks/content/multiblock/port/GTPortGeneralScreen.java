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
 * Window for a general input/output assembly.
 *
 * <p>{@code gt_port_io_general} is a sheet of its own rather than a stretched copy of the other
 * two: an assembly holds contents <em>and</em> energy, so it needs both the storage grid and the
 * trough, plus a third switch plate. Drawing all that from the item sheet would mean three
 * conditional offsets on every constant; a sheet of its own keeps the numbers literal.</p>
 *
 * <p>Its band sits eleven pixels lower than a single-kind panel's, so its switch row, its run
 * switch and its trough row were all re-cut at their own coordinates — see the constants below.
 * They are a set, not an offset: one shift applied to everything assumed the art moved together,
 * and putting a drawn plate somewhere the cursor did not follow is what made the buttons look
 * misplaced.</p>
 */
public final class GTPortGeneralScreen extends GTPortScreen {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID,
                    "textures/gui/gt_port_io_general.png");

    /**
     * The split-mode switch, on the first plate of the top-right block.
     *
     * <p>The assembly's counterpart of the stacking switch, but it answers a different question:
     * whether each kind may spread over all the cells, or whether the cells are shared out between
     * the four kinds. Same plate, different wording.</p>
     */
    private static final GTButton.Sprite PLATE = new GTButton.Sprite(44, 189, 21, 17, 65);

    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 65;
    private static final int MODE_ICON_V = 206;

    private static final int ACTIVE_ICON_U_PASSIVE = 44;
    private static final int ACTIVE_ICON_U_ACTIVE = 65;
    private static final int ACTIVE_ICON_V = 223;

    private static final int CHANNEL_ICON_U_JOINS = 86;
    private static final int CHANNEL_ICON_U_ISOLATED = 107;
    private static final int CHANNEL_ICON_V = 206;

    /**
     * The split-mode switch, on the first plate of the top-right block.
     *
     * <p>The assembly's counterpart of the stacking switch, but it answers a different question:
     * whether each kind may spread over all the cells, or whether the cells are shared out between
     * the four kinds. Same plate, different wording.</p>
     */
    private final GTButton modeButton = new GTButton(PLATE, PLATE, 154, 27,
            () -> List.of(Component.translatable(this.menu.isSingleCellMode()
                    ? "gui.goldentweaks.port.split.on"
                    : "gui.goldentweaks.port.split.off")));

    /**
     * The reach-through switch, on the second plate.
     *
     * <p>It keeps its plate: the assembly still has an inventory and a trough that a pipe can
     * reach, so the channel switch sits below it rather than taking its place.</p>
     */
    private final GTButton activeButton = new GTButton(PLATE, PLATE, 179, 27,
            () -> List.of(Component.translatable(this.menu.isActivePort()
                    ? "gui.goldentweaks.port.active.on"
                    : "gui.goldentweaks.port.active.off")));

    /**
     * The channel-participation switch, on the first plate of the second row.
     *
     * <p>It answers whether the part counts as an energy hatch or dynamo in its channel, which is
     * the difference between joining a same-tier pair (and the doubling that brings) and being a
     * part the machine draws no power from. Four strings rather than two: the answer depends on the
     * direction as well.</p>
     */
    private final GTButton channelButton = new GTButton(PLATE, PLATE, 154, 48,
            () -> List.of(this.channelTooltip()));

    /** The trough row, below the storage grid. */
    private static final int TROUGH_X = 139;
    private static final int TROUGH_Y = 90;
    private static final int TROUGH_SPRITE_V = 189;

    /** The run switch, whose band sits eleven pixels lower than a single-kind panel's. */
    private static final int RUN_Y = 106;
    private static final int RUN_ICON_V = 200;

    public GTPortGeneralScreen(GTPortMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected ResourceLocation sheet() {
        return TEXTURE;
    }

    @Override
    protected int sheetHeight() {
        return GTPortMenu.GENERAL_IMAGE_HEIGHT;
    }

    @Override
    protected int runY() {
        return RUN_Y;
    }

    @Override
    protected int runSpriteV() {
        return PLATE.v();
    }

    @Override
    protected int runIconV() {
        return RUN_ICON_V;
    }

    @Override
    protected void drawMiddle(GuiGraphics graphics) {
        drawStacks(graphics);
        drawRail(graphics);
        // The trough alone, with no captions under it: this window quotes its own byte and type
        // ceilings beside the name, and repeating the energy figures below would be the only place
        // in the window where two units shared a column.
        drawTrough(graphics, this.leftPos + TROUGH_X, this.topPos + TROUGH_Y, TROUGH_SPRITE_V);
    }

    @Override
    protected void drawSwitches(GuiGraphics graphics, int mouseX, int mouseY) {
        drawModeButton(graphics, mouseX, mouseY);
        drawChannelButton(graphics, mouseX, mouseY);
        drawActiveButton(graphics, mouseX, mouseY);
    }

    @Override
    protected List<GTButton> switches() {
        return List.of(this.modeButton, this.channelButton, this.activeButton);
    }

    /**
     * The trough's own tooltip.
     *
     * <p>The switches explain themselves, so only the non-button part is left for the window to
     * handle here.</p>
     */
    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        renderTroughTooltip(graphics,
                this.leftPos + TROUGH_X, this.topPos + TROUGH_Y, mouseX, mouseY);
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
        if (this.channelButton.hits(mouseX, mouseY, this.leftPos, this.topPos)) {
            toggle(C2SPortTogglePacket.Target.ENERGY_CHANNEL);
            return true;
        }
        if (this.activeButton.hits(mouseX, mouseY, this.leftPos, this.topPos)) {
            toggle(C2SPortTogglePacket.Target.ACTIVE);
            return true;
        }
        return false;
    }

    /**
     * The capacity readout: bytes and types, not a cells count.
     *
     * <p>The assembly's two ceilings are what the player compares against, and both are formatted
     * on the compact ladder so a top-tier figure stays readable. Read from the entity on both
     * sides — the ceilings are fixed by the tier, and the used figures are small enough to arrive
     * with the block update.</p>
     */
    @Override
    protected void drawCapacity(GuiGraphics graphics) {
        String amount = GTEnergyFormat.format(this.menu.generalBytesUsed())
                + " / " + GTEnergyFormat.format(this.menu.generalBytes())
                + " " + Component.translatable("gui.goldentweaks.port.bytes").getString();
        String types = this.menu.generalTypesUsed()
                + " / " + GTEnergyFormat.format(this.menu.generalTypes())
                + " " + Component.translatable("gui.goldentweaks.port.types").getString();

        graphics.pose().pushPose();
        graphics.pose().translate(CAPTION_X, CAPTION_Y, 0.0F);
        graphics.pose().scale(CAPTION_SCALE, CAPTION_SCALE, 1.0F);
        graphics.drawString(this.font, amount, 0, 0, CAPTION_COLOUR, false);
        graphics.drawString(this.font, types, 0, this.font.lineHeight, CAPTION_COLOUR, false);
        graphics.pose().popPose();
    }

    /** The split-mode switch: base plate, then the icon for the mode it is in. */
    private void drawModeButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean held = isPressed(this.leftPos + this.modeButton.x(),
                this.topPos + this.modeButton.y(),
                this.modeButton.width(), this.modeButton.height(), mouseX, mouseY);
        int iconU = this.menu.isSingleCellMode() ? MODE_ICON_U_SINGLE : MODE_ICON_U_MULTI;
        this.modeButton.draw(graphics, TEXTURE, held, iconU, MODE_ICON_V,
                this.leftPos, this.topPos);
    }

    /**
     * The channel-participation switch: plate, then the icon for the state it is in.
     *
     * <p>The plate shows the press, not the setting — the same rule the run switch follows. Both
     * states share one icon position, so the picture says what the switch <em>is</em> and the plate
     * says whether the cursor is on it.</p>
     */
    private void drawChannelButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean held = isPressed(this.leftPos + this.channelButton.x(),
                this.topPos + this.channelButton.y(),
                this.channelButton.width(), this.channelButton.height(), mouseX, mouseY);
        int iconU = this.menu.joinsEnergyChannel()
                ? CHANNEL_ICON_U_JOINS : CHANNEL_ICON_U_ISOLATED;
        this.channelButton.draw(graphics, TEXTURE, held, iconU, CHANNEL_ICON_V,
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

    /**
     * The channel switch's wording.
     *
     * <p>Four strings rather than two: the answer depends on the direction as well, since the part
     * joins as an energy hatch when it feeds the machine and as a dynamo when it receives from it.</p>
     */
    private Component channelTooltip() {
        boolean output = this.menu.mode() == PortMode.OUTPUT;
        boolean joins = this.menu.joinsEnergyChannel();
        String key;
        if (output) {
            key = joins ? "gui.goldentweaks.port.channel.joins.dynamo"
                    : "gui.goldentweaks.port.channel.isolated.dynamo";
        } else {
            key = joins ? "gui.goldentweaks.port.channel.joins.hatch"
                    : "gui.goldentweaks.port.channel.isolated.hatch";
        }
        return Component.translatable(key);
    }
}
