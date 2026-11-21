import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------------- dismantle: pool every port's contents into two clusters ----------------
s = s.replace('''            parts.add(stack);
        }
    }''',
'''            parts.add(stack);
        }
    }

    /**
     * Empties every port into two clusters: one for items, one for fluids.
     *
     * <p>The whole machine's contents are pooled first and packed once, so a dismantle yields one
     * item cluster and one fluid cluster rather than one per hatch — which is what "a better matter
     * cluster" means to a player: the machine comes back as a couple of spheres, not a wall of
     * separate stacks.</p>
     *
     * <p>Energy is deliberately not carried: a spent hatch comes back as the hatch alone. Charge has
     * no container item to travel in, and inventing one would let a player move power around as an
     * inventory item.</p>
     */
    private void collectPortContents(List<ItemStack> items, List<FluidStack> fluids) {
        if (this.level == null || this.structure == null) {
            return;
        }
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!(this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            if (entity.itemHandler() != null) {
                IItemHandler handler = entity.itemHandler();
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        items.add(stack.copy());
                        handler.extractItem(slot, stack.getCount(), false);
                    }
                }
            }
            if (entity.fluidHandler() != null) {
                IFluidHandler handler = entity.fluidHandler();
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    FluidStack held = handler.getFluidInTank(tank);
                    if (held.isEmpty()) {
                        continue;
                    }
                    FluidStack drained = handler.drain(held,
                            IFluidHandler.FluidAction.EXECUTE);
                    if (!drained.isEmpty()) {
                        fluids.add(drained);
                    }
                }
            }
        }
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('machine:', 'changed' if s != orig else 'NO CHANGE')
