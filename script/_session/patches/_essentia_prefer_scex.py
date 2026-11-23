"""Essentia is scex's own viewer entry when that mod is present.

A hand-drawn stack shows the icon but cannot be looked up: hover, search and "show recipes" all walk
the registered ingredient types. scex registers the aspect as one, so going through it is what gives
the page the same behaviour every other ingredient has.

Drawing by hand stays as the fallback for a pack without scex, where there is no supported entry to
use and an icon is better than an empty cell.
"""
import io

# ---- 1. the stack: prefer the registered entry ------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTEssentiaEmiStack.java"
s = io.open(p, encoding="utf-8").read()

old = '''    public GTEssentiaEmiStack(String tag, long amount) {
        this.tag = tag;
        this.amount = amount;
    }'''
new = '''    public GTEssentiaEmiStack(String tag, long amount) {
        this.tag = tag;
        this.amount = amount;
    }

    /**
     * The aspect as scex's own entry, or {@code null} when scex is not installed.
     *
     * <p>Returned through {@link GTAspectEmiStacks} rather than built here so this class stays
     * independent of that mod.</p>
     */
    @Nullable
    public static EmiStack registered(String tag, long amount) {
        EmiStack stack = GTAspectEmiStacks.of(tag);
        return stack == null ? null : stack.setAmount(amount);
    }'''
assert old in s
s = s.replace(old, new, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("stack can defer to scex")

# ---- 2. the conversion prefers it -------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTReactionStacks.java"
s2 = io.open(p2, encoding="utf-8").read()

old2 = '''            // Essentia has no stack of its own: aspects are not registry entries and ship no
            // item, so one is supplied here. It draws through Thaumcraft's renderer and prints
            // the amount as a whole-unit badge, matching the port window.
            case ESSENTIA -> new GTEssentiaEmiStack(stack.essentiaTag(), stack.amount());'''
new2 = '''            // Essentia: scex registers the aspect as a real viewer ingredient, so prefer that —
            // it answers hover, search and recipe lookups, which a hand-drawn stack cannot. The
            // hand-drawn one stays as the fallback for a pack without scex.
            case ESSENTIA -> {
                EmiStack registered = GTEssentiaEmiStack.registered(
                        stack.essentiaTag(), stack.amount());
                yield registered != null
                        ? registered
                        : new GTEssentiaEmiStack(stack.essentiaTag(), stack.amount());
            }'''
assert old2 in s2
s2 = s2.replace(old2, new2, 1)

if "import dev.emi.emi.api.stack.EmiStack;" not in s2:
    s2 = s2.replace(
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client.GTEssentiaEmiStack;",
        "import dev.emi.emi.api.stack.EmiStack;\n"
        "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client.GTEssentiaEmiStack;", 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("conversion prefers scex")
