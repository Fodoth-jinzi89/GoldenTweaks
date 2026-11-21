import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''                    /**
                     * The per-slot ceiling, widened in multi-cell mode.
                     *
                     * <p>Single-cell mode keeps a slot at {@code limit}: one entry, one cell's worth.
                     * Multi-cell mode lets a slot hold the whole port, because that is what lets one
                     * type span cells — the type accumulates here and the grid draws the overflow.</p>
                     */
                    @Override
                    public int getSlotLimit(int slot) {
                        return singleCell ? limit : limit * slots;
                    }''',
'''                    /** One cell's worth. The mode never changes this: it changes how many cells a
                     * type may spread across, not how much a single one holds. */
                    @Override
                    public int getSlotLimit(int slot) {
                        return limit;
                    }''', 1)

s = s.replace('''                     * <p>Not a drawing preference: in single-cell mode an entry may not outgrow one
                     * cell, so a type already present in a slot refuses every other slot, capping it
                     * at one slot's worth. Multi-cell mode allows the spread, which is the mode's
                     * whole purpose.</p>''',
'''                     * <p>Not a drawing preference. Multi-cell mode is simply a bigger chest: the
                     * same type may fill several slots, so it holds {@code slots × limit} in all —
                     * each slot still holds only its own worth. Single-cell mode confines a type to
                     * the one slot it landed in, so it holds {@code limit} and no more.</p>''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
