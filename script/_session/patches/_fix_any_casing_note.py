"""The "any casing" suffix names the machine's own casing.

The suffix was authored text reading "any PE casing", so an advanced machine's structure list ended
with a polypropylene line followed by a polyethylene note. The note is now built from the same key
that names the casing line above it, so the two cannot disagree.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTChemicalReactorItem.java"
s = io.open(p, encoding="utf-8").read()

# ---- the suffix takes the casing key ------------------------------------------------------------
old_note = '''    private static final String FREE_NOTE = "tooltip.goldentweaks.chemical_reactor.structure.any";'''
new_note = '''    /** {@code ：任意%s} — the casing the machine is built from fills the blank. */
    private static final String FREE_NOTE = "tooltip.goldentweaks.chemical_reactor.structure.any";'''
assert old_note in s
s = s.replace(old_note, new_note, 1)

# ---- pass the key through ----------------------------------------------------------------------
s = s.replace('''        for (Part part : partsFor(casingKey)) {
            tooltip.add(partLine(part));
        }''',
'''        for (Part part : partsFor(casingKey)) {
            tooltip.add(partLine(part, casingKey));
        }''', 1)

old_line = '''    /** One {@code 8+ 聚乙烯机壳} line, with the "any casing" note on the optional parts. */
    private static Component partLine(Part part) {
        Component line = Component.literal(INDENT + part.minimum() + "+").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" "))
                .append(Component.translatable(part.nameKey()).withStyle(ChatFormatting.WHITE));
        if (part.minimum() == 0) {
            // Only the ports are interchangeable with a casing; the casing line has no note.
            line = line.copy()
                    .append(Component.translatable(FREE_NOTE).withStyle(ChatFormatting.DARK_GRAY));
        }
        return line;
    }'''
new_line = '''    /**
     * One line of the structure list.
     *
     * <p>The optional parts carry an "or any casing" note, and that note names this machine's own
     * casing — the same key the casing line above it uses. Writing the name out in the language
     * file instead is what made an advanced machine's list end with a polyethylene note.</p>
     *
     * @param casingKey the translation key of the casing this machine is built from
     */
    private static Component partLine(Part part, String casingKey) {
        Component line = Component.literal(INDENT + part.minimum() + "+").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" "))
                .append(Component.translatable(part.nameKey()).withStyle(ChatFormatting.WHITE));
        if (part.minimum() == 0) {
            // Only the ports are interchangeable with a casing; the casing line has no note.
            line = line.copy()
                    .append(Component.translatable(FREE_NOTE,
                                    Component.translatable(casingKey))
                            .withStyle(ChatFormatting.DARK_GRAY));
        }
        return line;
    }'''
assert old_line in s
s = s.replace(old_line, new_line, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("any-casing note now names the machine's casing")

# ---- the language strings take the name --------------------------------------------------------
import json, collections
for path, value in [
    (r"src/main/resources/assets/goldentweaks/lang/zh_cn.json", "：任意%s"),
    (r"src/main/resources/assets/goldentweaks/lang/en_us.json", ": any %s"),
]:
    data = json.load(io.open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
    data["tooltip.goldentweaks.chemical_reactor.structure.any"] = value
    io.open(path, "w", encoding="utf-8", newline="\n").write(
        json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    print("lang updated:", path.split("/")[-1], "->", value)
