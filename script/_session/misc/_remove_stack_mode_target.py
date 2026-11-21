import io

# ---------------- the packet target is now unused ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SPortTogglePacket.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''        /** Whether each entry occupies one cell or spans as many as it needs. */
        STACK_MODE,
''', '')
s = s.replace('''                    case STACK_MODE -> port.toggleSingleCell();
''', '')
s = s.replace('''    /** Flips the stacking mode and tells nearby clients. */
    public void toggleSingleCell() {
        this.singleCell = !this.singleCell;
        onContentChanged();
    }

''', '')
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('packet:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- the entity setter ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''    /** Flips the stacking mode and tells nearby clients. */
    public void toggleSingleCell() {
        this.singleCell = !this.singleCell;
        onContentChanged();
    }

''', '')
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('entity:', 'changed' if s2 != orig2 else 'NO CHANGE')
