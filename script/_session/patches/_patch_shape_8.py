import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- fluid capacity check: simulate the same spill the delivery performs ----
s = s.replace('''        return MultiblockRecipeMatcher.canInsertItems(items, itemHandlers)
                && MultiblockRecipeMatcher.canInsertFluids(fluids, fluidHandlers);''',
'''        // Items have a list overload that already spans the hatches; fluids do not, so the spill is
        // simulated here the same way it is performed on delivery. Either way the question answered
        // is "does everything fit somewhere", not "does everything fit in one hatch".
        return MultiblockRecipeMatcher.canInsertItems(items, itemHandlers)
                && fitsFluids(fluidHandlers, fluids);''', 1)

s = s.replace('''        return MultiblockRecipeMatcher.canInsertItems(outputs, itemHandlers)
                && MultiblockRecipeMatcher.canInsertFluids(fluidOutputs, fluidHandlers);''',
'''        return MultiblockRecipeMatcher.canInsertItems(outputs, itemHandlers)
                && fitsFluids(fluidHandlers, fluidOutputs);''', 1)

# ---- the shared simulation helper ----
s = s.replace('''    /**
     * Inserts as much of {@code stacks} as {@code handler} will take, and returns the rest.''',
'''    /**
     * Whether every fluid in {@code stacks} fits across {@code handlers}, spilling as needed.
     *
     * <p>Simulated rather than performed: each tank is asked what it would accept, and the accepted
     * amount is deducted before the next tank is tried. The order matches delivery, so a craft that
     * passes here really does fit.</p>
     */
    private static boolean fitsFluids(List<IFluidHandler> handlers, List<FluidStack> stacks) {
        List<FluidStack> remaining = new ArrayList<>();
        for (FluidStack stack : stacks) {
            if (!stack.isEmpty()) {
                remaining.add(stack.copy());
            }
        }
        for (IFluidHandler handler : handlers) {
            if (remaining.isEmpty()) {
                break;
            }
            List<FluidStack> next = new ArrayList<>();
            for (FluidStack stack : remaining) {
                FluidStack rest = stack.copy();
                for (int tank = 0; tank < handler.getTanks() && !rest.isEmpty(); tank++) {
                    int accepted = handler.fill(rest,
                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
                    if (accepted <= 0) {
                        continue;
                    }
                    rest = rest.copyWithAmount(rest.getAmount() - accepted);
                }
                if (!rest.isEmpty()) {
                    next.add(rest);
                }
            }
            remaining = next;
        }
        return remaining.isEmpty();
    }

    /**
     * Inserts as much of {@code stacks} as {@code handler} will take, and returns the rest.''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
