package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import thaumcraft.api.aspects.Aspect;

/**
 * Tooltip data for a compound aspect's two components, rendered as a pair of
 * aspect icons in {@link GTResearchTableScreen}'s palette tooltip.
 */
public record GTAspectRecipeTooltip(Aspect first, Aspect second) implements TooltipComponent {
}
