import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- 1. restore the two locals the first check uses ----
s = s.replace('''        // Every output handler is offered the whole product list at once, rather than each being
        // asked on its own. That is what makes the spill-over work: a product that does not fit the
        // first hatch is tried against the next. Asking per handler would refuse a craft that two
        // hatches can plainly hold between them.
        List<IItemHandler> itemHandlers = new ArrayList<>();''',
'''        List<ItemStack> items = recipe.itemOutputs();
        List<FluidStack> fluids = recipe.fluidOutputs();
        // Every output handler is offered the whole product list at once, rather than each being
        // asked on its own. That is what makes the spill-over work: a product that does not fit the
        // first hatch is tried against the next. Asking per handler would refuse a craft that two
        // hatches can plainly hold between them.
        List<IItemHandler> itemHandlers = new ArrayList<>();''', 1)

# ---- 2. delivery: loop the hatch list instead of the single-handler overload ----
s = s.replace('''        MultiblockRecipeMatcher.insertItems(items, itemHandlers);
        MultiblockRecipeMatcher.insertFluids(fluids, fluidHandlers);''',
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

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
