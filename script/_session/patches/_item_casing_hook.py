"""Let the item tooltip name the right casing.

The tooltip's structure list names the shell block. The advanced kettle is built from
polypropylene, so reusing the item class unchanged would tell the player to bring the wrong
material. A constructor parameter carries the answer.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTChemicalReactorItem.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    private static final List<Part> PARTS = List.of(
            // The block's own name: it says exactly what the structure line needs, so there is no
            // second spelling of "PE casing" to keep in step.
            new Part(MIN_SHELLS, "block.goldentweaks.shell_pe"),''',
'''    /** The shell key this item's tooltip names; set per machine through the constructor. */
    private final String casingKey;

    private static final String DEFAULT_CASING = "block.goldentweaks.shell_pe";

    private static List<Part> partsFor(String casingKey) {
        return List.of(
            // The block's own name: it says exactly what the structure line needs, so there is no
            // second spelling of "PE casing" to keep in step.
            new Part(MIN_SHELLS, casingKey),''', 1)

s = s.replace('''            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.output_bus"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.fluid_output"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.energy_input"));''',
'''            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.output_bus"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.fluid_output"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.energy_input"));
    }''', 1)

s = s.replace('''    public GTChemicalReactorItem(Block block, Item.Properties properties) {
        super(block, properties);
    }''',
'''    public GTChemicalReactorItem(Block block, Item.Properties properties) {
        this(block, properties, DEFAULT_CASING);
    }

    /** The same item, naming the casing this particular machine is built from. */
    public GTChemicalReactorItem(Block block, Item.Properties properties, String casingKey) {
        super(block, properties);
        this.casingKey = casingKey;
    }''', 1)

s = s.replace('''        for (Part part : PARTS) {
            tooltip.add(partLine(part));
        }''',
'''        for (Part part : partsFor(this.casingKey)) {
            tooltip.add(partLine(part));
        }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("item tooltip names its own casing")

# ---------------- the advanced item passes PP ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorMultiblock.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''                    () -> new GTChemicalReactorItem(MACHINE.get(), new Item.Properties()));''',
'''                    () -> new GTChemicalReactorItem(MACHINE.get(), new Item.Properties(),
                            "block.goldentweaks.shell_pp"));''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("advanced item names the PP casing")
