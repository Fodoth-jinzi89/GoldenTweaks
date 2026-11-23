"""The port's casing becomes a block state value.

This is the change that makes CTM work. Continuity asks for a cell's sprite with
`SpriteCalculator.getSprite(state, direction)` — no model data — so it can only see what the state
resolves to. A casing recorded on the block entity is invisible to that check, and the plate then
answers "not connected" to the wall it is set into.

A bounded integer names it. The state's value set is closed, which is why `GTMultiblockBlocks.CASINGS`
exists: it is the ordered list the index refers to, and it caps how many casings can be added before
the blockstates have to be regenerated.

The property defaults to 0, the first casing, so a part that no machine has claimed needs no state
change.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java"
s = io.open(p, encoding="utf-8").read()

old = '''    public static final ModelProperty<BlockState> CASING_MODEL = new ModelProperty<>();'''
new = '''    public static final ModelProperty<BlockState> CASING_MODEL = new ModelProperty<>();

    /**
     * Which casing this port's plate is drawn with, as an index into
     * {@link GTMultiblockBlocks#CASINGS}.
     *
     * <p>In the state rather than on the block entity because CTM has to see it: Continuity resolves
     * a neighbouring cell's sprite from the state alone, so a plate that only differs at render time
     * never connects to the wall beside it.</p>
     *
     * <p>The count is fixed by the maximum of this property — a state's value set cannot be open —
     * so adding a casing beyond that needs the port blockstates regenerated.</p>
     */
    public static final IntegerProperty CASING = IntegerProperty.create("casing",
            0, GTMultiblockBlocks.CASINGS.size() - 1);'''
assert old in s
s = s.replace(old, new, 1)

old_default = '''        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));'''
new_default = '''        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CASING, 0));'''
assert old_default in s
s = s.replace(old_default, new_default, 1)

s = s.replace("        builder.add(FACING);", "        builder.add(FACING, CASING);", 1)

# the accessor: the casing block this state names
anchor = '''    /** The voltage tier this block belongs to. */
    public GTPortTier portTier() {
        return this.tier;
    }'''
assert anchor in s

accessor = anchor + '''

    /**
     * The casing block a state's plate is drawn with.
     *
     * <p>Resolved from the state's index, so this is the same answer CTM gets when it asks for this
     * cell's sprite.</p>
     */
    public static Block casingOf(BlockState state) {
        if (!state.hasProperty(CASING)) {
            return GTMultiblockBlocks.casingAt(0);
        }
        return GTMultiblockBlocks.casingAt(state.getValue(CASING));
    }'''
s = s.replace(anchor, accessor, 1)

if "import net.minecraft.world.level.block.state.properties.IntegerProperty;" not in s:
    s = s.replace("import net.minecraft.world.level.block.state.properties.DirectionProperty;",
                  "import net.minecraft.world.level.block.state.properties.DirectionProperty;\n"
                  "import net.minecraft.world.level.block.state.properties.IntegerProperty;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("casing is a block state value")
