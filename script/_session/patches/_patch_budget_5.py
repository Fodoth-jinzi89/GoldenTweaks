import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

for imp, anchor in [
    ('import net.fodoth.skina.goldentweaks.content.multiblock.GTPortEnergyTiers;',
     'import net.fodoth.skina.goldentweaks.content.multiblock.GTPortBlockEntity;'),
    ('import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;',
     'import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTypes;'),
]:
    if imp not in s:
        if anchor in s:
            s = s.replace(anchor, imp + '\n' + anchor, 1)
        else:
            print('MISSING ANCHOR for', imp)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
