package net.fodoth.skina.goldentweaks.compat.avaritia_more_items.recipe.client;

import dev.emi.emi.api.stack.EmiStack;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTScexAspectBridge;

/**
 * Turns an aspect into the encrypted-of-scex EMI entry, when that mod can supply one.
 *
 * <p>Kept here rather than beside the reflection because this package already depends on EMI: the
 * bridge class itself must load with neither scex nor EMI present, and naming {@code EmiStack} in
 * it would defeat that.</p>
 */
public final class GTAspectEmiStacks {

    private GTAspectEmiStacks() {
    }

    /**
     * The aspect as the viewer's own ingredient, or {@code null} when it cannot be one.
     *
     * <p>Returning scex's entry rather than a hand-drawn stack is what makes hover, search and
     * "show recipes" work: only registered ingredient types answer those.</p>
     */
    public static EmiStack of(String tag) {
        Object stack = GTScexAspectBridge.stackFor(tag);
        return stack instanceof EmiStack emi ? emi : null;
    }
}
