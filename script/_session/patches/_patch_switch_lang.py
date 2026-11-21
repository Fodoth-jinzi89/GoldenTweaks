import io
import json

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s = io.open(p, encoding='utf-8').read()
if 'import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMenu;' not in s:
    s = s.replace('import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTEnergyFormat;',
                  'import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMenu;\nimport net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTEnergyFormat;', 1)
if 'import net.minecraft.network.chat.MutableComponent;' not in s:
    s = s.replace('import net.minecraft.network.chat.Component;',
                  'import net.minecraft.network.chat.Component;\nimport net.minecraft.network.chat.MutableComponent;', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('imports ok')

zh = {
    'gui.goldentweaks.chemical_reactor.lock.short': '锁定',
    'gui.goldentweaks.chemical_reactor.void.short': '销毁',
    'gui.goldentweaks.chemical_reactor.batch.short': '批量',
    'gui.goldentweaks.chemical_reactor.isolate.short': '隔离',
}
en = {
    'gui.goldentweaks.chemical_reactor.lock.short': 'Locked',
    'gui.goldentweaks.chemical_reactor.void.short': 'Void',
    'gui.goldentweaks.chemical_reactor.batch.short': 'Batch',
    'gui.goldentweaks.chemical_reactor.isolate.short': 'Isolate',
}
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
