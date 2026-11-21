import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''        // An energy step is one second's worth at this tier, delivered in one go.
        int rate = portTier().reachInterval() <= 0 ? 0 : inputRate();''',
'''        // Energy has no slow cadence of its own: the tier's transfer rate *is* the per-tick figure,
        // set when the tiers were balanced, so it is moved every tick rather than in occasional
        // larger steps like items and fluids.
        int rate = inputRate();''', 1)

# the energy case must run every tick, so it cannot share the item/fluid countdown
s = s.replace('''        // Each tier moves a step every so many ticks: the cheap ports are slow on purpose, so a ULV
        // hatch cannot empty a chest as fast as a UV one.
        if (--this.reachCountdown > 0) {
            return;
        }
        this.reachCountdown = portTier().reachInterval();

        Direction front = getBlockState().getValue(GTPortBlock.FACING);''',
'''        // Items and fluids move in tier-sized steps every so many ticks: the cheap ports are slow on
        // purpose, so a ULV hatch cannot empty a chest as fast as a UV one. Energy is exempt — its
        // rate is already a per-tick figure — so the countdown only gates those two.
        boolean stepDue = --this.reachCountdown <= 0;
        if (stepDue) {
            this.reachCountdown = portTier().reachInterval();
        }

        Direction front = getBlockState().getValue(GTPortBlock.FACING);''', 1)

s = s.replace('''        switch (kind()) {
            case ITEM -> reachItems(target, front);
            case FLUID -> reachFluids(target, front);
            case ENERGY -> reachEnergy(target, front);
        }''',
'''        switch (kind()) {
            case ITEM -> {
                if (stepDue) {
                    reachItems(target, front);
                }
            }
            case FLUID -> {
                if (stepDue) {
                    reachFluids(target, front);
                }
            }
            case ENERGY -> reachEnergy(target, front);
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
