"""Give essentia a name and a fallback icon in the machine's product list.

Thaumcraft aspects are not registry entries, so there is no EMI stack for them. The name comes
from the aspect itself, which is what a player recognises; the icon falls back to null, and the
list already tolerates an entry without one.
"""
import io

# ---------------- the product name ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''        Component name = switch (stack.kind()) {
            case ITEM -> stack.toItemStack().getHoverName();
            case FLUID -> stack.toFluidStack().getHoverName();
            // Chemicals and AE keys carry no portable display name here, so the id is the honest
            // fallback instead of an empty label.
            case CHEMICAL, AE -> Component.literal(stack.id().getPath());
        };''',
'''        Component name = switch (stack.kind()) {
            case ITEM -> stack.toItemStack().getHoverName();
            case FLUID -> stack.toFluidStack().getHoverName();
            // Essentia's own name is a Thaumcraft string, not a translation key this build owns, so
            // it is read through the aspect when the mod is there and the tag is the fallback.
            case ESSENTIA -> essentiaName(stack);
            // Chemicals and AE keys carry no portable display name here, so the id is the honest
            // fallback instead of an empty label.
            case CHEMICAL, AE -> Component.literal(stack.id().getPath());
        };''', 1)

s = s.replace('''    /** Production rate in the unit the machine already uses: per second. */''',
'''    /**
     * An essentia entry's display name.
     *
     * <p>Guarded: {@code Aspect} is a Thaumcraft class, and this screen loads whether or not the
     * mod is installed. Without it the raw tag is shown, which is still unambiguous.</p>
     */
    private static Component essentiaName(GTReactionStack stack) {
        String tag = stack.essentiaTag();
        if (tag == null) {
            return Component.literal(stack.id().getPath());
        }
        try {
            var aspect = thaumcraft.api.aspects.Aspect.getAspect(tag);
            return aspect == null
                    ? Component.literal(tag)
                    : Component.literal(aspect.getName());
        } catch (NoClassDefFoundError absent) {
            return Component.literal(tag);
        }
    }

    /** Production rate in the unit the machine already uses: per second. */''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("product name handles essentia")

# ---------------- the EMI icon ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTReactionStacks.java"
s2 = io.open(p2, encoding="utf-8").read()

s2 = s2.replace('''            case AE -> {
                GenericStack generic = registries == null ? null : toGenericStack(stack, registries);
                yield generic == null ? null : GTAe2EmiStacks.toEmiStack(generic.what());
            }''',
'''            // Essentia has no EMI stack: Thaumcraft aspects are not registry entries and carry no
            // item of their own. The list draws the entry by name instead of by icon.
            case ESSENTIA -> null;
            case AE -> {
                GenericStack generic = registries == null ? null : toGenericStack(stack, registries);
                yield generic == null ? null : GTAe2EmiStacks.toEmiStack(generic.what());
            }''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("EMI conversion handles essentia")
