"""Remove the casing list left over from the per-casing model attempt.

The list existed so a block state could name a casing by index, which was for registering one model
per casing. Create's connected-texture model answers the same question per face at render time, so the
enumeration is not needed and the list is dead code.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s = io.open(p, encoding="utf-8").read()

start = s.find("    /**\n     * Every casing a port can show, in the order the block state's")
if start >= 0:
    end = s.index("    public static final DeferredItem<BlockItem> SHELL_PE_ITEM =")
    s = s[:start] + s[end:]
    io.open(p, "w", encoding="utf-8", newline="\n").write(s)
    print("casing list removed")
else:
    print("casing list already gone")
