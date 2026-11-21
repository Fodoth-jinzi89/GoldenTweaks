import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- the check itself ----
s = s.replace('''        PortRequirement unsatisfied = structure.firstUnsatisfied(getPortRequirements());
        if (unsatisfied != null) {''',
'''        // The pattern alone is not enough. Its "x" cells accept the casing tag, which also holds the
        // ports, so a bare frame of ports would shape up without a single real casing — and the
        // "any" cells accept anything at all, a dynamo hatch included. Both are rejected here.
        Component shapeProblem = checkShapeMaterials(structure);
        if (shapeProblem != null) {
            if (player != null) {
                player.displayClientMessage(shapeProblem, true);
            }
            return false;
        }

        PortRequirement unsatisfied = structure.firstUnsatisfied(getPortRequirements());
        if (unsatisfied != null) {''', 1)

# ---- the helper, next to the other structure code ----
s = s.replace('''    /**
     * Releases everything the machine holds onto outside itself, before the block goes away.''',
'''    /**
     * Why the assembled shape is still not buildable, or {@code null} when it is.
     *
     * <p>Two rules the pattern cannot express on its own. It needs a real wall of casings: every
     * "x" cell accepts the casing tag, and that tag lists the ports too, so without this a frame of
     * nothing but buses would pass. And a dynamo hatch is not a part of this machine, so one standing
     * in for a casing — which the tag would allow — must block the whole build.</p>
     */
    @Nullable
    private Component checkShapeMaterials(MultiblockStructure structure) {
        if (this.level == null) {
            return null;
        }
        int casings = 0;
        for (BlockPos pos : structure.getBlocksExcludingMaster()) {
            BlockState state = this.level.getBlockState(pos);
            if (state.is(GTMultiblockBlocks.SHELL_PE.get())) {
                casings++;
            } else if (state.getBlock() instanceof GTPortBlock port
                    && port.kind() == GTPortKind.ENERGY
                    && port.mode() == PortMode.OUTPUT) {
                // Named separately from the casing count: the player has to be told which of the two
                // rules they broke, or they will keep swapping casings around a hatch that can never
                // belong here.
                return Component.translatable(
                        "message.goldentweaks.chemical_reactor.no_dynamo");
            }
        }
        if (casings < MIN_CASINGS) {
            return Component.translatable(
                    "message.goldentweaks.chemical_reactor.not_enough_casing",
                    MIN_CASINGS);
        }
        return null;
    }

    /**
     * Releases everything the machine holds onto outside itself, before the block goes away.''', 1)

# ---- the constant ----
s = s.replace('''    /** Value of {@link #overclockLimit} meaning "no ceiling". */''',
'''    /** Casings the shape needs at least. Mirrored in the item tooltip, which states the same floor. */
    private static final int MIN_CASINGS = 8;

    /** Value of {@link #overclockLimit} meaning "no ceiling". */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
