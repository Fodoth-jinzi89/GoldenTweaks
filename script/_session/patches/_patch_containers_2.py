import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s
imp = 'import net.weibai.avaritia_more_items.common.items.matter_cluster.FluidMatterClusterItem;'
if imp not in s:
    anchor = 'import net.weibai.avaritia_more_items.common.items.matter_cluster.BetterMatterClusterItem;'
    if anchor in s:
        s = s.replace(anchor, anchor + '\n' + imp, 1)
    else:
        print('ANCHOR MISSING')
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
