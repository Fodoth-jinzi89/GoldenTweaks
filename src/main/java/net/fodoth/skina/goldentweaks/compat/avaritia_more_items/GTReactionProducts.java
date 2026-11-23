package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The products of each machine's current reaction, as last sent by the server.
 *
 * <p>Pure data: the server resolves the recipe and hands over the finished numbers, so the GUI only
 * has to draw them. That keeps the screen independent of any recipe class, and it means the figures
 * shown are necessarily the ones the machine is actually running.</p>
 */
public final class GTReactionProducts {

    /** One output of a reaction, with the amount produced by a single craft. */
    public record Line(GTReactionStack stack, long amountPerCraft, int processingTime) {

        /** Units produced per second, from the per-craft amount and the craft duration. */
        public double perSecond() {
            if (this.processingTime <= 0) {
                return 0.0D;
            }
            return this.amountPerCraft * 20.0D / this.processingTime;
        }
    }

    private static final Map<Long, List<Line>> PRODUCTS = new ConcurrentHashMap<>();

    private GTReactionProducts() {
    }

    public static void put(long pos, List<Line> lines) {
        if (lines.isEmpty()) {
            PRODUCTS.remove(pos);
        } else {
            PRODUCTS.put(pos, List.copyOf(lines));
        }
    }

    public static List<Line> get(long pos) {
        return PRODUCTS.getOrDefault(pos, List.of());
    }
}
