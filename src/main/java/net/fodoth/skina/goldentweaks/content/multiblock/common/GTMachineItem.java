package net.fodoth.skina.goldentweaks.content.multiblock.common;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

/**
 * The kettle's item, carrying the summary the block itself cannot show.
 *
 * <p>Written the way Create writes its machine summaries: the first lines describe what the part is
 * and what it does, and a faint prompt offers the structure breakdown behind <kbd>Shift</kbd> so the
 * tooltip does not turn into a wall of text on a casual hover.</p>
 *
 * <p>Everything in the breakdown is fixed text. The structure is not read at all: it is one authored
 * shape, and a tooltip that quoted the loaded pattern would read differently depending on whether the
 * data files had been parsed yet — and would break outright with no multiblock host installed.</p>
 */
public class GTMachineItem extends BlockItem {

    private static final String KIND_LABEL = "tooltip.goldentweaks.chemical_reactor.kind.label";
    private static final String KIND_VALUE = "tooltip.goldentweaks.chemical_reactor.kind.value";

    private static final String CONTROLS_HEADER = "tooltip.goldentweaks.chemical_reactor.controls";
    private static final String CONTROLS_LIST = "tooltip.goldentweaks.chemical_reactor.controls.list";

    private static final String CIRCUITS_OVERCLOCK = "tooltip.goldentweaks.chemical_reactor.circuit.overclock";
    private static final String CIRCUITS_PARALLEL = "tooltip.goldentweaks.chemical_reactor.circuit.parallel";

    private static final String SHIFT_PROMPT = "tooltip.goldentweaks.chemical_reactor.shift";
    private static final String SHIFT_KEY = "tooltip.goldentweaks.chemical_reactor.shift.key";
    private static final String SHIFT_SUFFIX = "tooltip.goldentweaks.chemical_reactor.shift.suffix";

    private static final String SIZE_LABEL = "tooltip.goldentweaks.chemical_reactor.size.label";
    private static final String SIZE_AXIS = "tooltip.goldentweaks.chemical_reactor.size.axis";
    private static final String STRUCT_HEADER = "tooltip.goldentweaks.chemical_reactor.structure";
    private static final String CONTROLLER_LABEL = "tooltip.goldentweaks.chemical_reactor.controller.label";
    private static final String CONTROLLER_VALUE = "tooltip.goldentweaks.chemical_reactor.controller.value";
    /** {@code ：任意%s} — the casing the machine is built from fills the blank. */
    private static final String FREE_NOTE = "tooltip.goldentweaks.chemical_reactor.structure.any";

    private static final String BOOST = "tooltip.goldentweaks.chemical_reactor.boost";
    private static final String HINT = "tooltip.goldentweaks.chemical_reactor.hint";

    /** The shape's block extent, in blocks: the same figure for every kettle ever built. */
    protected int sizeX() {
        return 3;
    }

    protected int sizeY() {
        return 3;
    }

    protected int sizeZ() {
        return 4;
    }

    /** Where the controller sits, as a player would say it out loud. */
    protected int controllerLayer() {
        return 2;
    }

    /** The shell minimum the shape check enforces, repeated here so the two cannot drift apart. */
    protected static final int MIN_SHELLS = 8;

    /** The casing key this machine's tooltip names, for a subclass building its own part list. */
    protected String casingKey() {
        return this.casingKey;
    }

    /** Indent for the structure block, so the list reads as belonging to the header above it. */
    private static final String INDENT = "    ";

    /**
     * One entry of the structure list.
     *
     * <p>A minimum of zero means "may be absent", which is why those lines read {@code 0+}. The
     * shell line is the only one with a real floor.</p>
     *
     * <p>Public because a machine in another package builds its own list: this type is part of the
     * subclassing contract, not an implementation detail.</p>
     */
    public record Part(int minimum, String nameKey) {
    }

    /** The shell key this item's tooltip names; set per machine through the constructor. */
    private final String casingKey;

    private static final String DEFAULT_CASING = "block.goldentweaks.shell_pe";

    /**
     * The parts a machine's Shift list names, in reading order.
     *
     * <p>Overridden per machine: the vacuum cooler accepts no chemical hopper and no essentia tank,
     * so listing them would describe a structure that cannot exist. The base list is what the two
     * kettles share.</p>
     */
    protected List<Part> parts() {
        return List.of(
            // The block's own name: it says exactly what the structure line needs, so there is no
            // second spelling of "PE casing" to keep in step.
            new Part(MIN_SHELLS, casingKey),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.input_bus"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.fluid_input"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.chemical_input"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.essentia_input"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.output_bus"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.fluid_output"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.chemical_output"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.essentia_output"),
            // The assemblies sit above the energy hatch, and an assembly already carries the item,
            // fluid and energy shares a machine needs, so they are alternatives to that hatch
            // rather than an addition to it.
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.general_input"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.general_output"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.energy_input"));
    }

    /** The kind name shown on the first line. */
    protected String kindKey() {
        return KIND_VALUE;
    }

    /** The circuit line's first entry: the overclock part this machine ships. */
    protected String overclockCircuitKey() {
        return CIRCUITS_OVERCLOCK;
    }

    /** The circuit line's second entry: the parallel part this machine ships. */
    protected String parallelCircuitKey() {
        return CIRCUITS_PARALLEL;
    }

    public GTMachineItem(Block block, Item.Properties properties) {
        this(block, properties, DEFAULT_CASING);
    }

    /** The same item, naming the casing this particular machine is built from. */
    public GTMachineItem(Block block, Item.Properties properties, String casingKey) {
        super(block, properties);
        this.casingKey = casingKey;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        tooltip.add(row(KIND_LABEL, ChatFormatting.WHITE, kindKey(), ChatFormatting.YELLOW));

        tooltip.add(Component.translatable(CONTROLS_HEADER).withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable(CONTROLS_LIST).withStyle(ChatFormatting.YELLOW));

        // Two circuits on one line, each in its own colour: the overclock circuit, then the
        // parallel one. Both names come from the machine, because the advanced kettle ships a
        // different parallel part than the basic one.
        tooltip.add(Component.translatable(overclockCircuitKey()).withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" "))
                .append(Component.translatable(parallelCircuitKey()).withStyle(ChatFormatting.GREEN)));

        if (shiftDown()) {
            addStructure(tooltip);
        } else {
            tooltip.add(Component.translatable(SHIFT_PROMPT).withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable(SHIFT_KEY).withStyle(ChatFormatting.WHITE))
                    .append(Component.translatable(SHIFT_SUFFIX).withStyle(ChatFormatting.DARK_GRAY)));
        }
    }

    /**
     * Whether the summary is being asked for.
     *
     * <p>{@code Screen} does not exist on a dedicated server, and a tooltip is a client-side
     * concept anyway, so the check is guarded rather than assumed.</p>
     */
    private static boolean shiftDown() {
        return FMLEnvironment.dist.isClient()
                && net.minecraft.client.gui.screens.Screen.hasShiftDown();
    }

    /** The breakdown behind the Shift prompt. */
    private void addStructure(List<Component> tooltip) {
        tooltip.add(sizeLine());
        tooltip.add(Component.translatable(STRUCT_HEADER).withStyle(ChatFormatting.WHITE));
        tooltip.add(controllerLine());

        for (Part part : parts()) {
            tooltip.add(partLine(part, this.casingKey));
        }
        tooltip.add(Component.translatable(BOOST).withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable(HINT).withStyle(ChatFormatting.GREEN));
    }

    /**
     * {@code 大小：3×3×4（L×W×H）}.
     *
     * <p>The numerals are the block extent; the letters inside the parentheses name the axes the
     * structure file is written in — layers, width, depth — because those are the three figures a
     * player comparing the file against the machine will be looking for.</p>
     */
    private Component sizeLine() {
        ChatFormatting value = ChatFormatting.GOLD;
        ChatFormatting dim = ChatFormatting.DARK_GRAY;

        MutableComponent line = Component.translatable(SIZE_LABEL).withStyle(ChatFormatting.WHITE)
                .append(Component.literal(String.valueOf(sizeX())).withStyle(value))
                .append(Component.translatable(SIZE_AXIS).withStyle(dim))
                .append(Component.literal(String.valueOf(sizeY())).withStyle(value))
                .append(Component.translatable(SIZE_AXIS).withStyle(dim))
                .append(Component.literal(String.valueOf(sizeZ())).withStyle(value))
                .append(Component.literal(" (").withStyle(dim));
        // The axes are named, not numbered: L, W and H are the file's own vocabulary.
        line.append(Component.literal("L").withStyle(value))
                .append(Component.translatable(SIZE_AXIS).withStyle(dim))
                .append(Component.literal("W").withStyle(value))
                .append(Component.translatable(SIZE_AXIS).withStyle(dim))
                .append(Component.literal("H").withStyle(value))
                .append(Component.literal(")").withStyle(dim));
        return line;
    }

    /** {@code 控制器：第二层，正面中央}, indented with the rest of the structure block. */
    private Component controllerLine() {
        return Component.literal(INDENT)
                .append(Component.translatable(CONTROLLER_LABEL).withStyle(ChatFormatting.WHITE))
                .append(Component.translatable(CONTROLLER_VALUE,
                                String.valueOf(controllerLayer()))
                        .withStyle(ChatFormatting.DARK_GRAY));
    }

    /**
     * One line of the structure list.
     *
     * <p>The optional parts carry an "or any casing" note, and that note names this machine's own
     * casing — the same key the casing line above it uses. Writing the name out in the language
     * file instead is what made an advanced machine's list end with a polyethylene note.</p>
     *
     * @param casingKey the translation key of the casing this machine is built from
     */
    private static Component partLine(Part part, String casingKey) {
        Component line = Component.literal(INDENT + part.minimum() + "+").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" "))
                .append(Component.translatable(part.nameKey()).withStyle(ChatFormatting.WHITE));
        if (part.minimum() == 0) {
            // Only the ports are interchangeable with a casing; the casing line has no note.
            line = line.copy()
                    .append(Component.translatable(FREE_NOTE,
                                    Component.translatable(casingKey))
                            .withStyle(ChatFormatting.DARK_GRAY));
        }
        return line;
    }

    /** A line made of a label and a value, each in its own colour. */
    private static Component row(String labelKey, ChatFormatting labelStyle,
                                 String valueKey, ChatFormatting valueStyle) {
        return Component.translatable(labelKey).withStyle(labelStyle)
                .append(Component.translatable(valueKey).withStyle(valueStyle));
    }
}
