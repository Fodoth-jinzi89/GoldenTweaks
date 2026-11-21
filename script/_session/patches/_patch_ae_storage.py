import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

old = '''                    /**
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
'''

new = '''                    /**
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

                    /**
                     * Takes as much as was asked for, not one vanilla stack of it.
                     *
                     * <p>The base implementation clips the request to the item's own stack size, so a
                     * slot holding 400 pickaxes would hand them out one at a time however large the
                     * amount asked for — a hopper pulling from the port would appear to swallow far
                     * less than it was given. The cell's own ceiling is the only limit here.</p>
                     */
                    @Override
                    public ItemStack extractItem(int slot, int amount, boolean simulate) {
                        if (amount <= 0) {
                            return ItemStack.EMPTY;
                        }
                        ItemStack existing = getStackInSlot(slot);
                        if (existing.isEmpty()) {
                            return ItemStack.EMPTY;
                        }
                        int taken = Math.min(amount, existing.getCount());
                        if (!simulate) {
                            ItemStack left = existing.copyWithCount(existing.getCount() - taken);
                            setStackInSlot(slot, left);
                        }
                        return existing.copyWithCount(taken);
                    }

                    /**
                     * Saves counts as plain ints rather than through {@link ItemStack#save}.
                     *
                     * <p>That path runs the stack through {@code ItemStack.CODEC}, whose count field is
                     * {@code intRange(1, 99)} — a cell holding 576 would be written back as 99 and the
                     * rest silently lost on every save. Keeping the count beside the stack is what lets
                     * a cell hold more than a vanilla inventory ever could, and survive the trip.</p>
                     */
                    @Override
                    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
                        CompoundTag tag = new CompoundTag();
                        ListTag list = new ListTag();
                        for (int slot = 0; slot < getSlots(); slot++) {
                            ItemStack stack = getStackInSlot(slot);
                            if (stack.isEmpty()) {
                                continue;
                            }
                            CompoundTag entry = new CompoundTag();
                            entry.putInt("Slot", slot);
                            entry.putInt("Count", stack.getCount());
                            entry.put("Stack", stack.copyWithCount(1).save(registries));
                            list.add(entry);
                        }
                        tag.put("Items", list);
                        return tag;
                    }

                    @Override
                    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
                        for (int slot = 0; slot < getSlots(); slot++) {
                            setStackInSlot(slot, ItemStack.EMPTY);
                        }
                        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
                        for (int index = 0; index < list.size(); index++) {
                            CompoundTag entry = list.getCompound(index);
                            int slot = entry.getInt("Slot");
                            if (slot < 0 || slot >= getSlots()) {
                                continue;
                            }
                            ItemStack stack = ItemStack.parseOptional(registries, entry.getCompound("Stack"));
                            if (stack.isEmpty()) {
                                continue;
                            }
                            // Clamped to the cell, not to the item: an older save may predate a tier
                            // change, and the amount still has to fit what this port can hold.
                            int count = Math.min(entry.getInt("Count"), limit);
                            setStackInSlot(slot, stack.copyWithCount(Math.max(1, count)));
                        }
                    }
'''

assert old in s, "anchor missing"
s = s.replace(old, new, 1)

s = s.replace('''import net.minecraft.nbt.CompoundTag;''',
'''import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
