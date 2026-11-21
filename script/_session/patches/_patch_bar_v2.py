import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- 1. build both labels first, so a shared width can be taken from the pair ----
s = s.replace('''        if (running) {
            tooltip.add(progressBar(root));
        }

        int capacity = root.getInt(GTReactorJadeProvider.CAPACITY);
        if (capacity > 0) {
            tooltip.add(energyBar(root, capacity));
        }''',
'''        // Both bars are built before either is added: when they appear together the wider text
        // sets the width for both, so the pair lines up as one column. On its own a bar falls back
        // to the fixed width instead.
        Component progressLabel = running ? progressLabel(root) : null;
        int capacity = root.getInt(GTReactorJadeProvider.CAPACITY);
        Component energyLabel = capacity > 0 ? energyLabel(root, capacity) : null;

        int width = sharedWidth(progressLabel, energyLabel);
        if (progressLabel != null) {
            tooltip.add(progressBar(root, pad(progressLabel, width)));
        }
        if (energyLabel != null) {
            tooltip.add(energyBar(root, capacity, pad(energyLabel, width)));
        }''')

# ---- 2. split the bar builders into label + build ----
s = s.replace('''    private static IElement progressBar(CompoundTag root) {
        int progress = root.getInt(GTReactorJadeProvider.PROGRESS);
        int length = root.getInt(GTReactorJadeProvider.LENGTH);
        float fraction = length <= 0 ? 0.0F
                : Math.max(0.0F, Math.min(1.0F, progress / (float) length));

        Component label = Component.translatable("gui.goldentweaks.jade.progress",
                GTDurationFormat.format(progress), GTDurationFormat.format(length));

        return bar(fraction, padded(label), PROGRESS_FILL);
    }''',
'''    private static Component progressLabel(CompoundTag root) {
        int progress = root.getInt(GTReactorJadeProvider.PROGRESS);
        int length = root.getInt(GTReactorJadeProvider.LENGTH);
        return Component.translatable("gui.goldentweaks.jade.progress",
                GTDurationFormat.format(progress), GTDurationFormat.format(length));
    }

    private static IElement progressBar(CompoundTag root, Component label) {
        int progress = root.getInt(GTReactorJadeProvider.PROGRESS);
        int length = root.getInt(GTReactorJadeProvider.LENGTH);
        float fraction = length <= 0 ? 0.0F
                : Math.max(0.0F, Math.min(1.0F, progress / (float) length));
        return bar(fraction, label, PROGRESS_FILL);
    }''')

s = s.replace('''    private static IElement energyBar(CompoundTag root, int capacity) {
        int stored = root.getInt(GTReactorJadeProvider.ENERGY);
        float fraction = Math.max(0.0F, Math.min(1.0F, stored / (float) capacity));
        Component label = Component.translatable("gui.goldentweaks.jade.energy",
                GTEnergyFormat.format(stored), GTEnergyFormat.format(capacity));

        return bar(fraction, padded(label), ENERGY_FILL);
    }''',
'''    private static Component energyLabel(CompoundTag root, int capacity) {
        int stored = root.getInt(GTReactorJadeProvider.ENERGY);
        return Component.translatable("gui.goldentweaks.jade.energy",
                GTEnergyFormat.format(stored), GTEnergyFormat.format(capacity));
    }

    private static IElement energyBar(CompoundTag root, int capacity, Component label) {
        int stored = root.getInt(GTReactorJadeProvider.ENERGY);
        float fraction = Math.max(0.0F, Math.min(1.0F, stored / (float) capacity));
        return bar(fraction, label, ENERGY_FILL);
    }''')

# ---- 3. the colour: -1 is Jade's "unset" sentinel ----
s = s.replace('''    private static final int BAR_TEXT = 0xFFFFFFFF;''',
'''    /**
     * Solid white.
     *
     * <p>Not {@code 0xFFFFFFFF}: Jade stores {@code -1} as its "unset" sentinel and swaps it for the
     * theme's normal colour, which is a mid grey — that is why pure white came out grey. One step
     * below {@code -1} is indistinguishable white and is taken at face value.</p>
     */
    private static final int BAR_TEXT = 0xFFFFFFFE;''')

s = s.replace('''    /**
     * Builds one bar with the shared appearance.
     *
     * <p>{@code autoTextColor} is switched off explicitly: left on, Jade picks black or white from
     * the fill's brightness and overrides the colour asked for, which is what made the text read as
     * muddy grey. With it off the white below is used as-is.</p>
     */''',
'''    /** Builds one bar with the shared appearance. */''')

s = s.replace('''        if (style instanceof snownee.jade.impl.ui.SimpleProgressStyle simple) {
            simple.autoTextColor = false;
        }
        return IElementHelper.get().progress(fraction, label, style,''',
'''        return IElementHelper.get().progress(fraction, label, style,''')

# ---- 4. width helpers ----
s = s.replace('''    /** The pixel width both bars are held to, so the two read as one column. */
    private static final int BAR_TEXT_WIDTH = 96;

    /**
     * Pads a label out to a fixed pixel width with spaces.
     *
     * <p>Jade sizes a bar from its text, so "00:12 / 01:40" and "29.1 k / 100 k FE" would otherwise
     * produce two different widths and the panel would look ragged. Padding to a shared width makes
     * both bars the same length; the text stays left-aligned inside, which is where a progress
     * readout belongs.</p>
     *
     * <p>A space is four pixels wide, so the padding lands on a four-pixel boundary. The remaining
     * one to three pixels are invisible inside the bar — Jade already adds a few pixels of its own
     * margin around the text.</p>
     */
    private static Component padded(Component label) {
        int width = font().width(label);
        if (width >= BAR_TEXT_WIDTH) {
            return label;
        }
        int spaces = (BAR_TEXT_WIDTH - width) / 4;
        if (spaces <= 0) {
            return label;
        }
        return Component.empty().append(label).append(" ".repeat(spaces));
    }''',
'''    /** Width used when only one bar is shown and there is nothing to align it with. */
    private static final int SOLO_BAR_TEXT_WIDTH = 96;

    /**
     * The width both bars are padded to.
     *
     * <p>With both on screen the wider label sets it, so the pair reads as one column. With only one
     * there is nothing to align to, so a fixed width keeps the bar from resizing as the numbers
     * tick.</p>
     */
    private static int sharedWidth(@Nullable Component progress, @Nullable Component energy) {
        if (progress == null || energy == null) {
            return SOLO_BAR_TEXT_WIDTH;
        }
        return Math.max(font().width(progress), font().width(energy));
    }

    /**
     * Pads a label out to a given pixel width with spaces.
     *
     * <p>Jade sizes a bar from its text width, so two different labels would otherwise give two
     * different bar lengths. A space is four pixels wide, so the padding lands on a four-pixel
     * boundary; the leftover one to three pixels sit inside the bar's own text margin and are not
     * visible.</p>
     */
    private static Component pad(Component label, int width) {
        int current = font().width(label);
        if (current >= width) {
            return label;
        }
        int spaces = (width - current) / 4;
        if (spaces <= 0) {
            return label;
        }
        return Component.empty().append(label).append(" ".repeat(spaces));
    }''')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('padded() left:', s.count('padded('))
