import io

# ---------- 1. the missing field, placed on its own ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    private boolean refusedForCost;''', '''    private boolean refusedForCost;''', 1)
if 'private boolean refusedForCost;' not in s:
    # Insert just before the budget helper's own doc comment.
    anchor = '''    /**
     * The ceiling on a recipe's base energy cost, in FE per tick.'''
    s = s.replace(anchor,
'''    /** Set when the last search skipped at least one recipe for cost alone. */
    private boolean refusedForCost;

''' + anchor, 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('field:', 'changed' if s != orig else 'NO CHANGE')

# ---------- 2. exhaustive switches ----------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''            // STARTING is reserved for later; until something sets it, it reads as the idle case.
            case STARTING, OTHER ->
                    Component.translatable("gui.goldentweaks.chemical_reactor.status.other");''',
'''            // STARTING is reserved for later; until something sets it, it reads as the idle case.
            case STARTING, OTHER ->
                    Component.translatable("gui.goldentweaks.chemical_reactor.status.other");
            case INSUFFICIENT_ENERGY_HATCH -> Component.translatable(
                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch");''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('screen text switch:', 'changed' if s2 != orig2 else 'NO CHANGE')
