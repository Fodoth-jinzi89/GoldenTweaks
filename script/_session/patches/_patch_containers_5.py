import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- the assembled dismantle hands the pooled fluids over as well ----
s = s.replace('''        // Hand everything over before the blocks go away, so this tile is still intact here.
        giveToPlayer(player, parts);''',
'''        // Hand everything over before the blocks go away, so this tile is still intact here.
        giveToPlayer(player, parts, fluids);''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('call sites:', s.count('giveToPlayer(player, parts'))
