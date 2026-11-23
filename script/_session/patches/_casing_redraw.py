"""Make the client redraw when the casing changes.

`sendBlockUpdated` ships the block entity's data, but a block's quads are cached per section. Sending
the data alone updates the entity on the client while the chunk keeps the mesh it already built, so
the plate stayed as it was.

`setBlocksDirty` marks the position for a rebuild, which is what actually makes the renderer ask
again — and asking again is what runs `getModelData`.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''        this.casing = casing;
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(),
                    Block.UPDATE_ALL);
        }'''
new = '''        this.casing = casing;
        setChanged();
        if (this.level == null) {
            return;
        }
        // The data packet alone is not enough: a section keeps the mesh it already built, so the
        // renderer never asks for the plate again. Marking the position dirty is what schedules the
        // rebuild that runs getModelData.
        this.level.setBlock(this.worldPosition, getBlockState(), Block.UPDATE_CLIENTS);
        this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(),
                Block.UPDATE_CLIENTS);'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("client rebuild scheduled on casing change")
