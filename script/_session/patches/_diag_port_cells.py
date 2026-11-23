"""Report every port the shape walk actually sees.

Four cells stamped out of ten ports means the walk is not reaching six of them. This prints each cell
that holds a `GTPortBlock`, its casing, and the index it was compared against, so the six that are
missing can be identified by their coordinates rather than guessed at.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''        for (BlockPos pos : shapeCells()) {
            BlockState portState = this.level.getBlockState(pos);
            if (!(portState.getBlock() instanceof GTPortBlock)
                    || !portState.hasProperty(GTPortBlock.CASING)) {
                continue;
            }'''
new = '''        for (BlockPos pos : shapeCells()) {
            BlockState portState = this.level.getBlockState(pos);
            if (!(portState.getBlock() instanceof GTPortBlock)) {
                continue;
            }
            GoldenTweaks.LOGGER.info("[GT/diag] port cell {} block={} casing={} want={}",
                    pos, BuiltInRegistries.BLOCK.getKey(portState.getBlock()),
                    portState.hasProperty(GTPortBlock.CASING)
                            ? portState.getValue(GTPortBlock.CASING) : "n/a",
                    casingIndex);
            if (!portState.hasProperty(GTPortBlock.CASING)) {
                continue;
            }'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("per-cell port report added")
