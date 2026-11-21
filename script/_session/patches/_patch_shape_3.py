import io

# ---------------- 1. revert the widened slot limit ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''                    /**
                     * The per-slot ceiling, widened in multi-cell mode.
                     *
                     * <p>Single-cell mode keeps a slot at {@code limit}: one entry, one cell's worth.
                     * Multi-cell mode lets a slot hold the whole port, because that is what lets one
                     * type span cells — the type accumulates here and the grid draws the overflow.</p>
                     */
                    @Override
                    public int getSlotLimit(int slot) {
                        return singleCell ? limit : limit * slots;
                    }''',
'''                    /** One cell's worth. The mode changes how many cells a type may spread across,
                     * never how much a single cell holds. */
                    @Override
                    public int getSlotLimit(int slot) {
                        return limit;
                    }''', 1)

s = s.replace('''                    /**
                     * Enforces the stacking mode on the way in.
                     *
                     * <p>Not a drawing preference: in single-cell mode an entry may not outgrow one
                     * cell, so a type already present in a slot refuses every other slot, capping it
                     * at one slot's worth. Multi-cell mode allows the spread, which is the mode's
                     * whole purpose.</p>
                     */''',
'''                    /**
                     * Enforces the stacking mode on the way in.
                     *
                     * <p>Not a drawing preference. Multi-cell mode is simply a bigger chest: one type
                     * may fill several slots, and still only one slot's worth each. Single-cell mode
                     * confines a type to the slot it landed in, so it holds one slot's worth and no
                     * more.</p>
                     */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- 2. one merged simulation instead of a per-kind count ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

start = s2.index('''    /**
     * Whether the output handlers can give each product kind a place of its own.''')
end = s2.index('''    /**
     * Hands out the products of a finished recipe.''')
s2 = s2[:start] + s2[end:]

s2 = s2.replace('''        List<ItemStack> items = recipe.itemOutputs();
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
                }
            } else if (port.type() == MultiblockPortTypes.FLUID) {
                Object capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
                if (capability instanceof IFluidHandler handler) {
                    if (!MultiblockRecipeMatcher.canInsertFluids(fluids, handler)) {
                        return false;
                    }
                }
            }
        }
        return hasPlacePerKind(items, fluids);
    }''',
'''        // Every output handler is offered the whole product list at once, rather than each handler
        // being asked on its own. That is what makes the spill-over work: a product that does not
        // fit in the first hatch is tried against the next, and a product that cannot fit anywhere
        // rejects the craft. Asking per handler would refuse a craft that two hatches can clearly
        // hold between them.
        List<IItemHandler> itemHandlers = new ArrayList<>();
        List<IFluidHandler> fluidHandlers = new ArrayList<>();
        for (MultiblockPortBinding port : sortedPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler) {
                    itemHandlers.add(handler);
                }
            } else if (port.type() == MultiblockPortTypes.FLUID) {
                Object capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
                if (capability instanceof IFluidHandler handler) {
                    fluidHandlers.add(handler);
                }
            }
        }
        return MultiblockRecipeMatcher.canInsertItems(recipe.itemOutputs(), itemHandlers)
                && MultiblockRecipeMatcher.canInsertFluids(recipe.fluidOutputs(), fluidHandlers);
    }''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('machine:', 'changed' if s2 != orig2 else 'NO CHANGE')
