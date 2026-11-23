"""The port records which casing it is set into, by block rather than by flag.

A boolean only answers "advanced or not", which stops working the moment there are more than two
casings. The port now carries the casing block itself as a registry id, so a new casing needs no
change here — the machine writes whichever block it is built from.

A string property rather than a block reference because block states are compared and serialised by
value; the id is resolved back to a block at draw time.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java"
s = io.open(p, encoding="utf-8").read()

old = '''    /**
     * Which machine's casing this port is set into.
     *
     * <p>The base model draws one grey plate and this picks its colour, so a hatch in the advanced
     * kettle matches the polypropylene wall around it instead of showing the basic machine's
     * polyethylene. Written by the machine when it assembles; a loose part keeps the default.</p>
     */
    public static final BooleanProperty ADVANCED_CASING = BooleanProperty.create("advanced_casing");'''

new = '''    /**
     * Which machine's casing this port is set into.
     *
     * <p>The id of the casing block, not a flag: a flag only says "advanced or not", and the machine
     * set is meant to grow. An empty string means "no machine" — a loose part, or a port whose
     * machine came apart — and the model's own plate is drawn.</p>
     *
     * <p>The block itself cannot be stored in a state (states are compared and serialised by value),
     * so the id is kept and resolved back at draw time.</p>
     */
    public static final StringProperty CASING = StringProperty.create("casing");'''

assert old in s
s = s.replace(old, new, 1)

s = s.replace('''                .setValue(FACING, Direction.NORTH)
                .setValue(ADVANCED_CASING, false));''',
'''                .setValue(FACING, Direction.NORTH)
                .setValue(CASING, ""));''', 1)

s = s.replace("builder.add(FACING, ADVANCED_CASING);", "builder.add(FACING, CASING);", 1)

old_acc = '''    /**
     * Whether this port currently shows the advanced machine's casing.
     *
     * <p>Read by the colour handler, which is the only thing that distinguishes the two plates.</p>
     */
    public static boolean advancedCasing(BlockState state) {
        return state.hasProperty(ADVANCED_CASING) && state.getValue(ADVANCED_CASING);
    }'''
new_acc = '''    /**
     * The casing this port is set into, or {@code null} when it is not part of a machine.
     *
     * <p>Read by the renderer, which is the only thing that distinguishes the plates.</p>
     */
    @Nullable
    public static BlockState casingOf(BlockState state, java.util.function.Function<ResourceLocation, BlockState> resolver) {
        if (!state.hasProperty(CASING)) {
            return null;
        }
        String id = state.getValue(CASING);
        if (id.isEmpty()) {
            return null;
        }
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location == null ? null : resolver.apply(location);
    }'''
assert old_acc in s
s = s.replace(old_acc, new_acc, 1)

for imp in [
    "import net.minecraft.resources.ResourceLocation;",
    "import net.minecraft.world.level.block.state.properties.StringProperty;",
]:
    if imp not in s:
        s = s.replace("import net.minecraft.world.level.block.state.properties.BooleanProperty;",
                      imp + "\nimport net.minecraft.world.level.block.state.properties.BooleanProperty;", 1)

# the boolean import is no longer used
s = s.replace("import net.minecraft.world.level.block.state.properties.BooleanProperty;\n", "", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("port stores the casing block's id")
