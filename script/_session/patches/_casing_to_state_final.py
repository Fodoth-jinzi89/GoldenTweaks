"""The machine stamps the casing index into the port's state.

Create's connected-texture hook reads the casing from the state, so that is where it goes. The block
entity field and its model-data plumbing are removed: they were there for a render-time-only plate,
which is the approach that could not connect.

A port outside any machine keeps index 0, the default, so nothing has to be cleaned up on its behalf.
"""
import io

# ---- 1. machine --------------------------------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''        ResourceLocation casingId = BuiltInRegistries.BLOCK.getKey(casingBlock());

        // Walked from our own layout rather than AMI's port list: that list holds only what AMI
        // matched as a port, and a hopper or canister is a port by our rules, not by AMI's.
        for (BlockPos pos : shapeCells()) {
            if (!(this.level.getBlockEntity(pos) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            if (casingId.equals(entity.casing())) {
                continue;
            }
            entity.setCasing(casingId);
            this.restyledPorts.add(pos.immutable());
        }'''
new = '''        int casingIndex = GTMultiblockBlocks.casingIndex(casingBlock());

        // Walked from our own layout rather than AMI's port list: that list holds only what AMI
        // matched as a port, and a hopper or canister is a port by our rules, not by AMI's.
        for (BlockPos pos : shapeCells()) {
            BlockState portState = this.level.getBlockState(pos);
            if (!(portState.getBlock() instanceof GTPortBlock)
                    || !portState.hasProperty(GTPortBlock.CASING)) {
                continue;
            }
            if (portState.getValue(GTPortBlock.CASING) == casingIndex) {
                continue;
            }
            // The state carries it: the connected-texture hook that picks a face's sheet is given
            // the state and nothing else.
            this.level.setBlock(pos, portState.setValue(GTPortBlock.CASING, casingIndex),
                    Block.UPDATE_ALL);
            this.restyledPorts.add(pos.immutable());
        }'''
assert old in s
s = s.replace(old, new, 1)

old_revert = '''        for (BlockPos pos : this.restyledPorts) {
            if (this.level.getBlockEntity(pos) instanceof GTPortBlockEntity entity) {
                entity.setCasing(null);
            }
        }'''
new_revert = '''        for (BlockPos pos : this.restyledPorts) {
            BlockState portState = this.level.getBlockState(pos);
            if (portState.getBlock() instanceof GTPortBlock
                    && portState.hasProperty(GTPortBlock.CASING)
                    && portState.getValue(GTPortBlock.CASING) != 0) {
                this.level.setBlock(pos, portState.setValue(GTPortBlock.CASING, 0),
                        Block.UPDATE_ALL);
            }
        }'''
assert old_revert in s
s = s.replace(old_revert, new_revert, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("machine writes the state")

# ---- 2. entity: drop the casing field, accessors, persistence ------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

start = s2.find("    /**\n     * The casing this port is set into, as a block id, or {@code null} when it is not part of a")
if start >= 0:
    end = s2.index("    private static final String TAG_PRIORITY")
    s2 = s2[:start] + s2[end:]
    print("  casing field block removed")

s2 = s2.replace('''

    /** The casing this port is set into; absent when it is not part of a machine. */
    private static final String TAG_CASING = "Casing";''', "")

start2 = s2.find("    /** The casing this port is set into, or {@code null} when it is not part of a machine. */")
if start2 >= 0:
    end2 = s2.index("    private static final String TAG_PRIORITY", start2) if "TAG_PRIORITY" in s2[start2:] else s2.index("    /** The {@code channel} field's text. */", start2)
    s2 = s2[:start2] + s2[end2:]
    print("  accessors removed")

s2 = s2.replace('''        if (this.casing != null) {
            tag.putString(TAG_CASING, this.casing.toString());
        }
''', "")
s2 = s2.replace('''        this.casing = tag.contains(TAG_CASING)
                ? ResourceLocation.tryParse(tag.getString(TAG_CASING))
                : null;
''', "")

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("entity cleaned, casing refs left:", s2.count("casing"))
