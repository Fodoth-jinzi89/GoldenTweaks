"""Finish the BigInteger migration at the call sites."""
import io

# ---------------- formatter: the long wrapper is unambiguous now ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTEnergyFormat.java"
s = io.open(p, encoding="utf-8").read()
s = s.replace('''    public static String formatWithUnit(long amount, String unit) {
        return formatWithUnit(BigInteger.valueOf(amount), unit);
    }''',
'''    public static String formatWithUnit(long amount, String unit) {
        return formatWithUnit(BigInteger.valueOf(amount), unit);
    }

    /** The compact figure, without a unit, for an amount of any magnitude. */
    public static String formatNumber(BigInteger amount) {
        return format(amount, false);
    }''', 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("formatter finished")

# ---------------- block entity: the renamed accessors ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

s2 = s2.replace('''                long capacity = output
                        ? GTPortEnergyTiers.outputCapacityLong(portTier().level())
                        : GTPortEnergyTiers.capacityLong(portTier().level());
                long transfer = output
                        ? GTPortEnergyTiers.outputTransferLong(portTier().level())
                        : GTPortEnergyTiers.transferLong(portTier().level());''',
'''                BigInteger capacity = output
                        ? GTPortEnergyTiers.outputCapacityExact(portTier().level())
                        : GTPortEnergyTiers.capacityExact(portTier().level());
                BigInteger transfer = output
                        ? GTPortEnergyTiers.outputTransferExact(portTier().level())
                        : GTPortEnergyTiers.transferExact(portTier().level());''', 1)

s2 = s2.replace('''            case ENERGY -> (int) Math.min(Integer.MAX_VALUE, mode() == PortMode.OUTPUT
                    ? GTPortEnergyTiers.outputCapacityLong(portTier().level())
                    : GTPortEnergyTiers.capacityLong(portTier().level()));''',
'''            // Clamped here only because the menu syncs an int; the port itself holds the exact
            // figure.
            case ENERGY -> (mode() == PortMode.OUTPUT
                    ? GTPortEnergyTiers.outputCapacityExact(portTier().level())
                    : GTPortEnergyTiers.capacityExact(portTier().level()))
                    .min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();''', 1)

s2 = s2.replace('''import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;''',
'''import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;

import java.math.BigInteger;''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("block entity updated")

# ---------------- item tooltip ----------------
p3 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortItem.java"
s3 = io.open(p3, encoding="utf-8").read()
s3 = s3.replace('''        long rate = output
                ? GTPortEnergyTiers.outputTransferLong(this.tier.level())
                : GTPortEnergyTiers.transferLong(this.tier.level());''',
'''        var rate = output
                ? GTPortEnergyTiers.outputTransferExact(this.tier.level())
                : GTPortEnergyTiers.transferExact(this.tier.level());''', 1)
s3 = s3.replace('''            case ENERGY -> GTEnergyFormat.formatWithUnit(
                    this.mode == PortMode.OUTPUT
                            ? GTPortEnergyTiers.outputCapacityLong(this.tier.level())
                            : GTPortEnergyTiers.capacityLong(this.tier.level()), "FE");''',
'''            case ENERGY -> GTEnergyFormat.formatWithUnit(
                    this.mode == PortMode.OUTPUT
                            ? GTPortEnergyTiers.outputCapacityExact(this.tier.level())
                            : GTPortEnergyTiers.capacityExact(this.tier.level()), "FE");''', 1)
io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("item tooltip updated")
