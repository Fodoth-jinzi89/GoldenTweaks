"""Wiring for the render-time plate.

Three pieces:
  - `GTMultiblockBlocks.casingFor` resolves a port's casing id to a block state;
  - the machine stamps that id when it forms, and clears it when the shape goes;
  - a model-loading hook wraps every port model in the re-skinning one.
"""
import io

# ---- 1. the resolver -------------------------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s = io.open(p, encoding="utf-8").read()

anchor = '''    /**
     * The structure a host block projects, or {@code null} if this state is not a multiblock host.'''
assert anchor in s

helper = '''    /**
     * The casing block state a port is set into, or {@code null} when it is not part of a machine.
     *
     * <p>Resolved from the id the port carries, so a casing this class has never heard of — one
     * added by a data pack, or a third machine's — works without any change here.</p>
     */
    @Nullable
    public static BlockState casingFor(BlockState portState) {
        return GTPortBlock.casingOf(portState, location ->
                BuiltInRegistries.BLOCK.getOptional(location)
                        .map(Block::defaultBlockState)
                        .orElse(null));
    }

'''
s = s.replace(anchor, helper + anchor, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("casing resolver added")

# ---- 2. the machine stamps the id ------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

old_stamp = '''        for (MultiblockPortBinding port : this.structure.getPorts()) {
            BlockState portState = this.level.getBlockState(port.pos());
            if (!(portState.getBlock() instanceof GTPortBlock)
                    || !portState.hasProperty(GTPortBlock.ADVANCED_CASING)) {
                continue;
            }
            if (portState.getValue(GTPortBlock.ADVANCED_CASING) == advanced) {
                continue;
            }
            this.level.setBlock(port.pos(), portState.setValue(GTPortBlock.ADVANCED_CASING, advanced),
                    Block.UPDATE_ALL);
            this.restyledPorts.add(port.pos().immutable());
        }'''
new_stamp = '''        String casingId = BuiltInRegistries.BLOCK.getKey(casingBlock()).toString();
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            BlockState portState = this.level.getBlockState(port.pos());
            if (!(portState.getBlock() instanceof GTPortBlock)
                    || !portState.hasProperty(GTPortBlock.CASING)) {
                continue;
            }
            if (casingId.equals(portState.getValue(GTPortBlock.CASING))) {
                continue;
            }
            this.level.setBlock(port.pos(),
                    portState.setValue(GTPortBlock.CASING, casingId), Block.UPDATE_ALL);
            this.restyledPorts.add(port.pos().immutable());
        }'''
assert old_stamp in s2
s2 = s2.replace(old_stamp, new_stamp, 1)

# the boolean parameter becomes unnecessary: the id says which casing
s2 = s2.replace('''     * @param advanced whether the ports should show the advanced machine's plate
     */
    private void applyCasingToPorts(boolean advanced) {''',
'''     */
    private void applyCasingToPorts() {''', 1)
s2 = s2.replace("applyCasingToPorts(usesAdvancedCasing());", "applyCasingToPorts();", 1)

# the old hook is gone with the flag
old_hook = '''    /**
     * Whether this machine is built from the advanced casing.
     *
     * <p>Asked of the same hook the structure check uses, so the port plates and the wall can never
     * disagree about which machine is standing here.</p>
     */
    protected boolean usesAdvancedCasing() {
        return false;
    }

'''
s2 = s2.replace(old_hook, "", 1)

# ---- revert ------------------------------------------------------------------------------------
old_revert = '''            if (portState.getBlock() instanceof GTPortBlock
                    && portState.hasProperty(GTPortBlock.ADVANCED_CASING)
                    && portState.getValue(GTPortBlock.ADVANCED_CASING)) {
                this.level.setBlock(pos,
                        portState.setValue(GTPortBlock.ADVANCED_CASING, false), Block.UPDATE_ALL);
            }'''
new_revert = '''            if (portState.getBlock() instanceof GTPortBlock
                    && portState.hasProperty(GTPortBlock.CASING)
                    && !portState.getValue(GTPortBlock.CASING).isEmpty()) {
                this.level.setBlock(pos, portState.setValue(GTPortBlock.CASING, ""),
                        Block.UPDATE_ALL);
            }'''
assert old_revert in s2
s2 = s2.replace(old_revert, new_revert, 1)

if "import net.minecraft.core.registries.BuiltInRegistries;" not in s2:
    lines = s2.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import net.minecraft.core"))
    lines.insert(last + 1, "import net.minecraft.core.registries.BuiltInRegistries;")
    s2 = "\n".join(lines)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("machine stamps the casing id")
