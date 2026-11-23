package net.fodoth.skina.goldentweaks.content.multiblock;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.weibai.avaritia_more_items.common.capability.AMICapabilities;
import mekanism.api.chemical.IChemicalHandler;
import thaumcraft.api.aspects.EssentiaCapabilities;
import thaumcraft.api.aspects.IEssentiaTransport;
import static mekanism.common.capabilities.Capabilities.CHEMICAL;

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
        assert GTMultiblockBlocks.PORT_ENTITY != null;
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

        // Chemicals and essentia are answered through their own mods' capabilities, and only when
        // those mods are present.
        if (ModList.get().isLoaded("mekanism")) {
            MekanismChemicals.register(event);
        }
        if (ModList.get().isLoaded("thaumcraft")) {
            ThaumcraftEssentia.register(event);
        }

        GoldenTweaks.LOGGER.debug("[GT] registered port capabilities for {} kinds",
                GTPortKind.values().length);
    }

    /**
     * The chemical capability, resolved in its own class.
     *
     * <p>{@code Capabilities.CHEMICAL} lives in Mekanism, so the reference has to sit behind the
     * mod check above. A nested class is resolved lazily, which is what makes that possible
     * without a reflective lookup.</p>
     */
    private static final class MekanismChemicals {
        private static void register(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(
                    CHEMICAL.block(),
                    GTMultiblockBlocks.PORT_ENTITY.get(),
                    (port, side) -> port.kind() == GTPortKind.CHEMICAL
                            ? (IChemicalHandler) port.chemicalStorage() : null);
        }
    }

    /** The essentia capability, resolved in its own class for the same reason. */
    private static final class ThaumcraftEssentia {
        private static void register(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(
                    EssentiaCapabilities.TRANSPORT,
                    GTMultiblockBlocks.PORT_ENTITY.get(),
                    (port, side) -> port.kind() == GTPortKind.ESSENTIA
                            ? (IEssentiaTransport) port.essentiaTransportObject()
                            : null);
        }
    }
}
