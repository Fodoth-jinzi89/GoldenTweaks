import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** Whether this port reaches through its face rather than waiting to be reached. */''',
'''    /**
     * One server tick: the reach-through step, when this port is active.
     *
     * <p>Passive ports do nothing here — that is the whole difference between the two settings. An
     * active one looks at the block in front of its face and moves contents across one slot per
     * boundary per tick, which is the rate a pipe would have managed and keeps a full chest from
     * being drained in a single tick.</p>
     *
     * <p>{@code null} now means something different from what it means to the capability: the
     * capability treats {@code null} as "the machine asking", while this asks the world, so the
     * front face is always resolved explicitly.</p>
     */
    public void serverTick() {
        if (!this.active || this.level == null || this.level.isClientSide) {
            return;
        }
        Direction front = getBlockState().getValue(GTPortBlock.FACING);
        BlockPos target = this.worldPosition.relative(front);
        if (!this.level.isLoaded(target)) {
            return;
        }

        switch (kind()) {
            case ITEM -> reachItems(target, front);
            case FLUID -> reachFluids(target, front);
            case ENERGY -> reachEnergy(target, front);
        }
    }

    /**
     * Moves one slot of items across the front face.
     *
     * <p>An input pulls, an output pushes. Both sides are resolved from the world rather than from
     * this port's own handlers, so the far side is whatever block happens to be there.</p>
     */
    private void reachItems(BlockPos target, Direction front) {
        IItemHandler ours = itemHandler();
        if (ours == null) {
            return;
        }
        IItemHandler theirs = this.level.getCapability(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, target, front.getOpposite());
        if (theirs == null) {
            return;
        }

        boolean input = mode() == PortMode.INPUT;
        IItemHandler from = input ? theirs : ours;
        IItemHandler to = input ? ours : theirs;
        for (int slot = 0; slot < from.getSlots(); slot++) {
            ItemStack stack = from.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack leftover = to instanceof net.neoforged.neoforge.items.wrapper.RangedWrapper
                    ? stack : insertInto(to, stack);
            if (leftover.getCount() == stack.getCount()) {
                continue;
            }
            from.extractItem(slot, stack.getCount() - leftover.getCount(), false);
            return;
        }
    }

    /**
     * Offers {@code stack} to a handler and returns what could not be taken.
     *
     * <p>Inserted one slot at a time so a partly filled destination keeps the remainder, which is
     * the amount that has to stay behind in the source.</p>
     */
    private static ItemStack insertInto(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    /** Fluid counterpart of {@link #reachItems}. */
    private void reachFluids(BlockPos target, Direction front) {
        IFluidHandler ours = this.fluids;
        if (ours == null) {
            return;
        }
        IFluidHandler theirs = this.level.getCapability(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, target,
                front.getOpposite());
        if (theirs == null) {
            return;
        }

        IFluidHandler from = mode() == PortMode.INPUT ? theirs : ours;
        IFluidHandler to = mode() == PortMode.INPUT ? ours : theirs;

        for (int tank = 0; tank < from.getTanks(); tank++) {
            FluidStack held = from.getFluidInTank(tank);
            if (held.isEmpty()) {
                continue;
            }
            // One tank's worth per tick at most, so a huge neighbour tank does not empty itself into
            // this hatch in one go.
            FluidStack offer = held.copyWithAmount(Math.min(held.getAmount(), 8000));
            int taken = to.fill(offer, IFluidHandler.FluidAction.EXECUTE);
            if (taken <= 0) {
                continue;
            }
            from.drain(offer.copyWithAmount(taken), IFluidHandler.FluidAction.EXECUTE);
            return;
        }
    }

    /** Energy counterpart of {@link #reachItems}: a fixed slice per tick, in either direction. */
    private void reachEnergy(BlockPos target, Direction front) {
        if (this.energy == null) {
            return;
        }
        var capability = net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK;
        net.neoforged.neoforge.energy.IEnergyStorage theirs =
                this.level.getCapability(capability, target, front.getOpposite());
        if (theirs == null) {
            return;
        }
        int rate = this.energy.getMaxEnergyStored() <= 0 ? 0 : inputRate();
        if (rate <= 0) {
            return;
        }

        if (mode() == PortMode.INPUT) {
            // Pull: ask the neighbour for a slice, offer it here.
            int wanted = theirs.extractEnergy(rate, true);
            if (wanted <= 0) {
                return;
            }
            int accepted = this.energy.receiveEnergy(wanted, false);
            if (accepted > 0) {
                theirs.extractEnergy(accepted, false);
            }
        } else {
            int wanted = this.energy.extractEnergy(rate, true);
            if (wanted <= 0) {
                return;
            }
            int accepted = theirs.receiveEnergy(wanted, false);
            if (accepted > 0) {
                this.energy.extractEnergy(accepted, false);
            }
        }
    }

    /** Whether this port reaches through its face rather than waiting to be reached. */''', 1)

# imports
for imp in ['import net.minecraft.world.item.ItemStack;',
            'import net.neoforged.neoforge.fluids.FluidStack;']:
    if imp not in s:
        anchor = 'import net.minecraft.nbt.CompoundTag;'
        s = s.replace(anchor, imp + '\n' + anchor, 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
