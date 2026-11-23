"""The port's casing goes back into the block state, and this time it stays.

Create's connected-texture machinery decides connectivity per face, at render time, from the world —
which is what the model-data approaches could never do. But its `getShift` hook, the one that picks
the sheet for a face, takes only the state. So the casing does have to be in the state for the plate
to be drawn from the right sheet.

What changes is what the state costs. Before, a casing meant a model file per port per casing. Now it
means one index, one CT sheet per casing, and no model files at all: `CTModel` wraps the single
authored port model and shifts the plate's UVs into the right sheet at draw time.

`CASINGS` is that index list, and it is the only thing to extend when a casing is added — along with
its 47-tile sheet.
"""
import io

# ---- 1. the casing list ------------------------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s = io.open(p, encoding="utf-8").read()

if "CASINGS" not in s:
    anchor = '''    public static final DeferredItem<BlockItem> SHELL_PE_ITEM ='''
    assert anchor in s

    addition = '''    /**
     * Every casing a port can show, in the order the block state's {@code casing} index refers to.
     *
     * <p>Index 0 is the default, so a port no machine has claimed needs no state change. The list is
     * ordered rather than a set because the state stores an index, and a state property can only hold
     * a bounded value.</p>
     *
     * <p>Adding a casing means appending here and giving it a connected-texture sheet; the port
     * models are not touched.</p>
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
        return CASINGS.get(Math.clamp(index, 0, CASINGS.size() - 1)).get();
    }

    public static final DeferredItem<BlockItem> SHELL_PE_ITEM ='''
    s = s.replace(anchor, addition, 1)
    print("casing list added")

if "import net.minecraft.world.level.block.Block;" not in s:
    s = s.replace("import net.minecraft.world.level.block.SoundType;",
                  "import net.minecraft.world.level.block.Block;\nimport net.minecraft.world.level.block.SoundType;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)

# ---- 2. the state property ---------------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java"
s2 = io.open(p2, encoding="utf-8").read()

if "CASING" not in s2:
    anchor2 = '''    public static final DirectionProperty FACING = BlockStateProperties.FACING;'''
    assert anchor2 in s2

    prop = '''    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /**
     * Which casing this port's plate is drawn with, as an index into
     * {@link GTMultiblockBlocks#CASINGS}.
     *
     * <p>In the state because the connected-texture hook that picks a face's sheet is given the state
     * and nothing else. The bound is the list's size, which is why adding a casing is an edit to that
     * list rather than to anything here.</p>
     */
    public static final IntegerProperty CASING = IntegerProperty.create("casing",
            0, GTMultiblockBlocks.CASINGS.size() - 1);'''
    s2 = s2.replace(anchor2, prop, 1)

    s2 = s2.replace('''        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));''',
'''        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CASING, 0));''', 1)

    s2 = s2.replace("        builder.add(FACING);", "        builder.add(FACING, CASING);", 1)

    anchor3 = '''    /** The voltage tier this block belongs to. */
    public GTPortTier portTier() {
        return this.tier;
    }'''
    s2 = s2.replace(anchor3, anchor3 + '''

    /** The casing block a state's plate is drawn with. */
    public static Block casingOf(BlockState state) {
        return state.hasProperty(CASING)
                ? GTMultiblockBlocks.casingAt(state.getValue(CASING))
                : GTMultiblockBlocks.casingAt(0);
    }''', 1)

    if "import net.minecraft.world.level.block.state.properties.IntegerProperty;" not in s2:
        s2 = s2.replace("import net.minecraft.world.level.block.state.properties.DirectionProperty;",
                        "import net.minecraft.world.level.block.state.properties.DirectionProperty;\n"
                        "import net.minecraft.world.level.block.state.properties.IntegerProperty;", 1)

    io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
    print("casing state property added")
