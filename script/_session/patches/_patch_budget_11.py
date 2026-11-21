import io, json

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''                : Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.parallel.max");''',
'''                : Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.parallel.max");
        second = overclock
                // The live reachable count, not a fixed phrase: it is exactly what the hatches pay for.
                ? Component.translatable("gui.goldentweaks.chemical_reactor.sidebar.max",
                        String.valueOf(this.menu.getReachableOverclock()))
                : second;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
