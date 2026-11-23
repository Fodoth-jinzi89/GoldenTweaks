"""The port's shell plate follows the machine it is built into.

The base model draws every port with one polyethylene plate, so a hatch set into the advanced
kettle kept the basic machine's casing texture — visibly wrong next to the polypropylene wall
around it.

The plate is now tinted, and the tint comes from a block-state flag the machine writes when it
assembles. A block state rather than a block entity field because the model is chosen from the
state: a field would need a block entity renderer to redraw the plate, and the tint would still be
baked at mesh-build time.

Only the flag is new; the plate texture stays one grey image, so no second model is needed.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java"
s = io.open(p, encoding="utf-8").read()

# ---- the property ----------------------------------------------------------------------------
old_facing = '''    public static final DirectionProperty FACING = BlockStateProperties.FACING;'''
new_facing = '''    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /**
     * Which machine's casing this port is set into.
     *
     * <p>The base model draws one grey plate and this picks its colour, so a hatch in the advanced
     * kettle matches the polypropylene wall around it instead of showing the basic machine's
     * polyethylene. Written by the machine when it assembles; a loose part keeps the default.</p>
     */
    public static final BooleanProperty ADVANCED_CASING = BooleanProperty.create("advanced_casing");'''
assert old_facing in s
s = s.replace(old_facing, new_facing, 1)

# ---- defaults --------------------------------------------------------------------------------
s = s.replace('''        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));''',
'''        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ADVANCED_CASING, false));''', 1)

# ---- the state definition --------------------------------------------------------------------
s = s.replace('''        builder.add(FACING);''', '''        builder.add(FACING, ADVANCED_CASING);''', 1)

# ---- the accessors ---------------------------------------------------------------------------
s = s.replace('''    /** The voltage tier this block belongs to. */
    public GTPortTier portTier() {
        return this.tier;
    }''',
'''    /** The voltage tier this block belongs to. */
    public GTPortTier portTier() {
        return this.tier;
    }

    /**
     * Whether this port currently shows the advanced machine's casing.
     *
     * <p>Read by the colour handler, which is the only thing that distinguishes the two plates.</p>
     */
    public static boolean advancedCasing(BlockState state) {
        return state.hasProperty(ADVANCED_CASING) && state.getValue(ADVANCED_CASING);
    }''', 1)

if "import net.minecraft.world.level.block.state.properties.BooleanProperty;" not in s:
    s = s.replace("import net.minecraft.world.level.block.state.properties.DirectionProperty;",
                  "import net.minecraft.world.level.block.state.properties.BooleanProperty;\n"
                  "import net.minecraft.world.level.block.state.properties.DirectionProperty;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("port block carries the casing flag")
