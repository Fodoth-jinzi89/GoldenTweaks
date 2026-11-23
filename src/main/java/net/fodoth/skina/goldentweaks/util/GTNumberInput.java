package net.fodoth.skina.goldentweaks.util;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Reads and writes the windows' integer boxes.
 *
 * <p>A player types an amount the way they think of it — {@code 1600}, {@code 1.6k},
 * {@code 16M}, {@code 1600G} — and the box shows the stored value back as a plain integer
 * grouped with the client's thousand separator. One helper for both halves keeps the two
 * halves agreeing on what a suffix means.</p>
 */
public final class GTNumberInput {

    /**
     * Suffixes, one step per thousand, matching the energy figures so a number reads the same
     * way everywhere in the mod. Lower case, since that is AMI's spelling; matching is
     * case-insensitive so a capital {@code K} is accepted too.
     */
    private static final String UNITS = "kmgtpezy";

    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

    private GTNumberInput() {
    }

    /** The grouped form of a stored value, e.g. {@code 1,600}. */
    public static String group(long value) {
        DecimalFormat format = new DecimalFormat("#,##0",
                DecimalFormatSymbols.getInstance(Locale.getDefault()));
        format.setRoundingMode(RoundingMode.DOWN);
        return format.format(value);
    }

    /**
     * The number a typed box means, clamped into the {@code int} range.
     *
     * <p>Grouping separators are dropped first, so a value read back out of a box whose text is
     * already grouped parses again. A trailing suffix multiplies by its thousand step; a
     * fractional part is truncated, because an amount that cannot be reached exactly should not
     * round up past what the player typed.</p>
     *
     * @return the value, or {@code null} when the text is not a number at all
     */
    @Nullable
    public static Integer parseInt(String raw) {
        BigDecimal value = parseDecimal(raw);
        if (value == null) {
            return null;
        }
        BigDecimal truncated = value.setScale(0, RoundingMode.DOWN);
        if (truncated.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            return Integer.MAX_VALUE;
        }
        if (truncated.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) < 0) {
            return Integer.MIN_VALUE;
        }
        return truncated.intValue();
    }

    /** The exact value a typed box means, or {@code null} when it is not a number. */
    @Nullable
    public static BigDecimal parseDecimal(String raw) {
        if (raw == null) {
            return null;
        }
        String text = stripGrouping(raw.trim());
        if (text.isEmpty()) {
            return null;
        }

        int steps = 0;
        char last = text.charAt(text.length() - 1);
        if (Character.isLetter(last)) {
            int unit = UNITS.indexOf(Character.toLowerCase(last));
            if (unit < 0) {
                return null;
            }
            // The ladder starts at thousands, so the first letter is one step, not zero.
            steps = unit + 1;
            text = text.substring(0, text.length() - 1);
        }
        if (text.isEmpty()) {
            return null;
        }

        BigDecimal value;
        try {
            value = new BigDecimal(text);
        } catch (NumberFormatException rejected) {
            return null;
        }
        return steps == 0 ? value : value.multiply(THOUSAND.pow(steps));
    }

    /**
     * Drops the separators a grouped figure carries.
     *
     * <p>Both the plain comma and the locale's own separator are removed: the box writes the
     * locale form, but a player used to the other one should not have their entry rejected.</p>
     */
    private static String stripGrouping(String text) {
        char separator = DecimalFormatSymbols.getInstance(Locale.getDefault()).getGroupingSeparator();
        StringBuilder out = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == ',' || character == separator || Character.isWhitespace(character)) {
                continue;
            }
            out.append(character);
        }
        return out.toString();
    }
}
