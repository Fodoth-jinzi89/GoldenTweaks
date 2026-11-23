"""Use the level-based energy figures, and add the six new colours.

The item tooltip was reading through AMI's tier, which clamps everything above UV to IX.
Passing our own level keeps the figures climbing.
"""
import io

# ---------------- GTPortItem ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortItem.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''        int rate = output
                ? GTPortEnergyTiers.outputTransfer(this.tier.ami())
                : GTPortEnergyTiers.transfer(this.tier.ami());''',
'''        int rate = output
                ? GTPortEnergyTiers.outputTransfer(this.tier.level())
                : GTPortEnergyTiers.transfer(this.tier.level());''', 1)

s = s.replace('''            case ENERGY -> GTEnergyFormat.formatWithUnit(
                    this.mode == PortMode.OUTPUT
                            ? GTPortEnergyTiers.outputCapacity(this.tier.ami())
                            : GTPortEnergyTiers.capacity(this.tier.ami()), "FE");''',
'''            case ENERGY -> GTEnergyFormat.formatWithUnit(
                    this.mode == PortMode.OUTPUT
                            ? GTPortEnergyTiers.outputCapacity(this.tier.level())
                            : GTPortEnergyTiers.capacity(this.tier.level()), "FE");''', 1)

s = s.replace('''    private static final ChatFormatting[] TIER_COLOURS = {
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
            // Reused: ChatFormatting has no second dark blue, and UIV sits next to IV on the
            // ladder, so sharing the shade reads as "same family, higher rung".
            ChatFormatting.BLUE,         // UIV
            ChatFormatting.DARK_AQUA,    // UMV — no brown exists; the darkest warm cue available
            ChatFormatting.WHITE,        // UXV — cyan has no exact match either
            ChatFormatting.WHITE         // MAX
    };''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("item colours extended")

# ---------------- block entity ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''                int capacity = output
                        ? GTPortEnergyTiers.outputCapacity(tier())
                        : GTPortEnergyTiers.capacity(tier());
                int transfer = output
                        ? GTPortEnergyTiers.outputTransfer(tier())
                        : GTPortEnergyTiers.transfer(tier());''',
'''                int capacity = output
                        ? GTPortEnergyTiers.outputCapacity(portTier().level())
                        : GTPortEnergyTiers.capacity(portTier().level());
                int transfer = output
                        ? GTPortEnergyTiers.outputTransfer(portTier().level())
                        : GTPortEnergyTiers.transfer(portTier().level());''', 1)

s2 = s2.replace('''            case ENERGY -> mode() == PortMode.OUTPUT
                    ? GTPortEnergyTiers.outputCapacity(tier())
                    : GTPortEnergyTiers.capacity(tier());''',
'''            case ENERGY -> mode() == PortMode.OUTPUT
                    ? GTPortEnergyTiers.outputCapacity(portTier().level())
                    : GTPortEnergyTiers.capacity(portTier().level());''', 1)

s2 = s2.replace('''        return mode() == PortMode.OUTPUT
                ? GTPortEnergyTiers.outputTransfer(tier())
                : GTPortEnergyTiers.transfer(tier());''',
'''        return mode() == PortMode.OUTPUT
                ? GTPortEnergyTiers.outputTransfer(portTier().level())
                : GTPortEnergyTiers.transfer(portTier().level());''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("block entity updated")
