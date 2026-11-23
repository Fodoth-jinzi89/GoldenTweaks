"""Let the formatter take any magnitude.

It built its ladder on `long`, and the unit table stopped at Y (10^24). Nothing below that
matters for the tiers we ship, but the ladder is what a `BigInteger` figure has to climb
through, so it moves to `BigDecimal` throughout and gains a fallback for values past the
last named unit.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTEnergyFormat.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    private static final String[] UNITS = {"", "k", "M", "G", "T", "P", "E", "Z", "Y"};''',
'''    private static final String[] UNITS = {"", "k", "M", "G", "T", "P", "E", "Z", "Y"};

    /**
     * Beyond {@code Y} the ladder stops naming units, so the count of thousands becomes an
     * exponent: {@code 1.2e30}. Past 10^27 that is the only honest way to write it without
     * inventing letters.
     */
    private static final String EXPONENT_MARK = "e";''', 1)

s = s.replace('''    public static String formatWithUnit(long amount, String unit) {
        String figure = format(amount);
        if (unit.isEmpty()) {
            return figure;
        }
        boolean compact = figure.length() > format(amount, false).length();
        return compact ? figure + unit : figure + " " + unit;
    }''',
'''    public static String formatWithUnit(long amount, String unit) {
        return formatWithUnit(BigInteger.valueOf(amount), unit);
    }

    /** The same, for an amount of any magnitude. */
    public static String formatWithUnit(BigInteger amount, String unit) {
        String figure = format(amount);
        if (unit.isEmpty()) {
            return figure;
        }
        boolean compact = figure.length() > format(amount, false).length();
        return compact ? figure + unit : figure + " " + unit;
    }''', 1)

s = s.replace('''    private static String format(long amount, boolean withUnit) {
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
    }''',
'''    /** The compact form, for an amount of any magnitude. */
    public static String format(BigInteger amount) {
        return format(amount, true);
    }

    private static String format(BigInteger amount, boolean withUnit) {
        boolean negative = amount.signum() < 0;
        BigDecimal value = new BigDecimal(amount.abs());

        int unit = 0;
        while (unit < UNITS.length - 1 && value.compareTo(THOUSAND) >= 0) {
            value = value.movePointLeft(3);
            unit++;
        }

        // Rounding (not truncating) keeps 999.999 k from displaying as "999.99 k" while the next
        // step would be 1 M; the carry may push the value to the next unit.
        value = value.setScale(SCALE, RoundingMode.HALF_UP);
        if (value.compareTo(THOUSAND) >= 0 && unit < UNITS.length - 1) {
            value = value.movePointLeft(3);
            unit++;
        }
        value = value.stripTrailingZeros();

        String number = value.toPlainString();
        if (negative) {
            number = "-" + number;
        }
        if (!withUnit) {
            return number;
        }
        if (unit > 0) {
            return number + " " + UNITS[unit];
        }
        // Past the last named unit, keep dividing and report the exponent instead.
        int thousands = 0;
        while (value.compareTo(THOUSAND) >= 0) {
            value = value.movePointLeft(3);
            thousands++;
        }
        if (thousands == 0) {
            return number;
        }
        return value.stripTrailingZeros().toPlainString() + EXPONENT_MARK + (thousands * 3);
    }

    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);''', 1)

s = s.replace('''import java.math.BigDecimal;''',
'''import java.math.BigDecimal;
import java.math.BigInteger;''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("formatter takes BigInteger")
