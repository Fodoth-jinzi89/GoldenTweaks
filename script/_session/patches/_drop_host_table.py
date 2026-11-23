"""Read the structure id from the host block itself and drop the id table.

`structureForHost` now asks the block, which carries the id it was registered with. That removes
the last per-machine entry from this class: the projector, the rotate helper, the build pass and
the structure matcher all resolve through the same field, so a third machine needs no edit here.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s = io.open(p, encoding="utf-8").read()

# ---- drop the table and the advanced-only constant ---------------------------------------------
old_consts = '''    /**
     * Host id to structure id, for every machine the projector can show.
     *
     * <p>Kept as a table so adding a machine is one line here instead of another branch inside the
     * lookup. The advanced kettle is registered by the compat package, but its ids are plain
     * strings, so naming them here costs no dependency.</p>
     */
    /** Structure file backing the advanced kettle. */
    public static final ResourceLocation ADVANCED_CHEMICAL_REACTOR_STRUCTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "advanced_chemical_reactor");

    private static final Map<ResourceLocation, ResourceLocation> HOST_STRUCTURES = Map.of(
            CHEMICAL_REACTOR_HOST, CHEMICAL_REACTOR_STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "advanced_chemical_plant_controller"),
            ADVANCED_CHEMICAL_REACTOR_STRUCTURE);
'''
assert old_consts in s
s = s.replace(old_consts, "", 1)

# ---- resolve through the block -----------------------------------------------------------------
old_lookup = '''    /** The structure a host block projects, or {@code null} if this state is not a multiblock host. */
    @Nullable
    public static ResourceLocation structureForHost(BlockState state) {
        return HOST_STRUCTURES.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }'''
new_lookup = '''    /**
     * The structure a host block projects, or {@code null} if this state is not a multiblock host.
     *
     * <p>Answered by the block, which carries the id it was registered with — so a new machine
     * declares its own structure and nothing here has to know it exists.</p>
     */
    @Nullable
    public static ResourceLocation structureForHost(BlockState state) {
        return state.getBlock() instanceof GTChemicalReactorMachineBlock host ? host.structureId() : null;
    }'''
assert old_lookup in s
s = s.replace(old_lookup, new_lookup, 1)

if "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMachineBlock;" not in s:
    s = s.replace(
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;",
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMachineBlock;\n"
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("structureForHost reads the block")
print("table removed:", "HOST_STRUCTURES" not in s)
print("Map still used:", "Map<" in s)
