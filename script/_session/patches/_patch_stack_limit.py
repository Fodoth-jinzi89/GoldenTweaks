import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''                    /** One cell's worth. The mode changes how many cells a type may spread across,
                     * never how much a single cell holds. */
                    @Override
                    public int getSlotLimit(int slot) {
                        return limit;
                    }
''',
'''                    /** One cell's worth. The mode changes how many cells a type may spread across,
                     * never how much a single cell holds. */
                    @Override
                    public int getSlotLimit(int slot) {
                        return limit;
                    }

                    /**
                     * The cell ceiling, ignoring what the item itself can stack to.
                     *
                     * <p>{@link ItemStackHandler} otherwise takes the smaller of this and the item's
                     * own {@code getMaxStackSize()}, which caps a slot at one for anything vanilla
                     * keeps unstackable — a single pickaxe or bucket per cell, however large the cell
                     * is. That is a vanilla inventory rule, not a storage one: AE's cells count items
                     * and pay no attention to it, and this is the same kind of buffer.</p>
                     */
                    @Override
                    protected int getStackLimit(int slot, ItemStack stack) {
                        return limit;
                    }
''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('stack limit:', 'changed' if s != orig else 'NO CHANGE')
print('getStackLimit overrides:', s.count('protected int getStackLimit'))
