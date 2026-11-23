"""The casing moves from the block entity onto the block state.

CTM decides whether two cells connect by comparing their sprites, and it asks for those sprites with
`SpriteCalculator.getSprite(state, direction)` — the three-argument `getQuads`, which carries no model
data. Whatever the model decides at runtime is invisible to that check, so a plate re-textured only in
`getQuads` never connects to the wall beside it.

The casing therefore has to be part of the state. A bounded integer is the only kind of state
property that can name it, so the count of casings is capped; `CASINGS` is the ordered list that
defines the indices, and adding one means registering it there and regenerating the port blockstates
(a script does that). No model or texture file is duplicated: the models are cloned from the same
baked model at load time.
"""
import io

# ---- 1. the casing list ------------------------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s = io.open(p, encoding="utf-8").read()

anchor = '''    public static final DeferredItem<BlockItem> SHELL_PE_ITEM ='''
assert anchor in s

addition = '''    /**
     * Every casing a port can show, in the order the block state's {@code casing} index refers to.
     *
     * <p>Index 0 is the default, so a port that has not been claimed by a machine needs no state
     * change. Adding a casing means appending here and regenerating the port blockstates — the
     * index has to be enumerable because CTM matches sprites by block state.</p>
     */
    public static final java.util.List<DeferredBlock<GTShellBlock>> CASINGS = java.util.List.of(
            SHELL_PE, SHELL_PP);

    /** The index of a casing in {@link #CASINGS}, or 0 when it is not one of them. */
    public static int casingIndex(Block casing) {
        for (int index = 0; index < CASINGS.size(); index++) {
            if (CASINGS.get(index).get() == casing) {
                return index;
            }
        }
        return 0;
    }

    /** The casing at an index, clamped into the list. */
    public static Block casingAt(int index) {
        int clamped = Math.clamp(index, 0, CASINGS.size() - 1);
        return CASINGS.get(clamped).get();
    }

    public static final DeferredItem<BlockItem> SHELL_PE_ITEM ='''

s = s.replace(anchor, addition, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("casing list added")
