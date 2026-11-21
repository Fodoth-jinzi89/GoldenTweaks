import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- the handler enforces the mode on insertion, not just on drawing ----
s = s.replace('''                int slots = portTier().itemSlots();
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
                };''',
'''                int slots = portTier().itemSlots();
                int limit = portTier().slotLimit();
                ItemStackHandler replacement = new ItemStackHandler(slots) {
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
                };''', 1)

# ---- the helper used above ----
s = s.replace('''    /** The item handler for an item port, or {@code null} for any other kind. */''',
'''    /**
     * Whether this handler already holds {@code stack}'s type in a slot other than {@code slot}.
     *
     * <p>Only meaningful in single-cell mode, where one type is confined to one cell. Counts as a
     * match only when the other slot holds the same item — a partially filled slot still counts, so
     * the type cannot creep into a second cell as the first one fills.</p>
     */
    private boolean holdsTypeElsewhere(int slot, ItemStack stack) {
        if (!(this.items instanceof ItemStackHandler handler)) {
            return false;
        }
        for (int index = 0; index < handler.getSlots(); index++) {
            if (index == slot) {
                continue;
            }
            ItemStack other = handler.getStackInSlot(index);
            if (!other.isEmpty() && ItemStack.isSameItemSameComponents(other, stack)) {
                return true;
            }
        }
        return false;
    }

    /** The item handler for an item port, or {@code null} for any other kind. */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
