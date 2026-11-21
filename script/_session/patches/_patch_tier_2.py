import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- resolve the tier from the block, not from the state ----
s = s.replace('''    /** Rebuilds the handler if the state's tier no longer matches the one it was built for. */
    private void ensureHandler() {
        int tierIndex = tier().ordinal() + 1;
        if (tierIndex == this.builtTier) {
            return;
        }

        this.builtTier = tierIndex;

        switch (kind()) {
            case ITEM -> {
                int slots = MultiblockPortTypes.ITEM_SLOTS_BY_TIER.get(tier());
                ItemStackHandler replacement = new ItemStackHandler(slots) {
                    @Override
                    protected void onContentsChanged(int slot) {
                        GTPortBlockEntity.this.onContentChanged();
                    }
                };
                this.items = replacement;
                this.fluids = null;
                this.energy = null;
            }
            case FLUID -> {
                int capacity = MultiblockPortTypes.FLUID_CAPACITY_BY_TIER.get(tier());
                MultiblockFluidBuffer replacement = new MultiblockFluidBuffer(1, capacity);
                replacement.setChangedHook(this::onContentChanged);
                this.fluids = replacement;
                this.items = null;
                this.energy = null;
            }''',
'''    /** This port's tier, read from its block, which was registered at that tier. */
    public GTPortTier portTier() {
        return getBlockState().getBlock() instanceof GTPortBlock port
                ? port.portTier() : GTPortTier.byLevel(1);
    }

    /** The AMI tier of the same rank, which the structure scan compares against. */
    public MultiblockPortTier tier() {
        return portTier().ami();
    }

    /** Rebuilds the handler if the tier no longer matches the one it was built for. */
    private void ensureHandler() {
        int tierIndex = portTier().level();
        if (tierIndex == this.builtTier) {
            return;
        }

        this.builtTier = tierIndex;

        switch (kind()) {
            case ITEM -> {
                // Slots scale with the level squared, and each slot's ceiling with the level, so a
                // tier IX bus holds 81 stacks of up to 576 rather than 9 of 64.
                int slots = portTier().itemSlots();
                int limit = portTier().slotLimit();
                ItemStackHandler replacement = new ItemStackHandler(slots) {
                    @Override
                    public int getSlotLimit(int slot) {
                        return limit;
                    }

                    @Override
                    protected void onContentsChanged(int slot) {
                        GTPortBlockEntity.this.onContentChanged();
                    }
                };
                this.items = replacement;
                this.fluids = null;
                this.energy = null;
            }
            case FLUID -> {
                // One tank per level, each holding 8000 mB per level.
                int tanks = portTier().fluidTanks();
                int capacity = portTier().tankCapacity();
                MultiblockFluidBuffer replacement = new MultiblockFluidBuffer(tanks, capacity);
                replacement.setChangedHook(this::onContentChanged);
                this.fluids = replacement;
                this.items = null;
                this.energy = null;
            }''', 1)

# ---- capacity() follows the same rules ----
s = s.replace('''    /** How much this port can hold, in items for item ports and mB for fluid ports. */
    public int capacity() {
        return switch (kind()) {
            case ITEM -> {
                int slots = MultiblockPortTypes.ITEM_SLOTS_BY_TIER.get(tier());
                yield slots * 64;
            }
            case FLUID -> MultiblockPortTypes.FLUID_CAPACITY_BY_TIER.get(tier());
            case ENERGY -> GTPortEnergyTiers.capacity(tier());
        };
    }''',
'''    /** How much this port can hold: items across all slots, or mB across all tanks. */
    public int capacity() {
        return switch (kind()) {
            case ITEM -> portTier().itemSlots() * portTier().slotLimit();
            case FLUID -> portTier().fluidTanks() * portTier().tankCapacity();
            case ENERGY -> GTPortEnergyTiers.capacity(tier());
        };
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity:', 'changed' if s != orig else 'NO CHANGE')
