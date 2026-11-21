import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''                ItemStackHandler replacement = new ItemStackHandler(slots) {
                    @Override
                    public int getSlotLimit(int slot) {
                        return limit;
                    }

                    /**
                     * Enforces the stacking mode on the way in.
                     *
                     * <p>Single-cell mode is not a drawing preference: an entry may not outgrow one
                     * cell, so a type already present in one slot refuses every other slot. That caps
                     * a type at one slot's worth — the whole point of the mode. Multi-cell mode lets
                     * the same type fill as many slots as it needs, up to the grid's own size.</p>
                     */
                    @Override
                    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                        if (singleCell && !stack.isEmpty() && holdsTypeElsewhere(slot, stack)) {
                            return stack;
                        }
                        return super.insertItem(slot, stack, simulate);
                    }

                    @Override
                    protected void onContentsChanged(int slot) {
                        GTPortBlockEntity.this.onContentChanged();
                    }
                };''',
'''                ItemStackHandler replacement = new ItemStackHandler(slots) {
                    /**
                     * The per-slot ceiling, widened in multi-cell mode.
                     *
                     * <p>Single-cell mode keeps a slot at {@code limit}: one entry, one cell's worth.
                     * Multi-cell mode lets a slot hold the whole port, because that is what lets one
                     * type span cells — the type accumulates here and the grid draws the overflow.</p>
                     */
                    @Override
                    public int getSlotLimit(int slot) {
                        return singleCell ? limit : limit * slots;
                    }

                    /**
                     * Enforces the stacking mode on the way in.
                     *
                     * <p>Not a drawing preference: in single-cell mode an entry may not outgrow one
                     * cell, so a type already present in a slot refuses every other slot, capping it
                     * at one slot's worth. Multi-cell mode allows the spread, which is the mode's
                     * whole purpose.</p>
                     */
                    @Override
                    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                        if (singleCell && !stack.isEmpty() && holdsTypeElsewhere(slot, stack)) {
                            return stack;
                        }
                        return super.insertItem(slot, stack, simulate);
                    }

                    @Override
                    protected void onContentsChanged(int slot) {
                        GTPortBlockEntity.this.onContentChanged();
                    }
                };''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
