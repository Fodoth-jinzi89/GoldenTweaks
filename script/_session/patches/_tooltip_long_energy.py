"""Show the true figures in the item tooltip.

The int accessors saturate, so a MAX hatch would advertise 2.1 G FE while actually holding
343 G. The formatter takes a long, so the tooltip can state the real number.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortItem.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''        int rate = output
                ? GTPortEnergyTiers.outputTransfer(this.tier.level())
                : GTPortEnergyTiers.transfer(this.tier.level());''',
'''        long rate = output
                ? GTPortEnergyTiers.outputTransferLong(this.tier.level())
                : GTPortEnergyTiers.transferLong(this.tier.level());''', 1)

s = s.replace('''            case ENERGY -> GTEnergyFormat.formatWithUnit(
                    this.mode == PortMode.OUTPUT
                            ? GTPortEnergyTiers.outputCapacity(this.tier.level())
                            : GTPortEnergyTiers.capacity(this.tier.level()), "FE");''',
'''            case ENERGY -> GTEnergyFormat.formatWithUnit(
                    this.mode == PortMode.OUTPUT
                            ? GTPortEnergyTiers.outputCapacityLong(this.tier.level())
                            : GTPortEnergyTiers.capacityLong(this.tier.level()), "FE");''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("tooltip shows the unclamped figures")
