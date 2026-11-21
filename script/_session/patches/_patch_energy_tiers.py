import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''            case ENERGY -> {
                int capacity = MultiblockPortTypes.ENERGY_CAPACITY_BY_TIER.get(tier());
                int transfer = MultiblockPortTypes.ENERGY_TRANSFER_BY_TIER.get(tier());''',
'''            case ENERGY -> {
                int capacity = GTPortEnergyTiers.capacity(tier());
                int transfer = GTPortEnergyTiers.transfer(tier());''', 1)

s = s.replace('''            case ENERGY -> MultiblockPortTypes.ENERGY_CAPACITY_BY_TIER.get(tier());''',
'''            case ENERGY -> GTPortEnergyTiers.capacity(tier());''', 1)

s = s.replace('''        return kind() == GTPortKind.ENERGY
                ? MultiblockPortTypes.ENERGY_TRANSFER_BY_TIER.get(tier()) : 0;''',
'''        return kind() == GTPortKind.ENERGY ? GTPortEnergyTiers.transfer(tier()) : 0;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('leftover AMI energy lookups:', s.count('ENERGY_TRANSFER_BY_TIER'), s.count('ENERGY_CAPACITY_BY_TIER'))
