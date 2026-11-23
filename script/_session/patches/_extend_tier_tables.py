"""Bring the machine, Jade and the block tint up to thirteen tiers.

The machine's effective-tier calculation clamps to AMI's enum length; Jade's code list ends
at UV; the block tint table ends at UV. All three are index-driven, so each needs the six
new entries or it silently reuses the last one.
"""
import io

# ---------------- machine: clamp to our own ladder ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()
s = s.replace('''        return Math.max(1, Math.min(MultiblockPortTier.values().length, tier));''',
'''        // Our own ladder, not AMI's: ours has the tiers above UV, and clamping to AMI's length
        // would report every one of them as IX.
        return Math.max(1, Math.min(GTPortTier.values().length, tier));''', 1)
s = s.replace('''import net.fodoth.skina.goldentweaks.content.multiblock.GTPortKind;''',
'''import net.fodoth.skina.goldentweaks.content.multiblock.GTPortKind;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortTier;''', 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("machine updated")

# ---------------- Jade: codes and colours ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''    private static final String[] TIER_CODES = {
            "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV"
    };''',
'''    private static final String[] TIER_CODES = {
            "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV",
            "UHV", "UEV", "UIV", "UMV", "UXV", "MAX"
    };''', 1)

s2 = s2.replace('''    private static final ChatFormatting[] TIER_COLOURS = {
            ChatFormatting.RED,          // ULV
            ChatFormatting.DARK_GRAY,    // LV
            ChatFormatting.GOLD,         // MV
            ChatFormatting.YELLOW,       // HV
            ChatFormatting.GRAY,         // EV
            ChatFormatting.DARK_BLUE,    // IV
            ChatFormatting.LIGHT_PURPLE, // LuV
            ChatFormatting.AQUA,         // ZPM
            ChatFormatting.DARK_GREEN    // UV
    };''',
'''    private static final ChatFormatting[] TIER_COLOURS = {
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
    };''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("jade updated")

# ---------------- block tint ----------------
p3 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTMultiblockClientExtensions.java"
s3 = io.open(p3, encoding="utf-8").read()
s3 = s3.replace('''            0xFF55FFFF, // ZPM 淡蓝
            0xFF00AA00  // UV  深绿
    };''',
'''            0xFF55FFFF, // ZPM 淡蓝
            0xFF00AA00, // UV  深绿
            0xFF8B0000, // UHV 深红
            0xFF800080, // UEV 紫
            0xFF00008B, // UIV 深蓝
            0xFF6B4423, // UMV 棕
            0xFF00FFFF, // UXV cyan
            0xFFFFFFFF  // MAX 白
    };''', 1)
io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("tints updated")
