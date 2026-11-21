import io

# ---------------- menu: expose the mode ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortMenu.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    public static final int DATA_RATE = 2;
    private static final int DATA_COUNT = 3;''',
'''    public static final int DATA_RATE = 2;
    /** One when entries are drawn one cell each, zero when they may span several. */
    public static final int DATA_SINGLE_CELL = 3;
    private static final int DATA_COUNT = 4;''', 1)

s = s.replace('''                case DATA_RATE -> GTPortMenu.this.inputRate();''',
'''                case DATA_RATE -> GTPortMenu.this.inputRate();
                case DATA_SINGLE_CELL -> GTPortMenu.this.isSingleCellMode() ? 1 : 0;''', 1)

s = s.replace('''    /** Synced input rate, in FE per tick. */''',
'''    /** Whether each entry occupies a single cell. */
    public boolean isSingleCellMode() {
        return this.port == null || this.port.isSingleCellMode();
    }

    /** Synced input rate, in FE per tick. */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('menu:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- screen: the old click path used the one-arg packet ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''                PacketDistributor.sendToServer(new C2SPortTogglePacket(port.getBlockPos()));''',
'''                PacketDistributor.sendToServer(new C2SPortTogglePacket(
                        port.getBlockPos(), C2SPortTogglePacket.Target.ENABLED));''', 1)
s2 = s2.replace('''                PacketDistributor.sendToServer(new C2SPortTogglePacket(port.getBlockPos(),
                        C2SPortTogglePacket.Target.STACK_MODE));''',
'''                PacketDistributor.sendToServer(new C2SPortTogglePacket(
                        port.getBlockPos(), C2SPortTogglePacket.Target.STACK_MODE));''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('screen:', 'changed' if s2 != orig2 else 'NO CHANGE')
