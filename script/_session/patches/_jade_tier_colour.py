"""Make Jade's energy-tier line read the code and colour from GTPortTier.

Its own two tables covered nine tiers and then guessed at the rest; the enum now knows both,
and knows the colour exactly rather than as the nearest ChatFormatting.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''            tooltip.add(Component.translatable("gui.goldentweaks.jade.energy_tier.label")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(tierName(energyTier))
                            .withStyle(tierColour(energyTier))));''',
'''            GTPortTier tier = GTPortTier.byLevel(energyTier);
            tooltip.add(Component.translatable("gui.goldentweaks.jade.energy_tier.label")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(tier.code())
                            .withStyle(Style.EMPTY.withColor(tier.colour()))));''', 1)

# The two local tables go away with it.
s = s.replace('''    /** The tier codes a player knows, in level order. */
    private static final String[] TIER_CODES = {
            "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV",
            "UHV", "UEV", "UIV", "UMV", "UXV", "MAX"
    };

    /** The one-based level as the shorthand written on the hatches. */
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
            ChatFormatting.DARK_GREEN,   // UV
            ChatFormatting.DARK_RED,     // UHV
            ChatFormatting.DARK_PURPLE,  // UEV
            ChatFormatting.BLUE,         // UIV
            ChatFormatting.DARK_AQUA,    // UMV — no brown exists in ChatFormatting
            ChatFormatting.WHITE,        // UXV — no cyan either; white until the art says otherwise
            ChatFormatting.WHITE         // MAX
    };

    /** The colour of a one-based tier level. */
    private static ChatFormatting tierColour(int level) {
        int index = Math.max(0, Math.min(TIER_COLOURS.length - 1, level - 1));
        return TIER_COLOURS[index];
    }

''', '')

s = s.replace('''import net.minecraft.network.chat.Component;''',
'''import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;''', 1)

s = s.replace('''import net.fodoth.skina.goldentweaks.GoldenTweaks;''',
'''import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortTier;''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("jade reads the tier")
print("TIER_CODES left:", s.count("TIER_CODES"), "TIER_COLOURS left:", s.count("TIER_COLOURS"))
