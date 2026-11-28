package net.fodoth.skina.goldentweaks.content.multiblock.advanced_chemical_plant;

import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineItem;
import net.minecraft.world.level.block.Block;

/**
 * The advanced kettle's item, naming the parts that machine actually ships.
 *
 * <p>The basic tooltip lists a 4/2 overclock circuit and a one-parallel circuit. The advanced
 * kettle swaps the single-parallel part for the fourfold one, so its line has to say so — a shared
 * string would have described the advanced machine with the basic machine's hardware.</p>
 */
public class GTAdvancedChemicalReactorItem extends GTMachineItem {

    /** The fourfold parallel circuit's name, as it appears on the part itself. */
    private static final String CIRCUIT_PARALLEL_4X = "tooltip.goldentweaks.chemical_reactor.circuit.parallel_4x";

    public GTAdvancedChemicalReactorItem(Block block, Properties properties, String casingKey) {
        super(block, properties, casingKey);
    }

    @Override
    protected String parallelCircuitKey() {
        return CIRCUIT_PARALLEL_4X;
    }
}
