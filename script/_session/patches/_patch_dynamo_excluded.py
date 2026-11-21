import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- channelGroups: a dynamo hatch must not invent a group of its own ----
s = s.replace('''        TreeSet<Integer> seen = new TreeSet<>();
        boolean anyWildcard = false;
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            for (int channel : channelsOf(port)) {''',
'''        TreeSet<Integer> seen = new TreeSet<>();
        boolean anyWildcard = false;
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            // Skipped for the same reason it is skipped in portAllowed: a dynamo belonging to
            // channel 3 would otherwise put a group 3 on the list that has nothing in it.
            if (isDynamo(port)) {
                continue;
            }
            for (int channel : channelsOf(port)) {''', 1)

# ---- port signature: likewise, it is not part of any group ----
s = s.replace('''    private String portSignatureFor(int channel) {
        StringBuilder out = new StringBuilder();
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (portJoins(port, channel)) {''',
'''    private String portSignatureFor(int channel) {
        StringBuilder out = new StringBuilder();
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!isDynamo(port) && portJoins(port, channel)) {''', 1)

# ---- the shared predicate ----
s = s.replace('''    private boolean portAllowed(MultiblockPortBinding port) {
        // A dynamo hatch is a legal part of the shape but never part of this machine. Only energy
        // *input* hatches feed a reaction; a dynamo sits in an "any" cell as a structural filler and
        // is left out of every port query, so it neither supplies power nor receives the products.
        if (port.type() == MultiblockPortTypes.ENERGY && !port.isInput()) {
            return false;
        }''',
'''    private boolean portAllowed(MultiblockPortBinding port) {
        if (isDynamo(port)) {
            return false;
        }''', 1)

s = s.replace('''    /**
     * Whether a port belongs to {@code channel}'s group. A {@code null} channel means "all".''',
'''    /**
     * Whether a port is a dynamo hatch — a legal part of the shape that this machine never uses.
     *
     * <p>Only energy <em>input</em> hatches power a reaction. A dynamo may still be placed, because
     * the pattern's {@code any} cell accepts one, but it is not part of the machine: it neither
     * supplies energy nor receives products, and it must not claim a channel group of its own.
     * Every port query starts here, so one predicate covers all of them.</p>
     */
    private static boolean isDynamo(MultiblockPortBinding port) {
        return port.type() == MultiblockPortTypes.ENERGY && !port.isInput();
    }

    /**
     * Whether a port belongs to {@code channel}'s group. A {@code null} channel means "all".''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('isDynamo refs:', s.count('isDynamo'))
