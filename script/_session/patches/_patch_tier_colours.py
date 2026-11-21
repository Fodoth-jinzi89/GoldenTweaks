import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''        int energyTier = root.getInt(GTReactorJadeProvider.ENERGY_TIER);
        if (energyTier > 0) {
            tooltip.add(Component.translatable("gui.goldentweaks.jade.energy_tier",
                    tierName(energyTier)).withStyle(ChatFormatting.GREEN));
        }''',
'''        int energyTier = root.getInt(GTReactorJadeProvider.ENERGY_TIER);
        if (energyTier > 0) {
            // The label stays green; only the tier is coloured, so a glance at the hue says which
            // class of hatch the machine is built from without reading the letters.
            tooltip.add(Component.translatable("gui.goldentweaks.jade.energy_tier.label")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(tierName(energyTier))
                            .withStyle(tierColour(energyTier))));
        }''', 1)

s = s.replace('''    /** The one-based level as the shorthand written on the hatches. */
    private static String tierName(int level) {
        int index = Math.max(0, Math.min(TIER_CODES.length - 1, level - 1));
        return TIER_CODES[index];
    }''',
'''    /** The one-based level as the shorthand written on the hatches. */
    private static String tierName(int level) {
        int index = Math.max(0, Math.min(TIER_CODES.length - 1, level - 1));
        return TIER_CODES[index];
    }

    /**
     * The colour each tier is drawn in, in the same order as {@link #TIER_CODES}.
     *
     * <p>Not a rainbow for its own sake: ULV through UV is a ladder of increasing power, and the
     * hues walk from red up to dark green so the line reads as a position on that ladder rather
     * than as a name that has to be spelled out.</p>
     */
    private static final ChatFormatting[] TIER_COLOURS = {
            ChatFormatting.RED,          // ULV
            ChatFormatting.DARK_GRAY,    // LV
            ChatFormatting.GOLD,         // MV
            ChatFormatting.YELLOW,       // HV
            ChatFormatting.GRAY,         // EV
            ChatFormatting.DARK_BLUE,    // IV
            ChatFormatting.LIGHT_PURPLE, // LuV
            ChatFormatting.AQUA,         // ZPM
            ChatFormatting.DARK_GREEN    // UV
    };

    /** The colour of a one-based tier level. */
    private static ChatFormatting tierColour(int level) {
        int index = Math.max(0, Math.min(TIER_COLOURS.length - 1, level - 1));
        return TIER_COLOURS[index];
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
