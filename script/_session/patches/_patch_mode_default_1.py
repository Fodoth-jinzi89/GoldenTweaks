import io, json

# ---------------- 1. default depends on the port's direction ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * Whether each stored entry occupies one cell.
     *
     * <p>On: an entry is shown in a single cell with its count beside it, the familiar item-grid
     * look. Off: an entry may span as many cells as it needs, so a stack of thousands reads as a
     * filled bar rather than a number. Defaults to on, which is what the grid was built for.</p>
     */
    private boolean singleCell = true;''',
'''    /**
     * Whether each kind of content is confined to its own share of the port.
     *
     * <p>The two directions want opposite defaults, because they solve opposite problems.</p>
     *
     * <p>An <b>input</b> port is fed by pipes that may deliver several things at unequal rates. It
     * has to keep room for each of them, or the faster one fills the whole port and the slower one
     * never gets in — so inputs start in single-cell mode, one share per kind.</p>
     *
     * <p>An <b>output</b> port receives whatever a finished craft produces, usually one kind at a
     * time, and wants the largest possible buffer for it — so outputs start in multi-cell mode.</p>
     */
    private boolean singleCell;''', 1)

s = s.replace('''        switch (kind()) {
            case ITEM -> {''',
'''        if (this.builtTier == -1) {
            // First build: the direction decides the resting mode. Later rebuilds keep whatever the
            // player chose, which is why this is only read when nothing has been built yet.
            this.singleCell = mode() == PortMode.INPUT;
        }

        switch (kind()) {
            case ITEM -> {''', 1)

# `builtTier` is already assigned just above, so capture the first-build case before that.
s = s.replace('''        this.builtTier = tierIndex;

        if (this.builtTier == -1) {
            // First build: the direction decides the resting mode. Later rebuilds keep whatever the
            // player chose, which is why this is only read when nothing has been built yet.
            this.singleCell = mode() == PortMode.INPUT;
        }

''',
'''        boolean firstBuild = this.builtTier == -1;
        this.builtTier = tierIndex;
        if (firstBuild) {
            // The direction decides the resting mode. Only on the first build: a later rebuild is a
            // tier change, and the player's own choice must survive it.
            this.singleCell = mode() == PortMode.INPUT;
        }

''', 1)

# load order: ensureHandler runs before the tag is read, so restore the stored value after it
s = s.replace('''        // Absent means "on": ports saved before this switch existed showed one cell per entry.
        this.singleCell = !tag.contains(TAG_SINGLE_CELL) || tag.getBoolean(TAG_SINGLE_CELL);''',
'''        // A saved value always wins over the direction default: the port was already configured.
        this.singleCell = tag.contains(TAG_SINGLE_CELL)
                ? tag.getBoolean(TAG_SINGLE_CELL)
                : mode() == PortMode.INPUT;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity:', 'changed' if s != orig else 'NO CHANGE')
