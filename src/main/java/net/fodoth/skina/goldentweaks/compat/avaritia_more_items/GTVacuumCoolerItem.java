package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import net.fodoth.skina.goldentweaks.content.multiblock.GTChemicalReactorItem;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * The vacuum cooler's item, describing the cooler's own structure rather than the kettle's.
 *
 * <p>Two things differ from the base tooltip. The kind line names this machine instead of the
 * kettle, and the Shift breakdown omits the chemical hopper and the essentia tank — the cooler's
 * casing tag deliberately excludes both, so listing them would describe a shape that cannot be
 * built. The size and the controller's layer are overridden for the same reason: this structure is
 * three layers deep, not four.</p>
 */
public class GTVacuumCoolerItem extends GTChemicalReactorItem {

    private static final String KIND = "tooltip.goldentweaks.vacuum_cooler.kind.value";

    /** The cooler's own structure file, which is three by three by three. */
    private static final int SIZE = 3;

    private static final String PART_INPUT_BUS = "tooltip.goldentweaks.chemical_reactor.part.input_bus";
    private static final String PART_FLUID_INPUT = "tooltip.goldentweaks.chemical_reactor.part.fluid_input";
    private static final String PART_OUTPUT_BUS = "tooltip.goldentweaks.chemical_reactor.part.output_bus";
    private static final String PART_FLUID_OUTPUT = "tooltip.goldentweaks.chemical_reactor.part.fluid_output";
    private static final String PART_ENERGY_INPUT = "tooltip.goldentweaks.chemical_reactor.part.energy_input";

    public GTVacuumCoolerItem(Block block, Properties properties, String casingKey) {
        super(block, properties, casingKey);
    }

    @Override
    protected String kindKey() {
        return KIND;
    }

    @Override
    protected int sizeX() {
        return SIZE;
    }

    @Override
    protected int sizeY() {
        return SIZE;
    }

    @Override
    protected int sizeZ() {
        return SIZE;
    }

    /** The controller sits in the middle layer of three, not the second of four. */
    @Override
    protected int controllerLayer() {
        return 2;
    }

    /**
     * Only the parts this machine actually accepts.
     *
     * <p>No chemical hopper and no essentia tank: the cooler's casing tag leaves both out, so they
     * are not wall material for this shape at all. The shell line still comes first, and the same
     * minimum applies — it is the same 3x3 wall pattern.</p>
     */
    @Override
    protected List<Part> parts() {
        return List.of(
                new Part(MIN_SHELLS, casingKey()),
                new Part(0, PART_INPUT_BUS),
                new Part(0, PART_FLUID_INPUT),
                new Part(0, PART_OUTPUT_BUS),
                new Part(0, PART_FLUID_OUTPUT),
                new Part(0, PART_ENERGY_INPUT));
    }
}
