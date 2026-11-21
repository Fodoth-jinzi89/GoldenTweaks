import io

# ---------- 1. menu: sync the refused cost every tick, with the status ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMenu.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''        Integer channel = this.tile.activeChannel();
        this.data.set(DATA_CHANNEL, channel == null ? NO_CHANNEL : channel);
    }''',
'''        Integer channel = this.tile.activeChannel();
        this.data.set(DATA_CHANNEL, channel == null ? NO_CHANNEL : channel);
        // Synced here, not only in the one-second pass: the status can flip to "not enough power"
        // on any tick, and the line quotes this figure — a stale zero would be printed for up to a
        // second before the real number arrived.
        int refused = this.tile.refusedCost();
        this.data.set(DATA_REFUSED_LO, refused & 0xFFFF);
        this.data.set(DATA_REFUSED_HI, (refused >>> 16) & 0xFFFF);
    }''', 1)

# and out of the slow pass, where it is now redundant
s = s.replace('''        this.data.set(DATA_PARALLEL, this.tile.getParallelLimit());
        int refused = this.tile.refusedCost();
        this.data.set(DATA_REFUSED_LO, refused & 0xFFFF);
        this.data.set(DATA_REFUSED_HI, (refused >>> 16) & 0xFFFF);''',
'''        this.data.set(DATA_PARALLEL, this.tile.getParallelLimit());''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('menu:', 'changed' if s != orig else 'NO CHANGE')

# ---------- 2. jade provider: send the figure ----------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeProvider.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''        root.putInt(FLAGS, machine.getStateFlags());''',
'''        root.putInt(FLAGS, machine.getStateFlags());
        // Carried alongside the status: the "not enough power" line names this figure, and the
        // client has no other way to learn it.
        root.putInt(REFUSED_COST, machine.refusedCost());''', 1)
s2 = s2.replace('''    static final String FLAGS = "Flags";''',
'''    static final String FLAGS = "Flags";
    /** Cost of the cheapest recipe the last search turned down, in FE per tick. */
    static final String REFUSED_COST = "RefusedCost";''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('jade provider:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---------- 3. jade component: pass it to the translatable ----------
p3 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s3 = io.open(p3, encoding='utf-8').read()
orig3 = s3
s3 = s3.replace('''        tooltip.add(statusLine(status));''',
'''        tooltip.add(statusLine(root, status));''', 1)
s3 = s3.replace('''    private static Component statusLine(GTReactorStatus status) {''',
'''    private static Component statusLine(CompoundTag root, GTReactorStatus status) {''', 1)
s3 = s3.replace('''        return Component.translatable(key).withStyle(switch (status) {''',
'''        if (status == GTReactorStatus.INSUFFICIENT_ENERGY_HATCH) {
            // The wording carries a figure, so it is filled in rather than passed through empty —
            // translating the bare key would print the raw "%s" placeholder.
            return Component.translatable(key,
                            GTEnergyFormat.formatExpanded(
                                    root.getInt(GTReactorJadeProvider.REFUSED_COST)))
                    .withStyle(ChatFormatting.RED);
        }
        return Component.translatable(key).withStyle(switch (status) {''', 1)
io.open(p3, 'w', encoding='utf-8', newline='\n').write(s3)
print('jade component:', 'changed' if s3 != orig3 else 'NO CHANGE')
