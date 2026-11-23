package net.fodoth.skina.goldentweaks.content.multiblock;

import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTEnergyFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.PortMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * One of the machine's own ports, as an item.
 *
 * <p>Two things the plain {@link BlockItem} cannot do. Its name has to carry the voltage tier in the
 * tier's own colour, so a row of parts in a chest reads as a ladder rather than a wall of
 * "ULV/LV/MV" text; and the part has to state what it holds and what it can take, which only the
 * tier knows.</p>
 *
 * <p>Carrying kind, mode and tier on the item is what makes this possible: the block keeps them in
 * fields for the same reason, and an item has no block state to read them back from.</p>
 */
public class GTPortItem extends BlockItem {

    private static final String CONTROLS_HEADER = "tooltip.goldentweaks.port.controls";
    /** The stacking switch is an item/fluid concept; an energy port has no cells to span. */
    private static final String CONTROLS_SINGLE_CELL = "tooltip.goldentweaks.port.controls.single_cell";
    private static final String CONTROLS_AUTO_IO = "tooltip.goldentweaks.port.controls.auto_io";

    private final GTPortKind kind;
    private final PortMode mode;
    private final GTPortTier tier;

    public GTPortItem(GTPortBlock block, Item.Properties properties) {
        super(block, properties);
        this.kind = block.kind();
        this.mode = block.mode();
        this.tier = block.portTier();
    }

    /**
     * The name, with the voltage tier coloured.
     *
     * <p>Built from two keys rather than one: the full name is "LV输入总线", and colouring only the
     * "LV" of a single translated string is not possible without guessing where the code ends in
     * every language. The tier code is written here, the rest comes from the translation.</p>
     */
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.literal(this.tier.code()).withStyle(Style.EMPTY.withColor(this.tier.colour()))
                .append(Component.translatable(suffixKey()).withStyle(ChatFormatting.WHITE));
    }

    /** The part name without the voltage prefix, e.g. {@code 输入总线}. */
    private String suffixKey() {
        return "tooltip.goldentweaks.port.name." + this.kind.id() + "_"
                + this.mode.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        tooltip.add(capacityLine());
        if (this.kind == GTPortKind.ENERGY) {
            tooltip.add(rateLine());
        } else {
            tooltip.add(cellsLine());
        }

        tooltip.add(Component.translatable(CONTROLS_HEADER).withStyle(ChatFormatting.WHITE));
        Component controls = this.kind == GTPortKind.ENERGY
                // An energy port has no cells for an entry to span, so its window has no stacking
                // switch and listing one here would promise a control that is not there.
                ? Component.translatable(CONTROLS_AUTO_IO).withStyle(ChatFormatting.YELLOW)
                : Component.translatable(CONTROLS_SINGLE_CELL).withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(" "))
                        .append(Component.translatable(CONTROLS_AUTO_IO)
                                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(controls);
    }

    /** {@code 容量：20 kFE}, or {@code 容量：3.05 kmB} for a fluid port. */
    private Component capacityLine() {
        return row("tooltip.goldentweaks.port.capacity", capacityText());
    }

    /** {@code 格数：9}, the port's slot or tank count, the same figure its window reports. */
    private Component cellsLine() {
        return row("tooltip.goldentweaks.port.cells", String.valueOf(cells()));
    }

    /** {@code 最大输入：128FE/t} or {@code 最大输出：64FE/t}, matching the window's caption. */
    private Component rateLine() {
        boolean output = this.mode == PortMode.OUTPUT;
        String label = output
                ? "tooltip.goldentweaks.port.max_output"
                : "tooltip.goldentweaks.port.max_input";
        var rate = output
                ? GTPortEnergyTiers.outputTransferExact(this.tier.level())
                : GTPortEnergyTiers.transferExact(this.tier.level());
        return row(label, GTEnergyFormat.formatWithUnit(rate, "FE/t"));
    }

    /** The port's total capacity, in the unit its window uses. */
    private String capacityText() {
        return switch (this.kind) {
            case ITEM -> GTEnergyFormat.formatWithUnit(
                    (long) this.tier.itemSlots() * this.tier.slotLimit(), "");
            case FLUID -> GTEnergyFormat.formatWithUnit(
                    (long) this.tier.fluidTanks() * this.tier.tankCapacity(), "mB");
            case ENERGY -> GTEnergyFormat.formatWithUnit(
                    this.mode == PortMode.OUTPUT
                            ? GTPortEnergyTiers.outputCapacityExact(this.tier.level())
                            : GTPortEnergyTiers.capacityExact(this.tier.level()), "FE");
            case CHEMICAL -> GTEnergyFormat.formatWithUnit(
                    (long) this.tier.chemicalTanks() * this.tier.chemicalTankCapacity(), "");
            case ESSENTIA -> GTEnergyFormat.formatWithUnit(
                    (long) this.tier.essentiaTanks() * this.tier.essentiaTankCapacity(), "");
        };
    }

    /** How many cells the port provides: slots for items, tanks for fluids. */
    private int cells() {
        return switch (this.kind) {
            case ITEM -> this.tier.itemSlots();
            case FLUID -> this.tier.fluidTanks();
            case ENERGY -> 0;
            case CHEMICAL -> this.tier.chemicalTanks();
            case ESSENTIA -> this.tier.essentiaTanks();
        };
    }

    /** A white label and a yellow value. */
    private static Component row(String labelKey, String value) {
        return Component.translatable(labelKey).withStyle(ChatFormatting.WHITE)
                .append(Component.literal(value).withStyle(ChatFormatting.YELLOW));
    }
}
