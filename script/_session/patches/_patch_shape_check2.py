import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

start = s.index('''    /**
     * Why the assembled shape is still not buildable, or {@code null} when it is.''')
end = s.index('''    /**
     * Releases everything the machine holds onto outside itself, before the block goes away.''')

replacement = '''    /**
     * Why the assembled shape is still not buildable, or {@code null} when it is.
     *
     * <p>Driven by the source layout rather than by the matched structure, because the two cells
     * differ in kind and AMI's pattern has forgotten which is which by the time it is matched:</p>
     *
     * <ul>
     *   <li>a casing cell needs a real casing, and the casing tag also lists the ports, so a frame
     *       of nothing but buses would otherwise shape up;</li>
     *   <li>an "any" cell is a structural gap, meant for filler or for a port, and contributes
     *       nothing to the casing count — a casing dropped there is not one of the必需的 eight;</li>
     *   <li>a dynamo hatch belongs to neither kind. It is not part of this machine, so it may stand
     *       in a gap but never in a casing cell.</li>
     * </ul>
     */
    @Nullable
    private Component checkShapeMaterials(MultiblockStructure structure) {
        GTMultiblockPattern layout = GTMultiblockStructures.layout(getPatternId());
        if (this.level == null || layout == null) {
            return null;
        }

        BlockState state = getBlockState();
        Direction facing = state.getValue(GTChemicalReactorMachineBlock.FACING);
        int roll = state.getValue(GTChemicalReactorMachineBlock.ROLL);
        boolean mirrored = state.getValue(GTChemicalReactorMachineBlock.MIRRORED);
        Direction forwards = GTChemicalReactorOrientation.patternForwards(facing);
        Direction up = GTChemicalReactorOrientation.up(facing, roll);

        // Same anchor and same cell mapping the projector uses, so the cells judged here are the
        // ones the player was told to fill.
        GTMultiblockPattern.Master master = layout.master();
        BlockPos anchor = GTChemicalReactorOrientation.frontTopLeft(
                this.worldPosition, forwards, up, mirrored,
                master.column(),
                layout.height() - 1 - master.layer(),
                layout.depth() - 1 - master.row());

        int casings = 0;
        for (int layer = 0; layer < layout.height(); layer++) {
            for (int row = 0; row < layout.depth(); row++) {
                for (int column = 0; column < layout.width(); column++) {
                    char symbol = layout.symbolAt(column, layer, row);
                    // An "any" cell is a gap. Whatever stands there is filler and does not count.
                    if (layout.isWildcard(symbol)) {
                        continue;
                    }
                    BlockPos pos = GTChemicalReactorOrientation.cell(
                            anchor, forwards, up, mirrored,
                            layout.height(), layout.depth(), column, layer, row);
                    if (pos.equals(this.worldPosition)) {
                        continue;
                    }

                    BlockState here = this.level.getBlockState(pos);
                    if (isDynamoBlock(here)) {
                        // Only reachable in a casing cell: an "any" cell was already skipped, so a
                        // dynamo here really is standing in for a casing.
                        return Component.translatable(
                                "message.goldentweaks.chemical_reactor.no_dynamo");
                    }
                    if (here.is(GTMultiblockBlocks.SHELL_PE.get())) {
                        casings++;
                    }
                }
            }
        }
        if (casings < MIN_CASINGS) {
            return Component.translatable(
                    "message.goldentweaks.chemical_reactor.not_enough_casing",
                    MIN_CASINGS);
        }
        return null;
    }

    /** Whether a state is one of this mod's energy output hatches. */
    private static boolean isDynamoBlock(BlockState state) {
        return state.getBlock() instanceof GTPortBlock port
                && port.kind() == GTPortKind.ENERGY
                && port.mode() == PortMode.OUTPUT;
    }

'''

s = s[:start] + replacement + s[end:]
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
