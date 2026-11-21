import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''                Component.translatable("gui.goldentweaks.chemical_reactor.energy",
                        GTEnergyFormat.format(this.menu.getEnergyStored()),
                        GTEnergyFormat.format(this.menu.getEnergyCapacity())),
                Component.translatable("gui.goldentweaks.chemical_reactor.energy_input",
                        GTEnergyFormat.format(this.menu.getInputEnergyRate())));''',
'''                Component.translatable("gui.goldentweaks.chemical_reactor.energy",
                        GTEnergyFormat.formatWithUnit(this.menu.getEnergyStored(), "FE"),
                        GTEnergyFormat.formatWithUnit(this.menu.getEnergyCapacity(), "FE")),
                Component.translatable("gui.goldentweaks.chemical_reactor.energy_input",
                        GTEnergyFormat.formatWithUnit(this.menu.getInputEnergyRate(), "FE/t")));''', 1)

s = s.replace('''                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch",
                    GTEnergyFormat.formatExpanded(this.menu.getRefusedCost()));''',
'''                    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch",
                    GTEnergyFormat.formatExpandedWithUnit(this.menu.getRefusedCost(), "FE/t"));''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen:', 'changed' if s != orig else 'NO CHANGE')
