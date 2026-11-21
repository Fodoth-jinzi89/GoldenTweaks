package net.fodoth.skina.goldentweaks.content.multiblock;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.weibai.avaritia_more_items.common.capability.AMICapabilities;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.PortMode;

/**
 * Exposes this mod's port storage to both AMI and the rest of the world.
 *
 * <p>Two registrations per kind are needed, and they are not redundant. AMI's multiblock machinery
 * never looks at the standard NeoForge capabilities: {@code MultiblockPortType.capability(level,
 * pos)} resolves through AMI's own {@code MultiTypeCapability} keys, so a port that only registered
 * the standard keys would look empty to the machine it belongs to. The standard keys are registered
 * as well so ordinary pipes, hoppers and cables can reach the port like any other inventory.</p>
 *
 * <p>Every port answers on its front face only. A port is embedded in the shell, so the other five
 * sides are inside the wall; offering a capability there would let a pipe plugged into the back of
 * the machine feed it.</p>
 */
public final class GTPortCapabilities {

    private GTPortCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        // AMI's own keys: this is what the multiblock matcher and recipe code query.
        event.registerBlockEntity(AMICapabilities.ITEM.block(),
                GTMultiblockBlocks.PORT_ENTITY.get(), (port, side) ->
                        port.kind() == GTPortKind.ITEM
                                ? port.itemHandler() : null);
        event.registerBlockEntity(AMICapabilities.FLUID.block(),
                GTMultiblockBlocks.PORT_ENTITY.get(), (port, side) ->
                        port.kind() == GTPortKind.FLUID
                                ? port.fluidHandler() : null);
        event.registerBlockEntity(AMICapabilities.ENERGY.block(),
                GTMultiblockBlocks.PORT_ENTITY.get(), (port, side) ->
                        port.kind() == GTPortKind.ENERGY
                                ? port.energyStorage() : null);

        // Standard keys, so other mods' transport can use the ports too.
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                GTMultiblockBlocks.PORT_ENTITY.get(), (port, side) ->
                        port.kind() == GTPortKind.ITEM
                                ? port.itemHandler() : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                GTMultiblockBlocks.PORT_ENTITY.get(), (port, side) ->
                        port.kind() == GTPortKind.FLUID
                                ? port.fluidHandler() : null);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                GTMultiblockBlocks.PORT_ENTITY.get(), (port, side) ->
                        port.kind() == GTPortKind.ENERGY
                                ? port.energyStorage() : null);

        GoldenTweaks.LOGGER.debug("[GT] registered port capabilities for {} kinds",
                GTPortKind.values().length);
    }
}
