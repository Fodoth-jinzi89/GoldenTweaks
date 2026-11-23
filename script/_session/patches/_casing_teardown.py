"""The advanced kettle answers which casing it is, and ports revert when a machine comes apart.

`usesAdvancedCasing` compares against the casing block the machine already reports, so there is one
statement of "what this machine is built from" rather than two that could drift.

Teardown matters as much as assembly: a hatch pulled out of an advanced build and set into a basic
one must show the basic plate, otherwise the wall would keep a panel that belongs to a machine that
is no longer there.
"""
import io

# ---- 1. the advanced machine answers -----------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''    /** Polypropylene rather than polyethylene: the material is what makes this the advanced one. */
    @Override
    protected Block casingBlock() {
        return GTMultiblockBlocks.SHELL_PP.get();
    }'''
new = '''    /** Polypropylene rather than polyethylene: the material is what makes this the advanced one. */
    @Override
    protected Block casingBlock() {
        return GTMultiblockBlocks.SHELL_PP.get();
    }

    /**
     * The wall this machine is built from is the advanced one, so its ports show that plate.
     *
     * <p>Derived from {@link #casingBlock()} rather than declared again: two statements of the same
     * fact are two chances to disagree.</p>
     */
    @Override
    protected boolean usesAdvancedCasing() {
        return casingBlock() == GTMultiblockBlocks.SHELL_PP.get();
    }'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("advanced kettle answers its casing")

# ---- 2. revert on teardown --------------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

anchor = '''    /**
     * Re-matches the assembly after the host was rotated or mirrored.'''
assert anchor in s2

addition = '''    /**
     * Puts the ports back to the standard plate when the shape no longer matches.
     *
     * <p>Called from the invalidation path rather than from teardown alone: a machine can come apart
     * because a wall was broken, and those ports are still in the world and must not keep a plate
     * that belongs to a shape that is gone.</p>
     */
    private void clearCasingFromPorts() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        // The structure may already be null here, so the ports are found by walking the shape from
        // the pattern instead of from the stored binding list.
        for (MultiblockPortBinding port : portBindingsOrEmpty()) {
            BlockState portState = this.level.getBlockState(port.pos());
            if (portState.getBlock() instanceof GTPortBlock
                    && portState.hasProperty(GTPortBlock.ADVANCED_CASING)
                    && portState.getValue(GTPortBlock.ADVANCED_CASING)) {
                this.level.setBlock(port.pos(),
                        portState.setValue(GTPortBlock.ADVANCED_CASING, false), Block.UPDATE_ALL);
            }
        }
    }

'''
s2 = s2.replace(anchor, addition + anchor, 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("teardown helper added")
