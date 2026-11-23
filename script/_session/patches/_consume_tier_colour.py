"""Point the item name and the block tint at GTPortTier's own colour."""
import io

# ---------------- item name: exact RGB instead of ChatFormatting ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortItem.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /** Colours by tier, in level order, matching the machine's own energy-tier line. */
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
            // Reused: ChatFormatting has no second dark blue, and UIV sits next to IV on the
            // ladder, so sharing the shade reads as "same family, higher rung".
            ChatFormatting.BLUE,         // UIV
            ChatFormatting.DARK_AQUA,    // UMV — no brown exists; the darkest warm cue available
            ChatFormatting.WHITE,        // UXV — cyan has no exact match either
            ChatFormatting.WHITE         // MAX
    };

''', '')

s = s.replace('''    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(this.tier.code()).withStyle(colour())
                .append(Component.translatable(suffixKey()).withStyle(ChatFormatting.WHITE));
    }''',
'''    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(this.tier.code()).withStyle(Style.EMPTY.withColor(this.tier.colour()))
                .append(Component.translatable(suffixKey()).withStyle(ChatFormatting.WHITE));
    }''', 1)

s = s.replace('''    private ChatFormatting colour() {
        int index = Math.max(0, Math.min(TIER_COLOURS.length - 1, this.tier.level() - 1));
        return TIER_COLOURS[index];
    }

''', '')

s = s.replace('''import net.minecraft.network.chat.Component;''',
'''import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("item name uses the tier colour")

# ---------------- block tint ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTMultiblockClientExtensions.java"
s2 = io.open(p2, encoding="utf-8").read()

s2 = s2.replace('''    /**
     * The voltage colours, in tier order.
     *
     * <p>The same ladder the names and the machine's Jade line use, so one voltage looks the same
     * wherever it appears — the ring on the block, the code in the item's name, the tier on the
     * panel.</p>
     */
    private static final int[] TIER_TINTS = {
            0xFFFF5555, // ULV 红
            0xFFAAAAAA, // LV  淡灰
            0xFFFFAA00, // MV  金
            0xFFFFFF55, // HV  黄
            0xFF555555, // EV  灰
            0xFF5555FF, // IV  深蓝
            0xFFFF55FF, // LuV 品红
            0xFF55FFFF, // ZPM 淡蓝
            0xFF00AA00, // UV  深绿
            0xFF8B0000, // UHV 深红
            0xFF800080, // UEV 紫
            0xFF00008B, // UIV 深蓝
            0xFF6B4423, // UMV 棕
            0xFF00FFFF, // UXV cyan
            0xFFFFFFFF  // MAX 白
    };

    /** Model tint index 0 is the ring. */
    private static final int TINT_RING = 0;

    private GTMultiblockClientExtensions() {
    }

    private static int tintOf(GTPortTier tier) {
        int index = Math.max(0, Math.min(TIER_TINTS.length - 1, tier.level() - 1));
        return TIER_TINTS[index];
    }
''',
'''    /** Model tint index 0 is the ring. */
    private static final int TINT_RING = 0;

    private GTMultiblockClientExtensions() {
    }

    /** The tier's colour with full alpha, which is what the block renderer wants. */
    private static int tintOf(GTPortTier tier) {
        return 0xFF000000 | tier.colour();
    }
''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("block tint reads the tier")
