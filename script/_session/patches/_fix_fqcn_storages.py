"""Replace inline fully-qualified names with imports.

The rule is that a code body never spells out a package. Imports do not weaken the lazy-loading
trick used below: an import is a compile-time alias, and the JVM still resolves a class only when
a method that mentions it first runs — which is why the mod-dependent work stays inside a nested
class regardless of how the name is written.
"""
import io
import re

# ---------------- GTPortStorages: import the two compat classes ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortStorages.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;''',
'''import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.fodoth.skina.goldentweaks.compat.mekanism.GTPortChemicalStorage;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTPortEssentiaStorage;''', 1)

s = s.replace('net.fodoth.skina.goldentweaks.compat.mekanism.GTPortChemicalStorage storage',
              'GTPortChemicalStorage storage')
s = s.replace('net.fodoth.skina.goldentweaks.compat.thaumcraft.GTPortEssentiaStorage storage',
              'GTPortEssentiaStorage storage')
s = s.replace('new net.fodoth.skina.goldentweaks.compat.mekanism.GTPortChemicalStorage(',
              'new GTPortChemicalStorage(')
s = s.replace('new net.fodoth.skina.goldentweaks.compat.thaumcraft.GTPortEssentiaStorage(',
              'new GTPortEssentiaStorage(')
s = s.replace('((net.fodoth.skina.goldentweaks.compat.mekanism.GTPortChemicalStorage) storage)',
              '((GTPortChemicalStorage) storage)')
s = s.replace('((net.fodoth.skina.goldentweaks.compat.thaumcraft.GTPortEssentiaStorage) storage)',
              '((GTPortEssentiaStorage) storage)')

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("GTPortStorages:", "clean" if "net.fodoth.skina.goldentweaks.compat" not in
      s.replace("import net.fodoth.skina.goldentweaks.compat", "") else "STILL DIRTY")
