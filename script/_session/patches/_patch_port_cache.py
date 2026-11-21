import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# cache fields next to the shuffle source
s = s.replace('''    private Random portShuffle = new Random();''',
'''    private Random portShuffle = new Random();

    /** Ports of the group in force, in the order fixed for the current craft. */
    @Nullable
    private List<MultiblockPortBinding> cachedPorts;

    /** Group the cached order was built for. */
    @Nullable
    private Integer cachedPortGroup;

    /** Bumped whenever the cached order is dropped, so a stale group check cannot reuse it. */
    private int portGeneration;''', 1)

# Objects import
s = s.replace('import java.util.Optional;',
              'import java.util.Objects;\nimport java.util.Optional;', 1)

# drop the cache whenever a craft starts, right where the shuffle is reseeded
s = s.replace('''            this.portShuffle = new Random(level.getRandom().nextLong());
            setChannelsLocked(true);''',
'''            this.portShuffle = new Random(level.getRandom().nextLong());
            invalidatePortOrder();
            setChannelsLocked(true);''', 1)

s = s.replace('''        this.portShuffle = new Random(level.getRandom().nextLong());
        setChannelsLocked(true);
        consumeInputs(nextBatched);''',
'''        this.portShuffle = new Random(level.getRandom().nextLong());
        invalidatePortOrder();
        setChannelsLocked(true);
        consumeInputs(nextBatched);''', 1)

# and whenever the group in force changes
s = s.replace('''        this.activeChannel = this.selectedChannel;
            // A fresh shuffle''',
'''        this.activeChannel = this.selectedChannel;
            // A fresh shuffle''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
