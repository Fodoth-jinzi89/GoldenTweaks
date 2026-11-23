"""Remove the temporary quads diagnostic.

The cause is known now: the wrapper was never applied (path test), and the hand-written BakedModel
implementation dropped the item transforms. Both are fixed.
"""
import io, re

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s = io.open(p, encoding="utf-8").read()

s = re.sub(
    r"\n        if \(side == Direction\.NORTH\) \{\n"
    r"            net\.fodoth\.skina\.goldentweaks\.GoldenTweaks\.LOGGER\.info\(\n"
    r"                    \"\[GT/diag\] port quads: state=\{\} casing=\{\} quads=\{\} total=\{\}\",\n"
    r"                    state == null \? \"null\" : state\.getBlock\(\), casing,\n"
    r"                    quads\.size\(\), quads\.size\(\)\);\n"
    r"        \}\n",
    "", s)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("diagnostic removed")
