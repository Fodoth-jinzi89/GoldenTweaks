import io

# ---------------- 1. a per-kind cell size ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    public int slotLimit() {
        return portTier().slotLimit();
    }''',
'''    public int slotLimit() {
        return portTier().slotLimit();
    }

    /**
     * How much one cell of this port holds, in whatever unit the port deals in.
     *
     * <p>Items and fluids are measured differently — a slot is a stack ceiling, a cell is a tank —
     * so the same number cannot describe both. Reading it from the port keeps the grid's arithmetic
     * in one place instead of assuming the item figure everywhere.</p>
     */
    public int cellSize() {
        return switch (kind()) {
            case ITEM -> portTier().slotLimit();
            case FLUID -> portTier().tankCapacity();
            case ENERGY -> Integer.MAX_VALUE;
        };
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- 2. the menu passes the right one through ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortMenu.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''    public int slotLimit() {
        return this.port == null ? 64 : this.port.slotLimit();
    }''',
'''    public int slotLimit() {
        return this.port == null ? 64 : this.port.cellSize();
    }''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('menu:', 'changed' if s2 != orig2 else 'NO CHANGE')
