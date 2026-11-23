package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Draws an aspect icon, for callers that only know the tag.
 *
 * <p>Kept beside {@link GTAspectNames} for the same reason: the mod's API is named only in this
 * package, so a caller in a general-recipe class can offer the icon without making the file
 * unloadable on a pack without Thaumcraft.</p>
 */
public final class GTAspectIcons {

    private GTAspectIcons() {
    }

    /**
     * Draws one aspect at the given spot.
     *
     * <p>Not a plain blit: the sheet comes from {@code Aspect.image()}, the tint from
     * {@code Aspect.color()}, and the cosmic aspects are not sheet-based at all — they render as a
     * dedicated item model. All three live in {@code AspectGuiRenderer}.</p>
     *
     * @return whether anything was drawn
     */
    public static boolean draw(GuiGraphics graphics, String tag, int x, int y) {
        if (tag == null) {
            return false;
        }
        try {
            if (GTAspectEntry.isCosmic(tag)) {
                graphics.renderItem(GTThaumcraftAdditionalItems.cosmicIconStack(), x, y);
                return true;
            }
            var aspect = thaumcraft.api.aspects.Aspect.getAspect(tag);
            if (aspect == null) {
                return false;
            }
            thaumcraft.client.gui.AspectGuiRenderer.draw(graphics, aspect, x, y, 16, 1.0F);
            return true;
        } catch (NoClassDefFoundError absent) {
            return false;
        }
    }
}
