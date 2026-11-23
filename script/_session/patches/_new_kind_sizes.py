"""Answer the size questions for the two new kinds.

`capacity` is the whole-port figure the window shows; `cellSize` is what one cell holds, which
is what the grid's span arithmetic divides by.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''            case ENERGY -> (mode() == PortMode.OUTPUT
                    ? GTPortEnergyTiers.outputCapacityExact(portTier().level())
                    : GTPortEnergyTiers.capacityExact(portTier().level()))
                    .min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
        };
    }''',
'''            case ENERGY -> (mode() == PortMode.OUTPUT
                    ? GTPortEnergyTiers.outputCapacityExact(portTier().level())
                    : GTPortEnergyTiers.capacityExact(portTier().level()))
                    .min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
            case CHEMICAL -> portTier().chemicalTanks() * portTier().chemicalTankCapacity();
            case ESSENTIA -> portTier().essentiaTanks() * portTier().essentiaTankCapacity();
        };
    }''', 1)

s = s.replace('''            case ITEM -> portTier().slotLimit();
            case FLUID -> portTier().tankCapacity();
            case ENERGY -> Integer.MAX_VALUE;
        };''',
'''            case ITEM -> portTier().slotLimit();
            case FLUID -> portTier().tankCapacity();
            case ENERGY -> Integer.MAX_VALUE;
            case CHEMICAL -> portTier().chemicalTankCapacity();
            case ESSENTIA -> portTier().essentiaTankCapacity();
        };''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("capacity and cellSize handle the new kinds")
