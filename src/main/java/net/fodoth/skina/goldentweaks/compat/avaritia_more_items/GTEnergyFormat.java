package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats energy amounts the way {@code avaritia_more_items} does.
 *
 * <p>The unit ladder is AMI's own — {@code "", k, M, G, T, P, E, Z, Y}, one step per thousand — and
 * the fractional part keeps at most two digits with trailing zeros dropped. Matching AMI matters
 * because its machines sit next to ours: the same number must not read differently in two
 * adjacent GUIs.</p>
 *
 * <p>AMI's {@code FluxAmountFormat} works on a decimal string and cannot be reused here: it is a
 * compat class tied to AE2's {@code BigBytes}, and it truncates instead of rounding. This
 * implementation takes a plain {@code long} and rounds, which keeps the last displayed digit
 * honest at the top of a unit band.</p>
 */
public final class GTEnergyFormat {

    private static final String[] UNITS = {"", "k", "M", "G", "T", "P", "E", "Z", "Y"};

    /** Fraction digits kept, matching AMI. */
    private static final int SCALE = 2;

    private GTEnergyFormat() {
    }

    /**
     * Formats an amount with its unit, e.g. {@code 106}, {@code 1.06 k}, {@code 1.6 M}.
     *
     * <p>A space separates the number from the unit, so {@code "1.06 k"} reads as one value rather
     * than as a number glued to a letter.</p>
     */
    public static String format(long amount) {
        return format(amount, true);
    }

    /** Formats without a unit, for callers that append their own (e.g. an energy suffix). */
    public static String formatNumber(long amount) {
        return format(amount, false);
    }

    /**
     * Writes every digit out, grouped the way the player's language groups numbers.
     *
     * <p>The compact form above is right for a machine's own GUI, where the number changes
     * constantly and space is tight. A recipe page is read once and has to state the cost exactly,
     * so it uses this form instead — {@code 5.6 M} would leave the player guessing.</p>
     *
     * <p>The grouping separator comes from the client's locale rather than being fixed to a comma:
     * English and Chinese both use commas, but the locale is what decides, so this stays correct for
     * any language the game is played in.</p>
     */
    public static String formatExpanded(long amount) {
        DecimalFormat format = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        format.setRoundingMode(RoundingMode.DOWN);
        return format.format(amount);
    }

    /**
     * Formats an amount and glues a unit on, with the separator the figure itself implies.
     *
     * <p>A compact prefix already ends the number in a word — {@code 1.06 k} — so the unit follows
     * it directly and the whole reads as one token, {@code 1.06 kFE/t}. A bare number has nothing
     * to attach to, so it keeps a space: {@code 64 FE/t}. Doing it here rather than in the
     * translations means every window spells it the same way.</p>
     */
    public static String formatWithUnit(long amount, String unit) {
        String figure = format(amount);
        if (unit.isEmpty()) {
            return figure;
        }
        boolean compact = figure.length() > format(amount, false).length();
        return compact ? figure + unit : figure + " " + unit;
    }

    /**
     * The expanded figure with its unit: {@code 1,728 FE}.
     *
     * <p>Always a space. An expanded number carries no compact prefix, so there is nothing for the
     * unit to attach to — the same reason a bare {@code 64} is spaced while {@code 1.06 k} is not.</p>
     */
    public static String formatExpandedWithUnit(long amount, String unit) {
        String figure = formatExpanded(amount);
        return unit.isEmpty() ? figure : figure + " " + unit;
    }

    private static String format(long amount, boolean withUnit) {
        boolean negative = amount < 0;
        BigDecimal value = BigDecimal.valueOf(Math.abs(amount));

        int unit = 0;
        while (unit < UNITS.length - 1 && value.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            value = value.movePointLeft(3);
            unit++;
        }

        // Rounding (not truncating) keeps 999.999 k from displaying as "999.99 k" while the next
        // step would be 1 M; the carry may push the value to the next unit.
        value = value.setScale(SCALE, RoundingMode.HALF_UP);
        if (value.compareTo(BigDecimal.valueOf(1000)) >= 0 && unit < UNITS.length - 1) {
            value = value.movePointLeft(3);
            unit++;
        }
        value = value.stripTrailingZeros();

        String number = value.toPlainString();
        if (negative) {
            number = "-" + number;
        }
        if (!withUnit || unit == 0) {
            return number;
        }
        return number + " " + UNITS[unit];
    }
}
