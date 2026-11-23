"""Create the block entity that matches the block being placed.

`machineTile` always built a basic kettle, so placing the advanced block produced an entity
whose registered type is the basic one. Vanilla validates that pairing on construction and
throws when they disagree — which is the crash on placement.

The block class is shared by both machines, so it now asks which one it is before building.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlock.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    @Override
    protected GTChemicalReactorMachineBlockEntity machineTile(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new GTChemicalReactorMachineBlockEntity(pos, state);
    }''',
'''    /**
     * The entity for this block.
     *
     * <p>Both machines share this block class, so the block has to say which one it is: an entity's
     * registered type must match the block it sits in, and vanilla throws on construction when it
     * does not. Deciding here keeps the difference in one place instead of needing a subclass whose
     * only job would be to answer it.</p>
     */
    @Override
    protected GTChemicalReactorMachineBlockEntity machineTile(@NotNull BlockPos pos, @NotNull BlockState state) {
        if (state.is(GTAdvancedChemicalReactorMultiblock.MACHINE.get())) {
            return new GTAdvancedChemicalReactorBlockEntity(pos, state);
        }
        return new GTChemicalReactorMachineBlockEntity(pos, state);
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("machineTile chooses by block")
