"""Clean up the remaining inline package names in the port and jade classes.

Every substitution gets an import, except the two kinds that must stay qualified:
- `net.minecraft.client.gui.screens.Screen`, which does not exist on a dedicated server;
- the mod-specific types reached from classes that always load, where the whole point is that
  a method body resolves them lazily.
"""
import io

def add_imports(s, imports):
    lines = s.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import "))
    fresh = [i for i in imports if i not in s]
    for offset, line in enumerate(fresh):
        lines.insert(last + 1 + offset, line)
    return "\n".join(lines)


def patch(path, pairs, imports):
    s = io.open(path, encoding="utf-8").read()
    for old, new in pairs:
        s = s.replace(old, new)
    s = add_imports(s, imports)
    io.open(path, "w", encoding="utf-8", newline="\n").write(s)
    print(path.split("/")[-1], "patched")


# ---------------- GTPortScreen ----------------
patch(
    r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java",
    [
        ("if (dev.emi.emi.config.EmiConfig.viewRecipes.matchesKey(keyCode, scanCode)) {",
         "if (EmiConfig.viewRecipes.matchesKey(keyCode, scanCode)) {"),
        ("if (dev.emi.emi.config.EmiConfig.viewUses.matchesKey(keyCode, scanCode)) {",
         "if (EmiConfig.viewUses.matchesKey(keyCode, scanCode)) {"),
        ("var context = dev.emi.emi.runtime.EmiDrawContext.wrap(graphics);",
         "var context = EmiDrawContext.wrap(graphics);"),
        ("private void drawStack(GuiGraphics graphics, dev.emi.emi.runtime.EmiDrawContext context,",
         "private void drawStack(GuiGraphics graphics, EmiDrawContext context,"),
        ("dev.emi.emi.EmiRenderHelper.renderAmount(context, x, y,",
         "EmiRenderHelper.renderAmount(context, x, y,"),
        ("ingredient.render(graphics, x, y, 0.0F, dev.emi.emi.api.stack.EmiIngredient.RENDER_ICON);",
         "ingredient.render(graphics, x, y, 0.0F, EmiIngredient.RENDER_ICON);"),
        ("net.minecraft.world.level.material.Fluid fluid = stack.fluid();",
         "Fluid fluid = stack.fluid();"),
        ("var holder = mekanism.api.MekanismAPI.CHEMICAL_REGISTRY.getHolder(stack.id()).orElse(null);",
         "var holder = MekanismAPI.CHEMICAL_REGISTRY.getHolder(stack.id()).orElse(null);"),
        ("thaumcraft.api.aspects.Aspect aspect = thaumcraft.api.aspects.Aspect.getAspect(tag);",
         "var aspect = GTAspectNames.of(tag);"),
        ("if (net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry.isCosmic(tag)) {",
         "if (GTAspectEntry.isCosmic(tag)) {"),
        ("graphics.renderItem(\n                        net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftAdditionalItems\n                                .cosmicIconStack(), x, y);",
         "graphics.renderItem(GTThaumcraftAdditionalItems.cosmicIconStack(), x, y);"),
        ("thaumcraft.client.gui.AspectGuiRenderer.draw(graphics, aspect, x, y, 16, 1.0F);",
         "AspectGuiRenderer.draw(graphics, aspect, x, y, 16, 1.0F);"),
        ("long phase = net.minecraft.Util.getMillis() % (MARQUEE_PAUSE_MILLIS + lapMillis);",
         "long phase = Util.getMillis() % (MARQUEE_PAUSE_MILLIS + lapMillis);"),
        ("&& this.menu.port().mode() == net.weibai.avaritia_more_items\n                        .common.blocks.abstracts.multiblock.port.PortMode.OUTPUT;",
         "&& this.menu.port().mode() == PortMode.OUTPUT;"),
    ],
    [
        "import dev.emi.emi.api.stack.EmiIngredient;",
        "import dev.emi.emi.config.EmiConfig;",
        "import dev.emi.emi.runtime.EmiDrawContext;",
        "import dev.emi.emi.EmiRenderHelper;",
        "import mekanism.api.MekanismAPI;",
        "import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectEntry;",
        "import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectNames;",
        "import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftAdditionalItems;",
        "import net.minecraft.Util;",
        "import net.minecraft.world.level.material.Fluid;",
        "import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.PortMode;",
        "import thaumcraft.client.gui.AspectGuiRenderer;",
    ],
)

# ---------------- Jade ----------------
patch(
    r"src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java",
    [
        ("private static net.minecraft.client.gui.Font font() {",
         "private static Font font() {"),
        ("return net.minecraft.client.Minecraft.getInstance().font;",
         "return Minecraft.getInstance().font;"),
        ("net.minecraft.nbt.Tag.TAG_COMPOUND);", "Tag.TAG_COMPOUND);"),
    ],
    [
        "import net.minecraft.client.Minecraft;",
        "import net.minecraft.client.gui.Font;",
        "import net.minecraft.nbt.Tag;",
    ],
)
