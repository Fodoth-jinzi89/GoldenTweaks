package net.fodoth.skina.goldentweaks.compat.mioicif.emi;

import net.minecraft.network.chat.Component;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Number formatting shared by the UU pages.
 *
 * <p>The figures span a wide range — a fraction of a millibucket up to millions — so they are
 * grouped the way the player's language groups numbers and given a unit that never reads as a raw
 * digit run.</p>
 */
final class GTUuMatterFormat {

    private GTUuMatterFormat() {
    }

    /** A millibucket figure with grouping, e.g. {@code 1,234,567 mB}. */
    static Component mb(long millibuckets) {
        return Component.literal(grouped(millibuckets) + " mB");
    }

    /** The bare grouped number, for places that supply their own unit. */
    static String grouped(long value) {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.ROOT);
        format.setGroupingUsed(true);
        return format.format(value);
    }
}
