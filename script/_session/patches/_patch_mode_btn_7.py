import io

# ---------- port: expose the per-slot ceiling ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''    /** Whether entries are drawn one cell each, rather than spanning as many as they need. */''',
'''    /** How much one slot of this port holds, which is what an entry's cell span is measured in. */
    public int slotLimit() {
        return portTier().slotLimit();
    }

    /** Whether entries are drawn one cell each, rather than spanning as many as they need. */''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity:', 'changed' if s != orig else 'NO CHANGE')

# ---------- menu: pass it through ----------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortMenu.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''    /** Whether each entry occupies a single cell. */''',
'''    /** How much one slot holds, which is what an entry's cell span is measured in. */
    public int slotLimit() {
        return this.port == null ? 64 : this.port.slotLimit();
    }

    /** Whether each entry occupies a single cell. */''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('menu:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---------- screen: Math.ceilDiv returns int, but amount() is long ----------
p3 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s3 = io.open(p3, encoding='utf-8').read()
orig3 = s3
s3 = s3.replace('''            int span = this.menu.isSingleCellMode()
                    ? 1 : Math.max(1, Math.ceilDiv(stack.amount(), perSlot));''',
'''            // The amount is a long while the ceiling is an int, so the division is done wide and
            // then clamped: an entry can never need more cells than the grid has.
            int span = this.menu.isSingleCellMode() ? 1 : (int) Math.max(1L,
                    Math.min(GTPortMenu.STORAGE_CELLS, (stack.amount() + perSlot - 1) / perSlot));''', 1)
io.open(p3, 'w', encoding='utf-8', newline='\n').write(s3)
print('screen:', 'changed' if s3 != orig3 else 'NO CHANGE')
