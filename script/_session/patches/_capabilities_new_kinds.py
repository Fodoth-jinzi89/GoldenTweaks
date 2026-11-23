"""Expose the two new storages to the world.

Both are registered only when their mod is loaded: the capability types themselves come from
those mods, so naming them unconditionally would fail at class load.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortCapabilities.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''        GoldenTweaks.LOGGER.debug("[GT] registered port capabilities for {} kinds",
                GTPortKind.values().length);''',
'''        // Chemicals and essentia are answered through their own mods' capabilities, and only when
        // those mods are present.
        if (ModList.get().isLoaded("mekanism")) {
            MekanismChemicals.register(event);
        }
        if (ModList.get().isLoaded("thaumcraft")) {
            ThaumcraftEssentia.register(event);
        }

        GoldenTweaks.LOGGER.debug("[GT] registered port capabilities for {} kinds",
                GTPortKind.values().length);''', 1)

s = s.replace('''import net.neoforged.neoforge.capabilities.Capabilities;''',
'''import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;''', 1)

s = s.rstrip()
# Drop the trailing brace so the nested classes can be appended.
assert s.endswith("}")
s = s[:-1].rstrip() + '''

    /**
     * The chemical capability, resolved in its own class.
     *
     * <p>{@code Capabilities.CHEMICAL} lives in Mekanism, so the reference has to sit behind the
     * mod check above. A nested class is resolved lazily, which is what makes that possible
     * without a reflective lookup.</p>
     */
    private static final class MekanismChemicals {
        private static void register(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(
                    mekanism.common.capabilities.Capabilities.CHEMICAL.getBlockCapability(),
                    GTMultiblockBlocks.PORT_ENTITY.get(),
                    (port, side) -> port.kind() == GTPortKind.CHEMICAL
                            ? (mekanism.api.chemical.IChemicalHandler) port.chemicalStorage() : null);
        }
    }

    /** The essentia capability, resolved in its own class for the same reason. */
    private static final class ThaumcraftEssentia {
        private static void register(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(
                    thaumcraft.api.aspects.EssentiaCapabilities.TRANSPORT,
                    GTMultiblockBlocks.PORT_ENTITY.get(),
                    (port, side) -> port.kind() == GTPortKind.ESSENTIA
                            ? port.essentiaTransport() : null);
        }
    }
}
'''

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("capabilities registered")
