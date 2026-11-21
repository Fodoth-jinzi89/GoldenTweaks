import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# colours: grey track, distinct fills, white text
s = s.replace('''    /** Grey track and yellow fill, with white text over the bar. */
    private static final int BAR_TRACK = 0xFF555555;
    private static final int BAR_FILL = 0xFFD8B33A;
    private static final int BAR_TEXT = 0xFFFFFFFF;''',
'''    /** Grey track, white text; the fill colour is chosen per bar. */
    private static final int BAR_TRACK = 0xFF555555;
    private static final int PROGRESS_FILL = 0xFFD8B33A;
    private static final int ENERGY_FILL = 0xFFB03030;
    private static final int BAR_TEXT = 0xFFFFFFFF;''')

# progress bar: yellow fill, fixed width, explicit text colour
s = s.replace('''        Component label = Component.translatable("gui.goldentweaks.jade.progress",
                GTDurationFormat.format(progress), GTDurationFormat.format(length));

        ProgressStyle style = IElementHelper.get().progressStyle()
                .color(BAR_TRACK, BAR_FILL)
                .textColor(BAR_TEXT)
                .fitContentX(false);
        return IElementHelper.get().progress(fraction, label, style,
                BoxStyle.getNestedBox(), false);
    }''',
'''        Component label = Component.translatable("gui.goldentweaks.jade.progress",
                GTDurationFormat.format(progress), GTDurationFormat.format(length));

        return bar(fraction, label, PROGRESS_FILL);
    }''')

# energy bar: red fill, same builder
s = s.replace('''        Component label = Component.translatable("gui.goldentweaks.jade.energy",
                GTEnergyFormat.format(stored), GTEnergyFormat.format(capacity));

        ProgressStyle style = IElementHelper.get().progressStyle()
                .color(BAR_TRACK, BAR_FILL)
                .textColor(BAR_TEXT)
                .fitContentX(false);
        return IElementHelper.get().progress(fraction, label, style,
                BoxStyle.getNestedBox(), false);
    }''',
'''        Component label = Component.translatable("gui.goldentweaks.jade.energy",
                GTEnergyFormat.format(stored), GTEnergyFormat.format(capacity));

        return bar(fraction, padded(label), ENERGY_FILL);
    }

    /**
     * Builds one bar with the shared appearance.
     *
     * <p>{@code autoTextColor} is switched off explicitly: left on, Jade picks black or white from
     * the fill's brightness and overrides the colour asked for, which is what made the text read as
     * muddy grey. With it off the white below is used as-is.</p>
     */
    private static IElement bar(float fraction, Component label, int fill) {
        ProgressStyle style = IElementHelper.get().progressStyle()
                .color(BAR_TRACK, fill)
                .textColor(BAR_TEXT)
                .fitContentX(false);
        if (style instanceof snownee.jade.impl.ui.SimpleProgressStyle simple) {
            simple.autoTextColor = false;
        }
        return IElementHelper.get().progress(fraction, label, style,
                BoxStyle.getNestedBox(), false);
    }

    /**
     * Widens a label so both bars come out the same size.
     *
     * <p>Jade sizes a bar from its text, so two different numbers give two different widths and the
     * panel looks ragged. Padding the text with spaces to a shared width evens them out; the bars
     * are laid out one above the other, so the pair reads as a single column.</p>
     */
    private static Component padded(Component label) {
        int target = barTextWidth();
        int width = font().width(label);
        if (width >= target) {
            return label;
        }
        // A space is four pixels wide, so the padding can only land on a four-pixel boundary; the
        // remaining one to three pixels are invisible inside the bar.
        int spaces = (target - width) / 4;
        MutableComponent grown = Component.empty().append(label);
        if (spaces > 0) {
            grown.append(" ".repeat(spaces));
        }
        return grown;
    }

    /** The width both bar labels are padded to: the wider of the two. */
    private static int barTextWidth() {
        if (CACHED_BAR_WIDTH < 0) {
            // Measured from representative labels, so the target does not drift as the numbers grow
            // and shrink every tick.
            CACHED_BAR_WIDTH = Math.max(
                    font().width(Component.translatable("gui.goldentweaks.jade.progress",
                            "00:00", "00:00")),
                    font().width(Component.translatable("gui.goldentweaks.jade.energy",
                            "000.00 M", "000.00 M")));
        }
        return CACHED_BAR_WIDTH;
    }

    private static net.minecraft.client.gui.Font font() {
        return net.minecraft.client.Minecraft.getInstance().font;
    }''')

# the progress label also needs padding
s = s.replace('        return bar(fraction, label, PROGRESS_FILL);',
              '        return bar(fraction, padded(label), PROGRESS_FILL);', 1)

# width cache field
s = s.replace('''    /** How many products are listed before the rest are summarised. */
    private static final int MAX_PRODUCTS = 3;''',
'''    /** How many products are listed before the rest are summarised. */
    private static final int MAX_PRODUCTS = 3;

    /** Shared bar-label width, measured once; negative until then. */
    private static int CACHED_BAR_WIDTH = -1;''')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
