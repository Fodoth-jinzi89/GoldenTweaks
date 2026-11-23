"""The machine stamps the casing into the port's state, not its entity.

CTM reads sprites from the state, so the state is where the casing has to live. The entity field and
its model data plumbing are removed with this change: they existed to work around the state's closed
value set, and the casing list solves that directly.
"""
import io

# ---- 1. machine: write the state -----------------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''        ResourceLocation casingId = BuiltInRegistries.BLOCK.getKey(casingBlock());
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
        for (BlockPos pos : shapeCells()) {
            BlockState portState = this.level.getBlockState(pos);
            if (!(portState.getBlock() instanceof GTPortBlock)
                    || !portState.hasProperty(GTPortBlock.CASING)) {
                continue;
            }
            if (portState.getValue(GTPortBlock.CASING) == casingIndex) {
                continue;
            }
            // The state carries it because CTM resolves a cell's sprite from the state alone.
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
        }
        this.restyledPorts.clear();'''
new_revert = '''        for (BlockPos pos : this.restyledPorts) {
            BlockState portState = this.level.getBlockState(pos);
            if (portState.getBlock() instanceof GTPortBlock
                    && portState.hasProperty(GTPortBlock.CASING)
                    && portState.getValue(GTPortBlock.CASING) != 0) {
                this.level.setBlock(pos, portState.setValue(GTPortBlock.CASING, 0),
                        Block.UPDATE_ALL);
            }
        }
        this.restyledPorts.clear();'''
assert old_revert in s
s = s.replace(old_revert, new_revert, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("machine writes the port state")

# ---- 2. entity: the casing field and its model data are gone -------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

start = s2.index("    /**\n     * The casing this port is set into, as a block id, or {@code null} when it is not part of a")
end = s2.index("    private static final String TAG_CHANNEL")
s2 = s2[:start] + s2[end:]

# the tag constant
s2 = s2.replace('''

    /** The casing this port is set into; absent when it is not part of a machine. */
    private static final String TAG_CASING = "Casing";''', "", 1)

# the accessors and the model data hook
start2 = s2.index("    /** The casing this port is set into, or {@code null} when it is not part of a machine. */")
end2 = s2.index("    /** The {@code channel} field's text. */")
s2 = s2[:start2] + s2[end2:]

# persistence
s2 = s2.replace('''        if (this.casing != null) {
            tag.putString(TAG_CASING, this.casing.toString());
        }
''', "")
s2 = s2.replace('''        this.casing = tag.contains(TAG_CASING)
                ? ResourceLocation.tryParse(tag.getString(TAG_CASING))
                : null;
''', "")

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("entity casing removed")

# ---- 3. the model-data property on the block is gone too ------------------------------------------
p3 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java"
s3 = io.open(p3, encoding="utf-8").read()

start3 = s3.index("    /**\n     * The casing this port is set into, filled in for the renderer.")
end3 = s3.index("    private final GTPortKind kind;")
s3 = s3[:start3] + s3[end3:]

for imp in ("import net.neoforged.neoforge.client.model.data.ModelProperty;\n",
            "import net.neoforged.neoforge.client.model.data.ModelData;\n",
            "import net.minecraft.world.level.BlockAndTintGetter;\n"):
    s3 = s3.replace(imp, "", 1)

io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("block model-data property removed")
