import io

# ---------- energy: input keeps the table, output is halved ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortEnergyTiers.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /** FE a port of this tier can hold. */
    public static int capacity(MultiblockPortTier tier) {
        return transfer(tier) * CAPACITY_IN_TICKS;
    }''',
'''    /** FE a port of this tier can hold. */
    public static int capacity(MultiblockPortTier tier) {
        return transfer(tier) * CAPACITY_IN_TICKS;
    }

    /**
     * The same figures for a dynamo hatch, at half the rate and half the store.
     *
     * <p>A generating hatch is meant to be a smaller commitment than a consuming one: it feeds the
     * machine rather than powering a recipe, so it supplies and buffers less for the same tier. The
     * halving applies at every tier, so the ratio between a hatch and a dynamo never changes.</p>
     */
    public static int outputTransfer(MultiblockPortTier tier) {
        return Math.max(1, transfer(tier) / 2);
    }

    public static int outputCapacity(MultiblockPortTier tier) {
        return Math.max(1, capacity(tier) / 2);
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('energy tiers:', 'changed' if s != orig else 'NO CHANGE')

# ---------- entity: pick the input or output figures from the block's mode ----------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''            case ENERGY -> {
                int capacity = GTPortEnergyTiers.capacity(tier());
                int transfer = GTPortEnergyTiers.transfer(tier());''',
'''            case ENERGY -> {
                // A dynamo hatch supplies and stores half what an energy hatch of the same tier
                // does; the block's own mode is what decides which of the two this port is.
                boolean output = mode() == PortMode.OUTPUT;
                int capacity = output
                        ? GTPortEnergyTiers.outputCapacity(tier())
                        : GTPortEnergyTiers.capacity(tier());
                int transfer = output
                        ? GTPortEnergyTiers.outputTransfer(tier())
                        : GTPortEnergyTiers.transfer(tier());''', 1)

s2 = s2.replace('''            case ENERGY -> GTPortEnergyTiers.capacity(tier());''',
'''            case ENERGY -> mode() == PortMode.OUTPUT
                    ? GTPortEnergyTiers.outputCapacity(tier())
                    : GTPortEnergyTiers.capacity(tier());''', 1)

s2 = s2.replace('''        return kind() == GTPortKind.ENERGY ? GTPortEnergyTiers.transfer(tier()) : 0;''',
'''        if (kind() != GTPortKind.ENERGY) {
            return 0;
        }
        return mode() == PortMode.OUTPUT
                ? GTPortEnergyTiers.outputTransfer(tier())
                : GTPortEnergyTiers.transfer(tier());''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('entity:', 'changed' if s2 != orig2 else 'NO CHANGE')
