import io
import json

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('import snownee.jade.api.BlockAccessor;',
              'import org.jetbrains.annotations.Nullable;\nimport snownee.jade.api.BlockAccessor;', 1)
s = s.replace('import snownee.jade.api.config.IPluginConfig;',
              'import snownee.jade.api.config.IPluginConfig;\nimport snownee.jade.api.fluid.JadeFluidObject;', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('component imports', 'changed' if s != orig else 'NO CHANGE')

zh = {'gui.goldentweaks.port.energy.output': '最大输出'}
en = {'gui.goldentweaks.port.energy.output': 'Max Output'}
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
