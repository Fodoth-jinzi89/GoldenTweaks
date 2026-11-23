"""Ports learn which machine they were assembled into.

The plate is chosen from the port's own block state, so the machine has to write that state when
it forms — and clear it when it comes apart, or a hatch reused in a basic build would keep the
advanced plate.

The ports are already walked when the structure is matched, so this rides along with that pass
rather than adding a new scan.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

anchor = '''        this.structure = structure;
        this.patternVersion = AMiBlockPatternReloadListener.INSTANCE.getVersion();
        this.structureCheckTick = getStructureCheckInterval();'''
assert anchor in s

addition = '''        this.structure = structure;
        this.patternVersion = AMiBlockPatternReloadListener.INSTANCE.getVersion();
        this.structureCheckTick = getStructureCheckInterval();

        // The ports' plates follow the machine they are set into, and that choice lives in their own
        // block state. Written here, on the tick the shape first matches, so the wall is correct
        // before the player looks at it.
        applyCasingToPorts(true);'''
s = s.replace(anchor, addition, 1)

# ---- the helper, plus the teardown path --------------------------------------------------------
anchor2 = '''    /**
     * Why the assembled shape is still not buildable, or {@code null} when it is.'''
assert anchor2 in s

helper = '''    /**
     * Stamps this machine's casing onto every port of the current shape.
     *
     * <p>A block state rather than a field on the port: the model is picked from the state, and a
     * block entity field would mean a block entity renderer for a texture choice.</p>
     *
     * @param advanced whether the ports should show the advanced machine's plate
     */
    private void applyCasingToPorts(boolean advanced) {
        if (this.level == null || this.level.isClientSide || this.structure == null) {
            return;
        }
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            BlockState portState = this.level.getBlockState(port.pos());
            if (!(portState.getBlock() instanceof GTPortBlock)
                    || !portState.hasProperty(GTPortBlock.ADVANCED_CASING)) {
                continue;
            }
            if (portState.getValue(GTPortBlock.ADVANCED_CASING) == advanced) {
                continue;
            }
            this.level.setBlock(port.pos(), portState.setValue(GTPortBlock.ADVANCED_CASING, advanced),
                    Block.UPDATE_ALL);
        }
    }

'''
s = s.replace(anchor2, helper + anchor2, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("machine stamps its casing onto its ports")

# ---- which casing this machine is --------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

old_call = "applyCasingToPorts(true);"
new_call = "applyCasingToPorts(usesAdvancedCasing());"
s2 = s2.replace(old_call, new_call, 1)

anchor3 = '''    /**
     * The wall block this machine's shape is built from.'''
assert anchor3 in s2

hook = '''    /**
     * Whether this machine is built from the advanced casing.
     *
     * <p>Asked of the same hook the structure check uses, so the port plates and the wall can never
     * disagree about which machine is standing here.</p>
     */
    protected boolean usesAdvancedCasing() {
        return false;
    }

'''
s2 = s2.replace(anchor3, hook + anchor3, 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("casing answer is a hook")
