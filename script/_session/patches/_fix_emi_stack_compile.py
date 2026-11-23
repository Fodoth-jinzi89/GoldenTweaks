"""Two compile fixes.

1. `GTReactionStacks` never imported the new stack class.
2. `EmiStack` also declares `getKey()` and `getComponentChanges()` as abstract.
"""
import io

# ---- the missing import, and a repaired comment ------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTReactionStacks.java"
raw = io.open(p, encoding="utf-8").read()

if "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client.GTEssentiaEmiStack;" not in raw:
    # 放在第一个 net.fodoth 导入之前，保持分组
    anchor = "import net.fodoth.skina.goldentweaks.GoldenTweaks;"
    raw = raw.replace(anchor,
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client.GTEssentiaEmiStack;\n"
        + anchor, 1)

old_comment = """            // Essentia has no stack of its own \u2014 aspects are not registry entries and ship no item
            // \u2014 so one is supplied here. It draws through Thaumcraft's renderer and prints the
            // amount as a whole-unit badge, matching the port window."""
new_comment = """            // Essentia has no stack of its own: aspects are not registry entries and ship no
            // item, so one is supplied here. It draws through Thaumcraft's renderer and prints
            // the amount as a whole-unit badge, matching the port window."""
if old_comment in raw:
    raw = raw.replace(old_comment, new_comment, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(raw)
print("GTReactionStacks: import added")

# ---- the two remaining abstract methods -------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTEssentiaEmiStack.java"
s = io.open(p2, encoding="utf-8").read()

old = """    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("thaumcraft", this.tag);
    }"""
new = """    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("thaumcraft", this.tag);
    }

    /**
     * What identifies this stack.
     *
     * <p>EMI compares keys to decide whether two stacks are the same thing — for favourites, for
     * the search index and for "already on the board" checks. The tag is the identity: two entries
     * naming {@code aer} are one aspect, whatever their amounts.</p>
     */
    @Override
    public Object getKey() {
        return "thaumcraft:essentia/" + this.tag;
    }

    /** Aspects are not data components, so there is never a patch to apply. */
    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }"""
assert old in s
s = s.replace(old, new, 1)

if "import net.minecraft.core.component.DataComponentPatch;" not in s:
    s = s.replace("import net.minecraft.client.gui.GuiGraphics;",
                  "import net.minecraft.client.gui.GuiGraphics;\nimport net.minecraft.core.component.DataComponentPatch;", 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s)
print("GTEssentiaEmiStack: getKey + getComponentChanges")
