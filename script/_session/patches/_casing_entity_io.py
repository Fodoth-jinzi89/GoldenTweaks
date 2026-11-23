"""The machine writes the casing onto the port entities, and both sides persist it."""
import io

# ---- 1. the machine writes the entity, not the state ------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old_stamp = '''        String casingId = BuiltInRegistries.BLOCK.getKey(casingBlock()).toString();
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            BlockState portState = this.level.getBlockState(port.pos());
            if (!(portState.getBlock() instanceof GTPortBlock)
                    || !portState.hasProperty(GTPortBlock.CASING)) {
                continue;
            }
            if (casingId.equals(portState.getValue(GTPortBlock.CASING))) {
                continue;
            }
            this.level.setBlock(port.pos(),
                    portState.setValue(GTPortBlock.CASING, casingId), Block.UPDATE_ALL);
            this.restyledPorts.add(port.pos().immutable());
        }'''
new_stamp = '''        ResourceLocation casingId = BuiltInRegistries.BLOCK.getKey(casingBlock());
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!(this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            if (casingId.equals(entity.casing())) {
                continue;
            }
            entity.setCasing(casingId);
            this.restyledPorts.add(port.pos().immutable());
        }'''
assert old_stamp in s
s = s.replace(old_stamp, new_stamp, 1)

old_revert = '''        for (BlockPos pos : this.restyledPorts) {
            BlockState portState = this.level.getBlockState(pos);
            if (portState.getBlock() instanceof GTPortBlock
                    && portState.hasProperty(GTPortBlock.CASING)
                    && !portState.getValue(GTPortBlock.CASING).isEmpty()) {
                this.level.setBlock(pos, portState.setValue(GTPortBlock.CASING, ""),
                        Block.UPDATE_ALL);
            }
        }
        this.restyledPorts.clear();'''
new_revert = '''        for (BlockPos pos : this.restyledPorts) {
            if (this.level.getBlockEntity(pos) instanceof GTPortBlockEntity entity) {
                entity.setCasing(null);
            }
        }
        this.restyledPorts.clear();'''
assert old_revert in s
s = s.replace(old_revert, new_revert, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("machine writes the port entity")

# ---- 2. save / load / sync ---------------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

# find saveAdditional and getUpdateTag to add the field
import re
m = re.search(r"(public void saveAdditional\([^)]*\)\s*\{\s*\n\s*super\.saveAdditional\([^)]*\);\n)", s2)
assert m, "saveAdditional not found"
s2 = s2[:m.end()] + '''        if (this.casing != null) {
            tag.putString(TAG_CASING, this.casing.toString());
        }
''' + s2[m.end():]

m2 = re.search(r"(public void loadAdditional\([^)]*\)\s*\{\s*\n\s*super\.loadAdditional\([^)]*\);\n)", s2)
assert m2, "loadAdditional not found"
s2 = s2[:m2.end()] + '''        this.casing = tag.contains(TAG_CASING)
                ? ResourceLocation.tryParse(tag.getString(TAG_CASING))
                : null;
''' + s2[m2.end():]

# the tag constant
s2 = s2.replace('''    private static final String TAG_CHANNEL = "Channel";''',
'''    private static final String TAG_CHANNEL = "Channel";

    /** The casing this port is set into; absent when it is not part of a machine. */
    private static final String TAG_CASING = "Casing";''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("port entity persists the casing")
