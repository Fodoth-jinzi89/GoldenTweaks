"""Restore the one fully-qualified name that has to stay, and import what is missing.

`net.minecraft.client.gui.screens.Screen` is deliberately spelled in full: the class only exists
on a client, so importing it would make the dedicated server resolve it while loading an item
class it does have to load. `FMLEnvironment`, by contrast, is safe to import and now is.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTChemicalReactorItem.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''        return FMLEnvironment.dist.isClient()
                && Screen.hasShiftDown();''',
'''        return FMLEnvironment.dist.isClient()
                && net.minecraft.client.gui.screens.Screen.hasShiftDown();''', 1)

if "import net.neoforged.fml.loading.FMLEnvironment;" not in s:
    lines = s.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import "))
    lines.insert(last + 1, "import net.neoforged.fml.loading.FMLEnvironment;")
    s = "\n".join(lines)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("GTChemicalReactorItem restored")
