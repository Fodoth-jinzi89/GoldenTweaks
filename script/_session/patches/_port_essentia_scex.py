"""The port window draws essentia through scex's entry too, so it answers recipe lookups.

The window already drew the aspect with Thaumcraft's renderer, which looks right but is inert: a
player cannot press R on it. scex registers the aspect as a viewer ingredient, so handing the port
slot that entry gives the same hover and lookup every other slot has.

The existing drawing stays as the fallback — without scex there is no registered entry, and an icon
is still better than nothing.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java"
s = io.open(p, encoding="utf-8").read()

old = '''        if (stack.kind() == GTReactionStack.Kind.ESSENTIA) {
            drawEssentia(graphics, stack, x, y);'''
new = '''        if (stack.kind() == GTReactionStack.Kind.ESSENTIA) {
            // scex registers the aspect as a viewer ingredient; drawing that entry instead of the
            // bare icon is what lets the player look a recipe up by hovering this slot.
            EmiStack registered = GTAspectEmiStacks.of(stack.essentiaTag());
            if (registered != null) {
                registered.setAmount(stack.amount())
                        .render(graphics, x, y, 0.0F, EmiIngredient.RENDER_ICON);
                if (ModList.get().isLoaded("emi")) {
                    EmiRenderHelper.renderAmount(context, x, y,
                            Component.literal(GTPortAmounts.number(stack.amount(), true)));
                } else {
                    drawAmount(graphics, stack.amount(), true, x, y);
                }
                return;
            }
            drawEssentia(graphics, stack, x, y);'''
assert old in s
s = s.replace(old, new, 1)

for imp in [
    "import dev.emi.emi.api.stack.EmiStack;",
    "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client.GTAspectEmiStacks;",
]:
    if imp not in s:
        s = s.replace("import net.fodoth.skina.goldentweaks.content.multiblock.GTPortAmounts;",
                      imp + "\nimport net.fodoth.skina.goldentweaks.content.multiblock.GTPortAmounts;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("port screen prefers the registered aspect entry")
