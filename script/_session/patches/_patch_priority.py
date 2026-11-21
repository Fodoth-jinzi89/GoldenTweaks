import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# imports
s = s.replace('import java.util.ArrayList;',
              'import java.util.ArrayList;\nimport java.util.Collections;\nimport java.util.Comparator;', 1)
s = s.replace('import java.util.Optional;',
              'import java.util.Optional;\nimport java.util.Random;', 1)

# shuffle source field
s = s.replace('''    /** Channel text of every port as of the last tick, used to notice edits. */
    private String lastChannelSignature = "";''',
'''    /** Channel text of every port as of the last tick, used to notice edits. */
    private String lastChannelSignature = "";

    /**
     * Source of the tie-breaking shuffle among equal-priority ports.
     *
     * <p>Reseeded when a craft starts, so the order is fixed for that craft — the inputs it drew
     * and the ports it will deliver to stay the same — but differs between crafts, which is what
     * keeps one port from always being the one drained first.</p>
     */
    private Random portShuffle = new Random();''', 1)

# restore effectiveChannel, which the previous edit removed
s = s.replace('''    /** A port's priority, or zero for ports that are not ours. */
    private int priorityOfPort(MultiblockPortBinding port) {''',
'''    /**
     * The group currently in force.
     *
     * <p>A running craft outranks a search: while {@link #activeChannel} is set, every port query
     * answers for that group, so the recipe keeps drawing energy and delivering products to the
     * line it started on. Between crafts the search sets {@link #selectedChannel} group by group.
     * {@code null} means no group is in force and the whole machine is visible.</p>
     */
    @Nullable
    private Integer effectiveChannel() {
        return this.activeChannel != null ? this.activeChannel : this.selectedChannel;
    }

    /** A port's priority, or zero for ports that are not ours. */
    private int priorityOfPort(MultiblockPortBinding port) {''', 1)

# switch every port enumeration over to the sorted list
s = s.replace('''        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.ITEM || !portAllowed(port)) {
                continue;
            }''',
'''        for (MultiblockPortBinding port : sortedPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.ITEM) {
                continue;
            }''', 1)

s = s.replace('''        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.FLUID || !portAllowed(port)) {
                continue;
            }''',
'''        for (MultiblockPortBinding port : sortedPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.FLUID) {
                continue;
            }''', 1)

s = s.replace('''        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.ENERGY || !portAllowed(port)) {
                continue;
            }''',
'''        for (MultiblockPortBinding port : sortedPorts()) {
            if (!port.isInput() || port.type() != MultiblockPortTypes.ENERGY) {
                continue;
            }''', 1)

s = s.replace('''        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (port.isInput() || !portAllowed(port)) {
                continue;
            }''',
'''        for (MultiblockPortBinding port : sortedPorts()) {
            if (port.isInput()) {
                continue;
            }''')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
