import io, json

# ---- status enum: append, never insert, because the ordinal travels over the wire ----
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTReactorStatus.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''    /** Switch is on and the machine is looking for work. */
    STARTING
}''',
'''    /** Switch is on and the machine is looking for work. */
    STARTING,

    /**
     * Every recipe the ports could match costs more than the energy hatches can carry.
     *
     * <p>Appended rather than inserted: the ordinal is what the status packet carries, so reordering
     * would silently relabel a running machine's status on clients with the old build.</p>
     */
    INSUFFICIENT_ENERGY_HATCH
}''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('status:', 'changed' if s != orig else 'NO CHANGE')

# ---- machine: report it when a search found candidates but refused them on cost ----
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''    /** Whether a recipe's base cost fits what the energy ports can carry.''',
'''    /** Set when the last search skipped at least one recipe for cost alone. */
    private boolean refusedForCost;

    /** Whether a recipe's base cost fits what the energy ports can carry.''', 1)

s2 = s2.replace('''            if (!withinEnergyBudget(recipe.getEnergyPerTick())) {
                continue;
            }''',
'''            if (!withinEnergyBudget(recipe.getEnergyPerTick())) {
                // Remembered so the status line can say why nothing started. A recipe skipped here is
                // indistinguishable from no recipe at all, otherwise.
                this.refusedForCost = true;
                continue;
            }''', 1)

# reset before each search
s2 = s2.replace('''    private MatchedMultiblockRecipe findLockedRecipe(Level level) {
        for (int channel : channelGroups()) {''',
'''    private MatchedMultiblockRecipe findLockedRecipe(Level level) {
        this.refusedForCost = false;
        for (int channel : channelGroups()) {''', 1)

# surface it in the status
s2 = s2.replace('''        if (this.activeRecipe != null) {
            return GTReactorStatus.PROCESSING;
        }
        return GTReactorStatus.IDLE;''',
'''        if (this.activeRecipe != null) {
            return GTReactorStatus.PROCESSING;
        }
        if (this.refusedForCost) {
            return GTReactorStatus.INSUFFICIENT_ENERGY_HATCH;
        }
        return GTReactorStatus.IDLE;''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('machine:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---- lang ----
zh = {'gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch':
          '能源仓等级不足：配方需求超出可用功率'}
en = {'gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch':
          'Energy hatches too weak: recipe exceeds available power'}
for path, extra in [
    (r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh),
    (r'src/main/resources/assets/goldentweaks/lang/en_us.json', en),
]:
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(extra)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print('lang updated', path)
