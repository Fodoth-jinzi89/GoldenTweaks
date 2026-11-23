"""Finish the FQCN cleanup across the port classes.

The remaining spots are a mix of long-standing code and this session's additions; all of them
get imports, since the rule does not distinguish.
"""
import io

def add_import(s, line):
    if line in s:
        return s
    lines = s.split("\n")
    existing = [i for i, l in enumerate(lines) if l.startswith("import ")]
    if not existing:
        lines.insert(0, line)
        return "\n".join(lines)
    lines.insert(existing[-1] + 1, line)
    return "\n".join(lines)


def patch(path, replacements, imports):
    s = io.open(path, encoding="utf-8").read()
    for old, new in replacements:
        s = s.replace(old, new)
    for line in imports:
        s = add_import(s, line)
    io.open(path, "w", encoding="utf-8", newline="\n").write(s)
    print(path.split("/")[-1], "done")


# ---------------- GTPortCapabilities ----------------
patch(
    r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortCapabilities.java",
    [
        ("private static void register(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {",
         "private static void register(RegisterCapabilitiesEvent event) {"),
        ("mekanism.common.capabilities.Capabilities.CHEMICAL.block(),",
         "CHEMICAL.block(),"),
        ("? (mekanism.api.chemical.IChemicalHandler) port.chemicalStorage() : null);",
         "? (IChemicalHandler) port.chemicalStorage() : null);"),
        ("thaumcraft.api.aspects.EssentiaCapabilities.TRANSPORT,",
         "EssentiaCapabilities.TRANSPORT,"),
        ("? (thaumcraft.api.aspects.IEssentiaTransport) port.essentiaTransportObject()",
         "? (IEssentiaTransport) port.essentiaTransportObject()"),
    ],
    [
        "import mekanism.api.chemical.IChemicalHandler;",
        "import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;",
        "import thaumcraft.api.aspects.EssentiaCapabilities;",
        "import thaumcraft.api.aspects.IEssentiaTransport;",
        "import static mekanism.common.capabilities.Capabilities.CHEMICAL;",
    ],
)

# ---------------- GTPortBlockEntity ----------------
patch(
    r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java",
    [
        ("net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, target, front.getOpposite());",
         "Capabilities.ItemHandler.BLOCK, target, front.getOpposite());"),
        ("net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, target,",
         "Capabilities.FluidHandler.BLOCK, target,"),
        ("var capability = net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK;",
         "var capability = Capabilities.EnergyStorage.BLOCK;"),
        ("net.neoforged.neoforge.energy.IEnergyStorage theirs =",
         "IEnergyStorage theirs ="),
    ],
    [
        "import net.neoforged.neoforge.capabilities.Capabilities;",
        "import net.neoforged.neoforge.energy.IEnergyStorage;",
    ],
)

# ---------------- GTChemicalReactorItem ----------------
patch(
    r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTChemicalReactorItem.java",
    [
        ("return net.neoforged.fml.loading.FMLEnvironment.dist.isClient()",
         "return FMLEnvironment.dist.isClient()"),
        ("&& net.minecraft.client.gui.screens.Screen.hasShiftDown();",
         "&& Screen.hasShiftDown();"),
    ],
    [
        "import net.neoforged.fml.loading.FMLEnvironment;",
    ],
)
