import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- the second, AMI-side capacity check: same merged simulation ----
s = s.replace('''        for (MultiblockPortBinding port : sortedPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler
                        && !MultiblockRecipeMatcher.canInsertItems(outputs, handler)) {
                    return false;
                }
            } else if (port.type() == MultiblockPortTypes.FLUID) {
                Object capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
                if (capability instanceof IFluidHandler handler
                        && !MultiblockRecipeMatcher.canInsertFluids(fluidOutputs, handler)) {
                    return false;
                }
            }
        }
        return true;
    }''',
'''        List<IItemHandler> itemHandlers = new ArrayList<>();
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
        // Offered together, so a product may land in whichever hatch has room for it. Asking per
        // hatch would refuse a craft the hatches can plainly hold between them.
        return MultiblockRecipeMatcher.canInsertItems(outputs, itemHandlers)
                && MultiblockRecipeMatcher.canInsertFluids(fluidOutputs, fluidHandlers);
    }''', 1)

# ---- delivery must spread across hatches the same way the check assumed ----
s = s.replace('''        for (MultiblockPortBinding port : sortedPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler) {
                    MultiblockRecipeMatcher.insertItems(items, handler);
                }
            } else if (port.type() == MultiblockPortTypes.FLUID) {
                Object capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
                if (capability instanceof IFluidHandler handler) {
                    MultiblockRecipeMatcher.insertFluids(fluids, handler);
                }
            }
        }''',
'''        // The same shape as the capacity check: every hatch is offered the whole list, so a product
        // spills over into the next one instead of loading up the first and dropping the rest. The
        // check and the delivery have to agree here, or a craft that passed would still lose output.
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
        MultiblockRecipeMatcher.insertItems(items, itemHandlers);
        MultiblockRecipeMatcher.insertFluids(fluids, fluidHandlers);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
