"""Build the energy store from the unclamped figures.

The int accessors saturate at Integer.MAX_VALUE, which is exactly the ceiling this change was
meant to remove. The store takes longs, so the port gets the real numbers.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''                int capacity = output
                        ? GTPortEnergyTiers.outputCapacity(portTier().level())
                        : GTPortEnergyTiers.capacity(portTier().level());
                int transfer = output
                        ? GTPortEnergyTiers.outputTransfer(portTier().level())
                        : GTPortEnergyTiers.transfer(portTier().level());''',
'''                // Long, not int: the top tiers hold more than an int can express, and clamping on
                // the way in is the ceiling this was meant to lift.
                long capacity = output
                        ? GTPortEnergyTiers.outputCapacityLong(portTier().level())
                        : GTPortEnergyTiers.capacityLong(portTier().level());
                long transfer = output
                        ? GTPortEnergyTiers.outputTransferLong(portTier().level())
                        : GTPortEnergyTiers.transferLong(portTier().level());''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("store built from unclamped figures")

# ---------------- the window's readouts ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''            case ENERGY -> mode() == PortMode.OUTPUT
                    ? GTPortEnergyTiers.outputCapacity(portTier().level())
                    : GTPortEnergyTiers.capacity(portTier().level());''',
'''            case ENERGY -> (int) Math.min(Integer.MAX_VALUE, mode() == PortMode.OUTPUT
                    ? GTPortEnergyTiers.outputCapacityLong(portTier().level())
                    : GTPortEnergyTiers.capacityLong(portTier().level()));''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("capacity readout widened")
