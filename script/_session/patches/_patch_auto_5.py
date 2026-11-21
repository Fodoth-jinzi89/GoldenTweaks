import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- throttle by tier ----
s = s.replace('''    public void serverTick() {
        if (!this.active || this.level == null || this.level.isClientSide) {
            return;
        }
        Direction front = getBlockState().getValue(GTPortBlock.FACING);''',
'''    public void serverTick() {
        if (!this.active || this.level == null || this.level.isClientSide) {
            return;
        }
        // Each tier moves a step every so many ticks: the cheap ports are slow on purpose, so a ULV
        // hatch cannot empty a chest as fast as a UV one.
        if (--this.reachCountdown > 0) {
            return;
        }
        this.reachCountdown = portTier().reachInterval();

        Direction front = getBlockState().getValue(GTPortBlock.FACING);''', 1)

# ---- per-type amounts come from the tier ----
s = s.replace('''        boolean input = mode() == PortMode.INPUT;
        IItemHandler from = input ? theirs : ours;
        IItemHandler to = input ? ours : theirs;
        for (int slot = 0; slot < from.getSlots(); slot++) {
            ItemStack stack = from.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack leftover = insertInto(to, stack);
            if (leftover.getCount() == stack.getCount()) {
                continue;
            }
            from.extractItem(slot, stack.getCount() - leftover.getCount(), false);
            return;
        }
    }''',
'''        int budget = portTier().reachItems();
        boolean input = mode() == PortMode.INPUT;
        IItemHandler from = input ? theirs : ours;
        IItemHandler to = input ? ours : theirs;
        for (int slot = 0; slot < from.getSlots() && budget > 0; slot++) {
            ItemStack stack = from.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack offer = stack.copyWithCount(Math.min(stack.getCount(), budget));
            ItemStack leftover = insertInto(to, offer);
            int moved = offer.getCount() - leftover.getCount();
            if (moved <= 0) {
                continue;
            }
            from.extractItem(slot, moved, false);
            budget -= moved;
        }
    }''', 1)

s = s.replace('''            // One tank's worth per tick at most, so a huge neighbour tank does not empty itself into
            // this hatch in one go.
            FluidStack offer = held.copyWithAmount(Math.min(held.getAmount(), 8000));''',
'''            // At most this tier's step, so a huge neighbour tank does not empty into this hatch.
            FluidStack offer = held.copyWithAmount(Math.min(held.getAmount(), portTier().reachFluid()));''', 1)

s = s.replace('''        int rate = this.energy.getMaxEnergyStored() <= 0 ? 0 : inputRate();''',
'''        // An energy step is one second's worth at this tier, delivered in one go.
        int rate = portTier().reachInterval() <= 0 ? 0 : inputRate();''', 1)

# ---- the countdown field ----
s = s.replace('''    private boolean active;''',
'''    private boolean active;

    /**
     * Ticks left before the next reach-through step.
     *
     * <p>Per tier, not per tick: a cheap port moves one item a second at best. Kept as a field so a
     * slow port still fires on a steady cadence rather than whenever it happens to be ticked.</p>
     */
    private int reachCountdown = 1;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
