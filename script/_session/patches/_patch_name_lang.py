import io
import json

zh = {'gui.goldentweaks.port.name': '名称',
      'gui.goldentweaks.chemical_reactor.name': '名称'}
en = {'gui.goldentweaks.port.name': 'Name',
      'gui.goldentweaks.chemical_reactor.name': 'Name'}
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
