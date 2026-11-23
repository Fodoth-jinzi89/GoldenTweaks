"""Make the projector recognise the advanced kettle, and drop the stray FQCN in register().

Two separate problems in this file:

1. `structureForHost` matched only the basic host id, so for the advanced controller it returned
   null. The projection renderer treats null as "not a host" and clears itself, which is why the
   projector did nothing at all on the advanced machine.

2. `register()` spelled out a package path instead of importing it, against the project's rule.

The structure id is kept here rather than read from the advanced class so this file does not have
to depend on the compat package; the two are joined by the id table below.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s = io.open(p, encoding="utf-8").read()

# ---- 1. the id table and the lookup -------------------------------------------------------
old_host = '''    /** Structure file backing the reaction kettle, relative to {@code data/<namespace>/multiblock}. */
    public static final ResourceLocation CHEMICAL_REACTOR_STRUCTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "chemical_reactor");'''
new_host = '''    /** Structure file backing the reaction kettle, relative to {@code data/<namespace>/multiblock}. */
    public static final ResourceLocation CHEMICAL_REACTOR_STRUCTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "chemical_reactor");

    /**
     * Host id to structure id, for every machine the projector can show.
     *
     * <p>Kept as a table so adding a machine is one line here instead of another branch inside the
     * lookup. The advanced kettle is registered by the compat package, but its ids are plain
     * strings, so naming them here costs no dependency.</p>
     */
    private static final Map<ResourceLocation, ResourceLocation> HOST_STRUCTURES = Map.of(
            CHEMICAL_REACTOR_HOST, CHEMICAL_REACTOR_STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "advanced_chemical_plant_controller"),
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "advanced_chemical_reactor"));'''
assert old_host in s
s = s.replace(old_host, new_host, 1)

old_lookup = '''        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return CHEMICAL_REACTOR_HOST.equals(id) ? CHEMICAL_REACTOR_STRUCTURE : null;'''
new_lookup = '''        return HOST_STRUCTURES.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()));'''
assert old_lookup in s
s = s.replace(old_lookup, new_lookup, 1)

# ---- 2. the FQCN in register() ------------------------------------------------------------
old_reg = '''            GTChemicalReactorMultiblock.register(eventBus);
            net.fodoth.skina.goldentweaks.compat.avaritia_more_items
                    .GTAdvancedChemicalReactorMultiblock.register(eventBus);'''
new_reg = '''            GTChemicalReactorMultiblock.register(eventBus);
            GTAdvancedChemicalReactorMultiblock.register(eventBus);'''
assert old_reg in s
s = s.replace(old_reg, new_reg, 1)

if "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;" not in s:
    s = s.replace(
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMultiblock;",
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTAdvancedChemicalReactorMultiblock;\n"
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMultiblock;", 1)

if "\nimport java.util.Map;" not in s:
    lines = s.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import "))
    lines.insert(last + 1, "import java.util.Map;")
    s = "\n".join(lines)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("host table added; FQCN replaced with import")
print("Map imported:", "import java.util.Map;" in s)
print("fqcn left:", "net.fodoth.skina.goldentweaks.compat.avaritia_more_items\n" in s)
