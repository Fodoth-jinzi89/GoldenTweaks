import io, json

zh = {
    'gui.goldentweaks.chemical_reactor.sidebar.ratio': '超频比：%s',
    'gui.goldentweaks.chemical_reactor.sidebar.max': '最大：%s',
    'gui.goldentweaks.chemical_reactor.sidebar.infinite': '无限',
}
en = {
    'gui.goldentweaks.chemical_reactor.sidebar.ratio': 'Ratio: %s',
    'gui.goldentweaks.chemical_reactor.sidebar.max': 'Max: %s',
    'gui.goldentweaks.chemical_reactor.sidebar.infinite': 'Infinite',
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
