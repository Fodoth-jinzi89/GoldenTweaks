"""Walk the shape's cells instead of AMI's port list.

`structure.getPorts()` only contains what AMI recognised as a port. A hopper or canister implements
our own capability set, but AMI's matcher has no reason to call it a port, so those blocks never
appeared in the list and never received a casing — which is why only the buses and hatches changed.

The shape is walked from our own layout, which is the file that says every cell's role. Any cell
holding a `GTPortBlock` gets the casing, whatever kind of port it is.

The client also needs a real rebuild. `sendBlockUpdated` tells it the entity changed, but the chunk
keeps the quads it already built; the section has to be marked for re-render, which
`setBlocksDirty` does and `setBlock` on an unchanged state does not.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old_stamp = s[s.index("    private void applyCasingToPorts() {"):]
old_stamp = old_stamp[:old_stamp.index("\n    }\n") + len("\n    }\n")]

new_stamp = '''    private void applyCasingToPorts() {
        if (this.level == null || this.level.isClientSide || this.structure == null) {
            return;
        }
        ResourceLocation casingId = BuiltInRegistries.BLOCK.getKey(casingBlock());
        int written = 0;

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
            written++;
        }

        GoldenTweaks.LOGGER.info("[GT/diag] applyCasingToPorts casing={} written={}",
                casingId, written);
    }

    /**
     * Every position the current shape occupies.
     *
     * <p>Read from the layout and the host's own orientation, which is the same mapping the
     * structure check and the projector use.</p>
     */
    private List<BlockPos> shapeCells() {
        GTMultiblockPattern layout = GTMultiblockStructures.layout(getPatternId());
        if (layout == null) {
            return List.of();
        }

        BlockState state = getBlockState();
        Direction facing = state.getValue(GTChemicalReactorMachineBlock.FACING);
        int roll = state.getValue(GTChemicalReactorMachineBlock.ROLL);
        boolean mirrored = state.getValue(GTChemicalReactorMachineBlock.MIRRORED);

        Direction forwards = GTChemicalReactorOrientation.patternForwards(facing);
        Direction up = GTChemicalReactorOrientation.up(facing, roll);

        GTMultiblockPattern.Master master = layout.master();
        BlockPos anchor = GTChemicalReactorOrientation.frontTopLeft(
                this.worldPosition, forwards, up, mirrored,
                master.column(),
                layout.height() - 1 - master.layer(),
                layout.depth() - 1 - master.row());

        List<BlockPos> cells = new ArrayList<>();
        for (int layer = 0; layer < layout.height(); layer++) {
            for (int row = 0; row < layout.depth(); row++) {
                for (int column = 0; column < layout.width(); column++) {
                    cells.add(GTChemicalReactorOrientation.cell(
                            anchor, forwards, up, mirrored,
                            layout.height(), layout.depth(), column, layer, row));
                }
            }
        }
        return cells;
    }
'''

s = s.replace(old_stamp, new_stamp, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("casing stamp walks the shape itself")

# ---- the client rebuild ----------------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

old2 = '''        // The data packet alone is not enough: a section keeps the mesh it already built, so the
        // renderer never asks for the plate again. Marking the position dirty is what schedules the
        // rebuild that runs getModelData.
        this.level.setBlock(this.worldPosition, getBlockState(), Block.UPDATE_CLIENTS);
        this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(),
                Block.UPDATE_CLIENTS);'''
new2 = '''        // The data packet alone is not enough: a section keeps the mesh it already built, so the
        // renderer never asks for the plate again. Both calls are needed — the first ships the new
        // field to the client, the second marks the section for the rebuild that re-runs
        // getModelData. `setBlock` with an unchanged state does neither.
        this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(),
                Block.UPDATE_CLIENTS);
        this.level.setBlocksDirty(this.worldPosition, getBlockState(), getBlockState());'''
assert old2 in s2
s2 = s2.replace(old2, new2, 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("client section marked for rebuild")
