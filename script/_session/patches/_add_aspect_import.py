"""Add the Aspect import the essentia draw now needs."""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java"
s = io.open(p, encoding="utf-8").read()
if "import thaumcraft.api.aspects.Aspect;" not in s:
    lines = s.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import "))
    lines.insert(last + 1, "import thaumcraft.api.aspects.Aspect;")
    s = "\n".join(lines)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("Aspect imported")
