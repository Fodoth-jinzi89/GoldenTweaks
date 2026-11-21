import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# Undo the speculative helpers and use the handlers' own answers instead.
start = s.index('''    /**
     * Whether an item handler can give each of {@code kinds} item types a slot of its own.''')
end = s.index('''    /**
     * Hands out the products of a finished recipe.''')
s = s[:start] + '''    /**
     * Whether the output handlers can give each product kind a place of its own.
     *
     * <p>Only a single-cell output needs this: it confines one kind to one slot, so a recipe with
     * more kinds than the hatch has slots would deliver the surplus nowhere. A multi-cell output
     * spreads a kind across the whole hatch, so it never runs out of places — only of room, which
     * the amount checks already cover.</p>
     *
     * <p>Counted rather than simulated: AMI's simulate-based checks only ask about amounts, and a
     * simulation would happily report room for three kinds in two slots because each partial
     * insert consumes a different slot.</p>
     */
    private boolean hasPlacePerKind(List<ItemStack> items, List<FluidStack> fluids) {
        long itemKinds = items.stream().filter(stack -> !stack.isEmpty())
                .map(ItemStack::getItem).distinct().count();
        long fluidKinds = fluids.stream().filter(stack -> !stack.isEmpty())
                .map(FluidStack::getFluid).distinct().count();
        if (itemKinds <= 0 && fluidKinds <= 0) {
            return true;
        }

        long itemPlaces = 0;
        long fluidPlaces = 0;
        for (MultiblockPortBinding port : sortedPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler
                        && handler instanceof GTPortItemHandler ours && ours.singleCellMode()) {
                    itemPlaces += ours.freeSlots();
                }
            } else if (port.type() == MultiblockPortTypes.FLUID) {
                Object capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());
                if (capability instanceof GTPortFluidStorage ours && ours.isSingleCellMode()) {
                    fluidPlaces += ours.emptyTanks();
                }
            }
        }
        // A kind already present still needs its own place only if it has none; the ports report
        // free places, and a type already stored occupies one of the used ones. Comparing counts
        // therefore errs on the side of refusing, which is the safe direction here.
        return itemPlaces >= itemKinds && fluidPlaces >= fluidKinds;
    }

''' + s[end:]

s = s.replace('''                    if (!hasSlotPerKind(handler, items.stream()
                            .filter(stack -> !stack.isEmpty())
                            .map(ItemStack::getItem)
                            .distinct().count())) {
                        return false;
                    }''', '', 1)
s = s.replace('''                    if (!hasTankPerKind(handler, fluids.stream()
                            .filter(stack -> !stack.isEmpty())
                            .map(FluidStack::getFluid)
                            .distinct().count())) {
                        return false;
                    }''', '', 1)

# one call for both sides, after the per-port amount loop
s = s.replace('''                if (capability instanceof IFluidHandler handler
                        && !MultiblockRecipeMatcher.canInsertFluids(fluids, handler)) {
                    return false;
                }
            }
        }
        return true;
    }''',
'''                if (capability instanceof IFluidHandler handler
                        && !MultiblockRecipeMatcher.canInsertFluids(fluids, handler)) {
                    return false;
                }
            }
        }
        return hasPlacePerKind(items, fluids);
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
