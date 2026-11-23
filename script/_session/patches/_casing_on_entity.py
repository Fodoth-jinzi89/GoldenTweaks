"""The port's casing lives on its block entity, not in its block state.

A block state property has to be a closed set — booleans, bounded integers, a fixed enum — so it can
never hold "whichever casing this machine happens to use" once that set is open. With hundreds of
casings planned, anything state-based is a dead end.

The model-data hook is the way out: it is given the level and the position, so it can read the block
entity directly. The port entity already outlives every reload, so a field there is both open-ended
and durable.
"""
import io

# ---- 1. the port block: no casing property at all ----------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java"
s = io.open(p, encoding="utf-8").read()

start = s.index("    /**\n     * Which machine's casing this port is set into.")
end = s.index("    private final GTPortKind kind;")
s = s[:start] + s[end:]

s = s.replace('''        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CASING, ""));''',
'''        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));''', 1)

s = s.replace("        builder.add(FACING, CASING);", "        builder.add(FACING);", 1)

# the accessor goes with the property
acc_start = s.find("    /**\n     * The casing this port is set into, or {@code null}")
if acc_start >= 0:
    acc_end = s.index("    }", s.index("resolver.apply(location)", acc_start)) + len("    }\n")
    s = s[:acc_start] + s[acc_end:]

for stale in (
    "import net.minecraft.world.level.block.state.properties.StringProperty;\n",
    "import net.minecraft.resources.ResourceLocation;\n",
):
    s = s.replace(stale, "", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("port block back to plain facing")

# ---- 2. the port entity carries the casing -----------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

anchor = "    private static final String TAG_CHANNEL = \"Channel\";"
assert anchor in s2

field = '''    private static final String TAG_CHANNEL = "Channel";

    /**
     * The casing this port is set into, as a block id, or {@code null} when it is not part of a
     * machine.
     *
     * <p>A field rather than a block state property: a state's value set is closed by construction
     * — booleans, bounded integers, a fixed enum — so it cannot name a casing from an open set.
     * This one is open-ended, which is what lets a new casing be a texture and nothing else.</p>
     *
     * <p>Saved and synced, so the plate survives a reload and shows on the client.</p>
     */
    @Nullable
    private ResourceLocation casing;

'''
s2 = s2.replace(anchor, field, 1)

# ---- the accessors, placed with the other display state ---------------------------------------
anchor2 = '''    /** The {@code channel} field's text. */'''
assert anchor2 in s2

accessors = '''    /** The casing this port is set into, or {@code null} when it is not part of a machine. */
    @Nullable
    public ResourceLocation casing() {
        return this.casing;
    }

    /** Sets the casing, or clears it with {@code null}. Only a change marks the block for a redraw. */
    public void setCasing(@Nullable ResourceLocation casing) {
        if (java.util.Objects.equals(this.casing, casing)) {
            return;
        }
        this.casing = casing;
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(),
                    Block.UPDATE_ALL);
        }
    }

'''
s2 = s2.replace(anchor2, accessors + anchor2, 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("port entity carries the casing")
