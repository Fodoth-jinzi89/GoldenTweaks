package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client;

import dev.emi.emi.api.stack.EmiStack;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectIcons;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTAspectNames;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import org.jetbrains.annotations.Nullable;

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
    }
}
