import io, shutil

# ---------------- 1. texture ----------------
src = r'multiblock\gt_port_io.png'
dst = r'src/main/resources/assets/goldentweaks/textures/gui/gt_port_io.png'
shutil.copyfile(src, dst)
print('texture updated', dst)

# ---------------- 2. block entity: the auto flag ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** Stacking mode: one cell per entry, or as many as it needs. */
    private static final String TAG_SINGLE_CELL = "SingleCell";''',
'''    /** Stacking mode: one cell per entry, or as many as it needs. */
    private static final String TAG_SINGLE_CELL = "SingleCell";
    /** Whether the port also pulls from / pushes to the block it faces. */
    private static final String TAG_ACTIVE = "Active";''', 1)

s = s.replace('''    private boolean singleCell;''',
'''    private boolean singleCell;

    /**
     * Whether this port reaches through its own face instead of waiting to be reached.
     *
     * <p>Passive is the plain behaviour: the port offers a capability and the machine, or a pipe,
     * comes to it. Active adds one extra step each tick — an input pulls from whatever block it
     * faces, an output pushes into it. That is what lets a hatch sit against a chest or a tank
     * without a pipe in between.</p>
     *
     * <p>Defaults follow the direction, because the two want opposite things: an input is normally
     * pushed into by a belt or a pipe, while an output is expected to hand its products on. Only the
     * first build applies the default; a later tier change keeps the player's choice.</p>
     */
    private boolean active;''', 1)

s = s.replace('''        if (firstBuild) {
            // The direction decides the resting mode. Only on the first build: a later rebuild is a
            // tier change, and the player's own choice must survive it.
            this.singleCell = mode() == PortMode.INPUT;
        }''',
'''        if (firstBuild) {
            // The direction decides both resting switches. Only on the first build: a later rebuild
            // is a tier change, and the player's own choices must survive it.
            this.singleCell = mode() == PortMode.INPUT;
            this.active = mode() == PortMode.OUTPUT;
        }''', 1)

s = s.replace('''    /** Whether entries are drawn one cell each, rather than spanning as many as they need. */''',
'''    /** Whether this port reaches through its face rather than waiting to be reached. */
    public boolean isActive() {
        return this.active;
    }

    /** Flips the reach-through behaviour and tells nearby clients. */
    public void toggleActive() {
        this.active = !this.active;
        onContentChanged();
    }

    /** Whether entries are drawn one cell each, rather than spanning as many as they need. */''', 1)

# persistence
s = s.replace('''        tag.putBoolean(TAG_SINGLE_CELL, this.singleCell);''',
'''        tag.putBoolean(TAG_SINGLE_CELL, this.singleCell);
        tag.putBoolean(TAG_ACTIVE, this.active);''', 1)

s = s.replace('''        this.singleCell = tag.contains(TAG_SINGLE_CELL)
                ? tag.getBoolean(TAG_SINGLE_CELL)
                : mode() == PortMode.INPUT;''',
'''        this.singleCell = tag.contains(TAG_SINGLE_CELL)
                ? tag.getBoolean(TAG_SINGLE_CELL)
                : mode() == PortMode.INPUT;
        this.active = tag.contains(TAG_ACTIVE)
                ? tag.getBoolean(TAG_ACTIVE)
                : mode() == PortMode.OUTPUT;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- 3. packet target ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SPortTogglePacket.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''        /** Whether each entry occupies one cell or spans as many as it needs. */
        STACK_MODE''',
'''        /** Whether each entry occupies one cell or spans as many as it needs. */
        STACK_MODE,
        /** Whether the port also reaches through its own face. */
        ACTIVE''', 1)
s2 = s2.replace('''                    case STACK_MODE -> port.toggleSingleCell();''',
'''                    case STACK_MODE -> port.toggleSingleCell();
                    case ACTIVE -> port.toggleActive();''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('packet:', 'changed' if s2 != orig2 else 'NO CHANGE')
