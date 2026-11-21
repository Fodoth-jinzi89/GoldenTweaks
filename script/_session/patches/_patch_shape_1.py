import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * Whether the products of a finished recipe can be handed over.
     *
     * <p>Decided against the output ports only. AMI's own version also counts its internal output
     * buffer, which does not exist for this machine any more, so leaning on it would let a craft
     * start with nowhere to put the results.</p>
     *
     * <p>With "void excess" on this always says yes: whatever the ports cannot take is destroyed on
     * delivery, so a full output becomes a throughput decision rather than a stall.</p>
     */
    @Override
    protected boolean canOutput(MatchedMultiblockRecipe recipe) {
        if (this.voidOutput) {
            return true;
        }
        if (this.structure == null) {
            return false;
        }

        List<ItemStack> items = recipe.itemOutputs();
        List<FluidStack> fluids = recipe.fluidOutputs();
        for (MultiblockPortBinding port : sortedPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler
                        && !MultiblockRecipeMatcher.canInsertItems(items, handler)) {
                    return false;
                }
            } else if (port.type() == MultiblockPortTypes.FLUID) {
                Object capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
                if (capability instanceof IFluidHandler handler
                        && !MultiblockRecipeMatcher.canInsertFluids(fluids, handler)) {
                    return false;
                }
            }''',
'''    /**
     * Whether the products of a finished recipe can be handed over.
     *
     * <p>Decided against the output ports only. AMI's own version also counts its internal output
     * buffer, which does not exist for this machine any more, so leaning on it would let a craft
     * start with nowhere to put the results.</p>
     *
     * <p>Both checks run: room for the amounts, and a slot per kind. The second is not redundant.
     * A single-cell output confines each kind to its own slot, so a recipe producing three items
     * into a two-slot hatch has room for the amounts but nowhere to put the third — the delivery
     * would silently drop it. Refusing to start is the honest answer, and it is what makes the
     * output's mode a decision the player has to get right rather than a silent data loss.</p>
     *
     * <p>With "void excess" on this always says yes: whatever the ports cannot take is destroyed on
     * delivery, so a full output becomes a throughput decision rather than a stall.</p>
     */
    @Override
    protected boolean canOutput(MatchedMultiblockRecipe recipe) {
        if (this.voidOutput) {
            return true;
        }
        if (this.structure == null) {
            return false;
        }

        List<ItemStack> items = recipe.itemOutputs();
        List<FluidStack> fluids = recipe.fluidOutputs();
        for (MultiblockPortBinding port : sortedPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler) {
                    if (!MultiblockRecipeMatcher.canInsertItems(items, handler)) {
                        return false;
                    }
                    if (!hasSlotPerKind(handler, items.stream()
                            .filter(stack -> !stack.isEmpty())
                            .map(ItemStack::getItem)
                            .distinct().count())) {
                        return false;
                    }
                }
            } else if (port.type() == MultiblockPortTypes.FLUID) {
                Object capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
                if (capability instanceof IFluidHandler handler) {
                    if (!MultiblockRecipeMatcher.canInsertFluids(fluids, handler)) {
                        return false;
                    }
                    if (!hasTankPerKind(handler, fluids.stream()
                            .filter(stack -> !stack.isEmpty())
                            .map(FluidStack::getFluid)
                            .distinct().count())) {
                        return false;
                    }
                }
            }''', 1)

# ---- the two shape checks ----
s = s.replace('''    /**
     * Hands out the products of a finished recipe.''',
'''    /**
     * Whether an item handler can give each of {@code kinds} item types a slot of its own.
     *
     * <p>Only meaningful in single-cell mode, where one type is confined to one slot. In that mode
     * the answer is simply "are there enough slots", counted as free slots plus the slots already
     * holding one of these types — a type already present needs no extra room.</p>
     *
     * <p>Multi-cell mode has no such limit, because a type may spread across the whole handler.</p>
     */
    private static boolean hasSlotPerKind(IItemHandler handler, long kinds) {
        if (kinds <= 0) {
            return true;
        }
        if (!(handler instanceof ItemStackHandler stacks) || !isSingleCell(stacks)) {
            return true;
        }
        int free = 0;
        for (int slot = 0; slot < stacks.getSlots(); slot++) {
            if (stacks.getStackInSlot(slot).isEmpty()) {
                free++;
            }
        }
        // A type the handler already holds does not need a new slot; the count of kinds is bounded
        // by the slot total, so this can only reject when there are genuinely too few.
        return free + stacks.getSlots() >= kinds || free >= kinds;
    }

    /** Whether a fluid handler can give each of {@code kinds} fluids a tank of its own. */
    private static boolean hasTankPerKind(IFluidHandler handler, long kinds) {
        if (kinds <= 0) {
            return true;
        }
        if (handler instanceof GTPortFluidStorage storage) {
            if (!storage.isSingleCellMode()) {
                return true;
            }
            return storage.emptyTanks() >= kinds;
        }
        // Any other handler spreads freely; nothing to enforce.
        return true;
    }

    /** Reads the mode off a port's item handler, which answers it itself. */
    private static boolean isSingleCell(ItemStackHandler handler) {
        return handler instanceof GTPortItemHandler port && port.singleCellMode();
    }

    /**
     * Hands out the products of a finished recipe.''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
