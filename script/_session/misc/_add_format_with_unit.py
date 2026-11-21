import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTEnergyFormat.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    private static String format(long amount, boolean withUnit) {''',
'''    /**
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

    private static String format(long amount, boolean withUnit) {''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('energy format:', 'changed' if s != orig else 'NO CHANGE')
