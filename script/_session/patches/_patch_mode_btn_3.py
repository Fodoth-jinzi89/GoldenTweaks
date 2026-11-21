import io

# ---------------- 1. block entity: the flag ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    private static final String TAG_ENABLED = "Enabled";''',
'''    private static final String TAG_ENABLED = "Enabled";
    /** Stacking mode: one cell per entry, or as many as it needs. */
    private static final String TAG_SINGLE_CELL = "SingleCell";''', 1)

s = s.replace('''    private boolean enabled = true;''',
'''    private boolean enabled = true;

    /**
     * Whether each stored entry occupies one cell.
     *
     * <p>On: an entry is shown in a single cell with its count beside it, the familiar item-grid
     * look. Off: an entry may span as many cells as it needs, so a stack of thousands reads as a
     * filled bar rather than a number. Defaults to on, which is what the grid was built for.</p>
     */
    private boolean singleCell = true;''', 1)

s = s.replace('''    /** Whether this port currently takes part in the machine. */''',
'''    /** Whether entries are drawn one cell each, rather than spanning as many as they need. */
    public boolean isSingleCellMode() {
        return this.singleCell;
    }

    /** Flips the stacking mode and tells nearby clients. */
    public void toggleSingleCell() {
        this.singleCell = !this.singleCell;
        onContentChanged();
    }

    /** Whether this port currently takes part in the machine. */''', 1)

# persistence
s = s.replace('''        tag.putBoolean(TAG_ENABLED, this.enabled);''',
'''        tag.putBoolean(TAG_ENABLED, this.enabled);
        tag.putBoolean(TAG_SINGLE_CELL, this.singleCell);''', 1)

s = s.replace('''        this.enabled = !tag.contains(TAG_ENABLED) || tag.getBoolean(TAG_ENABLED);''',
'''        this.enabled = !tag.contains(TAG_ENABLED) || tag.getBoolean(TAG_ENABLED);
        // Absent means "on": ports saved before this switch existed showed one cell per entry.
        this.singleCell = !tag.contains(TAG_SINGLE_CELL) || tag.getBoolean(TAG_SINGLE_CELL);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- 2. packet: carry the request ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SPortTogglePacket.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''/**
 * Turns a port's participation on or off.
 *
 * <p>The block entity decides the outcome, not the client: this only carries the request, and the
 * server applies it to the port at that position. Range and existence are checked the same way as
 * any other block interaction, so a packet cannot reach a port the player could not have opened.</p>
 */
public record C2SPortTogglePacket(BlockPos pos) implements CustomPacketPayload {''',
'''/**
 * Flips one of a port's two switches.
 *
 * <p>The block entity decides the outcome, not the client: this only carries the request, and the
 * server applies it to the port at that position. Range and existence are checked the same way as
 * any other block interaction, so a packet cannot reach a port the player could not have opened.</p>
 */
public record C2SPortTogglePacket(BlockPos pos, Target target) implements CustomPacketPayload {

    /** Which switch to flip. */
    public enum Target {
        /** Whether the port takes part in the machine. */
        ENABLED,
        /** Whether each entry occupies one cell or spans as many as it needs. */
        STACK_MODE
    }''', 1)

s2 = s2.replace('''    public static final StreamCodec<FriendlyByteBuf, C2SPortTogglePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, packet) -> buffer.writeBlockPos(packet.pos()),
                    buffer -> new C2SPortTogglePacket(buffer.readBlockPos()));''',
'''    public static final StreamCodec<FriendlyByteBuf, C2SPortTogglePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, packet) -> {
                        buffer.writeBlockPos(packet.pos());
                        buffer.writeEnum(packet.target());
                    },
                    buffer -> new C2SPortTogglePacket(buffer.readBlockPos(),
                            buffer.readEnum(Target.class)));''', 1)

s2 = s2.replace('''            if (player.level().getBlockEntity(pkt.pos()) instanceof GTPortBlockEntity port) {
                port.setEnabled(!port.isEnabled());
            }''',
'''            if (player.level().getBlockEntity(pkt.pos()) instanceof GTPortBlockEntity port) {
                switch (pkt.target()) {
                    case ENABLED -> port.setEnabled(!port.isEnabled());
                    case STACK_MODE -> port.toggleSingleCell();
                }
            }''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('packet:', 'changed' if s2 != orig2 else 'NO CHANGE')
