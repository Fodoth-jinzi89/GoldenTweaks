"""Let each host block declare its own structure id, instead of a lookup table keyed by id.

`HOST_STRUCTURES` mapped a block id to a structure id, which had to be edited for every new
machine and could silently disagree with what the machine itself believes (the tile gets its
structure id from `getMachineId()`).

The block now carries the id it was registered with, mirroring how the tile already answers.
One declaration per machine, no table to keep in step, and the projector, the rotate helper and
the build pass all read the same field.
"""
import io

# ---- 1. the block gains a field ---------------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlock.java"
s = io.open(p, encoding="utf-8").read()

old = '''    public GTChemicalReactorMachineBlock(Properties properties) {
        super(properties);'''
new = '''    /**
     * The structure this machine assembles, relative to {@code data/<namespace>/multiblock}.
     *
     * <p>Carried by the block rather than looked up from a table of block ids to structure ids:
     * the tile already answers {@link GTChemicalReactorMachineBlockEntity#getMachineId()}, and a
     * second table would be one more thing to keep in step. The projector, the rotate helper and
     * the build pass all read this one field, so a second machine costs a constructor argument and
     * nothing else.</p>
     */
    private final String structurePath;

    /**
     * @param structurePath the structure file name, e.g. {@code chemical_reactor}
     */
    public GTChemicalReactorMachineBlock(Properties properties, String structurePath) {
        super(properties);
        this.structurePath = structurePath;'''
assert old in s
s = s.replace(old, new, 1)

# the getter, placed next to the state definition
anchor = '''    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {'''
addition = '''    /**
     * Resolves the structure file this host projects and builds.
     *
     * <p>Returns {@code null} when the namespace-qualified id cannot be formed, which only happens
     * for a path that is not a valid resource location.</p>
     */
    @Nullable
    public ResourceLocation structureId() {
        return ResourceLocation.tryBuild(GoldenTweaks.MODID, this.structurePath);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {'''
assert anchor in s
s = s.replace(anchor, addition, 1)

for needed, after in [
    ("import net.fodoth.skina.goldentweaks.GoldenTweaks;", None),
    ("import net.minecraft.resources.ResourceLocation;", None),
    ("import org.jetbrains.annotations.Nullable;", None),
]:
    if needed not in s:
        lines = s.split("\n")
        last = max(i for i, l in enumerate(lines) if l.startswith("import "))
        lines.insert(last + 1, needed)
        s = "\n".join(lines)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("block carries its structure id")

# ---- 2. both registrations pass it ------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMultiblock.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace(
    '''() -> new GTChemicalReactorMachineBlock(GTMultiblockBlocks.properties()));''',
    '''() -> new GTChemicalReactorMachineBlock(GTMultiblockBlocks.properties(), "chemical_reactor"));''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("basic registration passes its structure")

p3 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorMultiblock.java"
s3 = io.open(p3, encoding="utf-8").read()
s3 = s3.replace(
    '''() -> new GTChemicalReactorMachineBlock(GTMultiblockBlocks.properties()));''',
    '''() -> new GTChemicalReactorMachineBlock(GTMultiblockBlocks.properties(),
                            "advanced_chemical_reactor"));''', 1)
io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("advanced registration passes its structure")
