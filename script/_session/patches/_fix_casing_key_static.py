"""Pass the casing key into the static structure builder."""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTChemicalReactorItem.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /** The breakdown behind the Shift prompt. */
    private static void addStructure(List<Component> tooltip) {''',
'''    /** The breakdown behind the Shift prompt. */
    private static void addStructure(List<Component> tooltip, String casingKey) {''', 1)

s = s.replace('''        for (Part part : partsFor(this.casingKey)) {''',
'''        for (Part part : partsFor(casingKey)) {''', 1)

s = s.replace('''            addStructure(tooltip);''',
'''            addStructure(tooltip, this.casingKey);''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("casing key passed through")
