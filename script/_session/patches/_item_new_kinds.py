"""Add the two new kinds to the item tooltip."""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortItem.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''            case ENERGY -> GTEnergyFormat.formatWithUnit(
                    this.mode == PortMode.OUTPUT
                            ? GTPortEnergyTiers.outputCapacityExact(this.tier.level())
                            : GTPortEnergyTiers.capacityExact(this.tier.level()), "FE");
        };''',
'''            case ENERGY -> GTEnergyFormat.formatWithUnit(
                    this.mode == PortMode.OUTPUT
                            ? GTPortEnergyTiers.outputCapacityExact(this.tier.level())
                            : GTPortEnergyTiers.capacityExact(this.tier.level()), "FE");
            case CHEMICAL -> GTEnergyFormat.formatWithUnit(
                    (long) this.tier.chemicalTanks() * this.tier.chemicalTankCapacity(), "");
            case ESSENTIA -> GTEnergyFormat.formatWithUnit(
                    (long) this.tier.essentiaTanks() * this.tier.essentiaTankCapacity(), "");
        };''', 1)

s = s.replace('''            case ITEM -> this.tier.itemSlots();
            case FLUID -> this.tier.fluidTanks();
            case ENERGY -> 0;
        };''',
'''            case ITEM -> this.tier.itemSlots();
            case FLUID -> this.tier.fluidTanks();
            case ENERGY -> 0;
            case CHEMICAL -> this.tier.chemicalTanks();
            case ESSENTIA -> this.tier.essentiaTanks();
        };''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("item tooltip handles the new kinds")
