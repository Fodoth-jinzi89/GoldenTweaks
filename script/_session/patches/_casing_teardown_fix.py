"""Ports revert when the machine comes apart, using the positions the machine already holds.

The invalidate path may already have dropped the structure by the time it runs, so the positions are
remembered when they are stamped rather than looked up afterwards. That also keeps the revert from
needing a pattern walk at a moment the pattern may no longer match.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

# ---- a remembered set of the ports whose plate this machine changed -----------------------------
anchor = '''    /**
     * Stamps this machine's casing onto every port of the current shape.'''
assert anchor in s

field = '''    /**
     * Ports whose plate this machine has changed.
     *
     * <p>Remembered because the revert runs after the structure is gone: walking the pattern again
     * at that moment would need a shape that no longer matches. Only ports this machine actually
     * touched are listed, so a port the player built into another machine is never disturbed.</p>
     */
    private final Set<BlockPos> restyledPorts = new HashSet<>();

'''
s = s.replace(anchor, field + anchor, 1)

# ---- record on stamp, and rewrite the revert ---------------------------------------------------
old_stamp = '''            this.level.setBlock(port.pos(), portState.setValue(GTPortBlock.ADVANCED_CASING, advanced),
                    Block.UPDATE_ALL);'''
new_stamp = '''            this.level.setBlock(port.pos(), portState.setValue(GTPortBlock.ADVANCED_CASING, advanced),
                    Block.UPDATE_ALL);
            this.restyledPorts.add(port.pos().immutable());'''
assert old_stamp in s
s = s.replace(old_stamp, new_stamp, 1)

old_revert = '''        // The structure may already be null here, so the ports are found by walking the shape from
        // the pattern instead of from the stored binding list.
        for (MultiblockPortBinding port : portBindingsOrEmpty()) {
            BlockState portState = this.level.getBlockState(port.pos());
            if (portState.getBlock() instanceof GTPortBlock
                    && portState.hasProperty(GTPortBlock.ADVANCED_CASING)
                    && portState.getValue(GTPortBlock.ADVANCED_CASING)) {
                this.level.setBlock(port.pos(),
                        portState.setValue(GTPortBlock.ADVANCED_CASING, false), Block.UPDATE_ALL);
            }
        }'''
new_revert = '''        for (BlockPos pos : this.restyledPorts) {
            BlockState portState = this.level.getBlockState(pos);
            if (portState.getBlock() instanceof GTPortBlock
                    && portState.hasProperty(GTPortBlock.ADVANCED_CASING)
                    && portState.getValue(GTPortBlock.ADVANCED_CASING)) {
                this.level.setBlock(pos,
                        portState.setValue(GTPortBlock.ADVANCED_CASING, false), Block.UPDATE_ALL);
            }
        }
        this.restyledPorts.clear();'''
assert old_revert in s
s = s.replace(old_revert, new_revert, 1)

for imp in ("import java.util.HashSet;", "import java.util.Set;"):
    if imp not in s:
        lines = s.split("\n")
        last = max(i for i, l in enumerate(lines) if l.startswith("import java.util"))
        lines.insert(last + 1, imp)
        s = "\n".join(lines)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("revert uses the remembered positions")
