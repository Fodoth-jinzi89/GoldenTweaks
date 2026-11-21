import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# Undo the leftover-based draft; it leaned on helpers that do not exist.
s = s.replace('''        // No list overload exists for insertion, so each hatch is filled in turn and anything it
        // cannot take is carried to the next. Same order and same result as the capacity check,
        // which is the point: a craft that passed must not still drop output here.
        List<ItemStack> remainingItems = items;
        for (IItemHandler handler : itemHandlers) {
            if (remainingItems.isEmpty()) {
                break;
            }
            MultiblockRecipeMatcher.insertItems(remainingItems, handler);
            remainingItems = leftoverItems(handler, remainingItems);
        }
        List<FluidStack> remainingFluids = fluids;
        for (IFluidHandler handler : fluidHandlers) {
            if (remainingFluids.isEmpty()) {
                break;
            }
            MultiblockRecipeMatcher.insertFluids(remainingFluids, handler);
            remainingFluids = leftoverFluids(handler, remainingFluids);
        }''',
'''        // No list overload exists for insertion, so each hatch is filled in turn and whatever it
        // could not take is carried on to the next. Same order as the capacity check, which is the
        // point: a craft that passed must not silently drop output here.
        List<ItemStack> remainingItems = items;
        for (IItemHandler handler : itemHandlers) {
            if (remainingItems.isEmpty()) {
                break;
            }
            remainingItems = insertAndLeftover(handler, remainingItems);
        }
        List<FluidStack> remainingFluids = fluids;
        for (IFluidHandler handler : fluidHandlers) {
            if (remainingFluids.isEmpty()) {
                break;
            }
            remainingFluids = fillAndLeftover(handler, remainingFluids);
        }''', 1)

# ---- the two spill helpers ----
s = s.replace('''    /** Multiplies item amounts, leaving empty entries alone. */''',
'''    /**
     * Inserts as much of {@code stacks} as {@code handler} will take, and returns the rest.
     *
     * <p>The accepted amount is measured by simulation first, because {@code insertItem} reports
     * leftovers per slot rather than per list: it is called slot by slot, and each call is given the
     * stack's running remainder. That is also how a single-cell handler refuses a kind it already
     * holds elsewhere, so the simulation honours the mode for free.</p>
     */
    private static List<ItemStack> insertAndLeftover(IItemHandler handler, List<ItemStack> stacks) {
        List<ItemStack> leftover = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remaining = stack.copy();
            for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
                remaining = handler.insertItem(slot, remaining, false);
            }
            if (!remaining.isEmpty()) {
                leftover.add(remaining);
            }
        }
        return leftover;
    }

    /** Fluid counterpart of {@link #insertAndLeftover}. */
    private static List<FluidStack> fillAndLeftover(IFluidHandler handler, List<FluidStack> stacks) {
        List<FluidStack> leftover = new ArrayList<>();
        for (FluidStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            FluidStack remaining = stack.copy();
            for (int tank = 0; tank < handler.getTanks() && !remaining.isEmpty(); tank++) {
                int accepted = handler.fill(remaining,
                        net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                if (accepted <= 0) {
                    continue;
                }
                remaining = remaining.copyWithAmount(remaining.getAmount() - accepted);
            }
            if (!remaining.isEmpty()) {
                leftover.add(remaining);
            }
        }
        return leftover;
    }

    /** Multiplies item amounts, leaving empty entries alone. */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
