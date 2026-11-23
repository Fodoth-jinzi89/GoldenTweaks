content = '''package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client;

import dev.emi.emi.api.stack.EmiStack;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectIcons;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectNames;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * A Thaumcraft aspect, drawn the way the port window draws it.
 *
 * <p>Essentia is not a registry entry and ships no item, so EMI has no stack for it and a recipe
 * page would show an empty cell. This subclass supplies the two things EMI asks a stack for: what
 * to draw, and what to print on hover.</p>
 *
 * <p>No Thaumcraft type is named here. Both the icon and the name go through the compat package,
 * which is the only place the mod's API appears — the recipe classes are loaded on packs that do
 * not have it.</p>
 */
public class GTEssentiaEmiStack extends EmiStack {

    /** What to show when the aspect cannot be resolved (mod missing, or a typo in the data pack). */
    private static final Component UNKNOWN = Component.literal("?");

    /** The aspect tag, e.g. {@code aer}. */
    private final String tag;

    private final long amount;

    public GTEssentiaEmiStack(String tag, long amount) {
        this.tag = tag;
        this.amount = amount;
    }

    public String tag() {
        return this.tag;
    }

    @Override
    public EmiStack copy() {
        GTEssentiaEmiStack copy = new GTEssentiaEmiStack(this.tag, this.amount);
        copy.setChance(this.getChance());
        return copy;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
        GTAspectIcons.draw(graphics, this.tag, x, y);
    }

    @Override
    public Component getName() {
        Component name = GTAspectNames.of(this.tag);
        return name != null ? name : UNKNOWN;
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(getName());
    }

    /** Essentia counts in whole units, so the page prints the item-shaped badge, not millibuckets. */
    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("thaumcraft", this.tag);
    }
}
'''

path = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTEssentiaEmiStack.java"
import io
io.open(path, "w", encoding="utf-8", newline="\n").write(content)
print("GTEssentiaEmiStack written (no FQCN, no direct Thaumcraft types)")
