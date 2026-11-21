import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# reseed the tie-break shuffle at the first start site
s = s.replace('''            this.activeChannel = this.selectedChannel;
            setChannelsLocked(true);''',
'''            this.activeChannel = this.selectedChannel;
            // A fresh shuffle for this craft only: the ports keep one order for its whole run, so
            // the inputs drawn and the outputs delivered stay consistent, but the next craft may
            // drain them in a different order.
            this.portShuffle = new Random(level.getRandom().nextLong());
            setChannelsLocked(true);''', 1)

# and at the single-tick restart site
s = s.replace('''        this.activeChannel = this.selectedChannel;
        setChannelsLocked(true);
        consumeInputs(nextBatched);''',
'''        this.activeChannel = this.selectedChannel;
        this.portShuffle = new Random(level.getRandom().nextLong());
        setChannelsLocked(true);
        consumeInputs(nextBatched);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
