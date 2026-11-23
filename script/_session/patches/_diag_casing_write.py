"""Trace the casing write at the one place that matters.

The state carries the casing now, so `setBlock` on the server is what changes the plate. If the plate
does not change until a reload, either the write never happens, or it happens and the client does not
rebuild the section.

This prints both sides, with the position, so they can be matched up.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''            if (portState.getValue(GTPortBlock.CASING) == casingIndex) {
                continue;
            }
            // The state carries it: the connected-texture hook that picks a face's sheet is given
            // the state and nothing else.
            this.level.setBlock(pos, portState.setValue(GTPortBlock.CASING, casingIndex),
                    Block.UPDATE_ALL);
            this.restyledPorts.add(pos.immutable());'''
new = '''            if (portState.getValue(GTPortBlock.CASING) == casingIndex) {
                continue;
            }
            // The state carries it: the connected-texture hook that picks a face's sheet is given
            // the state and nothing else.
            this.level.setBlock(pos, portState.setValue(GTPortBlock.CASING, casingIndex),
                    Block.UPDATE_ALL);
            this.restyledPorts.add(pos.immutable());
            GoldenTweaks.LOGGER.info("[GT/diag] stamped {} -> casing {}",
                    pos, casingIndex);'''
assert old in s
s = s.replace(old, new, 1)

# and how often the whole call runs, and with what
old2 = '''        int casingIndex = GTMultiblockBlocks.casingIndex(casingBlock());'''
new2 = '''        int casingIndex = GTMultiblockBlocks.casingIndex(casingBlock());
        GoldenTweaks.LOGGER.info("[GT/diag] applyCasingToPorts side={} casing={} cells={}",
                this.level.isClientSide ? "client" : "server", casingIndex, shapeCells().size());'''
assert old2 in s
s = s.replace(old2, new2, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("write diagnostics added")
