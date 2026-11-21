import io, json

# ---------- screen: name the shortfall ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''            case INSUFFICIENT_ENERGY_HATCH -> Component.translatable(
                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch");''',
'''            // The message names the figure the cheapest rejected recipe asked for, so the player
            // knows how much more power to install instead of only that something is missing.
            case INSUFFICIENT_ENERGY_HATCH -> Component.translatable(
                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch",
                    GTEnergyFormat.formatExpanded(this.menu.getRefusedCost()));''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen:', 'changed' if s != orig else 'NO CHANGE')

# ---------- lang ----------
zh = {'gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch':
          '此配方需要更高的功率：%s FE/t'}
en = {'gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch':
          'This recipe needs more power: %s FE/t'}
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

# ---------- jade: same figure ----------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''            case INSUFFICIENT_ENERGY_HATCH ->
                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch";''',
'''            case INSUFFICIENT_ENERGY_HATCH ->
                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch";''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('jade:', 'changed' if s2 != orig2 else 'NO CHANGE')
