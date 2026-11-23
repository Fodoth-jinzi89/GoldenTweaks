"""Let the block carry its own tile factory, so a new machine needs no edit to shared code.

`machineTile` branched on the block id to pick an entity class. That works for two machines and
stops working at three: every new machine would have to come back and add a branch here, in the
one file all machines share.

The block now takes the factory that builds its tile, the same way it already takes its structure
id. Registration names both, and this class never learns a machine's name again.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlock.java"
s = io.open(p, encoding="utf-8").read()

# ---- 1. the constructor takes the factory -----------------------------------------------------
old_ctor = '''    /**
     * @param structurePath the structure file name, e.g. {@code chemical_reactor}
     */
    public GTChemicalReactorMachineBlock(Properties properties, String structurePath) {
        super(properties);
        this.structurePath = structurePath;'''
new_ctor = '''    /**
     * Builds the block entity for a placed host.
     *
     * <p>Carried by the block for the same reason as {@link #structurePath}: an entity type has to
     * match the block it sits in, and both machines share this block class, so the difference has
     * to come from the registration rather than from a branch here. With the factory in hand this
     * class needs no knowledge of any particular machine.</p>
     */
    private final BiFunction<BlockPos, BlockState, GTChemicalReactorMachineBlockEntity> tileFactory;

    /**
     * @param structurePath the structure file name, e.g. {@code chemical_reactor}
     * @param tileFactory   constructs this machine's block entity
     */
    public GTChemicalReactorMachineBlock(Properties properties, String structurePath,
                                         BiFunction<BlockPos, BlockState,
                                                 GTChemicalReactorMachineBlockEntity> tileFactory) {
        super(properties);
        this.structurePath = structurePath;
        this.tileFactory = tileFactory;'''
assert old_ctor in s
s = s.replace(old_ctor, new_ctor, 1)

# ---- 2. machineTile just asks the factory -----------------------------------------------------
old_tile = '''    /**
     * The entity for this block.
     *
     * <p>Both machines share this block class, so the block has to say which one it is: an entity's
     * registered type must match the block it sits in, and vanilla throws on construction when it
     * does not. Deciding here keeps the difference in one place instead of needing a subclass whose
     * only job would be to answer it.</p>
     */
    @Override
    protected GTChemicalReactorMachineBlockEntity machineTile(@NotNull BlockPos pos, @NotNull BlockState state) {
        if (state.is(GTAdvancedChemicalReactorMultiblock.MACHINE.get())) {
            return new GTAdvancedChemicalReactorBlockEntity(pos, state);
        }
        return new GTChemicalReactorMachineBlockEntity(pos, state);
    }'''
new_tile = '''    /**
     * The entity for this block, built by the factory this machine was registered with.
     *
     * <p>An entity's registered type must match the block it sits in, and vanilla throws on
     * construction when it does not — so the factory has to be the one belonging to this machine,
     * which is why it comes from the registration.</p>
     */
    @Override
    protected GTChemicalReactorMachineBlockEntity machineTile(@NotNull BlockPos pos, @NotNull BlockState state) {
        return this.tileFactory.apply(pos, state);
    }'''
assert old_tile in s
s = s.replace(old_tile, new_tile, 1)

if "import java.util.function.BiFunction;" not in s:
    lines = s.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import "))
    lines.insert(last + 1, "import java.util.function.BiFunction;")
    s = "\n".join(lines)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("block carries its tile factory")

# ---- 3. both registrations name their factory -------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMultiblock.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace(
    '''() -> new GTChemicalReactorMachineBlock(GTMultiblockBlocks.properties(), "chemical_reactor"));''',
    '''() -> new GTChemicalReactorMachineBlock(GTMultiblockBlocks.properties(),
                            "chemical_reactor", GTChemicalReactorMachineBlockEntity::new));''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("basic registration passes its factory")

p3 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorMultiblock.java"
s3 = io.open(p3, encoding="utf-8").read()
s3 = s3.replace(
    '''() -> new GTChemicalReactorMachineBlock(GTMultiblockBlocks.properties(),
                            "advanced_chemical_reactor"));''',
    '''() -> new GTChemicalReactorMachineBlock(GTMultiblockBlocks.properties(),
                            "advanced_chemical_reactor", GTAdvancedChemicalReactorBlockEntity::new));''', 1)
io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("advanced registration passes its factory")
