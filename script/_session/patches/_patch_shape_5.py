import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- 1. the first check lost its locals when the block was replaced ----
s = s.replace('''        // Every output handler is offered the whole product list at once, rather than each being
        // asked on its own. That is what makes the spill-over work: a product that does not fit the
        // first hatch is tried against the next. Asking per handler would refuse a craft that two
        // hatches can plainly hold between them.''',
'''        List<ItemStack> items = recipe.itemOutputs();
        List<FluidStack> fluids = recipe.fluidOutputs();
        // Every output handler is offered the whole product list at once, rather than each being
        // asked on its own. That is what makes the spill-over work: a product that does not fit the
        // first hatch is tried against the next. Asking per handler would refuse a craft that two
        // hatches can plainly hold between them.''', 1)

# ---- 2. delivery: hand the surplus to the next hatch ----
s = s.replace('''        MultiblockRecipeMatcher.insertItems(items, itemHandlers);
        MultiblockRecipeMatcher.insertFluids(fluids, fluidHandlers);''',
'''        // No list overload exists for insertion, so each hatch is filled in turn and anything it
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
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
