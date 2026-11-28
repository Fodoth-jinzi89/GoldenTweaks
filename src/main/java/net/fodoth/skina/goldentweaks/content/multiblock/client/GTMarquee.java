package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Text that slides sideways when it does not fit its column.
 *
 * <p>Drawn twice, one cycle apart, with a gap between the runs — that is what makes a long line
 * read as one continuous strip sliding past instead of two copies appearing. Text that fits is
 * simply drawn where it was asked to go, so a short name sits still.</p>
 *
 * <p>The offset comes from the wall clock, so the speed does not depend on the frame rate, and each
 * lap holds still for a moment before travelling, which makes the beginning readable every time it
 * comes round.</p>
 */
public final class GTMarquee {

    private static final int GAP = 24;
    private static final double PIXELS_PER_SECOND = 28.0D;
    private static final long PAUSE_MILLIS = 1200L;

    private GTMarquee() {
    }

    /**
     * Draws {@code text} inside {@code width}, scrolling it when it is wider.
     *
     * @param clipped whether to scissor the run at the column's edges. A window that can spare the
     *                scissor gets it, since the second copy would otherwise bleed into whatever
     *                sits beside the column.
     */
    public static void draw(GuiGraphics graphics, Font font, Component text,
                            int x, int y, int width, int colour, boolean clipped) {
        if (font.width(text) <= width) {
            graphics.drawString(font, text, x, y, colour, false);
            return;
        }
        int cycle = font.width(text) + GAP;
        int offset = offset(cycle);
        if (clipped) {
            graphics.enableScissor(x, y, x + width, y + font.lineHeight);
        }
        graphics.drawString(font, text, x - offset, y, colour, false);
        graphics.drawString(font, text, x - offset + cycle, y, colour, false);
        if (clipped) {
            graphics.disableScissor();
        }
    }

    /** Where the run has slid to, given one full cycle's width. */
    static int offset(int cycle) {
        long lapMillis = Math.max(1L, Math.round(cycle / PIXELS_PER_SECOND * 1000.0D));
        long phase = Util.getMillis() % (PAUSE_MILLIS + lapMillis);
        if (phase < PAUSE_MILLIS) {
            return 0;
        }
        double travelled = (phase - PAUSE_MILLIS) / 1000.0D * PIXELS_PER_SECOND;
        return (int) Math.round(travelled % cycle);
    }
}
