import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace("import net.neoforged.neoforge.fluids.FluidStack;",
              "import net.neoforged.neoforge.energy.IEnergyStorage;\nimport net.neoforged.neoforge.fluids.FluidStack;", 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
