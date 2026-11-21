package net.fodoth.skina.goldentweaks.util;

import net.minecraft.network.chat.Component;

/**
 * Formats tick counts as a human-readable duration.
 *
 * <p>The ladder is fixed at 20 ticks per second, 60 seconds per minute, 60 minutes per hour and 24
 * hours per day; nothing above a day is expressed, so a very long reaction reads as a large number
 * of days rather than an invented month or year.</p>
 *
 * <p>Every unit down to the smallest one of the chosen scale is printed, zero included, so
 * {@code 1 分 0 秒} stays recognisable as a one-minute recipe instead of collapsing to
 * {@code 1 分}. Rounding happens once on the whole value, which is why a recipe can never read
 * {@code 1 分 60 秒}: the carry is applied before the units are split.</p>
 *
 * <p>Unit names and the shape of each combination are translation keys, never literals, so the text
 * follows the player's own language. Translators can reorder units inside a pattern (and drop the
 * ones they do not need) without touching code.</p>
 */
public final class GTDurationFormat {

    public static final int TICKS_PER_SECOND = 20;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int HOURS_PER_DAY = 24;

    private static final String KEY_TICKS = "goldentweaks.duration.ticks";
    private static final String KEY_SECONDS = "goldentweaks.duration.seconds";
    private static final String KEY_SECONDS_DECIMAL = "goldentweaks.duration.seconds_decimal";
    private static final String KEY_MINUTES_SECONDS = "goldentweaks.duration.minutes_seconds";
    private static final String KEY_HOURS = "goldentweaks.duration.hours";
    private static final String KEY_DAYS = "goldentweaks.duration.days";

    private GTDurationFormat() {
    }

    /**
     * The duration as a localized component.
     *
     * <p>Below one second the value stays in ticks: a fraction of a tick means nothing to a player.
     * From one second up the shape changes with magnitude exactly as the specification describes,
     * so these branches are deliberate rather than a generic formatter.</p>
     */
    public static Component format(int ticks) {
        if (ticks < TICKS_PER_SECOND) {
            return Component.translatable(KEY_TICKS, ticks);
        }

        double seconds = ticks / (double) TICKS_PER_SECOND;
        if (seconds < SECONDS_PER_MINUTE) {
            // The one-decimal form is its own key: some languages do not use a decimal point and
            // some would rather say "about 3 seconds" than "3.4 seconds".
            return Component.translatable(KEY_SECONDS_DECIMAL, oneDecimal(seconds));
        }

        long totalSeconds = Math.round(seconds);
        long totalMinutes = totalSeconds / SECONDS_PER_MINUTE;
        if (totalMinutes < MINUTES_PER_HOUR) {
            return Component.translatable(KEY_MINUTES_SECONDS,
                    totalMinutes, totalSeconds % SECONDS_PER_MINUTE);
        }

        long totalHours = totalMinutes / MINUTES_PER_HOUR;
        if (totalHours < HOURS_PER_DAY) {
            return Component.translatable(KEY_HOURS,
                    totalHours, totalMinutes % MINUTES_PER_HOUR, totalSeconds % SECONDS_PER_MINUTE);
        }

        return Component.translatable(KEY_DAYS,
                totalHours / HOURS_PER_DAY,
                totalHours % HOURS_PER_DAY,
                totalMinutes % MINUTES_PER_HOUR,
                totalSeconds % SECONDS_PER_MINUTE);
    }

    /**
     * One decimal place with a hard-coded point.
     *
     * <p>The value is interpolated into a translated pattern, so the separator has to be one the
     * pattern expects; {@code Locale.ROOT} keeps it a plain point rather than following the client's
     * number format and producing a comma that the pattern does not describe.</p>
     */
    private static String oneDecimal(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
