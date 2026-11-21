import io, json

zh = {'gui.goldentweaks.chemical_reactor.sidebar.overclock': '超频'}
en = {'gui.goldentweaks.chemical_reactor.sidebar.overclock': 'Overclock'}
for path, extra, drop in [
    (r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh,
     ['gui.goldentweaks.chemical_reactor.sidebar.open',
      'gui.goldentweaks.chemical_reactor.sidebar.close']),
    (r'src/main/resources/assets/goldentweaks/lang/en_us.json', en,
     ['gui.goldentweaks.chemical_reactor.sidebar.open',
      'gui.goldentweaks.chemical_reactor.sidebar.close']),
]:
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(extra)
    for key in drop:
        data.pop(key, None)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print('lang updated', path)
